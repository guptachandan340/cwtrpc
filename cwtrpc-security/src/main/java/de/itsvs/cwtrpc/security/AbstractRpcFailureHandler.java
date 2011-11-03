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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.UnexpectedException;

import de.itsvs.cwtrpc.core.CwtRpcUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public abstract class AbstractRpcFailureHandler implements ServletContextAware,
		InitializingBean {
	private final Log log = LogFactory.getLog(AbstractRpcFailureHandler.class);

	private ServletContext servletContext;

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void afterPropertiesSet() {
		Assert.notNull(getServletContext(), "servletContext must be specified");
	}

	protected void writeUnexpectedException(HttpServletRequest request,
			HttpServletResponse response, Throwable exception)
			throws IOException {
		if (CwtRpcUtils.getRpcSessionInvalidationPolicy(request)
				.isInvalidateOnUnexpectedException()) {
			invalidateSession(request);
		}

		response.reset();
		addNoCacheResponseHeaders(request, response);
		RPCServletUtils.writeResponseForUnexpectedFailure(getServletContext(),
				response, exception);
		response.flushBuffer();
	}

	protected void writeExpectedException(HttpServletRequest request,
			HttpServletResponse response, Exception remoteException)
			throws IOException {
		final RPCRequest rpcRequest;
		final String responsePayload;

		rpcRequest = AbstractRpcAuthenticationProcessingFilter
				.getRpcRequest(request);
		try {
			if (rpcRequest != null) {
				/* checked exceptions must be declared by service method */
				responsePayload = RPC.encodeResponseForFailure(
						rpcRequest.getMethod(), remoteException,
						rpcRequest.getSerializationPolicy());
			} else {
				log.warn("Could not determine RPC request. Using default serialization.");
				responsePayload = RPC.encodeResponseForFailure(null,
						remoteException);
			}
		} catch (UnexpectedException e) {
			if (rpcRequest != null) {
				log.error("Exception " + remoteException.getClass().getName()
						+ " is unexpected for method " + rpcRequest.getMethod()
						+ " of declaring class "
						+ rpcRequest.getMethod().getDeclaringClass().getName(),
						e);
			} else {
				log.error("Exception " + remoteException.getClass().getName()
						+ " is unexpected for unknown method", e);
			}
			writeUnexpectedException(request, response, remoteException);
			return;
		} catch (SerializationException e) {
			log.error("Error while serializing "
					+ remoteException.getClass().getName(), e);
			writeUnexpectedException(request, response, remoteException);
			return;
		}

		if (CwtRpcUtils.getRpcSessionInvalidationPolicy(request)
				.isInvalidateOnExpectedException()) {
			invalidateSession(request);
		}

		response.reset();
		addNoCacheResponseHeaders(request, response);
		RPCServletUtils.writeResponse(getServletContext(), response,
				responsePayload, false);
		response.flushBuffer();
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

	protected void addNoCacheResponseHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		CwtRpcUtils.addNoCacheResponseHeaders(request, response);
	}
}
