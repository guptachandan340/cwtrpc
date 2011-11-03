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
import de.itsvs.cwtrpc.controller.RemoteServiceModuleConfig;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceModuleConfigBeanDefinitionParserTest {
	private static ApplicationContext appContext;

	@BeforeClass
	public static void initClass() {
		ServletContext servletContext;
		XmlWebApplicationContext appContext;

		servletContext = new MockServletContext();

		appContext = new XmlWebApplicationContext();
		appContext
				.setConfigLocation("classpath:/de/itsvs/cwtrpc/controller/config/"
						+ "RemoteServiceModuleConfigBeanDefinitionParserTest1.xml");
		appContext.setServletContext(servletContext);
		appContext.refresh();
		RemoteServiceModuleConfigBeanDefinitionParserTest.appContext = appContext;
	}

	@AfterClass
	public static void destroyClass() {
		RemoteServiceModuleConfigBeanDefinitionParserTest.appContext = null;
	}

	@Test
	public void test1() {
		RemoteServiceModuleConfig config;

		config = appContext.getBean("serviceModule101",
				RemoteServiceModuleConfig.class);
		Assert.assertEquals("myModule5", config.getName());
		Assert.assertNull(config.getResponseCompressionEnabled());
		Assert.assertNull(config.getRpcTokenProtectionEnabled());
		Assert.assertNull(config.getRpcTokenValidatorName());
		Assert.assertEquals(0, config.getServiceGroupConfig()
				.getServiceConfigs().size());
		Assert.assertEquals(0, config.getServiceGroupConfig()
				.getChildGroupConfigs().size());
	}

	@Test
	public void test2() {
		RemoteServiceModuleConfig config;

		config = appContext.getBean("serviceModule102",
				RemoteServiceModuleConfig.class);
		Assert.assertTrue(config.getResponseCompressionEnabled());
	}

	@Test
	public void test3() {
		RemoteServiceModuleConfig config;

		config = appContext.getBean("serviceModule103",
				RemoteServiceModuleConfig.class);
		Assert.assertTrue(config.getRpcTokenProtectionEnabled());
	}

	@Test
	public void test4() {
		RemoteServiceModuleConfig config;

		config = appContext.getBean("serviceModule104",
				RemoteServiceModuleConfig.class);
		Assert.assertEquals("testRpcTokenValidatorXyz",
				config.getRpcTokenValidatorName());
	}

	@Test
	public void test5() {
		final RemoteServiceModuleConfig config;
		final Iterator<RemoteServiceGroupConfig> childConfigIter;
		RemoteServiceConfig serviceConfig;
		RemoteServiceGroupConfig childConfig;
		Iterator<RemoteServiceConfig> serviceConfigIter;

		config = appContext.getBean("serviceModule210",
				RemoteServiceModuleConfig.class);

		Assert.assertEquals(1, config.getServiceGroupConfig()
				.getServiceConfigs().size());
		serviceConfig = config.getServiceGroupConfig().getServiceConfigs()
				.iterator().next();
		Assert.assertEquals("testService2", serviceConfig.getServiceName());

		Assert.assertEquals(6, config.getServiceGroupConfig()
				.getChildGroupConfigs().size());
		childConfigIter = config.getServiceGroupConfig().getChildGroupConfigs()
				.iterator();

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
}
