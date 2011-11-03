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

import junit.framework.Assert;

import org.junit.Test;

import de.itsvs.cwtrpc.core.pattern.DefaultPattern;
import de.itsvs.cwtrpc.core.pattern.Pattern;
import de.itsvs.cwtrpc.core.pattern.RegexPatternMatcher;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultPatternTest {
	@Test
	public void testGetPatternString1() {
		RegexPatternMatcher matcher;
		Pattern pattern;

		matcher = new RegexPatternMatcher();
		pattern = DefaultPattern.createDefaultPattern(matcher, "abc.+def");
		Assert.assertEquals("abc.+def", pattern.getPatternString());
	}

	@Test
	public void testMatches1() {
		RegexPatternMatcher matcher;
		Pattern pattern;

		matcher = new RegexPatternMatcher();
		pattern = DefaultPattern.createDefaultPattern(matcher, "abc.+def");
		Assert.assertTrue(pattern.matches("abcxyzdef"));
		Assert.assertFalse(pattern.matches("abcdef"));
		Assert.assertFalse(pattern.matches(null));
	}

	@Test
	public void testToString() {
		RegexPatternMatcher matcher;
		Pattern pattern;

		matcher = new RegexPatternMatcher();
		pattern = DefaultPattern.createDefaultPattern(matcher, "abc.+def");
		Assert.assertTrue(pattern.toString().contains("abc.+def"));
	}
}
