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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class PreparedCacheControlUriConfigBuilder {
	private final Log log = LogFactory
			.getLog(PreparedCacheControlUriConfigBuilder.class);

	public List<PreparedCacheControlUriConfig> build(
			List<CacheControlUriConfig> uriConfigs) {
		final List<PreparedCacheControlUriConfig> preparedUriConfigs;

		if (uriConfigs.isEmpty()) {
			return Collections.emptyList();
		}

		preparedUriConfigs = new ArrayList<PreparedCacheControlUriConfig>();
		for (CacheControlUriConfig uriConfig : uriConfigs) {
			preparedUriConfigs.add(build(uriConfig));
		}

		return preparedUriConfigs;
	}

	protected PreparedCacheControlUriConfig build(
			CacheControlUriConfig uriConfig) {
		final PreparedCacheControlUriConfig preparedUriConfig;

		preparedUriConfig = new PreparedCacheControlUriConfig(
				uriConfig.getValue(), uriConfig.getMethod(),
				buildCacheControlValue(uriConfig), getExpires(uriConfig),
				getPragmaNoCache(uriConfig));
		if (log.isDebugEnabled()) {
			log.debug("Prepared URI configuration for " + uriConfig + " is "
					+ preparedUriConfig);
		}

		return preparedUriConfig;
	}

	protected String buildCacheControlValue(CacheControlUriConfig uriConfig) {
		final StringBuilder sb = new StringBuilder();

		if (uriConfig.isPublicContent()) {
			addCacheControlDirective(sb, "public");
		}
		if (uriConfig.isPrivateContent()) {
			addCacheControlDirective(sb, "private");
		}
		if (uriConfig.isNoCache()) {
			addCacheControlDirective(sb, "no-cache");
		}
		if (uriConfig.isNoStore()) {
			addCacheControlDirective(sb, "no-store");
		}
		if (uriConfig.isNoTransform()) {
			addCacheControlDirective(sb, "no-transform");
		}
		if (uriConfig.isMustRevalidate()) {
			addCacheControlDirective(sb, "must-revalidate");
		}
		if (uriConfig.isProxyRevalidate()) {
			addCacheControlDirective(sb, "proxy-revalidate");
		}
		if (uriConfig.getMaxAge() != null) {
			if (uriConfig.getMaxAge() < 0) {
				throw new IllegalArgumentException(
						"'maxAge'must not be negative");
			}
			addCacheControlDirective(sb, "max-age=" + uriConfig.getMaxAge());
		}
		if (uriConfig.getSharedMaxage() != null) {
			if (uriConfig.getSharedMaxage() < 0) {
				throw new IllegalArgumentException(
						"'s-maxage'must not be negative");
			}
			addCacheControlDirective(sb,
					"s-maxage=" + uriConfig.getSharedMaxage());
		}

		return ((sb.length() > 0) ? sb.toString() : null);
	}

	protected Integer getExpires(CacheControlUriConfig uriConfig) {
		if (uriConfig.getExpires() != null) {
			return uriConfig.getExpires();
		}

		/* use HTTP 1.1 max-age instead */
		return uriConfig.getMaxAge();
	}

	protected boolean getPragmaNoCache(CacheControlUriConfig uriConfig) {
		if (uriConfig.isPragmaNoCache()) {
			return true;
		}

		/* use HTTP 1.1 no-cache instead */
		return uriConfig.isNoCache();
	}

	protected StringBuilder addCacheControlDirective(StringBuilder sb,
			String directive) {
		if (sb.length() > 0) {
			sb.append(", ");
		}
		sb.append(directive);

		return sb;
	}
}
