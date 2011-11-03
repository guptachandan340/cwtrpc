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

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceGroupConfig extends BaseServiceConfig {
	private Boolean responseCompressionEnabled;

	private Collection<RemoteServiceConfig> serviceConfigs = Collections
			.emptyList();

	private Collection<RemoteServiceGroupConfig> childGroupConfigs = Collections
			.emptyList();

	public RemoteServiceGroupConfig() {
		super();
	}

	public RemoteServiceGroupConfig(
			Collection<RemoteServiceConfig> serviceConfigs,
			Collection<RemoteServiceGroupConfig> childGroupConfigs) {
		setServiceConfigs(serviceConfigs);
		setChildGroupConfigs(childGroupConfigs);
	}

	@Override
	public Boolean getResponseCompressionEnabled() {
		return responseCompressionEnabled;
	}

	@Override
	public void setResponseCompressionEnabled(Boolean responseCompressionEnabled) {
		this.responseCompressionEnabled = responseCompressionEnabled;
	}

	public Collection<RemoteServiceGroupConfig> getChildGroupConfigs() {
		return childGroupConfigs;
	}

	public void setChildGroupConfigs(
			Collection<RemoteServiceGroupConfig> childGroupConfigs) {
		Assert.notNull(childGroupConfigs,
				"'childGroupConfigs' must not be null");
		this.childGroupConfigs = childGroupConfigs;
	}

	public Collection<RemoteServiceConfig> getServiceConfigs() {
		return serviceConfigs;
	}

	public void setServiceConfigs(Collection<RemoteServiceConfig> serviceConfigs) {
		Assert.notNull(serviceConfigs, "'serviceConfigs' must not be null");
		this.serviceConfigs = serviceConfigs;
	}
}
