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

package de.itsvs.cwtrpc.core;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextException;

import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.server.rpc.RPCServletUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public abstract class CwtRpcUtils {
	public static final String PRELOADED_CLASSES_INIT_PARAM_NAME = "contextLoaderPreloadedClasses";

	protected static final String GWT_RPC_REQUEST_CONTENT_ATTR_NAME = CwtRpcUtils.class
			.getName().concat(".gwtRpcRequestContent");

	protected static final String RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME = CwtRpcUtils.class
			.getName().concat(".rpcSessionInvalidationPolicy");

	protected static final RpcSessionInvalidationPolicy defaultRpcSessionInvalidationPolicy = new DefaultRpcSessionInvalidationPolicy();

	private CwtRpcUtils() {
		super();
	}

	public static void preloadContextClasses(ServletContext servletContext)
			throws ApplicationContextException {
		final Log log = LogFactory.getLog(CwtRpcUtils.class);
		final String preloadedClasses;

		preloadedClasses = servletContext
				.getInitParameter(PRELOADED_CLASSES_INIT_PARAM_NAME);
		if (preloadedClasses != null) {
			final String[] classNames = preloadedClasses.split("\\s");

			for (String className : classNames) {
				if (className.length() > 0) {
					if (log.isDebugEnabled()) {
						log.debug("Preloading class: " + className);
					}
					try {
						CwtRpcUtils.class.getClassLoader().loadClass(className);
					} catch (ClassNotFoundException e) {
						throw new ApplicationContextException(
								"Could not load class '" + className + "'", e);
					}
				}
			}
		}
	}

	public static boolean containsStrongName(HttpServletRequest request) {
		return (request.getHeader(RpcRequestBuilder.STRONG_NAME_HEADER) != null);
	}

	public static boolean isContentRead(HttpServletRequest request) {
		return (request.getAttribute(GWT_RPC_REQUEST_CONTENT_ATTR_NAME) != null);
	}

	public static String readContent(HttpServletRequest request)
			throws ServletException, IOException, SecurityException {
		String payload;

		/*
		 * Content can only be read once from servlet request. In order to
		 * access it multiple times the content must be stored in request after
		 * it has been read.
		 */
		payload = (String) request
				.getAttribute(GWT_RPC_REQUEST_CONTENT_ATTR_NAME);
		if (payload == null) {
			if (!containsStrongName(request)) {
				throw new SecurityException(
						"Request does not contain required strong name header");
			}
			payload = RPCServletUtils.readContentAsGwtRpc(request);
			request.setAttribute(GWT_RPC_REQUEST_CONTENT_ATTR_NAME, payload);
		}

		return payload;
	}

	public static RpcSessionInvalidationPolicy getDefaultRpcSessionInvalidationPolicy() {
		return defaultRpcSessionInvalidationPolicy;
	}

	public static void saveRpcSessionInvalidationPolicy(
			HttpServletRequest request, RpcSessionInvalidationPolicy policy) {
		request.setAttribute(RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME, policy);
	}

	public static boolean isRpcSessionInvalidationPolicySet(
			HttpServletRequest request) {
		return (request.getAttribute(RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME) != null);
	}

	public static RpcSessionInvalidationPolicy getRpcSessionInvalidationPolicy(
			HttpServletRequest request) {
		RpcSessionInvalidationPolicy policy;

		policy = (RpcSessionInvalidationPolicy) request
				.getAttribute(RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME);
		if (policy == null) {
			policy = getDefaultRpcSessionInvalidationPolicy();
		}
		return policy;
	}

	public static void addNoCacheResponseHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache, no-store, no-transform");
		response.setDateHeader("Expires", -1L);
		response.setHeader("Pragma", "no-cache");
	}

	protected static class DefaultRpcSessionInvalidationPolicy implements
			RpcSessionInvalidationPolicy {
		private static final long serialVersionUID = -1565577317645452477L;

		public boolean isInvalidateAfterInvocation() {
			return false;
		}

		public boolean isInvalidateOnUnexpectedException() {
			return false;
		}

		public boolean isInvalidateOnExpectedException() {
			return false;
		}
	}
}
