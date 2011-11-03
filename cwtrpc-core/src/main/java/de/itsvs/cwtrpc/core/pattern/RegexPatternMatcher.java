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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RegexPatternMatcher implements PatternMatcher<Pattern> {
	public Pattern compile(String pattern) {
		try {
			return Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			throw new IllegalArgumentException("Pattern '" + pattern
					+ "' is invalid", e);
		}
	}

	public boolean matches(Pattern compiledPattern, String value) {
		if (value == null) {
			return false;
		}

		return compiledPattern.matcher(value).matches();
	}
}
