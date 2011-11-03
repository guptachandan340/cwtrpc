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

import de.itsvs.cwtrpc.core.pattern.RegexPatternMatcher;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RegexPatternMatcherTest {
	@Test
	public void testCompile1() {
		RegexPatternMatcher matcher;

		matcher = new RegexPatternMatcher();
		Assert.assertEquals("/.+/abc\\.def", matcher.compile("/.+/abc\\.def")
				.pattern());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCompile2() {
		RegexPatternMatcher matcher;

		matcher = new RegexPatternMatcher();
		matcher.compile("/.+(/abc\\.def");
	}

	@Test
	public void testMatch1() {
		RegexPatternMatcher matcher;

		matcher = new RegexPatternMatcher();
		Assert.assertTrue(matcher.matches(
				matcher.compile("/abc/.+/def.*ghi\\.jkl"),
				"/abc/sfds/afk/dsf/defxyzghi.jkl"));
		Assert.assertTrue(matcher.matches(
				matcher.compile("/abc/.+/def.*ghi\\.jkl"),
				"/abc/sfds/afk/dsf/defghi.jkl"));
		Assert.assertFalse(matcher.matches(
				matcher.compile("/abc/.+/def.*ghi\\.jkl"),
				"/abc/sfds/afk/dsf/defghi.jk"));
		Assert.assertFalse(matcher.matches(
				matcher.compile("/abc/.+/def.*ghi\\.jkl"),
				"abc/sfds/afk/dsf/defxyzghi.jkl"));
	}
}
