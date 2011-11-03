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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultExtendedSerializationPolicyProvider implements
		ExtendedSerializationPolicyProvider, InitializingBean,
		ServletContextAware {
	private final Log log = LogFactory
			.getLog(DefaultExtendedSerializationPolicyProvider.class);

	private final Map<SerializationPolicyKey, SerializationPolicy> serializationPoliciesByUri = new HashMap<SerializationPolicyKey, SerializationPolicy>();

	private ServletContext servletContext;

	public DefaultExtendedSerializationPolicyProvider() {
		super();
	}

	public DefaultExtendedSerializationPolicyProvider(
			ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getServletContext(), "servletContext must be specified");
	}

	public SerializationPolicy getSerializationPolicy(
			HttpServletRequest request, String moduleBaseUrl, String strongName) {
		final String moduleBasePath;
		final SerializationPolicyKey key;
		SerializationPolicy serializationPolicy;

		moduleBasePath = getModuleBasePath(request, moduleBaseUrl);
		key = new SerializationPolicyKey(moduleBasePath, strongName);
		synchronized (serializationPoliciesByUri) {
			serializationPolicy = serializationPoliciesByUri.get(key);
		}
		if (serializationPolicy == null) {
			serializationPolicy = loadSerializationPolicy(request,
					moduleBasePath, strongName);
			synchronized (serializationPoliciesByUri) {
				serializationPoliciesByUri.put(key, serializationPolicy);
			}
		}

		return serializationPolicy;
	}

	protected SerializationPolicy loadSerializationPolicy(
			HttpServletRequest request, String moduleBasePath, String strongName)
			throws IncompatibleRemoteServiceException, CwtRpcException {
		final String relativeModuleBasePath;
		final String policyFilePath;
		final InputStream is;
		final SerializationPolicy serializationPolicy;

		if (log.isDebugEnabled()) {
			log.debug("Trying to load serialization policy "
					+ "for module base path '" + moduleBasePath
					+ "' with strong name '" + strongName + "'");
		}
		relativeModuleBasePath = getRelativeModuleBasePath(request,
				moduleBasePath);
		policyFilePath = getPolicyPath(relativeModuleBasePath, strongName);

		if (log.isDebugEnabled()) {
			log.debug("Trying to load policy file '" + policyFilePath
					+ "' from servlet context");
		}
		is = getServletContext().getResourceAsStream(policyFilePath);
		try {
			if (is != null) {
				try {
					serializationPolicy = SerializationPolicyLoader
							.loadFromStream(is, null);
				} catch (ParseException e) {
					log.error("Failed to parse policy file '" + policyFilePath
							+ "'", e);
					throw new CwtRpcException(
							"System error while initializing serialization");
				} catch (IOException e) {
					log.error("Error while reading policy file '"
							+ policyFilePath + "'", e);
					throw new CwtRpcException(
							"System error while initializing serialization");
				}
			} else {
				if (log.isWarnEnabled()) {
					log.warn("Requested policy file '" + policyFilePath
							+ "' does not exist");
				}
				throw new IncompatibleRemoteServiceException("Strong name '"
						+ strongName + "' seems to be invalid");
			}
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				log.error("Error while closing servlet context resource '"
						+ policyFilePath + "'", e);
			}
		}

		return serializationPolicy;
	}

	protected String getModuleBasePath(HttpServletRequest request,
			String moduleBaseUrl) throws IncompatibleRemoteServiceException,
			SecurityException {
		final String path;

		try {
			path = new URL(moduleBaseUrl).getPath();
		} catch (MalformedURLException e) {
			if (log.isWarnEnabled()) {
				log.warn("Received module base Url is invalid: "
						+ moduleBaseUrl);
			}
			throw new IncompatibleRemoteServiceException(
					"Module base Url is invalid");
		}

		return path;
	}

	protected String getRelativeModuleBasePath(HttpServletRequest request,
			String moduleBasePath) throws IncompatibleRemoteServiceException,
			SecurityException {
		final String contextPath;
		final String relativeModuleBasePath;

		contextPath = request.getContextPath();
		if (!moduleBasePath.startsWith(contextPath)) {
			throw new IncompatibleRemoteServiceException(
					"Module base URL is invalid "
							+ "(does not start with context path)");
		}

		relativeModuleBasePath = moduleBasePath.substring(contextPath.length());
		if (!relativeModuleBasePath.startsWith("/")
				|| !relativeModuleBasePath.endsWith("/")) {
			throw new IncompatibleRemoteServiceException(
					"Module base URL is invalid (must start and end with slash)");
		}
		if (relativeModuleBasePath.contains("/..")) {
			throw new SecurityException("Specified module base URL contains "
					+ "relative path to sub directory: " + moduleBasePath);
		}

		return relativeModuleBasePath;
	}

	protected String getPolicyPath(String relativeModuleBasePath,
			String strongName) throws SecurityException {
		final StringBuilder strongNamePathBuilder;
		final String resultingPath;

		strongNamePathBuilder = new StringBuilder();
		strongNamePathBuilder.append(relativeModuleBasePath);
		strongNamePathBuilder.append(strongName);
		if (strongNamePathBuilder.indexOf("/..") >= 0) {
			throw new SecurityException(
					"Specified combination of module base Url and "
							+ "strong name contains relative path to "
							+ "sub directory: " + strongNamePathBuilder);
		}

		resultingPath = SerializationPolicyLoader
				.getSerializationPolicyFileName(strongNamePathBuilder
						.toString());
		return resultingPath;
	}

	protected static final class SerializationPolicyKey {
		private final String moduleBasePath;

		private final String strongName;

		public SerializationPolicyKey(String moduleBasePath, String strongName) {
			this.moduleBasePath = moduleBasePath;
			this.strongName = strongName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((moduleBasePath == null) ? 0 : moduleBasePath.hashCode());
			result = prime * result
					+ ((strongName == null) ? 0 : strongName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SerializationPolicyKey other = (SerializationPolicyKey) obj;
			if (moduleBasePath == null) {
				if (other.moduleBasePath != null)
					return false;
			} else if (!moduleBasePath.equals(other.moduleBasePath))
				return false;
			if (strongName == null) {
				if (other.strongName != null)
					return false;
			} else if (!strongName.equals(other.strongName))
				return false;
			return true;
		}
	}
}
