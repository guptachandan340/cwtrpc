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
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.itsvs.cwtrpc.core.RpcSessionInvalidationPolicy;
import de.itsvs.cwtrpc.core.CwtRpcUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultRpcHttpSessionStrategy implements RpcHttpSessionStrategy {
	private final Log log = LogFactory
			.getLog(DefaultRpcHttpSessionStrategy.class);

	private boolean createSession = true;

	private boolean clearSession = true;

	private boolean invalidateSessionOnExpectedException = true;

	public boolean isCreateSession() {
		return createSession;
	}

	public void setCreateSession(boolean createSession) {
		this.createSession = createSession;
	}

	public boolean isClearSession() {
		return clearSession;
	}

	public void setClearSession(boolean clearSession) {
		this.clearSession = clearSession;
	}

	public boolean isInvalidateSessionOnExpectedException() {
		return invalidateSessionOnExpectedException;
	}

	public void setInvalidateSessionOnExpectedException(
			boolean invalidateSessionOnExpectedException) {
		this.invalidateSessionOnExpectedException = invalidateSessionOnExpectedException;
	}

	public RpcSessionInvalidationPolicy prepareSession(
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		final RpcSessionInvalidationPolicy policy;

		if (CwtRpcUtils.isRpcSessionInvalidationPolicySet(request)) {
			log.debug("RPC session invalidation policy has "
					+ "already been applied. Returning default policy.");
			policy = CwtRpcUtils.getRpcSessionInvalidationPolicy(request);
		} else {
			policy = prepareUnpreparedSession(request, response);
		}

		return policy;
	}

	protected RpcSessionInvalidationPolicy prepareUnpreparedSession(
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		final HttpSession existingSession;
		final RpcSessionInvalidationPolicy policy;
		boolean createdSession = false;

		existingSession = request.getSession(false);
		if ((existingSession != null) && log.isDebugEnabled()) {
			log.debug("Existing session " + existingSession.getId());
		}

		if (isClearSession() && (existingSession != null)) {
			if (log.isDebugEnabled()) {
				log.debug("Clearing attributes of existing session "
						+ existingSession.getId());
			}
			clearSession(request, response, existingSession);
		}

		if (isCreateSession() && (existingSession == null)) {
			final HttpSession newSession;

			newSession = request.getSession(true);
			if (log.isDebugEnabled()) {
				log.debug("Created new session " + newSession.getId());
			}
			createdSession = true;
		}

		policy = createRpcSessionInvalidationPolicy(request, response,
				createdSession);
		CwtRpcUtils.saveRpcSessionInvalidationPolicy(request, policy);

		return policy;
	}

	protected void clearSession(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws IOException, ServletException {
		for (@SuppressWarnings("unchecked")
		Enumeration<String> names = session.getAttributeNames(); names
				.hasMoreElements();) {
			session.removeAttribute(names.nextElement());
		}
	}

	protected RpcSessionInvalidationPolicy createRpcSessionInvalidationPolicy(
			HttpServletRequest request, HttpServletResponse response,
			boolean createdSession) throws IOException, ServletException {
		final RpcSessionInvalidationPolicyImpl policy;

		policy = new RpcSessionInvalidationPolicyImpl();
		policy.setInvalidateAfterInvocation(false);
		policy.setInvalidateOnExpectedException(createdSession
				&& isInvalidateSessionOnExpectedException());
		policy.setInvalidateOnUnexpectedException(createdSession);

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
