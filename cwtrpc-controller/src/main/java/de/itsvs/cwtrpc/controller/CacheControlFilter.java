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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.itsvs.cwtrpc.core.pattern.MatcherType;
import de.itsvs.cwtrpc.core.pattern.PatternFactory;
import de.itsvs.cwtrpc.core.pattern.PatternType;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class CacheControlFilter implements Filter {
	private final Log log = LogFactory.getLog(CacheControlFilter.class);

	protected static final String PROCESSED_ALREADY_ATTR_NAME = CacheControlFilter.class
			.getName().concat(".processedAlready");

	public static final String CONFIG_BEAN_NAME_INIT_PARAM = "configBeanName";

	public static final String DEFAULT_NOCACHE_URI_PATTERN = ".+\\.nocache\\.\\w+";

	public static final String DEFAULT_CACHE_URI_PATTERN = ".+\\.cache\\.\\w+";

	private boolean lowerCaseMatch;

	private List<PreparedCacheControlUriConfig> uriConfigs;

	public boolean isLowerCaseMatch() {
		return lowerCaseMatch;
	}

	protected void setLowerCaseMatch(boolean lowerCaseMatch) {
		this.lowerCaseMatch = lowerCaseMatch;
	}

	public List<PreparedCacheControlUriConfig> getUriConfigs() {
		return uriConfigs;
	}

	protected void setUriConfigs(List<PreparedCacheControlUriConfig> uriConfigs) {
		this.uriConfigs = uriConfigs;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		final WebApplicationContext applicationContext;
		final CacheControlConfig config;
		final List<CacheControlUriConfig> uriConfigs;
		String configBeanName;

		configBeanName = filterConfig
				.getInitParameter(CONFIG_BEAN_NAME_INIT_PARAM);
		if (configBeanName == null) {
			configBeanName = CacheControlConfig.DEFAULT_BEAN_ID;
		}

		applicationContext = WebApplicationContextUtils
				.getWebApplicationContext(filterConfig.getServletContext());
		if (log.isDebugEnabled()) {
			log.debug("Resolving cache control config with bean name '"
					+ configBeanName + "' from application context");
		}
		config = applicationContext.getBean(configBeanName,
				CacheControlConfig.class);

		setLowerCaseMatch(config.isLowerCaseMatch());

		uriConfigs = new ArrayList<CacheControlUriConfig>();
		if (config.isDefaultsEnabled()) {
			log.debug("Adding default URI configurations");
			uriConfigs.addAll(createDefaultUriConfigs(config));
		}
		uriConfigs.addAll(config.getUriConfigs());
		setUriConfigs(createCacheControlUriConfigBuilder().build(uriConfigs));
	}

	protected List<CacheControlUriConfig> createDefaultUriConfigs(
			CacheControlConfig config) {
		final List<CacheControlUriConfig> uriConfigs;
		CacheControlUriConfig uriConfig;

		uriConfigs = new ArrayList<CacheControlUriConfig>();

		uriConfig = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.REGEX, MatcherType.URI, DEFAULT_CACHE_URI_PATTERN));
		uriConfig.setMaxAge(config.getCacheMaxAge());
		uriConfigs.add(uriConfig);

		uriConfig = new CacheControlUriConfig(
				PatternFactory.compile(PatternType.REGEX, MatcherType.URI,
						DEFAULT_NOCACHE_URI_PATTERN));
		uriConfig.setNoCache(true);
		uriConfig.setExpires(-1);
		uriConfigs.add(uriConfig);

		return uriConfigs;
	}

	protected PreparedCacheControlUriConfigBuilder createCacheControlUriConfigBuilder() {
		return new PreparedCacheControlUriConfigBuilder();
	}

	public void destroy() {
		/* nothing to be done */
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		/* filter must not be applied on an internal forward or on error */
		if (request.getAttribute(PROCESSED_ALREADY_ATTR_NAME) == null) {
			final PreparedCacheControlUriConfig uriConfig;

			uriConfig = getPreparedUriConfig((HttpServletRequest) request);
			if (uriConfig != null) {
				addHeaders((HttpServletResponse) response, uriConfig);
			}

			request.setAttribute(PROCESSED_ALREADY_ATTR_NAME, Boolean.TRUE);
		}

		chain.doFilter(request, response);
	}

	protected PreparedCacheControlUriConfig getPreparedUriConfig(
			HttpServletRequest request) {
		final String contextPath;
		final StringBuilder sb;
		final String uri;

		contextPath = request.getContextPath();
		sb = new StringBuilder(request.getRequestURI());
		if ((contextPath.length() > 0) && (sb.indexOf(contextPath) == 0)) {
			sb.delete(0, contextPath.length());
		}
		if ((sb.length() == 0) || (sb.charAt(0) != '/')) {
			sb.insert(0, '/');
		}
		uri = sb.toString();

		if (log.isDebugEnabled()) {
			log.debug("Checked URI is '" + uri + "'");
		}
		for (PreparedCacheControlUriConfig config : getUriConfigs()) {
			if ((config.getMethod() == null)
					|| config.getMethod().name().equals(request.getMethod())) {
				if (config.getValue().matches(uri)) {
					if (log.isDebugEnabled()) {
						log.debug("Matching configuration for URI '" + uri
								+ "' and method '" + request.getMethod()
								+ "' is " + config);
					}
					return config;
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("No matching configuration for URI '" + uri
					+ "' and method '" + request.getMethod() + "'");
		}
		return null;
	}

	protected void addHeaders(HttpServletResponse response,
			PreparedCacheControlUriConfig uriConfig) {
		final Long expiresValue;

		expiresValue = getExpiresValue(uriConfig.getExpires());

		if (uriConfig.getCacheControl() != null) {
			response.setHeader("Cache-Control", uriConfig.getCacheControl());
		}
		if (expiresValue != null) {
			response.setDateHeader("Expires", expiresValue);
		}
		if (uriConfig.isPragmaNoCache()) {
			response.setHeader("Pragma", "no-cache");
		}
	}

	protected Long getExpiresValue(Integer deltaSeconds) {
		final int value;

		if (deltaSeconds == null) {
			return null;
		}

		value = deltaSeconds;
		if (value < 0) {
			return Long.valueOf(-1L);
		}

		return (System.currentTimeMillis() + (value * 1000L));
	}
}
