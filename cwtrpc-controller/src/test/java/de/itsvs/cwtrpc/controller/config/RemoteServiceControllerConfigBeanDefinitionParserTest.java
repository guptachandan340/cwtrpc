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

import de.itsvs.cwtrpc.controller.RemoteServiceControllerConfig;
import de.itsvs.cwtrpc.controller.RemoteServiceModuleConfig;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceControllerConfigBeanDefinitionParserTest {
	private static ApplicationContext appContext;

	@BeforeClass
	public static void initClass() {
		ServletContext servletContext;
		XmlWebApplicationContext appContext;

		servletContext = new MockServletContext();

		appContext = new XmlWebApplicationContext();
		appContext
				.setConfigLocation("classpath:/de/itsvs/cwtrpc/controller/config/"
						+ "RemoteServiceControllerConfigBeanDefinitionParserTest1.xml");
		appContext.setServletContext(servletContext);
		appContext.refresh();
		RemoteServiceControllerConfigBeanDefinitionParserTest.appContext = appContext;
	}

	@AfterClass
	public static void destroyClass() {
		RemoteServiceControllerConfigBeanDefinitionParserTest.appContext = null;
	}

	@Test
	public void test1() {
		RemoteServiceControllerConfig config;

		config = appContext.getBean("serviceController100",
				RemoteServiceControllerConfig.class);
		Assert.assertEquals(1, config.getServiceModuleConfigs().size());
		Assert.assertEquals("myModule700", config.getServiceModuleConfigs()
				.iterator().next().getName());
		Assert.assertTrue(config.getResponseCompressionEnabled());
		Assert.assertFalse(config.getRpcTokenProtectionEnabled());
		Assert.assertEquals("rpcTokenService",
				config.getRpcTokenValidatorName());
		Assert.assertSame(appContext.getBean("serializationPolicyProvider"),
				config.getSerializationPolicyProvider());
	}

	@Test
	public void test2() {
		RemoteServiceControllerConfig config;

		config = appContext.getBean("serviceController101",
				RemoteServiceControllerConfig.class);
		Assert.assertEquals(1, config.getServiceModuleConfigs().size());
		Assert.assertEquals("myModule701", config.getServiceModuleConfigs()
				.iterator().next().getName());
		Assert.assertTrue(config.getResponseCompressionEnabled());
		Assert.assertFalse(config.getRpcTokenProtectionEnabled());
		Assert.assertEquals("rpcTokenService",
				config.getRpcTokenValidatorName());
		Assert.assertSame(appContext.getBean("serializationPolicyProvider"),
				config.getSerializationPolicyProvider());
	}

	@Test
	public void test3() {
		RemoteServiceControllerConfig config;

		config = appContext.getBean("serviceController102",
				RemoteServiceControllerConfig.class);
		Assert.assertFalse(config.getResponseCompressionEnabled());
	}

	@Test
	public void test4() {
		RemoteServiceControllerConfig config;

		config = appContext.getBean("serviceController103",
				RemoteServiceControllerConfig.class);
		Assert.assertTrue(config.getRpcTokenProtectionEnabled());
	}

	@Test
	public void test5() {
		RemoteServiceControllerConfig config;

		config = appContext.getBean("serviceController104",
				RemoteServiceControllerConfig.class);
		Assert.assertEquals("rpcTokenService2",
				config.getRpcTokenValidatorName());
	}

	@Test
	public void test6() {
		RemoteServiceControllerConfig config;

		config = appContext.getBean("serviceController105",
				RemoteServiceControllerConfig.class);
		Assert.assertSame(appContext.getBean("serializationPolicyProvider2"),
				config.getSerializationPolicyProvider());
	}

	@Test
	public void test7() {
		RemoteServiceControllerConfig config;
		Iterator<RemoteServiceModuleConfig> moduleConfigIter;
		RemoteServiceModuleConfig moduleConfig;

		config = appContext.getBean("remoteServiceControllerConfig",
				RemoteServiceControllerConfig.class);
		Assert.assertEquals(4, config.getServiceModuleConfigs().size());
		moduleConfigIter = config.getServiceModuleConfigs().iterator();

		moduleConfig = moduleConfigIter.next();
		Assert.assertEquals("myModule400", moduleConfig.getName());
		Assert.assertEquals(1, moduleConfig.getServiceGroupConfig()
				.getServiceConfigs().size());

		moduleConfig = moduleConfigIter.next();
		Assert.assertEquals("myModule500", moduleConfig.getName());
		Assert.assertEquals(1, moduleConfig.getServiceGroupConfig()
				.getServiceConfigs().size());

		moduleConfig = moduleConfigIter.next();
		Assert.assertEquals("myModule100", moduleConfig.getName());
		Assert.assertEquals(1, moduleConfig.getServiceGroupConfig()
				.getServiceConfigs().size());

		moduleConfig = moduleConfigIter.next();
		Assert.assertEquals("myModule200", moduleConfig.getName());
		Assert.assertEquals(1, moduleConfig.getServiceGroupConfig()
				.getServiceConfigs().size());
	}
}
