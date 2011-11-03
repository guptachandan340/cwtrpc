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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import de.itsvs.cwtrpc.core.RpcSessionInvalidationPolicy;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RpcSessionManagementFilter extends GenericFilterBean {
	private final Log log = LogFactory.getLog(RpcSessionManagementFilter.class);

	protected static final String PROCESSED_ALREADY_ATTR_NAME = RpcSessionManagementFilter.class
			.getName().concat(".processedAlready");

	protected static final String APPLIED_SESSION_AUTHENTICATION_STRATEGIES_ATTR_NAME = RpcSessionManagementFilter.class
			.getName().concat(".appliedSessionAuthenticationStrategies");

	protected static final String INVALID_SESSION_TEXT = "INVALID_SESSION";

	private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

	private SecurityContextRepository securityContextRepository;

	private AuthenticationFailureHandler authenticationFailureHandler;

	private SessionAuthenticationStrategy sessionAuthenticationStrategy;

	private RedirectStrategy invalidSessionRedirectStrategy;

	private String invalidSessionUrl;

	private RpcHttpSessionStrategy rpcHttpSessionStrategy;

	@SuppressWarnings("unchecked")
	public static void saveAppliedSessionAuthenticationStrategy(
			HttpServletRequest request, SessionAuthenticationStrategy strategy) {
		List<Class<? extends SessionAuthenticationStrategy>> appliedStrategies;

		appliedStrategies = (List<Class<? extends SessionAuthenticationStrategy>>) request
				.getAttribute(APPLIED_SESSION_AUTHENTICATION_STRATEGIES_ATTR_NAME);
		if (appliedStrategies == null) {
			appliedStrategies = new ArrayList<Class<? extends SessionAuthenticationStrategy>>();
		}
		appliedStrategies.add(strategy.getClass());
		request.setAttribute(
				APPLIED_SESSION_AUTHENTICATION_STRATEGIES_ATTR_NAME,
				appliedStrategies);
	}

	@SuppressWarnings("unchecked")
	public static boolean appliedSessionAuthenticationStrategy(
			HttpServletRequest request, SessionAuthenticationStrategy strategy) {
		List<Class<? extends SessionAuthenticationStrategy>> appliedStrategies;

		appliedStrategies = (List<Class<? extends SessionAuthenticationStrategy>>) request
				.getAttribute(APPLIED_SESSION_AUTHENTICATION_STRATEGIES_ATTR_NAME);
		if (appliedStrategies == null) {
			return false;
		}

		return appliedStrategies.contains(strategy.getClass());
	}

	public SecurityContextRepository getSecurityContextRepository() {
		return securityContextRepository;
	}

	public void setSecurityContextRepository(
			SecurityContextRepository securityContextRepository) {
		Assert.notNull(securityContextRepository,
				"'securityContextRepository' must not be null");
		this.securityContextRepository = securityContextRepository;
	}

	public AuthenticationFailureHandler getAuthenticationFailureHandler() {
		return authenticationFailureHandler;
	}

	public void setAuthenticationFailureHandler(
			AuthenticationFailureHandler authenticationFailureHandler) {
		Assert.notNull(securityContextRepository,
				"'authenticationFailureHandler' must not be null");
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	public SessionAuthenticationStrategy getSessionAuthenticationStrategy() {
		return sessionAuthenticationStrategy;
	}

	public void setSessionAuthenticationStrategy(
			SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		Assert.notNull(securityContextRepository,
				"'sessionAuthenticationStrategy' must not be null");
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}

	public AuthenticationTrustResolver getAuthenticationTrustResolver() {
		return authenticationTrustResolver;
	}

	public RedirectStrategy getInvalidSessionRedirectStrategy() {
		return invalidSessionRedirectStrategy;
	}

	public void setInvalidSessionRedirectStrategy(
			RedirectStrategy invalidSessionRedirectStrategy) {
		Assert.notNull(invalidSessionRedirectStrategy,
				"'invalidSessionRedirectStrategy' must not be null");
		this.invalidSessionRedirectStrategy = invalidSessionRedirectStrategy;
	}

	public String getInvalidSessionUrl() {
		return invalidSessionUrl;
	}

	public void setInvalidSessionUrl(String invalidSessionUrl) {
		this.invalidSessionUrl = invalidSessionUrl;
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

		Assert.notNull(getSecurityContextRepository(),
				"'securityContextRepository' must be specified");

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
		if (getInvalidSessionRedirectStrategy() == null) {
			final RpcRedirectStrategy redirectStrategy;

			redirectStrategy = new RpcRedirectStrategy();
			redirectStrategy.setText(INVALID_SESSION_TEXT);
			setInvalidSessionRedirectStrategy(redirectStrategy);
		}
		if (getRpcHttpSessionStrategy() == null) {
			setRpcHttpSessionStrategy(new DefaultRpcHttpSessionStrategy());
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		final boolean continueChain;

		if (request.getAttribute(PROCESSED_ALREADY_ATTR_NAME) == null) {
			request.setAttribute(PROCESSED_ALREADY_ATTR_NAME, Boolean.TRUE);
			continueChain = process((HttpServletRequest) request,
					(HttpServletResponse) response);
		} else {
			continueChain = true;
		}

		if (continueChain) {
			chain.doFilter(request, response);
		}
	}

	protected boolean process(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		if (!getSecurityContextRepository().containsContext(request)) {
			log.debug("Security context repository does not "
					+ "contain current context");
			return processNewContext(request, response);
		}
		return true;
	}

	protected boolean processNewContext(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		final Authentication authentication;
		final RpcSessionInvalidationPolicy policy;
		boolean ok = false;

		policy = getRpcHttpSessionStrategy().prepareSession(request, response);
		try {
			authentication = SecurityContextHolder.getContext()
					.getAuthentication();
			if ((authentication != null)
					&& !getAuthenticationTrustResolver().isAnonymous(
							authentication)) {
				/*
				 * Authentication has been completed while processing this
				 * request. It must be save to the security context repository
				 * to make it available to subsequent requests of this client.
				 */
				ok = saveSecurityContext(request, response, authentication);
			} else {
				ok = processUnauthenticatedRequest(request, response,
						authentication);
			}
		} finally {
			if (!ok && policy.isInvalidateOnUnexpectedException()) {
				invalidateSession(request);
			}
		}

		return ok;
	}

	protected boolean saveSecurityContext(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		log.debug("Saving security context");

		try {
			applySessionAuthenticationStrategy(authentication, request,
					response);
		} catch (SessionAuthenticationException e) {
			log.info("Session authentication strategy "
					+ "rejected authentication request", e);
			unsuccessfulAuthentication(request, response, e);
			return false;
		}

		getSecurityContextRepository().saveContext(
				SecurityContextHolder.getContext(), request, response);
		return true;
	}

	protected void applySessionAuthenticationStrategy(
			Authentication authentication, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		final SessionAuthenticationStrategy sessionAuthenticationStrategy;

		sessionAuthenticationStrategy = getSessionAuthenticationStrategy();
		if (!appliedSessionAuthenticationStrategy(request,
				sessionAuthenticationStrategy)) {
			sessionAuthenticationStrategy.onAuthentication(authentication,
					request, response);
			saveAppliedSessionAuthenticationStrategy(request,
					sessionAuthenticationStrategy);
		} else {
			log.debug("Session authentication strategy "
					+ "has been applied already");
		}
	}

	protected void unsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		SecurityContextHolder.clearContext();
		getAuthenticationFailureHandler().onAuthenticationFailure(request,
				response, exception);
	}

	protected boolean processUnauthenticatedRequest(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		if ((request.getRequestedSessionId() != null)
				&& !request.isRequestedSessionIdValid()) {
			log.debug("Request does not contain a valid session ID");

			getInvalidSessionRedirectStrategy().sendRedirect(request, response,
					getInvalidSessionUrl());
			return false;
		}

		return true;
	}

	protected void invalidateSession(HttpServletRequest request) {
		final HttpSession session;

		session = request.getSession(false);
		if (session != null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalidating session " + session.getId());
			}
			session.invalidate();
		}
	}
}
