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
import java.lang.reflect.Method;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;

import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProviderDelegate;
import de.itsvs.cwtrpc.core.CwtRpcException;
import de.itsvs.cwtrpc.core.CwtRpcUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public abstract class AbstractRpcProcessingFilter extends GenericFilterBean
		implements BeanFactoryAware {
	private final Log log = LogFactory
			.getLog(AbstractRpcProcessingFilter.class);

	public static final String GWT_RPC_REQUEST_ATTR_NAME = AbstractRpcProcessingFilter.class
			.getName().concat(".gwtRpcRequest");

	private static final String POST_METHOD = "POST";

	private BeanFactory beanFactory;

	private String filterProcessesUrl;

	private boolean postOnly = true;

	private Class<?> serviceInterface;

	private String methodName;

	private ExtendedSerializationPolicyProvider serializationPolicyProvider;

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public String getFilterProcessesUrl() {
		return filterProcessesUrl;
	}

	public void setFilterProcessesUrl(String filterProcessesUrl) {
		Assert.hasText(filterProcessesUrl,
				"'filterProcessesUrl' must not be null");
		Assert.isTrue(UrlUtils.isValidRedirectUrl(filterProcessesUrl),
				filterProcessesUrl + " is not a valid URL");
		this.filterProcessesUrl = filterProcessesUrl;
	}

	public boolean isPostOnly() {
		return postOnly;
	}

	public void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly;
	}

	public Class<?> getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class<?> serviceInterface) {
		Assert.notNull(serviceInterface, "'serviceInterface' must not be null");
		this.serviceInterface = serviceInterface;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		Assert.hasText(methodName, "'methodName' must not be empty");
		this.methodName = methodName;
	}

	public ExtendedSerializationPolicyProvider getSerializationPolicyProvider() {
		return serializationPolicyProvider;
	}

	public void setSerializationPolicyProvider(
			ExtendedSerializationPolicyProvider serializationPolicyProvider) {
		Assert.notNull(serializationPolicyProvider,
				"'serializationPolicyProvider' must not be null");
		this.serializationPolicyProvider = serializationPolicyProvider;
	}

	@Override
	public void afterPropertiesSet() throws ServletException {
		boolean found;

		super.afterPropertiesSet();

		Assert.notNull(getFilterProcessesUrl(),
				"'filterProcessesUrl' must be specified");
		Assert.notNull(getServiceInterface(),
				"'serviceInterface' must be specified");
		Assert.isTrue(getServiceInterface().isInterface(),
				"'serviceInterface' " + getServiceInterface().getName()
						+ " must be an interface");
		Assert.notNull(getMethodName(), "'methodName' must be specified");

		found = false;
		for (Method method : getServiceInterface().getMethods()) {
			if (method.getName().equals(getMethodName())) {
				found = true;
				break;
			}
		}
		Assert.isTrue(found, "'serviceInterface' "
				+ getServiceInterface().getName()
				+ " does not include method '" + getMethodName() + "'");

		if (getSerializationPolicyProvider() == null) {
			if (getBeanFactory().containsBean(
					ExtendedSerializationPolicyProvider.DEFAULT_BEAN_ID)) {
				if (log.isInfoEnabled()) {
					log.info("Using serialization policy provided with bean name '"
							+ ExtendedSerializationPolicyProvider.DEFAULT_BEAN_ID
							+ "'");
				}
				setSerializationPolicyProvider(getBeanFactory().getBean(
						ExtendedSerializationPolicyProvider.DEFAULT_BEAN_ID,
						ExtendedSerializationPolicyProvider.class));
			} else {
				throw new CwtRpcException(
						"Either 'serializationPolicyProvider' must be specified or bean '"
								+ ExtendedSerializationPolicyProvider.DEFAULT_BEAN_ID
								+ "' must be available (controller or serialization "
								+ "policy provider must be declared before this bean)");
			}
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (filterProcesses((HttpServletRequest) request)) {
			process((HttpServletRequest) request,
					(HttpServletResponse) response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	protected boolean filterProcesses(HttpServletRequest request)
			throws IOException, ServletException {
		if (!matchesFilterProcessesUrl(request)) {
			return false;
		}
		if (!matchesMethodName(request)) {
			return false;
		}

		return true;
	}

	protected boolean matchesFilterProcessesUrl(HttpServletRequest request)
			throws IOException, ServletException {
		final String contextPath;
		final StringBuilder sb;
		final String uri;

		contextPath = request.getContextPath();
		sb = new StringBuilder(request.getRequestURI());
		if ((contextPath.length() > 0) && (sb.indexOf(contextPath) == 0)) {
			sb.delete(0, contextPath.length());
		}
		if ((sb.length() == 0) || (sb.charAt(0) != '/')) {
			sb.insert(0, '/');
		}
		uri = sb.toString();

		if (log.isDebugEnabled()) {
			log.debug("Checking received URI '" + uri
					+ "' against configured URI '" + getFilterProcessesUrl()
					+ "'");
		}
		return getFilterProcessesUrl().equals(uri);
	}

	protected boolean matchesMethodName(HttpServletRequest request)
			throws IOException, ServletException {
		final RPCRequest rpcRequest;

		rpcRequest = readRpcRequest(request);
		if (log.isDebugEnabled()) {
			log.debug("Checking received method name '"
					+ rpcRequest.getMethod().getName()
					+ "' against configured method name '" + getMethodName()
					+ "'");
		}

		return getMethodName().equals(rpcRequest.getMethod().getName());
	}

	protected void invalidateSession(HttpServletRequest request)
			throws IOException, ServletException {
		final HttpSession session;

		session = request.getSession(false);
		if (session != null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalidating session " + session.getId());
			}
			session.invalidate();
		}
	}

	protected abstract void process(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException;

	protected RPCRequest readRpcRequest(HttpServletRequest request)
			throws IOException, ServletException {
		RPCRequest rpcRequest;

		rpcRequest = getRpcRequest(request);
		if (rpcRequest == null) {
			final String requestPayload;

			if (isPostOnly() && !POST_METHOD.equals(request.getMethod())) {
				throw new AuthenticationServiceException(
						"Authentication method not supported: "
								+ request.getMethod());
			}

			requestPayload = readContent(request);
			rpcRequest = RPC.decodeRequest(requestPayload,
					getServiceInterface(),
					new ExtendedSerializationPolicyProviderDelegate(
							getSerializationPolicyProvider(), request));
			request.setAttribute(GWT_RPC_REQUEST_ATTR_NAME, rpcRequest);
		}

		return rpcRequest;
	}

	protected String readContent(HttpServletRequest request)
			throws ServletException, IOException, SecurityException {
		return CwtRpcUtils.readContent(request);
	}

	public static RPCRequest getRpcRequest(HttpServletRequest request) {
		return ((RPCRequest) request.getAttribute(GWT_RPC_REQUEST_ATTR_NAME));
	}
}
