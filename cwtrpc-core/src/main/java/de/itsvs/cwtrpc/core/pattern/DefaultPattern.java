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

import org.springframework.util.Assert;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultPattern<T> implements Pattern {
	public static <T> DefaultPattern<T> createDefaultPattern(
			PatternMatcher<T> matcher, String pattern) {
		return new DefaultPattern<T>(matcher, pattern);
	}

	private final PatternMatcher<T> matcher;

	private final String patternString;

	private final T compiledPattern;

	public DefaultPattern(PatternMatcher<T> matcher, String pattern) {
		Assert.notNull(matcher, "'matcher' must not be null");
		Assert.notNull(pattern, "'pattern' must not be null");

		this.matcher = matcher;
		this.patternString = pattern;
		this.compiledPattern = matcher.compile(pattern);
	}

	PatternMatcher<T> getMatcher() {
		return matcher;
	}

	T getCompiledPattern() {
		return compiledPattern;
	}

	public String getPatternString() {
		return patternString;
	}

	public boolean matches(String value) {
		return getMatcher().matches(getCompiledPattern(), value);
	}

	@Override
	public String toString() {
		return "[" + DefaultPattern.class.getSimpleName() + ": patternString='"
				+ patternString + "']";
	}
}
