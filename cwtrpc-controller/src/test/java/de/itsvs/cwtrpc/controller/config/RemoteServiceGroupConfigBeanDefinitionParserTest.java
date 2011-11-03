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

import de.itsvs.cwtrpc.controller.RemoteServiceConfig;
import de.itsvs.cwtrpc.controller.RemoteServiceGroupConfig;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceGroupConfigBeanDefinitionParserTest {
	private static ApplicationContext appContext;

	private static ApplicationContext appContext2;

	@BeforeClass
	public static void initClass() {
		ServletContext servletContext;
		XmlWebApplicationContext appContext;

		servletContext = new MockServletContext();

		appContext = new XmlWebApplicationContext();
		appContext
				.setConfigLocation("classpath:/de/itsvs/cwtrpc/controller/config/"
						+ "RemoteServiceGroupConfigBeanDefinitionParserTest1.xml");
		appContext.setServletContext(servletContext);
		appContext.refresh();
		RemoteServiceGroupConfigBeanDefinitionParserTest.appContext = appContext;

		appContext = new XmlWebApplicationContext();
		appContext
				.setConfigLocation("classpath:/de/itsvs/cwtrpc/controller/config/"
						+ "RemoteServiceGroupConfigBeanDefinitionParserTest2.xml");
		appContext.setServletContext(servletContext);
		appContext.refresh();
		RemoteServiceGroupConfigBeanDefinitionParserTest.appContext2 = appContext;
	}

	@AfterClass
	public static void destroyClass() {
		RemoteServiceGroupConfigBeanDefinitionParserTest.appContext = null;
		RemoteServiceGroupConfigBeanDefinitionParserTest.appContext2 = null;
	}

	@Test
	public void test1() {
		RemoteServiceGroupConfig config;

		config = appContext.getBean("serviceGroup101",
				RemoteServiceGroupConfig.class);
		Assert.assertNull(config.getResponseCompressionEnabled());
		Assert.assertNull(config.getRpcTokenProtectionEnabled());
		Assert.assertNull(config.getRpcTokenValidatorName());
		Assert.assertEquals(0, config.getServiceConfigs().size());
		Assert.assertEquals(0, config.getChildGroupConfigs().size());
	}

	@Test
	public void test2() {
		RemoteServiceGroupConfig config;

		config = appContext.getBean("serviceGroup102",
				RemoteServiceGroupConfig.class);
		Assert.assertTrue(config.getResponseCompressionEnabled());
	}

	@Test
	public void test3() {
		RemoteServiceGroupConfig config;

		config = appContext.getBean("serviceGroup103",
				RemoteServiceGroupConfig.class);
		Assert.assertTrue(config.getRpcTokenProtectionEnabled());
	}

	@Test
	public void test4() {
		RemoteServiceGroupConfig config;

		config = appContext.getBean("serviceGroup104",
				RemoteServiceGroupConfig.class);
		Assert.assertEquals("testRpcTokenValidatorXyz",
				config.getRpcTokenValidatorName());
	}

	@Test
	public void test5() {
		final RemoteServiceGroupConfig config;
		final Iterator<RemoteServiceGroupConfig> childConfigIter;
		RemoteServiceConfig serviceConfig;
		RemoteServiceGroupConfig childConfig;
		Iterator<RemoteServiceConfig> serviceConfigIter;

		config = appContext.getBean("serviceGroup210",
				RemoteServiceGroupConfig.class);

		Assert.assertEquals(1, config.getServiceConfigs().size());
		serviceConfig = config.getServiceConfigs().iterator().next();
		Assert.assertEquals("testService2", serviceConfig.getServiceName());

		Assert.assertEquals(6, config.getChildGroupConfigs().size());
		childConfigIter = config.getChildGroupConfigs().iterator();

		childConfig = childConfigIter.next(); /* 0 */
		Assert.assertEquals(0, childConfig.getChildGroupConfigs().size());
		Assert.assertEquals(1, childConfig.getServiceConfigs().size());
		Assert.assertEquals("testService3", childConfig.getServiceConfigs()
				.iterator().next().getServiceName());

		childConfig = childConfigIter.next(); /* 1 */
		Assert.assertEquals(0, childConfig.getChildGroupConfigs().size());
		Assert.assertEquals(1, childConfig.getServiceConfigs().size());
		Assert.assertEquals("testService4", childConfig.getServiceConfigs()
				.iterator().next().getServiceName());

		childConfig = childConfigIter.next(); /* 2 */
		Assert.assertEquals(0, childConfig.getChildGroupConfigs().size());
		Assert.assertEquals(1, childConfig.getServiceConfigs().size());
		Assert.assertEquals("testService20", childConfig.getServiceConfigs()
				.iterator().next().getServiceName());

		childConfig = childConfigIter.next(); /* 3 */
		Assert.assertEquals(0, childConfig.getChildGroupConfigs().size());
		Assert.assertEquals(2, childConfig.getServiceConfigs().size());
		serviceConfigIter = childConfig.getServiceConfigs().iterator();
		Assert.assertEquals("testService10", serviceConfigIter.next()
				.getServiceName());
		Assert.assertEquals("testService1", serviceConfigIter.next()
				.getServiceName());

		childConfig = childConfigIter.next(); /* 4 */
		Assert.assertEquals(0, childConfig.getChildGroupConfigs().size());
		Assert.assertEquals(1, childConfig.getServiceConfigs().size());
		Assert.assertEquals("testService3", childConfig.getServiceConfigs()
				.iterator().next().getServiceName());

		childConfig = childConfigIter.next(); /* 5 */
		Assert.assertEquals(0, childConfig.getChildGroupConfigs().size());
		Assert.assertEquals(1, childConfig.getServiceConfigs().size());
		Assert.assertEquals("testService4", childConfig.getServiceConfigs()
				.iterator().next().getServiceName());
	}

	@Test
	public void testService1() {
		final RemoteServiceGroupConfig config;
		RemoteServiceConfig serviceConfig;
		Iterator<RemoteServiceConfig> serviceConfigIter;

		config = appContext2.getBean("serviceGroup1",
				RemoteServiceGroupConfig.class);
		Assert.assertEquals(7, config.getServiceConfigs().size());
		serviceConfigIter = config.getServiceConfigs().iterator();

		serviceConfig = serviceConfigIter.next(); /* 0 */
		Assert.assertEquals("testService10", serviceConfig.getServiceName());
		Assert.assertEquals(TestService11.class,
				serviceConfig.getServiceInterface());
		Assert.assertNull(serviceConfig.getRelativePath());
		Assert.assertNull(serviceConfig.getResponseCompressionEnabled());
		Assert.assertNull(serviceConfig.getRpcTokenProtectionEnabled());
		Assert.assertNull(serviceConfig.getRpcTokenValidatorName());

		serviceConfig = serviceConfigIter.next(); /* 1 */
		Assert.assertEquals("testService1", serviceConfig.getServiceName());
		Assert.assertNull(serviceConfig.getServiceInterface());
		Assert.assertNull(serviceConfig.getRelativePath());

		serviceConfig = serviceConfigIter.next(); /* 2 */
		Assert.assertEquals("testService2", serviceConfig.getServiceName());
		Assert.assertNull(serviceConfig.getServiceInterface());
		Assert.assertTrue(serviceConfig.getResponseCompressionEnabled());
		Assert.assertTrue(serviceConfig.getRpcTokenProtectionEnabled());
		Assert.assertNull(serviceConfig.getRpcTokenValidatorName());

		serviceConfig = serviceConfigIter.next(); /* 3 */
		Assert.assertEquals("testService3", serviceConfig.getServiceName());
		Assert.assertFalse(serviceConfig.getResponseCompressionEnabled());
		Assert.assertNull(serviceConfig.getRpcTokenProtectionEnabled());
		Assert.assertNull(serviceConfig.getRpcTokenValidatorName());

		serviceConfig = serviceConfigIter.next(); /* 4 */
		Assert.assertEquals("testService4", serviceConfig.getServiceName());
		Assert.assertNull(serviceConfig.getResponseCompressionEnabled());
		Assert.assertFalse(serviceConfig.getRpcTokenProtectionEnabled());
		Assert.assertNull(serviceConfig.getRpcTokenValidatorName());

		serviceConfig = serviceConfigIter.next(); /* 5 */
		Assert.assertEquals("testService5", serviceConfig.getServiceName());
		Assert.assertNull(serviceConfig.getResponseCompressionEnabled());
		Assert.assertNull(serviceConfig.getRpcTokenProtectionEnabled());
		Assert.assertEquals("myRpcTokenValidator",
				serviceConfig.getRpcTokenValidatorName());

		serviceConfig = serviceConfigIter.next(); /* 6 */
		Assert.assertEquals("testService20", serviceConfig.getServiceName());
		Assert.assertEquals("other/20special.service",
				serviceConfig.getRelativePath());
	}
}
