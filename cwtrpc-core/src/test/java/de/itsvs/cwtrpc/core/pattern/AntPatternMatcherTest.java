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

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class AntPatternMatcherTest {
	@Test
	public void testGetPathSeparator1() {
		AntPatternMatcher matcher;

		matcher = new AntPatternMatcher(".");
		Assert.assertEquals(".", matcher.getPathSeparator());
	}

	@Test
	public void testCompile1() {
		AntPatternMatcher matcher;

		matcher = new AntPatternMatcher(".");
		Assert.assertEquals("/**/abc", matcher.compile("/**/abc"));
		Assert.assertEquals("**.abc", matcher.compile("**.abc"));
	}

	@Test
	public void testMatch1() {
		AntPatternMatcher matcher;

		matcher = new AntPatternMatcher("/");
		Assert.assertTrue(matcher.matches("/abc/**/def*ghi.jkl",
				"/abc/sfds/afk/dsf/defxyzghi.jkl"));
		Assert.assertTrue(matcher.matches("/abc/**/def*ghi.jkl",
				"/abc/sfds/afk/dsf/defxy.zghi.jkl"));
		Assert.assertFalse(matcher.matches("/abc/**/def*ghi.jkl",
				"/abc/sfds/afk/dsf/defxyzghi.jk"));
		Assert.assertFalse(matcher.matches("/abc/**/def*ghi.jkl",
				"abc/sfds/afk/dsf/defxyzghi.jkl"));
		Assert.assertFalse(matcher.matches("/abc/**/def*ghi.jkl",
				"/Abc/sfds/afk/dsf/defxyzghi.jkl"));
		Assert.assertFalse(matcher.matches("/abc/**/def*ghi.jkl",
				"/abc/sfds/afk/dsf/defxyzghm.jkl"));
		Assert.assertFalse(matcher.matches("/abc/**/def*ghi.jkl", null));
		Assert.assertFalse(matcher.matches("abc.**.def*ghi",
				"abc.sfds.af/k.dsf.defx/yzghi"));
	}

	@Test
	public void testMatch2() {
		AntPatternMatcher matcher;

		matcher = new AntPatternMatcher(".");
		Assert.assertTrue(matcher.matches("abc.**.def*ghi",
				"abc.sfds.afk.dsf.defxyzghi"));
		Assert.assertTrue(matcher.matches("abc.**.def*ghi",
				"abc.sfds.af/k.dsf.defx/yzghi"));
		Assert.assertFalse(matcher.matches("abc.**.def*ghi",
				"abc.sfds.afk.dsf.defxyzgh"));
		Assert.assertFalse(matcher.matches("abc.**.def*ghi",
				"bc.sfds.afk.dsf.defxyzghi"));
		Assert.assertFalse(matcher.matches("abc.**.def*ghi",
				"Abc.sfds.afk.dsf.defxyzghi"));
		Assert.assertFalse(matcher.matches("abc.**.def*ghi",
				"abc.sfds.af/k.dsf.defxyzghm"));
		Assert.assertFalse(matcher.matches("abc.**.def*ghi", null));
		Assert.assertFalse(matcher.matches("/abc/**/def*ghi.jkl",
				"/abc/sfds/afk/dsf/defxy.zghi.jkl"));
	}
}
