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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.itsvs.cwtrpc.controller.CacheControlUriConfig;
import de.itsvs.cwtrpc.controller.PreparedCacheControlUriConfig;
import de.itsvs.cwtrpc.controller.PreparedCacheControlUriConfigBuilder;
import de.itsvs.cwtrpc.controller.RequestMethod;
import de.itsvs.cwtrpc.core.pattern.MatcherType;
import de.itsvs.cwtrpc.core.pattern.PatternFactory;
import de.itsvs.cwtrpc.core.pattern.PatternType;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class PreparedCacheControlUriConfigBuilderTest {
	@Test
	public void testAddCacheControlDirective1() {
		PreparedCacheControlUriConfigBuilder builder;
		StringBuilder sb;

		builder = new PreparedCacheControlUriConfigBuilder();
		sb = new StringBuilder();

		builder.addCacheControlDirective(sb, "no-cache");
		Assert.assertEquals("no-cache", sb.toString());

		builder.addCacheControlDirective(sb, "private");
		Assert.assertEquals("no-cache, private", sb.toString());

		builder.addCacheControlDirective(sb, "max-age=10");
		Assert.assertEquals("no-cache, private, max-age=10", sb.toString());
	}

	@Test
	public void testGetPragmaNoCache1() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertFalse(builder.getPragmaNoCache(config));
	}

	@Test
	public void testGetPragmaNoCache2() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setPragmaNoCache(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertTrue(builder.getPragmaNoCache(config));
	}

	@Test
	public void testGetPragmaNoCache3() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setNoCache(true);
		config.setPragmaNoCache(false);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertTrue(builder.getPragmaNoCache(config));
	}

	@Test
	public void testGetExpires1() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertNull(builder.getExpires(config));
	}

	@Test
	public void testGetExpires2() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setExpires(27344);
		config.setMaxAge(875639);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals(Integer.valueOf(27344), builder.getExpires(config));
	}

	@Test
	public void testGetExpires3() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setMaxAge(57344);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals(Integer.valueOf(57344), builder.getExpires(config));
	}

	@Test
	public void testBuildCacheControlValue1() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setExpires(23213);
		config.setPragmaNoCache(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertNull(builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue2() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setPublicContent(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("public", builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue3() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setPrivateContent(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("private", builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue4() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setNoCache(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("no-cache", builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue5() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setNoStore(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("no-store", builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue6() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setNoTransform(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("no-transform",
				builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue7() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setMustRevalidate(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("must-revalidate",
				builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue8() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setProxyRevalidate(true);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("proxy-revalidate",
				builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue9() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setMaxAge(545635);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("max-age=545635",
				builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue10() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setSharedMaxage(845635);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("s-maxage=845635",
				builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuildCacheControlValue11() {
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;

		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**"));
		config.setNoCache(true);
		config.setMaxAge(345635);

		builder = new PreparedCacheControlUriConfigBuilder();
		Assert.assertEquals("no-cache, max-age=345635",
				builder.buildCacheControlValue(config));
	}

	@Test
	public void testBuild1() {
		List<CacheControlUriConfig> configs;
		CacheControlUriConfig config;
		PreparedCacheControlUriConfigBuilder builder;
		List<PreparedCacheControlUriConfig> result;

		configs = new ArrayList<CacheControlUriConfig>();
		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**1"));
		config.setNoCache(true);
		config.setMaxAge(345635);
		configs.add(config);
		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**2"));
		config.setMethod(RequestMethod.POST);
		config.setPrivateContent(true);
		config.setExpires(3632432);
		configs.add(config);
		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**3"));
		config.setPublicContent(true);
		config.setPragmaNoCache(true);
		configs.add(config);
		config = new CacheControlUriConfig(PatternFactory.compile(
				PatternType.ANT, MatcherType.URI, "/**4"));
		config.setExpires(853753);
		configs.add(config);

		builder = new PreparedCacheControlUriConfigBuilder();
		result = builder.build(configs);
		Assert.assertEquals(4, result.size());

		Assert.assertEquals("/**1", result.get(0).getValue().getPatternString());
		Assert.assertNull(result.get(0).getMethod());
		Assert.assertEquals("no-cache, max-age=345635", result.get(0)
				.getCacheControl());
		Assert.assertEquals(Integer.valueOf(345635), result.get(0).getExpires());
		Assert.assertTrue(result.get(0).isPragmaNoCache());

		Assert.assertEquals("/**2", result.get(1).getValue().getPatternString());
		Assert.assertEquals(RequestMethod.POST, result.get(1).getMethod());
		Assert.assertEquals("private", result.get(1).getCacheControl());
		Assert.assertEquals(Integer.valueOf(3632432), result.get(1)
				.getExpires());
		Assert.assertFalse(result.get(1).isPragmaNoCache());

		Assert.assertEquals("/**3", result.get(2).getValue().getPatternString());
		Assert.assertNull(result.get(2).getMethod());
		Assert.assertEquals("public", result.get(2).getCacheControl());
		Assert.assertNull(result.get(2).getExpires());
		Assert.assertTrue(result.get(2).isPragmaNoCache());

		Assert.assertEquals("/**4", result.get(3).getValue().getPatternString());
		Assert.assertNull(result.get(3).getMethod());
		Assert.assertNull(result.get(3).getCacheControl());
		Assert.assertEquals(Integer.valueOf(853753), result.get(3).getExpires());
		Assert.assertFalse(result.get(3).isPragmaNoCache());
	}
}
