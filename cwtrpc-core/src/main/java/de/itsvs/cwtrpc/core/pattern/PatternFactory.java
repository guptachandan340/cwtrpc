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
public abstract class PatternFactory {
	private PatternFactory() {
		/* nothing to be done */
	}

	private static final AntPatternMatcher uriAntPatternMatcher = new AntPatternMatcher(
			AntPatternMatcher.URI_PATH_SEPARATOR);

	private static final AntPatternMatcher packageAntPatternMatcher = new AntPatternMatcher(
			AntPatternMatcher.PACKAGE_PATH_SEPARATOR);

	private static final RegexPatternMatcher regexAntPatternMatcher = new RegexPatternMatcher();

	public static Pattern compile(String patternType, MatcherType matcherType,
			String pattern) throws IllegalArgumentException {
		final PatternType convertedPatternType;

		Assert.notNull(patternType, "'patternType' must not be null");
		try {
			convertedPatternType = PatternType.valueOf(patternType
					.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Pattern type '" + patternType
					+ "' is unsupported");
		}

		return compile(convertedPatternType, matcherType, pattern);
	}

	public static Pattern compile(PatternType patternType,
			MatcherType matcherType, String pattern)
			throws IllegalArgumentException {
		final PatternMatcher<?> matcher;

		Assert.notNull(patternType, "'patternType' must not be null");
		Assert.notNull(matcherType, "'matcherType' must not be null");

		if (patternType == PatternType.REGEX) {
			matcher = regexAntPatternMatcher;
		} else if (patternType == PatternType.ANT) {
			switch (matcherType) {
			case URI:
				matcher = uriAntPatternMatcher;
				break;
			case PACKAGE:
				matcher = packageAntPatternMatcher;
				break;
			default:
				throw new IllegalStateException("Unhandled matcher type: "
						+ matcherType);
			}
		} else {
			throw new IllegalStateException("Unhandled pattern type: "
					+ patternType);
		}

		return DefaultPattern.createDefaultPattern(matcher, pattern);
	}
}
