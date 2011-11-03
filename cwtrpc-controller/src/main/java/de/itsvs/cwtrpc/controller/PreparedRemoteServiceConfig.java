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

import org.springframework.util.Assert;

import de.itsvs.cwtrpc.controller.token.RpcTokenValidator;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class PreparedRemoteServiceConfig {
	private final String serviceName;

	private final Class<?> serviceInterface;

	private final boolean responseCompressionEnabled;

	private final RpcTokenValidator rpcTokenValidator;

	public PreparedRemoteServiceConfig(String serviceName,
			Class<?> serviceInterface, boolean responseCompressionEnabled,
			RpcTokenValidator rpcTokenValidator) {
		Assert.hasText(serviceName, "'serviceName' must not be empty");
		Assert.notNull(serviceInterface, "'serviceInterface' must not be null");

		if (!serviceInterface.isInterface()) {
			throw new IllegalArgumentException(
					"'serviceInterface' must be an interface");
		}

		this.serviceName = serviceName;
		this.serviceInterface = serviceInterface;
		this.responseCompressionEnabled = responseCompressionEnabled;
		this.rpcTokenValidator = rpcTokenValidator;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Class<?> getServiceInterface() {
		return serviceInterface;
	}

	public boolean isResponseCompressionEnabled() {
		return responseCompressionEnabled;
	}

	public RpcTokenValidator getRpcTokenValidator() {
		return rpcTokenValidator;
	}

	@Override
	public String toString() {
		return "["
				+ PreparedRemoteServiceConfig.class.getSimpleName()
				+ ": serviceName='"
				+ serviceName
				+ "', serviceInterface="
				+ serviceInterface.getName()
				+ ", responseCompressionEnabled="
				+ responseCompressionEnabled
				+ ", rpcTokenValidator="
				+ ((rpcTokenValidator != null) ? rpcTokenValidator.getClass()
						.getName() : "") + "]";
	}
}