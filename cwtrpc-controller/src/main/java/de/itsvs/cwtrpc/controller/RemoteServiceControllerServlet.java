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

package de.itsvs.cwtrpc.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.UnexpectedException;

import de.itsvs.cwtrpc.controller.token.RpcTokenValidator;
import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProviderDelegate;
import de.itsvs.cwtrpc.core.CwtRpcUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 2027209101475019552L;

	private Log log = LogFactory.getLog(RemoteServiceControllerServlet.class);

	public static final String CONFIG_BEAN_NAME_INIT_PARAM = "configBeanName";

	public static final String SPRING_REQUEST_CONTEXT_ENABLED_INIT_PARAM = "springRequestContextEnabled";

	public static final boolean DEFAULT_SPRING_REQUEST_CONTEXT_ENABLED = true;

	protected static final String ACCESS_DENIED_EXCEPTION_NAME = "org.springframework.security.access.AccessDeniedException";

	private final RequestContextListener requestContextListener = new RequestContextListener();

	private boolean springRequestContextEnabled;

	private WebApplicationContext applicationContext;

	private Map<String, PreparedRemoteServiceConfig> serviceConfigsByUri;

	private ExtendedSerializationPolicyProvider serializationPolicyProvider;

	private Set<Class<? extends RuntimeException>> uncaughtExceptions;

	public boolean isSpringRequestContextEnabled() {
		return springRequestContextEnabled;
	}

	protected void setSpringRequestContextEnabled(
			boolean springRequestContextEnabled) {
		this.springRequestContextEnabled = springRequestContextEnabled;
	}

	public WebApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(WebApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Map<String, PreparedRemoteServiceConfig> getServiceConfigsByUri() {
		return serviceConfigsByUri;
	}

	protected void setServiceConfigsByUri(
			Map<String, PreparedRemoteServiceConfig> serviceConfigsByUri) {
		this.serviceConfigsByUri = serviceConfigsByUri;
	}

	public ExtendedSerializationPolicyProvider getSerializationPolicyProvider() {
		return serializationPolicyProvider;
	}

	protected void setSerializationPolicyProvider(
			ExtendedSerializationPolicyProvider serializationPolicyProvider) {
		this.serializationPolicyProvider = serializationPolicyProvider;
	}

	public Set<Class<? extends RuntimeException>> getUncaughtExceptions() {
		return uncaughtExceptions;
	}

	protected void setUncaughtExceptions(
			Set<Class<? extends RuntimeException>> uncaughtExceptions) {
		this.uncaughtExceptions = uncaughtExceptions;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		final WebApplicationContext applicationContext;
		final RemoteServiceControllerConfig controllerConfig;
		String configBeanName;
		String initParam;

		super.init(config);

		initParam = config
				.getInitParameter(SPRING_REQUEST_CONTEXT_ENABLED_INIT_PARAM);
		if (initParam != null) {
			setSpringRequestContextEnabled(Boolean.valueOf(initParam));
		} else {
			setSpringRequestContextEnabled(DEFAULT_SPRING_REQUEST_CONTEXT_ENABLED);
		}
		if (log.isInfoEnabled()) {
			log.info("Spring request context initialization on every request: "
					+ isSpringRequestContextEnabled());
		}

		configBeanName = config.getInitParameter(CONFIG_BEAN_NAME_INIT_PARAM);
		if (configBeanName == null) {
			configBeanName = RemoteServiceControllerConfig.DEFAULT_BEAN_ID;
		}

		applicationContext = WebApplicationContextUtils
				.getWebApplicationContext(config.getServletContext());
		if (log.isDebugEnabled()) {
			log.debug("Resolving controller config with bean name '"
					+ configBeanName + "' from application context");
		}
		controllerConfig = applicationContext.getBean(configBeanName,
				RemoteServiceControllerConfig.class);

		setApplicationContext(applicationContext);
		setSerializationPolicyProvider(controllerConfig
				.getSerializationPolicyProvider());
		setServiceConfigsByUri(Collections
				.unmodifiableMap(createPreparedRemoteServiceConfigBuilder()
						.build(controllerConfig)));

		setUncaughtExceptions(getUncaughtExceptions(controllerConfig));
	}

	protected PreparedRemoteServiceConfigBuilder createPreparedRemoteServiceConfigBuilder() {
		final PreparedRemoteServiceConfigBuilder builder;

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(getApplicationContext());

		return builder;
	}

	@SuppressWarnings("unchecked")
	protected Set<Class<? extends RuntimeException>> getUncaughtExceptions(
			RemoteServiceControllerConfig controllerConfig) {
		final Set<Class<? extends RuntimeException>> exceptions;
		final Class<? extends RuntimeException> exception;

		exceptions = new HashSet<Class<? extends RuntimeException>>();
		try {
			exception = (Class<? extends RuntimeException>) Class
					.forName(ACCESS_DENIED_EXCEPTION_NAME);
			exceptions.add(exception);
		} catch (ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.debug("Did not find class " + ACCESS_DENIED_EXCEPTION_NAME
						+ ". Exception will not be used "
						+ "as uncaught exception.", e);
			}
		}

		return Collections.unmodifiableSet(exceptions);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			final PreparedRemoteServiceConfig serviceConfig;
			final RemoteServiceContext serviceContext;
			final ServletRequestEvent servletRequestEvent;

			serviceConfig = getServiceConfig(request);
			if (serviceConfig == null) {
				if (log.isDebugEnabled()) {
					log.debug("No service has been configured for requested URI: "
							+ request.getRequestURI());
				}
				if (CwtRpcUtils.getRpcSessionInvalidationPolicy(request)
						.isInvalidateOnUnexpectedException()) {
					invalidateSession(request);
				}
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"No service has been configured for requested URI");
				return;
			}

			serviceContext = new DefaultRemoteServiceContext(request, response);
			RemoteServiceContextHolder.setContext(serviceContext);

			if (isSpringRequestContextEnabled()) {
				servletRequestEvent = new ServletRequestEvent(
						getServletContext(), request);
			} else {
				servletRequestEvent = null;
			}
			try {
				if (servletRequestEvent != null) {
					requestContextListener
							.requestInitialized(servletRequestEvent);
				}
				processRpcCall(request, response, serviceConfig);
			} finally {
				if (servletRequestEvent != null) {
					requestContextListener
							.requestDestroyed(servletRequestEvent);
				}
			}
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			if (isUncaughtException(e)) {
				if (CwtRpcUtils.getRpcSessionInvalidationPolicy(request)
						.isInvalidateOnUnexpectedException()) {
					invalidateSession(request);
				}
				throw e;
			}
			processUnexpectedFailure(request, response, e);
		} catch (Throwable e) {
			processUnexpectedFailure(request, response, e);
		} finally {
			RemoteServiceContextHolder.resetContext();
		}
	}

	protected PreparedRemoteServiceConfig getServiceConfig(
			HttpServletRequest request) {
		final String contextPath;
		final StringBuilder uri;
		final PreparedRemoteServiceConfig serviceConfig;

		contextPath = request.getContextPath();
		uri = new StringBuilder(request.getRequestURI());
		if ((contextPath.length() > 0) && (uri.indexOf(contextPath) == 0)) {
			uri.delete(0, contextPath.length());
		}
		if ((uri.length() > 0) || (uri.charAt(0) == '/')) {
			uri.deleteCharAt(0);
		}

		if (log.isDebugEnabled()) {
			log.debug("Service URI is '" + uri + "'");
		}
		serviceConfig = getServiceConfigsByUri().get(uri.toString());

		return serviceConfig;
	}

	protected void invalidateSession(HttpServletRequest request)
			throws ServletException {
		final HttpSession session;

		session = request.getSession(false);
		if (session != null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalidating session " + session.getId());
			}
			session.invalidate();
		}
	}

	protected void processUnexpectedFailure(HttpServletRequest request,
			HttpServletResponse response, Throwable exception)
			throws ServletException, IOException {
		log.error("Unexpected error while processing service request",
				exception);

		if (CwtRpcUtils.getRpcSessionInvalidationPolicy(request)
				.isInvalidateOnUnexpectedException()) {
			invalidateSession(request);
		}
		if (!response.isCommitted()) {
			response.reset();
			addNoCacheResponseHeaders(request, response);
			RPCServletUtils.writeResponseForUnexpectedFailure(
					getServletContext(), response, exception);
			/*
			 * Flush all remaining output to the client (client can continue
			 * immediately). Also response will be marked as being committed
			 * (status may be required later by a filter).
			 */
			response.flushBuffer();
		}
	}

	protected boolean isUncaughtException(Throwable e) {
		for (Class<? extends RuntimeException> exception : getUncaughtExceptions()) {
			if (exception.isInstance(e)) {
				if (log.isDebugEnabled()) {
					log.debug("Exception " + e.getClass().getName()
							+ " is uncaught exception (extends "
							+ exception.getName() + ")");
				}
				return true;
			}
		}

		return false;
	}

	protected void processRpcCall(HttpServletRequest request,
			HttpServletResponse response,
			PreparedRemoteServiceConfig serviceConfig) throws ServletException,
			IOException, SerializationException, SecurityException {
		final String requestPayload;
		final String responsePayload;

		requestPayload = readContent(request);
		responsePayload = processRpcCall(request, response, serviceConfig,
				requestPayload);
		writeResponse(request, response, serviceConfig, responsePayload);
	}

	protected String readContent(HttpServletRequest request)
			throws ServletException, IOException, SecurityException {
		return CwtRpcUtils.readContent(request);
	}

	protected void writeResponse(HttpServletRequest request,
			HttpServletResponse response,
			PreparedRemoteServiceConfig serviceConfig, String payload)
			throws IOException {
		final boolean gzipResponse;

		gzipResponse = serviceConfig.isResponseCompressionEnabled()
				&& RPCServletUtils.acceptsGzipEncoding(request)
				&& RPCServletUtils
						.exceedsUncompressedContentLengthLimit(payload);

		if (log.isDebugEnabled()) {
			log.debug("Writing response (gzip=" + gzipResponse + ")");
		}
		addNoCacheResponseHeaders(request, response);
		RPCServletUtils.writeResponse(getServletContext(), response, payload,
				gzipResponse);
		/*
		 * Flush all remaining output to the client (client can continue
		 * immediately). Also response will be marked as being committed (status
		 * may be required later by a filter).
		 */
		response.flushBuffer();
	}

	protected void addNoCacheResponseHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		CwtRpcUtils.addNoCacheResponseHeaders(request, response);
	}

	protected String processRpcCall(HttpServletRequest servletRequest,
			HttpServletResponse response,
			PreparedRemoteServiceConfig serviceConfig, String requestPayload)
			throws ServletException, IOException, SerializationException {
		String responsePayload;

		try {
			final RPCRequest rpcRequest;
			final Object service;

			rpcRequest = RPC.decodeRequest(requestPayload, serviceConfig
					.getServiceInterface(),
					new ExtendedSerializationPolicyProviderDelegate(
							getSerializationPolicyProvider(), servletRequest));
			validateRpcToken(servletRequest, serviceConfig, rpcRequest);

			if (log.isDebugEnabled()) {
				log.debug("Invoking method " + rpcRequest.getMethod()
						+ " of service interface "
						+ serviceConfig.getServiceName() + " (service '"
						+ serviceConfig.getServiceName() + "')");
			}
			service = getServiceBean(servletRequest, serviceConfig);
			responsePayload = invokeAndEncodeResponse(servletRequest, response,
					service, rpcRequest);
		} catch (IncompatibleRemoteServiceException e) {
			log.info("Incompatible remote service detected "
					+ "while processing request", e);
			responsePayload = processGwtRpcException(servletRequest, e);
		} catch (RpcTokenException e) {
			log.info("Invalid RPC token detected while processing request", e);
			responsePayload = processGwtRpcException(servletRequest, e);
		}

		return responsePayload;
	}

	protected void validateRpcToken(HttpServletRequest servletRequest,
			PreparedRemoteServiceConfig serviceConfig, RPCRequest rpcRequest)
			throws RpcTokenException {
		final RpcTokenValidator rpcTokenValidator;

		rpcTokenValidator = serviceConfig.getRpcTokenValidator();
		if (rpcTokenValidator != null) {
			if (rpcTokenValidator.shouldValidateToken(servletRequest,
					rpcRequest)) {
				rpcTokenValidator.validateToken(servletRequest, rpcRequest);
			}
		} else {
			log.debug("RPC token validation has not been enabled for service");
		}
	}

	protected Object getServiceBean(HttpServletRequest request,
			PreparedRemoteServiceConfig serviceConfig) {
		return getApplicationContext().getBean(serviceConfig.getServiceName());
	}

	protected String invokeAndEncodeResponse(HttpServletRequest servletRequest,
			HttpServletResponse response, Object service, RPCRequest rpcRequest)
			throws ServletException, IOException, SecurityException,
			SerializationException {
		String responsePayload;

		try {
			final Object invocationResult;

			invocationResult = rpcRequest.getMethod().invoke(service,
					rpcRequest.getParameters());
			responsePayload = RPC.encodeResponseForSuccess(
					rpcRequest.getMethod(), invocationResult,
					rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());

			if (CwtRpcUtils.getRpcSessionInvalidationPolicy(servletRequest)
					.isInvalidateAfterInvocation()) {
				invalidateSession(servletRequest);
			}
		} catch (IllegalAccessException e) {
			throw new SecurityException(
					"Illegal access detected when invoking method "
							+ rpcRequest.getMethod() + " on service "
							+ service.getClass().getName()
							+ " (as requested by client)", e);
		} catch (IllegalArgumentException e) {
			throw new SecurityException(
					"Illegal argument types detected when invoking method "
							+ rpcRequest.getMethod() + " with arguments \""
							+ createTypeNameString(rpcRequest.getParameters())
							+ "\" on service " + service.getClass().getName()
							+ " (as requested by client)", e);
		} catch (InvocationTargetException e) {
			responsePayload = processInvocationException(servletRequest,
					service, rpcRequest, e.getCause());
		}

		return responsePayload;
	}

	protected String createTypeNameString(Object[] values) {
		final StringBuilder sb = new StringBuilder();

		if (values != null) {
			for (Object value : values) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				if (value != null) {
					sb.append(value.getClass().getName());
				} else {
					sb.append("null");
				}
			}
		}

		return sb.toString();
	}

	protected String processInvocationException(
			HttpServletRequest servletRequest, Object service,
			RPCRequest rpcRequest, Throwable exception)
			throws ServletException, IOException, SerializationException,
			RuntimeException {
		final String responsePayload;

		if (RPCServletUtils.isExpectedException(rpcRequest.getMethod(),
				exception)) {
			if (log.isDebugEnabled()) {
				log.debug("Expected exception thrown when invoking method "
						+ rpcRequest.getMethod() + " on service "
						+ service.getClass().getName(), exception);
			}
			responsePayload = processExpectedException(servletRequest, service,
					rpcRequest, exception);
		} else if (exception instanceof RuntimeException) {
			throw ((RuntimeException) exception);
		} else if (exception instanceof Error) {
			throw ((Error) exception);
		} else {
			throw new UnexpectedException("Unexpected checked exception "
					+ "occured while invoking method " + rpcRequest.getMethod()
					+ " on service " + service.getClass().getName(), exception);
		}

		return responsePayload;
	}

	protected String processExpectedException(
			HttpServletRequest servletRequest, Object service,
			RPCRequest rpcRequest, Throwable exception)
			throws ServletException, IOException, SerializationException {
		final String responsePayload;

		if (CwtRpcUtils.getRpcSessionInvalidationPolicy(servletRequest)
				.isInvalidateOnExpectedException()) {
			invalidateSession(servletRequest);
		}
		responsePayload = RPC.encodeResponseForFailure(rpcRequest.getMethod(),
				exception, rpcRequest.getSerializationPolicy(),
				rpcRequest.getFlags());

		return responsePayload;
	}

	protected String processGwtRpcException(HttpServletRequest servletRequest,
			Exception exception) throws ServletException, IOException,
			SerializationException {
		final String responsePayload;

		if (CwtRpcUtils.getRpcSessionInvalidationPolicy(servletRequest)
				.isInvalidateOnUnexpectedException()) {
			invalidateSession(servletRequest);
		}
		responsePayload = RPC.encodeResponseForFailure(null, exception);

		return responsePayload;
	}

	protected static class DefaultRemoteServiceContext implements
			RemoteServiceContext {
		private final HttpServletRequest request;

		private final HttpServletResponse response;

		public DefaultRemoteServiceContext(HttpServletRequest request,
				HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}

		public HttpServletRequest getServletRequest() {
			return request;
		}

		public HttpServletResponse getServletResponse() {
			return response;
		}
	}
}
