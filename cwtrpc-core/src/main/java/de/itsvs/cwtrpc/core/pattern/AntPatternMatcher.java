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

package de.itsvs.cwtrpc.core.pattern;

import org.springframework.util.AntPathMatcher;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class AntPatternMatcher implements PatternMatcher<String> {
	public static final String URI_PATH_SEPARATOR = "/";

	public static final String PACKAGE_PATH_SEPARATOR = ".";

	private final String pathSeparator;

	private final AntPathMatcher pathMatcher;

	public AntPatternMatcher(String pathSeparator) {
		this.pathSeparator = pathSeparator;
		this.pathMatcher = new AntPathMatcher();
		this.pathMatcher.setPathSeparator(pathSeparator);
	}

	AntPathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public String getPathSeparator() {
		return pathSeparator;
	}

	public String compile(String pattern) {
		return pattern;
	}

	public boolean matches(String compiledPattern, String value) {
		if (value == null) {
			return false;
		}

		return getPathMatcher().match(compiledPattern, value);
	}
}
