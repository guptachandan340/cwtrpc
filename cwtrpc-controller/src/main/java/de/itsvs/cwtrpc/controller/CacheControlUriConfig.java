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
import de.itsvs.cwtrpc.core.pattern.PatternType;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class CacheControlUriConfig {
	public static final String DEFAULT_BEAN_ID = "cacheControlConfig";

	public static final PatternType DEFAULT_PATTERN_TYPE = PatternType.ANT;

	private Pattern value;

	private RequestMethod method;

	private boolean publicContent;

	private boolean privateContent;

	private boolean noCache;

	private boolean noStore;

	private boolean noTransform;

	private boolean mustRevalidate;

	private boolean proxyRevalidate;

	private Integer maxAge;

	private Integer sharedMaxage;

	private Integer expires;

	private boolean pragmaNoCache;

	public CacheControlUriConfig(Pattern value) {
		setValue(value);
	}

	public Pattern getValue() {
		return value;
	}

	public void setValue(Pattern value) {
		Assert.notNull(value, "'value' must not be null");
		this.value = value;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

	public boolean isPublicContent() {
		return publicContent;
	}

	public void setPublicContent(boolean publicContent) {
		this.publicContent = publicContent;
	}

	public boolean isPrivateContent() {
		return privateContent;
	}

	public void setPrivateContent(boolean privateContent) {
		this.privateContent = privateContent;
	}

	public boolean isNoCache() {
		return noCache;
	}

	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
	}

	public boolean isNoStore() {
		return noStore;
	}

	public void setNoStore(boolean noStore) {
		this.noStore = noStore;
	}

	public boolean isNoTransform() {
		return noTransform;
	}

	public void setNoTransform(boolean noTransform) {
		this.noTransform = noTransform;
	}

	public boolean isMustRevalidate() {
		return mustRevalidate;
	}

	public void setMustRevalidate(boolean mustRevalidate) {
		this.mustRevalidate = mustRevalidate;
	}

	public boolean isProxyRevalidate() {
		return proxyRevalidate;
	}

	public void setProxyRevalidate(boolean proxyRevalidate) {
		this.proxyRevalidate = proxyRevalidate;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Integer maxAge) {
		if ((maxAge != null) && (maxAge < 0)) {
			throw new IllegalArgumentException("'maxAge' must not be negative");
		}
		this.maxAge = maxAge;
	}

	public Integer getSharedMaxage() {
		return sharedMaxage;
	}

	public void setSharedMaxage(Integer sharedMaxage) {
		if ((sharedMaxage != null) && (sharedMaxage < 0)) {
			throw new IllegalArgumentException(
					"'sharedMaxage' must not be negative");
		}
		this.sharedMaxage = sharedMaxage;
	}

	public Integer getExpires() {
		return expires;
	}

	public void setExpires(Integer expires) {
		this.expires = expires;
	}

	public boolean isPragmaNoCache() {
		return pragmaNoCache;
	}

	public void setPragmaNoCache(boolean pragmaNoCache) {
		this.pragmaNoCache = pragmaNoCache;
	}

	@Override
	public String toString() {
		return "[" + CacheControlUriConfig.class.getSimpleName() + ": value="
				+ value + ", method=" + method + ", publicContent="
				+ publicContent + ", privateContent=" + privateContent
				+ ", noCache=" + noCache + ", noStore=" + noStore
				+ ", noTransform=" + noTransform + ", mustRevalidate="
				+ mustRevalidate + ", proxyRevalidate=" + proxyRevalidate
				+ ", maxAge=" + ((maxAge != null) ? maxAge : "")
				+ ", sharedMaxage="
				+ ((sharedMaxage != null) ? sharedMaxage : "") + ", expires="
				+ ((expires != null) ? expires : "") + ", pragmaNoCache="
				+ pragmaNoCache + "]";
	}
}
