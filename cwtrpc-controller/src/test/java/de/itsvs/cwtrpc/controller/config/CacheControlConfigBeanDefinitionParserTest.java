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

package de.itsvs.cwtrpc.controller.config;

import javax.servlet.ServletContext;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import de.itsvs.cwtrpc.controller.CacheControlConfig;
import de.itsvs.cwtrpc.controller.RequestMethod;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class CacheControlConfigBeanDefinitionParserTest {
	private static ApplicationContext appContext;

	@BeforeClass
	public static void initClass() {
		ServletContext servletContext;
		XmlWebApplicationContext appContext;

		servletContext = new MockServletContext();
		appContext = new XmlWebApplicationContext();
		appContext
				.setConfigLocation("classpath:/de/itsvs/cwtrpc/controller/config/"
						+ "CacheControlConfigBeanDefinitionParserTest1.xml");
		appContext.setServletContext(servletContext);
		appContext.refresh();

		CacheControlConfigBeanDefinitionParserTest.appContext = appContext;
	}

	@AfterClass
	public static void destroyClass() {
		CacheControlConfigBeanDefinitionParserTest.appContext = null;
	}

	@Test
	public void test1() {
		CacheControlConfig config;

		Assert.assertTrue(appContext.containsBean("cacheControlConfig"));
		Assert.assertEquals(CacheControlConfig.class,
				appContext.getType("cacheControlConfig"));

		config = appContext.getBean("cacheControlConfig",
				CacheControlConfig.class);
		Assert.assertEquals(2, config.getUriConfigs().size());

		Assert.assertEquals("/**/*.txt", config.getUriConfigs().get(0)
				.getValue().getPatternString());
		Assert.assertTrue(config.getUriConfigs().get(0).getValue()
				.matches("/my/value/test.txt"));
		Assert.assertEquals(Integer.valueOf(4712), config.getUriConfigs()
				.get(0).getMaxAge());

		Assert.assertEquals("/.*\\.gif", config.getUriConfigs().get(1)
				.getValue().getPatternString());
		Assert.assertTrue(config.getUriConfigs().get(1).getValue()
				.matches("/my/value/pic.gif"));
		Assert.assertEquals(Integer.valueOf(2837), config.getUriConfigs()
				.get(1).getSharedMaxage());
	}

	@Test
	public void test2() {
		CacheControlConfig config;

		Assert.assertTrue(appContext.containsBean("cacheControlConfigTest2"));
		Assert.assertEquals(CacheControlConfig.class,
				appContext.getType("cacheControlConfigTest2"));

		config = appContext.getBean("cacheControlConfigTest2",
				CacheControlConfig.class);
		Assert.assertEquals(true, config.isDefaultsEnabled());
		Assert.assertEquals(true, config.isLowerCaseMatch());
		Assert.assertEquals(4 * 7 * 24 * 60 * 60, config.getCacheMaxAge());
		Assert.assertEquals(1, config.getUriConfigs().size());

		Assert.assertEquals("/**/*.pdf", config.getUriConfigs().get(0)
				.getValue().getPatternString());
		Assert.assertFalse(config.getUriConfigs().get(0).isMustRevalidate());
		Assert.assertFalse(config.getUriConfigs().get(0).isNoCache());
		Assert.assertFalse(config.getUriConfigs().get(0).isNoStore());
		Assert.assertFalse(config.getUriConfigs().get(0).isNoTransform());
		Assert.assertFalse(config.getUriConfigs().get(0).isPragmaNoCache());
		Assert.assertFalse(config.getUriConfigs().get(0).isPrivateContent());
		Assert.assertFalse(config.getUriConfigs().get(0).isProxyRevalidate());
		Assert.assertFalse(config.getUriConfigs().get(0).isPublicContent());
		Assert.assertNull(config.getUriConfigs().get(0).getExpires());
		Assert.assertNull(config.getUriConfigs().get(0).getMaxAge());
		Assert.assertNull(config.getUriConfigs().get(0).getMethod());
		Assert.assertNull(config.getUriConfigs().get(0).getSharedMaxage());
	}

	@Test
	public void test3() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest3",
				CacheControlConfig.class);
		Assert.assertEquals(false, config.isDefaultsEnabled());
		Assert.assertEquals(true, config.isLowerCaseMatch());
		Assert.assertEquals(4 * 7 * 24 * 60 * 60, config.getCacheMaxAge());
	}

	@Test
	public void test4() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest4",
				CacheControlConfig.class);
		Assert.assertEquals(true, config.isDefaultsEnabled());
		Assert.assertEquals(false, config.isLowerCaseMatch());
		Assert.assertEquals(4 * 7 * 24 * 60 * 60, config.getCacheMaxAge());
	}

	@Test
	public void test5() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest5",
				CacheControlConfig.class);
		Assert.assertEquals(true, config.isDefaultsEnabled());
		Assert.assertEquals(true, config.isLowerCaseMatch());
		Assert.assertEquals(63458, config.getCacheMaxAge());
	}

	@Test
	public void test100() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest100",
				CacheControlConfig.class);
		Assert.assertEquals(3738, config.getCacheMaxAge());

		Assert.assertFalse(config.getUriConfigs().get(0).isMustRevalidate());
		Assert.assertFalse(config.getUriConfigs().get(1).isNoCache());
		Assert.assertFalse(config.getUriConfigs().get(2).isNoStore());
		Assert.assertFalse(config.getUriConfigs().get(3).isNoTransform());
		Assert.assertFalse(config.getUriConfigs().get(4).isPragmaNoCache());
		Assert.assertFalse(config.getUriConfigs().get(5).isPrivateContent());
		Assert.assertFalse(config.getUriConfigs().get(6).isProxyRevalidate());
		Assert.assertFalse(config.getUriConfigs().get(7).isPublicContent());
		Assert.assertEquals(Integer.valueOf(123), config.getUriConfigs().get(8)
				.getMaxAge());
		Assert.assertEquals(Integer.valueOf(456), config.getUriConfigs().get(9)
				.getSharedMaxage());
	}

	@Test
	public void test101() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest101",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isMustRevalidate());
	}

	@Test
	public void test102() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest102",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isNoCache());
		Assert.assertFalse(config.getUriConfigs().get(0).isPragmaNoCache());
	}

	@Test
	public void test103() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest103",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isNoStore());
	}

	@Test
	public void test104() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest104",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isNoTransform());
	}

	@Test
	public void test105() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest105",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isPragmaNoCache());
		Assert.assertFalse(config.getUriConfigs().get(0).isNoCache());
	}

	@Test
	public void test106() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest106",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isPrivateContent());
	}

	@Test
	public void test107() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest107",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isProxyRevalidate());
	}

	@Test
	public void test108() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest108",
				CacheControlConfig.class);

		Assert.assertTrue(config.getUriConfigs().get(0).isPublicContent());
	}

	@Test
	public void test109() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest109",
				CacheControlConfig.class);

		Assert.assertEquals(Integer.valueOf(76234),
				config.getUriConfigs().get(0).getExpires());
		Assert.assertNull(config.getUriConfigs().get(0).getMaxAge());
		Assert.assertNull(config.getUriConfigs().get(0).getSharedMaxage());
	}

	@Test
	public void test110() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest110",
				CacheControlConfig.class);

		Assert.assertEquals(Integer.valueOf(8374), config.getUriConfigs()
				.get(0).getMaxAge());
		Assert.assertNull(config.getUriConfigs().get(0).getSharedMaxage());
		Assert.assertNull(config.getUriConfigs().get(0).getExpires());
	}

	@Test
	public void test111() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest111",
				CacheControlConfig.class);

		Assert.assertEquals(Integer.valueOf(3853), config.getUriConfigs()
				.get(0).getSharedMaxage());
		Assert.assertNull(config.getUriConfigs().get(0).getMaxAge());
		Assert.assertNull(config.getUriConfigs().get(0).getExpires());
	}

	@Test
	public void test112() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest112",
				CacheControlConfig.class);

		Assert.assertEquals(RequestMethod.GET, config.getUriConfigs().get(0)
				.getMethod());
	}

	@Test
	public void test113() {
		CacheControlConfig config;

		config = appContext.getBean("cacheControlConfigTest113",
				CacheControlConfig.class);

		Assert.assertEquals(RequestMethod.POST, config.getUriConfigs().get(0)
				.getMethod());
	}
}
