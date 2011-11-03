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

import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class CacheControlConfig {
	public static final String DEFAULT_BEAN_ID = "cacheControlConfig";

	public static final boolean DEFAULT_LOWER_CASE_MATCH = true;

	public static final boolean DEFAULT_DEFAULTS_ENABLED = true;

	public static final int DEFAULT_CACHE_MAX_AGE = 4 * 7 * 24 * 60 * 60;

	private boolean lowerCaseMatch = DEFAULT_LOWER_CASE_MATCH;

	private boolean defaultsEnabled = DEFAULT_DEFAULTS_ENABLED;

	private int cacheMaxAge = DEFAULT_CACHE_MAX_AGE;

	private List<CacheControlUriConfig> uriConfigs = Collections.emptyList();

	public boolean isLowerCaseMatch() {
		return lowerCaseMatch;
	}

	public void setLowerCaseMatch(boolean lowerCaseMatch) {
		this.lowerCaseMatch = lowerCaseMatch;
	}

	public boolean isDefaultsEnabled() {
		return defaultsEnabled;
	}

	public void setDefaultsEnabled(boolean defaultsEnabled) {
		this.defaultsEnabled = defaultsEnabled;
	}

	public int getCacheMaxAge() {
		return cacheMaxAge;
	}

	public void setCacheMaxAge(int cacheMaxAge) {
		if (cacheMaxAge < 0) {
			throw new IllegalArgumentException(
					"'cacheMaxAge' must not be negative");
		}
		this.cacheMaxAge = cacheMaxAge;
	}

	public List<CacheControlUriConfig> getUriConfigs() {
		return uriConfigs;
	}

	public void setUriConfigs(List<CacheControlUriConfig> uriConfigs) {
		Assert.notNull(uriConfigs, "'uriConfigs' must not be null");
		this.uriConfigs = uriConfigs;
	}
}
