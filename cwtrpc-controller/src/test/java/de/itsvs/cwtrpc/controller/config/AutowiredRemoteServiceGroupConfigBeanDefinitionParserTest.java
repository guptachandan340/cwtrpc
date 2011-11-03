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

import java.util.Iterator;

import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import de.itsvs.cwtrpc.controller.AutowiredRemoteServiceGroupConfig;
import de.itsvs.cwtrpc.core.pattern.Pattern;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class AutowiredRemoteServiceGroupConfigBeanDefinitionParserTest {
	private static ApplicationContext appContext;

	@BeforeClass
	public static void initClass() {
		ServletContext servletContext;
		XmlWebApplicationContext appContext;

		servletContext = new MockServletContext();
		appContext = new XmlWebApplicationContext();
		appContext
				.setConfigLocation("classpath:/de/itsvs/cwtrpc/controller/config/"
						+ "AutowiredRemoteServiceGroupConfigBeanDefinitionParserTest1.xml");
		appContext.setServletContext(servletContext);
		appContext.refresh();

		AutowiredRemoteServiceGroupConfigBeanDefinitionParserTest.appContext = appContext;
	}

	@AfterClass
	public static void destroyClass() {
		AutowiredRemoteServiceGroupConfigBeanDefinitionParserTest.appContext = null;
	}

	@Test
	public void test1() {
		AutowiredRemoteServiceGroupConfig config;

		config = appContext.getBean("autowiredServiceGroup101",
				AutowiredRemoteServiceGroupConfig.class);
		Assert.assertEquals(1, config.getBasePackages().size());
		Assert.assertEquals("de.itsvs.test.", config.getBasePackages()
				.iterator().next());
		Assert.assertNull(config.getResponseCompressionEnabled());
		Assert.assertNull(config.getRpcTokenProtectionEnabled());
		Assert.assertNull(config.getRpcTokenValidatorName());
		Assert.assertEquals(0, config.getServiceConfigs().size());
		Assert.assertEquals(0, config.getChildGroupConfigs().size());
		Assert.assertEquals(0, config.getIncludeFilters().size());
		Assert.assertEquals(0, config.getExcludeFilters().size());
	}

	@Test
	public void test2() {
		AutowiredRemoteServiceGroupConfig config;
		Iterator<String> packageNameIter;

		config = appContext.getBean("autowiredServiceGroup102",
				AutowiredRemoteServiceGroupConfig.class);
		Assert.assertTrue(config.getResponseCompressionEnabled());

		Assert.assertEquals(3, config.getBasePackages().size());
		packageNameIter = config.getBasePackages().iterator();
		Assert.assertEquals("de.itsvs.test.", packageNameIter.next());
		Assert.assertEquals("de.itsvs.test2.", packageNameIter.next());
		Assert.assertEquals("de.itsvs.test3", packageNameIter.next());
	}

	@Test
	public void test3() {
		AutowiredRemoteServiceGroupConfig config;

		config = appContext.getBean("autowiredServiceGroup103",
				AutowiredRemoteServiceGroupConfig.class);
		Assert.assertTrue(config.getRpcTokenProtectionEnabled());
	}

	@Test
	public void test4() {
		AutowiredRemoteServiceGroupConfig config;

		config = appContext.getBean("autowiredServiceGroup104",
				AutowiredRemoteServiceGroupConfig.class);
		Assert.assertEquals("testRpcTokenValidatorXyz",
				config.getRpcTokenValidatorName());
	}

	@Test
	public void test5() {
		AutowiredRemoteServiceGroupConfig config;
		Iterator<Pattern> filterIter;
		Pattern pattern;

		config = appContext.getBean("autowiredServiceGroup200",
				AutowiredRemoteServiceGroupConfig.class);
		Assert.assertEquals(1, config.getBasePackages().size());
		Assert.assertEquals("de.itsvs.test.", config.getBasePackages()
				.iterator().next());
		Assert.assertNull(config.getResponseCompressionEnabled());
		Assert.assertNull(config.getRpcTokenProtectionEnabled());
		Assert.assertNull(config.getRpcTokenValidatorName());

		Assert.assertEquals(3, config.getIncludeFilters().size());
		filterIter = config.getIncludeFilters().iterator();
		pattern = filterIter.next();
		Assert.assertEquals("de\\..*Service10.+", pattern.getPatternString());
		Assert.assertTrue(pattern.matches("de.itsvs.test.Service10xyz"));
		pattern = filterIter.next();
		Assert.assertEquals("de.**.Service10*", pattern.getPatternString());
		Assert.assertTrue(pattern.matches("de.itsvs.test.Service10xyz"));
		pattern = filterIter.next();
		Assert.assertEquals("de\\..*Service20", pattern.getPatternString());
		Assert.assertTrue(pattern.matches("de.itsvs.test.Service20"));

		Assert.assertEquals(2, config.getExcludeFilters().size());
		filterIter = config.getExcludeFilters().iterator();
		pattern = filterIter.next();
		Assert.assertEquals("de\\..*Service40.+", pattern.getPatternString());
		Assert.assertTrue(pattern.matches("de.itsvs.test.Service40x"));
		pattern = filterIter.next();
		Assert.assertEquals("de.**.Service50*", pattern.getPatternString());
		Assert.assertTrue(pattern.matches("de.itsvs.test.Service50a"));
	}
}
