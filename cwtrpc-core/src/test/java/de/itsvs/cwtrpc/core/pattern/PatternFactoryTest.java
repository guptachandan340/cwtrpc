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

import de.itsvs.cwtrpc.core.pattern.AntPatternMatcher;
import de.itsvs.cwtrpc.core.pattern.DefaultPattern;
import de.itsvs.cwtrpc.core.pattern.MatcherType;
import de.itsvs.cwtrpc.core.pattern.PatternFactory;
import de.itsvs.cwtrpc.core.pattern.PatternType;
import de.itsvs.cwtrpc.core.pattern.RegexPatternMatcher;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class PatternFactoryTest {
	@Test
	public void testCompile1() {
		DefaultPattern<?> pattern;

		pattern = (DefaultPattern<?>) PatternFactory.compile(PatternType.ANT,
				MatcherType.URI, "/abc/**/def*ghi.jkl");
		Assert.assertEquals("/abc/**/def*ghi.jkl", pattern.getPatternString());
		Assert.assertEquals(AntPatternMatcher.class, pattern.getMatcher()
				.getClass());
		Assert.assertEquals("/",
				((AntPatternMatcher) pattern.getMatcher()).getPathSeparator());
	}

	@Test
	public void testCompile2() {
		DefaultPattern<?> pattern;

		pattern = (DefaultPattern<?>) PatternFactory.compile(PatternType.ANT,
				MatcherType.PACKAGE, "abc.**.def*ghi");
		Assert.assertEquals("abc.**.def*ghi", pattern.getPatternString());
		Assert.assertEquals(AntPatternMatcher.class, pattern.getMatcher()
				.getClass());
		Assert.assertEquals(".",
				((AntPatternMatcher) pattern.getMatcher()).getPathSeparator());
	}

	@Test
	public void testCompile3() {
		DefaultPattern<?> pattern;

		pattern = (DefaultPattern<?>) PatternFactory.compile(PatternType.REGEX,
				MatcherType.URI, "/abc/.+/def.*ghi.jkl");
		Assert.assertEquals("/abc/.+/def.*ghi.jkl", pattern.getPatternString());
		Assert.assertEquals(RegexPatternMatcher.class, pattern.getMatcher()
				.getClass());
	}

	@Test
	public void testCompile4() {
		DefaultPattern<?> pattern;

		pattern = (DefaultPattern<?>) PatternFactory.compile(PatternType.REGEX,
				MatcherType.PACKAGE, "/abc/.+/def.*ghi\\.jkl");
		Assert.assertEquals("/abc/.+/def.*ghi\\.jkl",
				pattern.getPatternString());
		Assert.assertEquals(RegexPatternMatcher.class, pattern.getMatcher()
				.getClass());
	}

	@Test
	public void testCompile5() {
		DefaultPattern<?> pattern;

		pattern = (DefaultPattern<?>) PatternFactory.compile("AnT",
				MatcherType.URI, "/abc/**/def*ghi.jkl");
		Assert.assertEquals("/abc/**/def*ghi.jkl", pattern.getPatternString());
		Assert.assertEquals(AntPatternMatcher.class, pattern.getMatcher()
				.getClass());
		Assert.assertEquals("/",
				((AntPatternMatcher) pattern.getMatcher()).getPathSeparator());
	}

	@Test
	public void testCompile6() {
		DefaultPattern<?> pattern;

		pattern = (DefaultPattern<?>) PatternFactory.compile("rEgEx",
				MatcherType.PACKAGE, "/abc/.+/def.*ghi\\.jkl");
		Assert.assertEquals("/abc/.+/def.*ghi\\.jkl",
				pattern.getPatternString());
		Assert.assertEquals(RegexPatternMatcher.class, pattern.getMatcher()
				.getClass());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCompile7() {
		PatternFactory.compile("rEEx", MatcherType.PACKAGE,
				"/abc/.+/def.*ghi\\.jkl");
	}
}
