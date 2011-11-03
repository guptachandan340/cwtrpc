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

import de.itsvs.cwtrpc.core.pattern.Pattern;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class PreparedCacheControlUriConfig {
	private final Pattern value;

	private final RequestMethod method;

	private final String cacheControl;

	private final Integer expires;

	private final boolean pragmaNoCache;

	public PreparedCacheControlUriConfig(Pattern value, RequestMethod method,
			String cacheControl, Integer expires, boolean pragmaNoCache) {
		Assert.notNull(value, "'value' must not be null");
		if (cacheControl != null) {
			Assert.hasText(cacheControl,
					"'cacheControl' must not be an empty string");
		}

		this.value = value;
		this.method = method;
		this.cacheControl = cacheControl;
		this.expires = expires;
		this.pragmaNoCache = pragmaNoCache;
	}

	public Pattern getValue() {
		return value;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public String getCacheControl() {
		return cacheControl;
	}

	public Integer getExpires() {
		return expires;
	}

	public boolean isPragmaNoCache() {
		return pragmaNoCache;
	}
}
