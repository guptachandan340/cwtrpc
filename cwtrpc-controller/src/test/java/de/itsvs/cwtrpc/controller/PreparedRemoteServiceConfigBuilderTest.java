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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import de.itsvs.cwtrpc.controller.BaseServiceConfig;
import de.itsvs.cwtrpc.controller.PreparedRemoteServiceConfig;
import de.itsvs.cwtrpc.controller.PreparedRemoteServiceConfigBuilder;
import de.itsvs.cwtrpc.controller.RemoteServiceConfig;
import de.itsvs.cwtrpc.controller.RemoteServiceControllerConfig;
import de.itsvs.cwtrpc.controller.RemoteServiceGroupConfig;
import de.itsvs.cwtrpc.controller.RemoteServiceModuleConfig;
import de.itsvs.cwtrpc.controller.config.TestRpcTokenValidator1Impl;
import de.itsvs.cwtrpc.controller.config.TestService1;
import de.itsvs.cwtrpc.controller.config.TestService10Impl;
import de.itsvs.cwtrpc.controller.config.TestService11;
import de.itsvs.cwtrpc.controller.config.TestService1Impl;
import de.itsvs.cwtrpc.controller.config.TestService2;
import de.itsvs.cwtrpc.controller.config.TestService20;
import de.itsvs.cwtrpc.controller.config.TestService20Impl;
import de.itsvs.cwtrpc.controller.config.TestService2Impl;
import de.itsvs.cwtrpc.controller.config.TestService3;
import de.itsvs.cwtrpc.controller.config.TestService3Impl;
import de.itsvs.cwtrpc.controller.config.TestService4;
import de.itsvs.cwtrpc.controller.config.TestService4Impl;
import de.itsvs.cwtrpc.controller.config.TestService5;
import de.itsvs.cwtrpc.controller.config.TestService5Impl;
import de.itsvs.cwtrpc.controller.token.DefaultXsrfTokenService;
import de.itsvs.cwtrpc.core.DefaultExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.CwtRpcException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class PreparedRemoteServiceConfigBuilderTest {
	private static ApplicationContext appContext;

	@BeforeClass
	public static void initClass() {
		StaticApplicationContext appContext;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("testService1", TestService1Impl.class);
		appContext.registerPrototype("testService2", TestService2Impl.class);
		appContext.registerSingleton("testService3", TestService3Impl.class);
		appContext.registerPrototype("testService4", TestService4Impl.class);
		appContext.registerSingleton("testService5", TestService5Impl.class);
		appContext.registerPrototype("testService10", TestService10Impl.class);
		appContext.registerSingleton("testService20", TestService20Impl.class);
		appContext.registerSingleton("rpcTokenValidatorXyz",
				TestRpcTokenValidator1Impl.class);
		appContext.registerSingleton("rpcTokenValidator100",
				TestRpcTokenValidator1Impl.class);
		appContext.registerSingleton("rpcTokenValidator200",
				TestRpcTokenValidator1Impl.class);
		appContext.registerSingleton("rpcTokenService",
				DefaultXsrfTokenService.class);

		PreparedRemoteServiceConfigBuilderTest.appContext = appContext;
	}

	@AfterClass
	public static void destroyClass() {
		PreparedRemoteServiceConfigBuilderTest.appContext = null;
	}

	@Test
	public void testBuild1() {
		RemoteServiceControllerConfig controllerConfig;
		RemoteServiceGroupConfig groupConfig;
		RemoteServiceModuleConfig moduleConfig1;
		RemoteServiceModuleConfig moduleConfig2;
		RemoteServiceModuleConfig moduleConfig3;
		PreparedRemoteServiceConfigBuilder builder;
		Map<String, PreparedRemoteServiceConfig> result;

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService1") }));
		moduleConfig1 = new RemoteServiceModuleConfig("myModule10", groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setResponseCompressionEnabled(false);
		groupConfig.setRpcTokenProtectionEnabled(true);
		groupConfig.setRpcTokenValidatorName("rpcTokenValidator100");
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService2") }));
		moduleConfig2 = new RemoteServiceModuleConfig("myModule20", groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService3") }));
		moduleConfig3 = new RemoteServiceModuleConfig("myModule30", groupConfig);

		controllerConfig = new RemoteServiceControllerConfig(
				new DefaultExtendedSerializationPolicyProvider());
		controllerConfig.setServiceModuleConfigs(Arrays
				.asList(new RemoteServiceModuleConfig[] { moduleConfig1,
						moduleConfig2, moduleConfig3 }));

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		result = builder.build(controllerConfig);
		Assert.assertEquals(3, result.size());

		Assert.assertTrue(result.get("myModule10/test1.service")
				.isResponseCompressionEnabled());
		Assert.assertNull(result.get("myModule10/test1.service")
				.getRpcTokenValidator());
		Assert.assertFalse(result.get("myModule20/test2.service")
				.isResponseCompressionEnabled());
		Assert.assertSame(appContext.getBean("rpcTokenValidator100"), result
				.get("myModule20/test2.service").getRpcTokenValidator());
		Assert.assertTrue(result.get("myModule30/test3.service")
				.isResponseCompressionEnabled());
		Assert.assertNull(result.get("myModule30/test3.service")
				.getRpcTokenValidator());
	}

	@Test
	public void testBuild2() {
		RemoteServiceControllerConfig controllerConfig;
		RemoteServiceGroupConfig groupConfig;
		RemoteServiceModuleConfig moduleConfig1;
		RemoteServiceModuleConfig moduleConfig2;
		RemoteServiceModuleConfig moduleConfig3;
		PreparedRemoteServiceConfigBuilder builder;
		Map<String, PreparedRemoteServiceConfig> result;

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setRpcTokenValidatorName("rpcTokenValidator200");
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService1") }));
		moduleConfig1 = new RemoteServiceModuleConfig("myModule10", groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setResponseCompressionEnabled(true);
		groupConfig.setRpcTokenProtectionEnabled(false);
		groupConfig.setRpcTokenValidatorName("rpcTokenValidator100");
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService2") }));
		moduleConfig2 = new RemoteServiceModuleConfig("myModule20", groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService3") }));
		moduleConfig3 = new RemoteServiceModuleConfig("myModule30", groupConfig);

		controllerConfig = new RemoteServiceControllerConfig(
				new DefaultExtendedSerializationPolicyProvider());
		controllerConfig.setResponseCompressionEnabled(false);
		controllerConfig.setRpcTokenProtectionEnabled(true);
		controllerConfig.setServiceModuleConfigs(Arrays
				.asList(new RemoteServiceModuleConfig[] { moduleConfig1,
						moduleConfig2, moduleConfig3 }));

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		result = builder.build(controllerConfig);
		Assert.assertEquals(3, result.size());

		Assert.assertFalse(result.get("myModule10/test1.service")
				.isResponseCompressionEnabled());
		Assert.assertSame(appContext.getBean("rpcTokenValidator200"), result
				.get("myModule10/test1.service").getRpcTokenValidator());
		Assert.assertTrue(result.get("myModule20/test2.service")
				.isResponseCompressionEnabled());
		Assert.assertNull(result.get("myModule20/test2.service")
				.getRpcTokenValidator());
		Assert.assertFalse(result.get("myModule30/test3.service")
				.isResponseCompressionEnabled());
		Assert.assertSame(appContext.getBean("rpcTokenService"),
				result.get("myModule30/test3.service").getRpcTokenValidator());
	}

	@Test
	public void testCreateRemoteServiceConfigsByUri1() {
		PreparedRemoteServiceConfigBuilder builder;
		BaseServiceConfig baseServiceConfig;
		RemoteServiceModuleConfig moduleConfig;
		RemoteServiceGroupConfig groupConfig;
		List<RemoteServiceConfig> serviceConfigs;
		List<RemoteServiceGroupConfig> childGroupConfigs;
		Map<String, PreparedRemoteServiceConfig> result;

		baseServiceConfig = new BaseServiceConfig();
		baseServiceConfig.setResponseCompressionEnabled(true);
		baseServiceConfig.setRpcTokenProtectionEnabled(false);
		baseServiceConfig.setRpcTokenValidatorName("rpcTokenValidatorXyz");

		childGroupConfigs = new ArrayList<RemoteServiceGroupConfig>();
		/* first child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] {
						new RemoteServiceConfig("testService1"),
						new RemoteServiceConfig("testService10",
								TestService11.class) }));
		childGroupConfigs.add(groupConfig);
		/* second child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService2") }));
		childGroupConfigs.add(groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		serviceConfigs = new ArrayList<RemoteServiceConfig>();
		serviceConfigs.add(new RemoteServiceConfig("testService3"));
		serviceConfigs.add(new RemoteServiceConfig("testService4"));
		serviceConfigs.add(new RemoteServiceConfig("testService5"));
		serviceConfigs.add(createRemoteServiceConfig("testService20", null,
				"test20.service", null, null, null));
		groupConfig.setServiceConfigs(serviceConfigs);
		groupConfig.setChildGroupConfigs(childGroupConfigs);
		moduleConfig = new RemoteServiceModuleConfig("myModule10", groupConfig);

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		result = builder.createRemoteServiceConfigsByUri(baseServiceConfig,
				moduleConfig, moduleConfig.getServiceGroupConfig());

		Assert.assertEquals(7, result.size());
		Assert.assertTrue(result.get("myModule10/test1.service")
				.isResponseCompressionEnabled());
		Assert.assertTrue(result.get("myModule10/test2.service")
				.isResponseCompressionEnabled());
		Assert.assertTrue(result.get("myModule10/test5.service")
				.isResponseCompressionEnabled());
		Assert.assertNull(result.get("myModule10/test11.service")
				.getRpcTokenValidator());
		Assert.assertNull(result.get("myModule10/test2.service")
				.getRpcTokenValidator());
		Assert.assertNull(result.get("myModule10/test4.service")
				.getRpcTokenValidator());
	}

	@Test
	public void testCreateRemoteServiceConfigsByUri2() {
		PreparedRemoteServiceConfigBuilder builder;
		BaseServiceConfig baseServiceConfig;
		RemoteServiceModuleConfig moduleConfig;
		RemoteServiceGroupConfig groupConfig;
		List<RemoteServiceConfig> serviceConfigs;
		List<RemoteServiceGroupConfig> childGroupConfigs;
		Map<String, PreparedRemoteServiceConfig> result;

		baseServiceConfig = new BaseServiceConfig();
		baseServiceConfig.setResponseCompressionEnabled(false);
		baseServiceConfig.setRpcTokenProtectionEnabled(true);
		baseServiceConfig.setRpcTokenValidatorName("rpcTokenValidatorXyz");

		childGroupConfigs = new ArrayList<RemoteServiceGroupConfig>();
		/* first child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] {
						createRemoteServiceConfig("testService1", null, null,
								true, false, "rpcTokenValidator100"),
						new RemoteServiceConfig("testService10",
								TestService11.class) }));
		childGroupConfigs.add(groupConfig);
		/* second child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService2") }));
		childGroupConfigs.add(groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		serviceConfigs = new ArrayList<RemoteServiceConfig>();
		serviceConfigs.add(new RemoteServiceConfig("testService3"));
		serviceConfigs.add(createRemoteServiceConfig("testService4", null,
				null, true, true, "rpcTokenValidator200"));
		serviceConfigs.add(new RemoteServiceConfig("testService5"));
		serviceConfigs.add(createRemoteServiceConfig("testService20", null,
				"test20.service", null, true, null));
		groupConfig.setServiceConfigs(serviceConfigs);
		groupConfig.setChildGroupConfigs(childGroupConfigs);
		moduleConfig = new RemoteServiceModuleConfig("myModule10", groupConfig);

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		result = builder.createRemoteServiceConfigsByUri(baseServiceConfig,
				moduleConfig, moduleConfig.getServiceGroupConfig());

		Assert.assertEquals(7, result.size());

		Assert.assertTrue(result.get("myModule10/test1.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test11.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test2.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test3.service")
				.isResponseCompressionEnabled());
		Assert.assertTrue(result.get("myModule10/test4.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test5.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test20.service")
				.isResponseCompressionEnabled());

		Assert.assertNull(result.get("myModule10/test1.service")
				.getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test11.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test3.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidator200"), result
				.get("myModule10/test4.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test5.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test20.service").getRpcTokenValidator());
	}

	@Test
	public void testCreateRemoteServiceConfigsByUri3() {
		PreparedRemoteServiceConfigBuilder builder;
		BaseServiceConfig baseServiceConfig;
		RemoteServiceModuleConfig moduleConfig;
		RemoteServiceGroupConfig groupConfig;
		List<RemoteServiceConfig> serviceConfigs;
		List<RemoteServiceGroupConfig> childGroupConfigs;
		Map<String, PreparedRemoteServiceConfig> result;

		baseServiceConfig = new BaseServiceConfig();
		baseServiceConfig.setResponseCompressionEnabled(false);
		baseServiceConfig.setRpcTokenProtectionEnabled(true);
		baseServiceConfig.setRpcTokenValidatorName("rpcTokenValidator300");

		childGroupConfigs = new ArrayList<RemoteServiceGroupConfig>();
		/* first child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] {
						new RemoteServiceConfig("testService1"),
						new RemoteServiceConfig("testService10",
								TestService11.class) }));
		childGroupConfigs.add(groupConfig);
		/* second child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService2") }));
		childGroupConfigs.add(groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setResponseCompressionEnabled(true);
		groupConfig.setRpcTokenProtectionEnabled(false);
		groupConfig.setRpcTokenValidatorName("rpcTokenValidatorXyz");
		serviceConfigs = new ArrayList<RemoteServiceConfig>();
		serviceConfigs.add(new RemoteServiceConfig("testService3"));
		serviceConfigs.add(new RemoteServiceConfig("testService4"));
		serviceConfigs.add(new RemoteServiceConfig("testService5"));
		serviceConfigs.add(createRemoteServiceConfig("testService20", null,
				"test20.service", null, null, null));
		groupConfig.setServiceConfigs(serviceConfigs);
		groupConfig.setChildGroupConfigs(childGroupConfigs);
		moduleConfig = new RemoteServiceModuleConfig("myModule10", groupConfig);

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		result = builder.createRemoteServiceConfigsByUri(baseServiceConfig,
				moduleConfig, moduleConfig.getServiceGroupConfig());

		Assert.assertEquals(7, result.size());
		Assert.assertTrue(result.get("myModule10/test1.service")
				.isResponseCompressionEnabled());
		Assert.assertTrue(result.get("myModule10/test2.service")
				.isResponseCompressionEnabled());
		Assert.assertTrue(result.get("myModule10/test5.service")
				.isResponseCompressionEnabled());
		Assert.assertNull(result.get("myModule10/test11.service")
				.getRpcTokenValidator());
		Assert.assertNull(result.get("myModule10/test2.service")
				.getRpcTokenValidator());
		Assert.assertNull(result.get("myModule10/test4.service")
				.getRpcTokenValidator());
	}

	@Test
	public void testCreateRemoteServiceConfigsByUri4() {
		PreparedRemoteServiceConfigBuilder builder;
		BaseServiceConfig baseServiceConfig;
		RemoteServiceModuleConfig moduleConfig;
		RemoteServiceGroupConfig groupConfig;
		List<RemoteServiceConfig> serviceConfigs;
		List<RemoteServiceGroupConfig> childGroupConfigs;
		Map<String, PreparedRemoteServiceConfig> result;

		baseServiceConfig = new BaseServiceConfig();
		baseServiceConfig.setResponseCompressionEnabled(true);
		baseServiceConfig.setRpcTokenProtectionEnabled(false);
		baseServiceConfig.setRpcTokenValidatorName("rpcTokenValidator999");

		childGroupConfigs = new ArrayList<RemoteServiceGroupConfig>();
		/* first child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] {
						createRemoteServiceConfig("testService1", null, null,
								true, false, "rpcTokenValidator100"),
						new RemoteServiceConfig("testService10",
								TestService11.class) }));
		childGroupConfigs.add(groupConfig);
		/* second child group */
		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setResponseCompressionEnabled(true);
		groupConfig.setRpcTokenProtectionEnabled(false);
		groupConfig.setRpcTokenValidatorName("rpcTokenValidator888");
		groupConfig.setServiceConfigs(Arrays
				.asList(new RemoteServiceConfig[] { new RemoteServiceConfig(
						"testService2") }));
		childGroupConfigs.add(groupConfig);

		groupConfig = new RemoteServiceGroupConfig();
		groupConfig.setResponseCompressionEnabled(false);
		groupConfig.setRpcTokenProtectionEnabled(true);
		groupConfig.setRpcTokenValidatorName("rpcTokenValidatorXyz");
		serviceConfigs = new ArrayList<RemoteServiceConfig>();
		serviceConfigs.add(new RemoteServiceConfig("testService3"));
		serviceConfigs.add(createRemoteServiceConfig("testService4", null,
				null, true, true, "rpcTokenValidator200"));
		serviceConfigs.add(new RemoteServiceConfig("testService5"));
		serviceConfigs.add(createRemoteServiceConfig("testService20", null,
				"test20.service", null, true, null));
		groupConfig.setServiceConfigs(serviceConfigs);
		groupConfig.setChildGroupConfigs(childGroupConfigs);
		moduleConfig = new RemoteServiceModuleConfig("myModule10", groupConfig);

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		result = builder.createRemoteServiceConfigsByUri(baseServiceConfig,
				moduleConfig, moduleConfig.getServiceGroupConfig());

		Assert.assertEquals(7, result.size());

		Assert.assertTrue(result.get("myModule10/test1.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test11.service")
				.isResponseCompressionEnabled());
		Assert.assertTrue(result.get("myModule10/test2.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test3.service")
				.isResponseCompressionEnabled());
		Assert.assertTrue(result.get("myModule10/test4.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test5.service")
				.isResponseCompressionEnabled());
		Assert.assertFalse(result.get("myModule10/test20.service")
				.isResponseCompressionEnabled());

		Assert.assertNull(result.get("myModule10/test1.service")
				.getRpcTokenValidator());
		Assert.assertNull(result.get("myModule10/test2.service")
				.getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test11.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test3.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidator200"), result
				.get("myModule10/test4.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test5.service").getRpcTokenValidator());
		Assert.assertSame(appContext.getBean("rpcTokenValidatorXyz"), result
				.get("myModule10/test20.service").getRpcTokenValidator());
	}

	protected RemoteServiceConfig createRemoteServiceConfig(String serviceName,
			Class<?> serviceInterface, String relativePath,
			Boolean responseCompressionEnabled,
			Boolean rpcTokenProtectionEnabled, String rpcTokenValidatorName) {
		final RemoteServiceConfig config;

		config = new RemoteServiceConfig(serviceName, serviceInterface);
		config.setRelativePath(relativePath);
		config.setResponseCompressionEnabled(responseCompressionEnabled);
		config.setRpcTokenProtectionEnabled(rpcTokenProtectionEnabled);
		config.setRpcTokenValidatorName(rpcTokenValidatorName);

		return config;
	}

	@Test
	public void testMerge1() {
		PreparedRemoteServiceConfigBuilder builder;
		Map<String, PreparedRemoteServiceConfig> destMap;
		Map<String, PreparedRemoteServiceConfig> srcMap;

		destMap = new HashMap<String, PreparedRemoteServiceConfig>();
		destMap.put("cfg1", new PreparedRemoteServiceConfig("service1",
				TestService1.class, false, null));
		destMap.put("cfg2", new PreparedRemoteServiceConfig("service2",
				TestService2.class, false, null));
		srcMap = new HashMap<String, PreparedRemoteServiceConfig>();
		srcMap.put("cfg3", new PreparedRemoteServiceConfig("service3",
				TestService3.class, false, null));
		srcMap.put("cfg4", new PreparedRemoteServiceConfig("service4",
				TestService4.class, false, null));
		srcMap.put("cfg5", new PreparedRemoteServiceConfig("service5",
				TestService5.class, false, null));

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.merge(destMap, srcMap);

		Assert.assertEquals(5, destMap.size());
		Assert.assertTrue(destMap.containsKey("cfg1"));
		Assert.assertTrue(destMap.containsKey("cfg2"));
		Assert.assertTrue(destMap.containsKey("cfg3"));
		Assert.assertTrue(destMap.containsKey("cfg4"));
		Assert.assertTrue(destMap.containsKey("cfg5"));
	}

	@Test(expected = CwtRpcException.class)
	public void testMerge2() {
		PreparedRemoteServiceConfigBuilder builder;
		Map<String, PreparedRemoteServiceConfig> destMap;
		Map<String, PreparedRemoteServiceConfig> srcMap;

		destMap = new HashMap<String, PreparedRemoteServiceConfig>();
		destMap.put("cfg1", new PreparedRemoteServiceConfig("service1",
				TestService1.class, false, null));
		destMap.put("cfg2", new PreparedRemoteServiceConfig("service2",
				TestService2.class, false, null));
		srcMap = new HashMap<String, PreparedRemoteServiceConfig>();
		srcMap.put("cfg3", new PreparedRemoteServiceConfig("service3",
				TestService3.class, false, null));
		srcMap.put("cfg2", new PreparedRemoteServiceConfig("service4",
				TestService4.class, false, null));
		srcMap.put("cfg5", new PreparedRemoteServiceConfig("service5",
				TestService5.class, false, null));

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.merge(destMap, srcMap);
	}

	@Test
	public void testGetRemoteServiceUri1() {
		PreparedRemoteServiceConfigBuilder builder;
		RemoteServiceConfig config;

		config = new RemoteServiceConfig("service1");

		builder = new PreparedRemoteServiceConfigBuilder();
		Assert.assertEquals("myModule1/test1.service", builder
				.getRemoteServiceUri("myModule1", config, TestService1.class));
	}

	@Test
	public void testGetRemoteServiceUri2() {
		PreparedRemoteServiceConfigBuilder builder;
		RemoteServiceConfig config;

		config = new RemoteServiceConfig("service1");
		config.setRelativePath("myPath/xyz.service");

		builder = new PreparedRemoteServiceConfigBuilder();
		Assert.assertEquals("myModule1/myPath/xyz.service", builder
				.getRemoteServiceUri("myModule1", config, TestService1.class));
	}

	@Test(expected = CwtRpcException.class)
	public void testGetRemoteServiceUri3() {
		PreparedRemoteServiceConfigBuilder builder;
		RemoteServiceConfig config;

		config = new RemoteServiceConfig("service1");

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.getRemoteServiceUri("myModule1", config, TestService20.class);
	}

	@Test
	public void testGetRemoteServiceInterface1() {
		final StaticApplicationContext appContext;
		final PreparedRemoteServiceConfigBuilder builder;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("testService", TestService1Impl.class);

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		Assert.assertEquals(TestService1.class,
				builder.getRemoteServiceInterface("testService"));
	}

	@Test(expected = CwtRpcException.class)
	public void testGetRemoteServiceInterface2() {
		final StaticApplicationContext appContext;
		final PreparedRemoteServiceConfigBuilder builder;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("testService", TestService10Impl.class);

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		builder.getRemoteServiceInterface("testService");
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testGetRemoteServiceInterface3() {
		final StaticApplicationContext appContext;
		final PreparedRemoteServiceConfigBuilder builder;

		appContext = new StaticApplicationContext();

		builder = new PreparedRemoteServiceConfigBuilder();
		builder.setBeanFactory(appContext);

		builder.getRemoteServiceInterface("testService");
	}
}
