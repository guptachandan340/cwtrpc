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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.itsvs.cwtrpc.controller.token.RpcTokenValidator;
import de.itsvs.cwtrpc.core.CwtRpcException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class PreparedRemoteServiceConfigBuilder implements BeanFactoryAware {
	private final Log log = LogFactory
			.getLog(PreparedRemoteServiceConfigBuilder.class);

	private BeanFactory beanFactory;

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public Map<String, PreparedRemoteServiceConfig> build(
			RemoteServiceControllerConfig config) throws CwtRpcException {
		if (getBeanFactory() == null) {
			throw new IllegalStateException("Bean factory must be specified");
		}

		return createRemoteServiceConfigsByUri(config,
				config.getServiceModuleConfigs());
	}

	protected Map<String, PreparedRemoteServiceConfig> createRemoteServiceConfigsByUri(
			BaseServiceConfig baseServiceConfig,
			Collection<RemoteServiceModuleConfig> moduleConfigs)
			throws CwtRpcException {
		final Map<String, PreparedRemoteServiceConfig> serviceConfigsByUri;

		serviceConfigsByUri = new HashMap<String, PreparedRemoteServiceConfig>();
		for (RemoteServiceModuleConfig moduleConfig : moduleConfigs) {
			merge(serviceConfigsByUri,
					createRemoteServiceConfigsByUri(baseServiceConfig
							.createMergedBaseServiceConfig(moduleConfig),
							moduleConfig, moduleConfig.getServiceGroupConfig()));
		}

		return serviceConfigsByUri;
	}

	protected Map<String, PreparedRemoteServiceConfig> createRemoteServiceConfigsByUri(
			BaseServiceConfig baseServiceConfig,
			RemoteServiceModuleConfig moduleConfig,
			RemoteServiceGroupConfig groupConfig) throws CwtRpcException {
		final BaseServiceConfig groupBaseServiceConfig;
		final Map<String, PreparedRemoteServiceConfig> serviceConfigsByUri;

		groupBaseServiceConfig = baseServiceConfig
				.createMergedBaseServiceConfig(groupConfig);
		serviceConfigsByUri = new HashMap<String, PreparedRemoteServiceConfig>();
		if (groupConfig.getServiceConfigs() != null) {
			for (RemoteServiceConfig serviceConfig : groupConfig
					.getServiceConfigs()) {
				merge(serviceConfigsByUri,
						createRemoteServiceConfigsByUri(groupBaseServiceConfig,
								moduleConfig, serviceConfig));
			}
		}
		if (groupConfig.getChildGroupConfigs() != null) {
			for (RemoteServiceGroupConfig childGroupConfig : groupConfig
					.getChildGroupConfigs()) {
				merge(serviceConfigsByUri,
						createRemoteServiceConfigsByUri(groupBaseServiceConfig,
								moduleConfig, childGroupConfig));
			}
		}

		return serviceConfigsByUri;
	}

	protected Map<String, PreparedRemoteServiceConfig> createRemoteServiceConfigsByUri(
			BaseServiceConfig baseServiceConfig,
			RemoteServiceModuleConfig moduleConfig,
			RemoteServiceConfig serviceConfig) throws CwtRpcException {
		final Map<String, PreparedRemoteServiceConfig> serviceConfigsByUri;
		final BaseServiceConfig resultingBaseServiceConfig;
		final String serviceName;
		final PreparedRemoteServiceConfig preparedServiceConfig;
		final String uri;
		final RpcTokenValidator rpcTokenValidator;
		Class<?> serviceInterface;

		resultingBaseServiceConfig = baseServiceConfig
				.createMergedBaseServiceConfig(serviceConfig);

		serviceName = serviceConfig.getServiceName();
		if (!getBeanFactory().containsBean(serviceName)) {
			throw new CwtRpcException(
					"Bean factory does not contain service with name '"
							+ serviceName + "'");
		}

		serviceInterface = serviceConfig.getServiceInterface();
		if (serviceInterface == null) {
			serviceInterface = getRemoteServiceInterface(serviceName);
		}
		if (!getBeanFactory().isTypeMatch(serviceName, serviceInterface)) {
			throw new CwtRpcException("Service '" + serviceName
					+ "' does not implement specified service " + "interface "
					+ serviceInterface.getName());
		}

		if (resultingBaseServiceConfig.getRpcTokenProtectionEnabled()
				.booleanValue()) {
			rpcTokenValidator = getBeanFactory().getBean(
					resultingBaseServiceConfig.getRpcTokenValidatorName(),
					RpcTokenValidator.class);
		} else {
			rpcTokenValidator = null;
		}

		uri = getRemoteServiceUri(moduleConfig.getName(), serviceConfig,
				serviceInterface);
		preparedServiceConfig = new PreparedRemoteServiceConfig(serviceName,
				serviceInterface, resultingBaseServiceConfig
						.getResponseCompressionEnabled().booleanValue(),
				rpcTokenValidator);

		if (log.isDebugEnabled()) {
			log.debug("Registering service " + preparedServiceConfig
					+ " for URI '" + uri + "'");
		}
		serviceConfigsByUri = new HashMap<String, PreparedRemoteServiceConfig>(
				1);
		serviceConfigsByUri.put(uri, preparedServiceConfig);

		return serviceConfigsByUri;
	}

	protected Class<?> getRemoteServiceInterface(String serviceName)
			throws CwtRpcException {
		final Class<?> serviceClass;
		Class<?> foundInterface = null;

		serviceClass = getBeanFactory().getType(serviceName);
		for (Class<?> c : serviceClass.getInterfaces()) {
			if (RemoteService.class.isAssignableFrom(c)) {
				if (foundInterface != null) {
					throw new CwtRpcException("Service class "
							+ serviceClass.getName() + " of service '"
							+ serviceName + "' implements multiple "
							+ "remote service interfaces (specify service "
							+ "interface explicitly)");
				}
				foundInterface = c;
			}
		}

		return foundInterface;
	}

	protected String getRemoteServiceUri(String moduleName,
			RemoteServiceConfig serviceConfig, Class<?> serviceInterface)
			throws CwtRpcException {
		final String relativePathValue;
		final StringBuilder uri;

		if (serviceConfig.getRelativePath() != null) {
			relativePathValue = serviceConfig.getRelativePath();
		} else {
			final RemoteServiceRelativePath relativePath;

			relativePath = serviceInterface
					.getAnnotation(RemoteServiceRelativePath.class);
			if (relativePath == null) {
				throw new CwtRpcException("Service interface "
						+ serviceInterface.getName() + " does not specify "
						+ "the remote service relative path annotation. "
						+ "Specify relative path explicitly!");
			}

			relativePathValue = relativePath.value();
			if (relativePathValue == null) {
				throw new CwtRpcException("Service interface "
						+ serviceInterface.getName() + " must specify "
						+ "a valid remote service relative path");
			}
			if (relativePathValue.startsWith("/")) {
				throw new CwtRpcException("Service interface "
						+ serviceInterface.getName() + " must not specify "
						+ "an absolute path as remote service relative path");
			}
		}

		uri = new StringBuilder();
		uri.append(moduleName);
		uri.append('/');
		uri.append(relativePathValue);

		return uri.toString();
	}

	protected void merge(Map<String, PreparedRemoteServiceConfig> destMap,
			Map<String, PreparedRemoteServiceConfig> srcMap)
			throws CwtRpcException {
		for (Map.Entry<String, PreparedRemoteServiceConfig> entry : srcMap
				.entrySet()) {
			PreparedRemoteServiceConfig duplicate;

			duplicate = destMap.put(entry.getKey(), entry.getValue());
			if (duplicate != null) {
				throw new CwtRpcException("Service '"
						+ entry.getValue().getServiceName()
						+ "' is either include twice or uses same URI ("
						+ entry.getKey() + ") as service '"
						+ duplicate.getServiceName() + "'");
			}
		}
	}
}
