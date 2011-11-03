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

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class BaseServiceConfig {
	private Boolean responseCompressionEnabled;

	private Boolean rpcTokenProtectionEnabled;

	private String rpcTokenValidatorName;

	public BaseServiceConfig() {
		super();
	}

	public BaseServiceConfig(BaseServiceConfig config) {
		setResponseCompressionEnabled(config.getResponseCompressionEnabled());
		setRpcTokenProtectionEnabled(config.getRpcTokenProtectionEnabled());
		setRpcTokenValidatorName(config.getRpcTokenValidatorName());
	}

	public Boolean getResponseCompressionEnabled() {
		return responseCompressionEnabled;
	}

	public void setResponseCompressionEnabled(Boolean responseCompressionEnabled) {
		this.responseCompressionEnabled = responseCompressionEnabled;
	}

	public Boolean getRpcTokenProtectionEnabled() {
		return rpcTokenProtectionEnabled;
	}

	public void setRpcTokenProtectionEnabled(Boolean rpcTokenProtectionEnabled) {
		this.rpcTokenProtectionEnabled = rpcTokenProtectionEnabled;
	}

	public String getRpcTokenValidatorName() {
		return rpcTokenValidatorName;
	}

	public void setRpcTokenValidatorName(String rpcTokenValidatorName) {
		this.rpcTokenValidatorName = rpcTokenValidatorName;
	}

	public BaseServiceConfig createMergedBaseServiceConfig(
			BaseServiceConfig config) {
		final BaseServiceConfig mergedConfig = new BaseServiceConfig(this);

		if (config.getResponseCompressionEnabled() != null) {
			mergedConfig.setResponseCompressionEnabled(config
					.getResponseCompressionEnabled());
		}
		if (config.getRpcTokenProtectionEnabled() != null) {
			mergedConfig.setRpcTokenProtectionEnabled(config
					.getRpcTokenProtectionEnabled());
		}
		if (config.getRpcTokenValidatorName() != null) {
			mergedConfig.setRpcTokenValidatorName(config
					.getRpcTokenValidatorName());
		}

		return mergedConfig;
	}
}
