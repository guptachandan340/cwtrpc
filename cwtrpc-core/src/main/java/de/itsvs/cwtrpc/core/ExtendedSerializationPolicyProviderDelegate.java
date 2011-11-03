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

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class ExtendedSerializationPolicyProviderDelegate implements
		SerializationPolicyProvider {
	private final ExtendedSerializationPolicyProvider provider;

	private final HttpServletRequest request;

	public ExtendedSerializationPolicyProviderDelegate(
			ExtendedSerializationPolicyProvider provider,
			HttpServletRequest request) {
		this.provider = provider;
		this.request = request;
	}

	public ExtendedSerializationPolicyProvider getProvider() {
		return provider;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public SerializationPolicy getSerializationPolicy(String moduleBaseURL,
			String strongName) {
		return getProvider().getSerializationPolicy(getRequest(),
				moduleBaseURL, strongName);
	}
}
