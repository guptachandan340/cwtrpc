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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.StaticApplicationContext;

import de.itsvs.cwtrpc.controller.AutowiredRemoteServiceGroupConfig;
import de.itsvs.cwtrpc.controller.RemoteServiceConfig;
import de.itsvs.cwtrpc.controller.config.TestService1;
import de.itsvs.cwtrpc.controller.config.TestService10;
import de.itsvs.cwtrpc.controller.config.TestService10Impl;
import de.itsvs.cwtrpc.controller.config.TestService1Impl;
import de.itsvs.cwtrpc.controller.config.TestService2;
import de.itsvs.cwtrpc.controller.config.TestService20Impl;
import de.itsvs.cwtrpc.controller.config.TestService2Impl;
import de.itsvs.cwtrpc.controller.config.TestService3;
import de.itsvs.cwtrpc.controller.config.TestService3Impl;
import de.itsvs.cwtrpc.controller.config.TestService5;
import de.itsvs.cwtrpc.controller.config.TestService5Impl;
import de.itsvs.cwtrpc.core.pattern.MatcherType;
import de.itsvs.cwtrpc.core.pattern.Pattern;
import de.itsvs.cwtrpc.core.pattern.PatternFactory;
import de.itsvs.cwtrpc.core.pattern.PatternType;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class AutowiredRemoteServiceGroupConfigTest {
	@Test
	public void testAfterPropertiesSet1() {
		final StaticApplicationContext appContext;
		final AutowiredRemoteServiceGroupConfig config;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("testService1", TestService1Impl.class);
		appContext.registerSingleton("testService10", TestService10Impl.class);
		appContext.registerSingleton("testService20", TestService20Impl.class);

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setApplicationContext(appContext);
		config.afterPropertiesSet();

		Assert.assertTrue(config.getChildGroupConfigs().isEmpty());
		Assert.assertEquals(1, config.getServiceConfigs().size());
		Assert.assertTrue(containsServiceConfig(appContext,
				config.getServiceConfigs(), TestService1Impl.class));
	}

	@Test
	public void testAfterPropertiesSet2() {
		final StaticApplicationContext appContext;
		final AutowiredRemoteServiceGroupConfig config;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("testService1", TestService1Impl.class);

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setApplicationContext(appContext);
		config.afterPropertiesSet();

		Assert.assertTrue(config.getChildGroupConfigs().isEmpty());
		Assert.assertEquals(1, config.getServiceConfigs().size());
	}

	@Test
	public void testAfterPropertiesSet3() {
		final StaticApplicationContext appContext;
		final AutowiredRemoteServiceGroupConfig config;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("testService1", TestService1Impl.class);

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config2." }));
		config.setApplicationContext(appContext);
		config.afterPropertiesSet();

		Assert.assertTrue(config.getChildGroupConfigs().isEmpty());
		Assert.assertEquals(0, config.getServiceConfigs().size());
	}

	@Test
	public void testAfterPropertiesSet4() {
		final StaticApplicationContext appContext;
		final AutowiredRemoteServiceGroupConfig config;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("testService1", TestService1Impl.class);

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays.asList(new String[] {
				"de.itsvs.cwtrpc.controller.config2.",
				"de.itsvs.cwtrpc.controller.config." }));
		config.setApplicationContext(appContext);
		config.afterPropertiesSet();

		Assert.assertTrue(config.getChildGroupConfigs().isEmpty());
		Assert.assertEquals(1, config.getServiceConfigs().size());
	}

	@Test
	public void testAfterPropertiesSet5() {
		final StaticApplicationContext appContext;
		final AutowiredRemoteServiceGroupConfig config;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("tService1", TestService1Impl.class);
		appContext.registerSingleton("tService2", TestService2Impl.class);
		appContext.registerSingleton("tService3", TestService3Impl.class);
		appContext.registerSingleton("tService5", TestService5Impl.class);

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setIncludeFilters(Arrays.asList(new Pattern[] {
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService[13]"),
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService5") }));
		config.setApplicationContext(appContext);
		config.afterPropertiesSet();

		Assert.assertTrue(config.getChildGroupConfigs().isEmpty());
		Assert.assertEquals(3, config.getServiceConfigs().size());
		Assert.assertTrue(containsServiceConfig(appContext,
				config.getServiceConfigs(), TestService1Impl.class));
		Assert.assertTrue(containsServiceConfig(appContext,
				config.getServiceConfigs(), TestService3Impl.class));
		Assert.assertTrue(containsServiceConfig(appContext,
				config.getServiceConfigs(), TestService5Impl.class));
	}

	@Test
	public void testAfterPropertiesSet6() {
		final StaticApplicationContext appContext;
		final AutowiredRemoteServiceGroupConfig config;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("tService1", TestService1Impl.class);
		appContext.registerSingleton("tService2", TestService2Impl.class);
		appContext.registerSingleton("tService3", TestService3Impl.class);
		appContext.registerSingleton("tService5", TestService5Impl.class);

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setExcludeFilters(Arrays.asList(new Pattern[] {
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService[13]"),
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService5") }));
		config.setApplicationContext(appContext);
		config.afterPropertiesSet();

		Assert.assertTrue(config.getChildGroupConfigs().isEmpty());
		Assert.assertEquals(1, config.getServiceConfigs().size());
		Assert.assertTrue(containsServiceConfig(appContext,
				config.getServiceConfigs(), TestService2Impl.class));
	}

	@Test
	public void testAfterPropertiesSet7() {
		final StaticApplicationContext appContext;
		final AutowiredRemoteServiceGroupConfig config;

		appContext = new StaticApplicationContext();
		appContext.registerSingleton("tService1", TestService1Impl.class);
		appContext.registerSingleton("tService2", TestService2Impl.class);
		appContext.registerSingleton("tService3", TestService3Impl.class);
		appContext.registerSingleton("tService5", TestService5Impl.class);

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setIncludeFilters(Arrays.asList(new Pattern[] {
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService[13]"),
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService5") }));
		config.setExcludeFilters(Arrays.asList(new Pattern[] { PatternFactory
				.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService1") }));
		config.setApplicationContext(appContext);
		config.afterPropertiesSet();

		Assert.assertTrue(config.getChildGroupConfigs().isEmpty());
		Assert.assertEquals(2, config.getServiceConfigs().size());
		Assert.assertTrue(containsServiceConfig(appContext,
				config.getServiceConfigs(), TestService3Impl.class));
		Assert.assertTrue(containsServiceConfig(appContext,
				config.getServiceConfigs(), TestService5Impl.class));
	}

	@Test
	public void testGetRemoteServiceInterface() {
		final AutowiredRemoteServiceGroupConfig config;

		config = new AutowiredRemoteServiceGroupConfig();
		Assert.assertEquals(TestService1.class,
				config.getRemoteServiceInterface(TestService1Impl.class));
		Assert.assertNull(config
				.getRemoteServiceInterface(TestService10Impl.class));
		Assert.assertNull(config
				.getRemoteServiceInterface(ByteArrayInputStream.class));
	}

	@Test
	public void testIsAcceptedServiceInterface2() {
		final AutowiredRemoteServiceGroupConfig config;

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));

		Assert.assertFalse(config.isAcceptedServiceInterface(Pattern.class));
		Assert.assertTrue(config
				.isAcceptedServiceInterface(TestService10.class));
	}

	@Test
	public void testIsAcceptedServiceInterface3() {
		final AutowiredRemoteServiceGroupConfig config;

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config2." }));

		Assert.assertFalse(config.isAcceptedServiceInterface(Pattern.class));
		Assert.assertFalse(config
				.isAcceptedServiceInterface(TestService10.class));
	}

	@Test
	public void testIsAcceptedServiceInterface4() {
		final AutowiredRemoteServiceGroupConfig config;

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays.asList(new String[] {
				"de.itsvs.cwtrpc.controller.config2.",
				"de.itsvs.cwtrpc.controller.config." }));

		Assert.assertFalse(config.isAcceptedServiceInterface(Pattern.class));
		Assert.assertTrue(config
				.isAcceptedServiceInterface(TestService10.class));
	}

	@Test
	public void testIsAcceptedServiceInterface5() {
		final AutowiredRemoteServiceGroupConfig config;

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setIncludeFilters(Arrays.asList(new Pattern[] {
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService[13]"),
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService5") }));

		Assert.assertTrue(config.isAcceptedServiceInterface(TestService1.class));
		Assert.assertFalse(config
				.isAcceptedServiceInterface(TestService2.class));
		Assert.assertTrue(config.isAcceptedServiceInterface(TestService3.class));
		Assert.assertTrue(config.isAcceptedServiceInterface(TestService5.class));
	}

	@Test
	public void testIsAcceptedServiceInterface6() {
		final AutowiredRemoteServiceGroupConfig config;

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setExcludeFilters(Arrays.asList(new Pattern[] {
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService[13]"),
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService5") }));

		Assert.assertFalse(config
				.isAcceptedServiceInterface(TestService1.class));
		Assert.assertTrue(config.isAcceptedServiceInterface(TestService2.class));
		Assert.assertFalse(config
				.isAcceptedServiceInterface(TestService3.class));
		Assert.assertFalse(config
				.isAcceptedServiceInterface(TestService5.class));
	}

	@Test
	public void testIsAcceptedServiceInterface7() {
		final AutowiredRemoteServiceGroupConfig config;

		config = new AutowiredRemoteServiceGroupConfig();
		config.setBasePackages(Arrays
				.asList(new String[] { "de.itsvs.cwtrpc.controller.config." }));
		config.setIncludeFilters(Arrays.asList(new Pattern[] {
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService[13]"),
				PatternFactory.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService5") }));
		config.setExcludeFilters(Arrays.asList(new Pattern[] { PatternFactory
				.compile(PatternType.REGEX, MatcherType.PACKAGE,
						"de\\..+\\.TestService1") }));

		Assert.assertFalse(config
				.isAcceptedServiceInterface(TestService1.class));
		Assert.assertFalse(config
				.isAcceptedServiceInterface(TestService2.class));
		Assert.assertTrue(config.isAcceptedServiceInterface(TestService3.class));
		Assert.assertTrue(config.isAcceptedServiceInterface(TestService5.class));
	}

	protected boolean containsServiceConfig(BeanFactory beanFactory,
			Collection<RemoteServiceConfig> serviceConfigs,
			Class<?> serviceClass) {
		for (RemoteServiceConfig serviceConfig : serviceConfigs) {
			if (serviceClass.equals(beanFactory.getType(serviceConfig
					.getServiceName()))) {
				Assert.assertNull(serviceConfig.getResponseCompressionEnabled());
				Assert.assertNull(serviceConfig.getRpcTokenProtectionEnabled());
				Assert.assertNull(serviceConfig.getRpcTokenValidatorName());
				Assert.assertNull(serviceConfig.getServiceInterface());
				Assert.assertNull(serviceConfig.getRelativePath());
				return true;
			}
		}
		return false;
	}
}
