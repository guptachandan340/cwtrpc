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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.ServletContextAware;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;

import de.itsvs.cwtrpc.core.CwtRpcException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public abstract class AbstractRpcSuccessHandler implements ServletContextAware,
		InitializingBean {
	private final Log log = LogFactory.getLog(AbstractRpcSuccessHandler.class);

	private ServletContext servletContext;

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void afterPropertiesSet() throws ServletException {
		/* nothing to be done at the moment */
	}

	protected void sendResponse(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		final RPCRequest rpcRequest;
		final String responsePayload;

		rpcRequest = AbstractRpcAuthenticationProcessingFilter
				.getRpcRequest(request);
		if (rpcRequest == null) {
			throw new CwtRpcException(
					"RPC request has not been stored in request ("
							+ AbstractRpcAuthenticationProcessingFilter.class
									.getName() + " must be used)");
		}

		try {
			addNoCacheResponseHeaders(request, response);
			responsePayload = RPC.encodeResponseForSuccess(
					rpcRequest.getMethod(),
					getResponse(request, rpcRequest, authentication),
					rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
			RPCServletUtils.writeResponse(getServletContext(), response,
					responsePayload, false);
		} catch (SerializationException e) {
			log.error("Serialization error while processing service request", e);
			if (!response.isCommitted()) {
				RPCServletUtils.writeResponseForUnexpectedFailure(
						getServletContext(), response, e);
			}
		}
	}

	protected void addNoCacheResponseHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache, no-store");
		response.setDateHeader("Expires", -1L);
		response.setHeader("Pragma", "no-cache");
	}

	protected Object getResponse(HttpServletRequest request,
			RPCRequest rpcRequest, Authentication authentication) {
		return null;
	}
}
