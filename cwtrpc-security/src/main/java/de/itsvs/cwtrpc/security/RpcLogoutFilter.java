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
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.Assert;

import de.itsvs.cwtrpc.core.RpcSessionInvalidationPolicy;
import de.itsvs.cwtrpc.core.CwtRpcException;
import de.itsvs.cwtrpc.core.CwtRpcUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RpcLogoutFilter extends AbstractRpcProcessingFilter {
	private final Log log = LogFactory.getLog(RpcLogoutFilter.class);

	private List<LogoutHandler> logoutHandlers;

	private LogoutSuccessHandler logoutSuccessHandler;

	private LogoutFailureHandler logoutFailureHandler;

	private boolean invalidateSession = true;

	private boolean invalidateSessionOnExpectedException = true;

	private boolean invalidateSessionOnUnexpectedException = true;

	public List<LogoutHandler> getLogoutHandlers() {
		return logoutHandlers;
	}

	public void setLogoutHandlers(List<LogoutHandler> logoutHandlers) {
		this.logoutHandlers = logoutHandlers;
	}

	public LogoutSuccessHandler getLogoutSuccessHandler() {
		return logoutSuccessHandler;
	}

	public void setLogoutSuccessHandler(
			LogoutSuccessHandler logoutSuccessHandler) {
		Assert.notNull(logoutSuccessHandler,
				"'logoutSuccessHandler' must not be null");
		this.logoutSuccessHandler = logoutSuccessHandler;
	}

	public LogoutFailureHandler getLogoutFailureHandler() {
		return logoutFailureHandler;
	}

	public void setLogoutFailureHandler(
			LogoutFailureHandler logoutFailureHandler) {
		Assert.notNull(logoutFailureHandler,
				"'logoutFailureHandler' must not be null");
		this.logoutFailureHandler = logoutFailureHandler;
	}

	public boolean isInvalidateSession() {
		return invalidateSession;
	}

	public void setInvalidateSession(boolean invalidateSession) {
		this.invalidateSession = invalidateSession;
	}

	public boolean isInvalidateSessionOnExpectedException() {
		return invalidateSessionOnExpectedException;
	}

	public void setInvalidateSessionOnExpectedException(
			boolean invalidateSessionOnExpectedException) {
		this.invalidateSessionOnExpectedException = invalidateSessionOnExpectedException;
	}

	public boolean isInvalidateSessionOnUnexpectedException() {
		return invalidateSessionOnUnexpectedException;
	}

	public void setInvalidateSessionOnUnexpectedException(
			boolean invalidateSessionOnUnexpectedException) {
		this.invalidateSessionOnUnexpectedException = invalidateSessionOnUnexpectedException;
	}

	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();

		if (getLogoutSuccessHandler() == null) {
			final DefaultRpcLogoutSuccessHandler handler;

			handler = new DefaultRpcLogoutSuccessHandler();
			handler.setServletContext(getServletContext());
			handler.afterPropertiesSet();
			setLogoutSuccessHandler(handler);
		}
		if (getLogoutFailureHandler() == null) {
			final DefaultRpcLogoutFailureHandler handler;

			handler = new DefaultRpcLogoutFailureHandler();
			handler.setServletContext(getServletContext());
			handler.afterPropertiesSet();
			setLogoutFailureHandler(handler);
		}
	}

	@Override
	protected void process(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final Authentication authentication;
		boolean ok = false;

		authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!CwtRpcUtils.isRpcSessionInvalidationPolicySet(request)) {
			CwtRpcUtils.saveRpcSessionInvalidationPolicy(
					request,
					createRpcSessionInvalidationPolicy(request, response,
							authentication));
		} else {
			log.debug("RPC session invalidation policy "
					+ "has already been applied.");
		}

		try {
			if (authentication != null) {
				if (log.isDebugEnabled()) {
					log.debug("Logging out user '" + authentication.getName()
							+ "'");
				}

				chain.doFilter(request, response);

				if (getLogoutHandlers() != null) {
					for (LogoutHandler handler : getLogoutHandlers()) {
						handler.logout(request, response, authentication);
					}
				}
			} else {
				if ((request.getRequestedSessionId() == null)
						|| request.isRequestedSessionIdValid()) {
					log.debug("Request does not belong to "
							+ "an authenticated session");
					getLogoutFailureHandler().onLogoutFailure(
							request,
							response,
							new CwtRpcException("Request does not belong to "
									+ "an authenticated session."));
					return;
				}
				log.debug("Request does not include a valid "
						+ "authentication. It seems to be a result of a "
						+ "session timeout. Sending success response.");
			}

			/*
			 * If session has not been invalidated up to now, this is the last
			 * possibility to invalidate the session. The logout success hander
			 * may send the response to the client. The session should be
			 * invalidated before sending the response.
			 */
			if (isInvalidateSession()) {
				invalidateSession(request);
			}
			getLogoutSuccessHandler().onLogoutSuccess(request, response,
					authentication);

			ok = true;
		} finally {
			if (!ok && isInvalidateSession()) {
				invalidateSession(request);
			}
		}
	}

	protected RpcSessionInvalidationPolicy createRpcSessionInvalidationPolicy(
			HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		final boolean invalidateSession = isInvalidateSession();
		final RpcSessionInvalidationPolicyImpl policy;

		policy = new RpcSessionInvalidationPolicyImpl();
		policy.setInvalidateAfterInvocation(invalidateSession);
		policy.setInvalidateOnExpectedException(invalidateSession
				&& isInvalidateSessionOnExpectedException());
		policy.setInvalidateOnUnexpectedException(invalidateSession
				&& isInvalidateSessionOnUnexpectedException());

		return policy;
	}

	protected static class RpcSessionInvalidationPolicyImpl implements
			RpcSessionInvalidationPolicy {
		private static final long serialVersionUID = 8325887953635922832L;

		private boolean invalidateAfterInvocation;

		private boolean invalidateOnUnexpectedException;

		private boolean invalidateOnExpectedException;

		public boolean isInvalidateAfterInvocation() {
			return invalidateAfterInvocation;
		}

		public void setInvalidateAfterInvocation(
				boolean invalidateAfterInvocation) {
			this.invalidateAfterInvocation = invalidateAfterInvocation;
		}

		public boolean isInvalidateOnUnexpectedException() {
			return invalidateOnUnexpectedException;
		}

		public void setInvalidateOnUnexpectedException(
				boolean invalidateOnUnexpectedException) {
			this.invalidateOnUnexpectedException = invalidateOnUnexpectedException;
		}

		public boolean isInvalidateOnExpectedException() {
			return invalidateOnExpectedException;
		}

		public void setInvalidateOnExpectedException(
				boolean invalidateOnExpectedException) {
			this.invalidateOnExpectedException = invalidateOnExpectedException;
		}
	}
}
