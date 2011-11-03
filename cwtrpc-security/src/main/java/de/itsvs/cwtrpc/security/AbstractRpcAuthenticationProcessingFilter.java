/*
 *  Copyright 2011 IT Services VS GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.itsvs.cwtrpc.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.util.Assert;

import com.google.gwt.user.server.rpc.RPCRequest;

import de.itsvs.cwtrpc.core.RpcSessionInvalidationPolicy;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public abstract class AbstractRpcAuthenticationProcessingFilter extends
		AbstractRpcProcessingFilter implements ApplicationEventPublisherAware {
	private final Log log = LogFactory
			.getLog(AbstractRpcAuthenticationProcessingFilter.class);

	private ApplicationEventPublisher applicationEventPublisher;

	private AuthenticationManager authenticationManager;

	private AuthenticationDetailsSource authenticationDetailsSource;

	private AuthenticationSuccessHandler authenticationSuccessHandler;

	private AuthenticationFailureHandler authenticationFailureHandler;

	private RememberMeServices rememberMeServices;

	private SessionAuthenticationStrategy sessionAuthenticationStrategy;

	private RpcHttpSessionStrategy rpcHttpSessionStrategy;

	public ApplicationEventPublisher getApplicationEventPublisher() {
		return applicationEventPublisher;
	}

	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(
			AuthenticationManager authenticationManager) {
		Assert.notNull(authenticationManager,
				"'authenticationManager' must not be null");
		this.authenticationManager = authenticationManager;
	}

	public AuthenticationSuccessHandler getAuthenticationSuccessHandler() {
		return authenticationSuccessHandler;
	}

	public void setAuthenticationSuccessHandler(
			AuthenticationSuccessHandler authenticationSuccessHandler) {
		Assert.notNull(authenticationSuccessHandler,
				"'authenticationSuccessHandler' must not be null");
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}

	public AuthenticationFailureHandler getAuthenticationFailureHandler() {
		return authenticationFailureHandler;
	}

	public void setAuthenticationFailureHandler(
			AuthenticationFailureHandler authenticationFailureHandler) {
		Assert.notNull(authenticationFailureHandler,
				"'authenticationFailureHandler' must not be null");
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	public AuthenticationDetailsSource getAuthenticationDetailsSource() {
		return authenticationDetailsSource;
	}

	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource authenticationDetailsSource) {
		Assert.notNull(authenticationDetailsSource,
				"'authenticationDetailsSource' must not be null");
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	public RememberMeServices getRememberMeServices() {
		return rememberMeServices;
	}

	public void setRememberMeServices(RememberMeServices rememberMeServices) {
		Assert.notNull(rememberMeServices,
				"'rememberMeServices' must not be null");
		this.rememberMeServices = rememberMeServices;
	}

	public SessionAuthenticationStrategy getSessionAuthenticationStrategy() {
		return sessionAuthenticationStrategy;
	}

	public void setSessionAuthenticationStrategy(
			SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		Assert.notNull(sessionAuthenticationStrategy,
				"'sessionAuthenticationStrategy' must not be null");
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}

	public RpcHttpSessionStrategy getRpcHttpSessionStrategy() {
		return rpcHttpSessionStrategy;
	}

	public void setRpcHttpSessionStrategy(
			RpcHttpSessionStrategy rpcHttpSessionStrategy) {
		Assert.notNull(rpcHttpSessionStrategy,
				"'rpcHttpSessionStrategy' must not be null");
		this.rpcHttpSessionStrategy = rpcHttpSessionStrategy;
	}

	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();

		Assert.notNull(getAuthenticationManager(),
				"'authenticationManager' must be specified");

		if (getAuthenticationSuccessHandler() == null) {
			final DefaultRpcAuthenticationSuccessHandler handler;

			handler = new DefaultRpcAuthenticationSuccessHandler();
			handler.setServletContext(getServletContext());
			handler.afterPropertiesSet();
			setAuthenticationSuccessHandler(handler);
		}
		if (getAuthenticationFailureHandler() == null) {
			final SimpleRpcAuthenticationFailureHandler handler;

			handler = new SimpleRpcAuthenticationFailureHandler();
			handler.setServletContext(getServletContext());
			handler.afterPropertiesSet();
			setAuthenticationFailureHandler(handler);
		}
		if (getSessionAuthenticationStrategy() == null) {
			setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy());
		}
		if (getAuthenticationDetailsSource() == null) {
			setAuthenticationDetailsSource(new WebAuthenticationDetailsSource());
		}
		if (getRememberMeServices() == null) {
			setRememberMeServices(new NullRememberMeServices());
		}
		if (getRpcHttpSessionStrategy() == null) {
			setRpcHttpSessionStrategy(new DefaultRpcHttpSessionStrategy());
		}
	}

	@Override
	protected void process(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final Authentication authentication;
		RpcSessionInvalidationPolicy policy = null;
		boolean ok = false;

		try {
			try {
				authentication = attemptAuthentication(request, response);
				if (authentication == null) {
					return;
				}

				/*
				 * Since authentication strategy may require the session, we
				 * need to prepare the session.
				 */
				policy = getRpcHttpSessionStrategy().prepareSession(request,
						response);

				applySessionAuthenticationStrategy(authentication, request,
						response);
			} catch (AuthenticationException e) {
				if (e instanceof AuthenticationServiceException) {
					log.error("An authentication service error occured", e);
				} else {
					log.info("Authentication failed", e);
				}
				unsuccessfulAuthentication(request, response, e);
				return;
			}

			successfulAuthentication(request, response, authentication);
			chain.doFilter(request, response);
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				log.debug("Authentication has been removed "
						+ "(inidicates previous errors in filter chain)");
			} else {
				successfulAuthenticationEnd(request, response, authentication);
				ok = true;
			}
		} finally {
			if (!ok) {
				log.debug("Clearing security context due to previous errors");

				SecurityContextHolder.clearContext();
				if ((policy != null)
						&& policy.isInvalidateOnUnexpectedException()) {
					invalidateSession(request);
				}
			}
		}
	}

	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {
		final RPCRequest rpcRequest;
		final Authentication authentication;

		rpcRequest = readRpcRequest(request);

		authentication = createAuthenticationToken(request, rpcRequest);
		return getAuthenticationManager().authenticate(authentication);
	}

	protected abstract Authentication createAuthenticationToken(
			HttpServletRequest servletRequest, RPCRequest rpcRequest)
			throws AuthenticationException, IOException, ServletException;

	protected void applySessionAuthenticationStrategy(
			Authentication authentication, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		final SessionAuthenticationStrategy sessionAuthenticationStrategy;

		sessionAuthenticationStrategy = getSessionAuthenticationStrategy();
		if (!RpcSessionManagementFilter.appliedSessionAuthenticationStrategy(
				request, sessionAuthenticationStrategy)) {
			sessionAuthenticationStrategy.onAuthentication(authentication,
					request, response);
			RpcSessionManagementFilter
					.saveAppliedSessionAuthenticationStrategy(request,
							sessionAuthenticationStrategy);
		} else {
			log.debug("Session authentication strategy "
					+ "has been applied already");
		}
	}

	protected void unsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		log.debug("Unsuccessful authentication", exception);

		SecurityContextHolder.clearContext();

		getRememberMeServices().loginFail(request, response);
		getAuthenticationFailureHandler().onAuthenticationFailure(request,
				response, exception);
	}

	protected void successfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		if (log.isDebugEnabled()) {
			log.debug("Successful authentication of '"
					+ authentication.getName() + "'");
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);

		getRememberMeServices().loginSuccess(request, response, authentication);
		if (getApplicationEventPublisher() != null) {
			getApplicationEventPublisher().publishEvent(
					new InteractiveAuthenticationSuccessEvent(authentication,
							getClass()));
		}
	}

	protected void successfulAuthenticationEnd(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		if (log.isDebugEnabled()) {
			log.debug("Successful authentication of '"
					+ authentication.getName() + "' ended");
		}

		getAuthenticationSuccessHandler().onAuthenticationSuccess(request,
				response, authentication);
	}
}
