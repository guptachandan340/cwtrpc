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
import java.util.Collections;

import org.springframework.util.Assert;

import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProvider;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceControllerConfig extends BaseServiceConfig {
	public static final String DEFAULT_BEAN_ID = "remoteServiceControllerConfig";

	public static final boolean DEFAULT_RESPONSE_COMPRESSION_ENABLED = true;

	public static final boolean DEFAULT_RPC_TOKEN_PROTECTION_ENABLED = false;

	public static final String DEFAULT_RPC_VALIDATOR_SERVICE_NAME = "rpcTokenService";

	private ExtendedSerializationPolicyProvider serializationPolicyProvider;

	private Collection<RemoteServiceModuleConfig> serviceModuleConfigs = Collections
			.emptyList();

	public RemoteServiceControllerConfig(
			ExtendedSerializationPolicyProvider serializationPolicyProvider) {
		setResponseCompressionEnabled(DEFAULT_RESPONSE_COMPRESSION_ENABLED);
		setRpcTokenProtectionEnabled(DEFAULT_RPC_TOKEN_PROTECTION_ENABLED);
		setRpcTokenValidatorName(DEFAULT_RPC_VALIDATOR_SERVICE_NAME);
		setSerializationPolicyProvider(serializationPolicyProvider);
	}

	@Override
	public void setResponseCompressionEnabled(Boolean responseCompressionEnabled) {
		Assert.notNull(responseCompressionEnabled,
				"'responseCompressionEnabled' must not be null");
		super.setResponseCompressionEnabled(responseCompressionEnabled);
	}

	@Override
	public void setRpcTokenProtectionEnabled(Boolean rpcTokenProtectionEnabled) {
		Assert.notNull(rpcTokenProtectionEnabled,
				"'rpcTokenProtectionEnabled' must not be null");
		super.setRpcTokenProtectionEnabled(rpcTokenProtectionEnabled);
	}

	@Override
	public void setRpcTokenValidatorName(String rpcTokenServiceName) {
		Assert.notNull(rpcTokenServiceName,
				"'rpcTokenValidatorName' must not be null");
		super.setRpcTokenValidatorName(rpcTokenServiceName);
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

	public Collection<RemoteServiceModuleConfig> getServiceModuleConfigs() {
		return serviceModuleConfigs;
	}

	public void setServiceModuleConfigs(
			Collection<RemoteServiceModuleConfig> serviceModuleConfigs) {
		Assert.notNull(serviceModuleConfigs,
				"'serviceModuleConfigs' must not be null");
		this.serviceModuleConfigs = serviceModuleConfigs;
	}
}
