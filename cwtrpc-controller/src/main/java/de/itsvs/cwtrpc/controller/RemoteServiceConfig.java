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
import org.springframework.util.StringUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceConfig extends BaseServiceConfig {
	private String serviceName;

	private Class<?> serviceInterface;

	private String relativePath;

	public RemoteServiceConfig(String serviceId) {
		this(serviceId, null);
	}

	public RemoteServiceConfig(String serviceName, Class<?> serviceInterface) {
		setServiceName(serviceName);
		setServiceInterface(serviceInterface);
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		Assert.hasText(serviceName, "'serviceName' must not be empty");
		this.serviceName = serviceName;
	}

	public Class<?> getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class<?> serviceInterface) {
		if ((serviceInterface != null) && !serviceInterface.isInterface()) {
			throw new IllegalArgumentException(serviceInterface.getName()
					+ " is no interface");
		}
		this.serviceInterface = serviceInterface;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		if ((relativePath != null) && !StringUtils.hasText(relativePath)) {
			throw new IllegalArgumentException(
					"'relativePath' must either be null or not be empty");
		}
		this.relativePath = relativePath;
	}
}
