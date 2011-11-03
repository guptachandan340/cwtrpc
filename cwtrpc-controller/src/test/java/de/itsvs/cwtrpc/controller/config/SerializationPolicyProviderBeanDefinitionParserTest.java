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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import de.itsvs.cwtrpc.core.DefaultExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProvider;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class SerializationPolicyProviderBeanDefinitionParserTest {
	@Test
	public void test1() {
		ServletContext servletContext;
		XmlWebApplicationContext appContext;

		servletContext = new MockServletContext();
		appContext = new XmlWebApplicationContext();
		appContext
				.setConfigLocation("classpath:/de/itsvs/cwtrpc/controller/config/"
						+ "SerializationPolicyProviderBeanDefinitionParserTest1.xml");
		appContext.setServletContext(servletContext);
		appContext.refresh();

		Assert.assertEquals(
				2,
				appContext
						.getBeanNamesForType(ExtendedSerializationPolicyProvider.class).length);
		Assert.assertTrue(appContext
				.containsBean("serializationPolicyProvider"));
		Assert.assertEquals(DefaultExtendedSerializationPolicyProvider.class,
				appContext.getType("serializationPolicyProvider"));
		Assert.assertNotNull(appContext.getBean("serializationPolicyProvider",
				DefaultExtendedSerializationPolicyProvider.class)
				.getServletContext());
		Assert.assertTrue(appContext
				.containsBean("serializationPolicyProviderTest"));
		Assert.assertEquals(DefaultExtendedSerializationPolicyProvider.class,
				appContext.getType("serializationPolicyProviderTest"));
		Assert.assertNotNull(appContext.getBean(
				"serializationPolicyProviderTest",
				DefaultExtendedSerializationPolicyProvider.class)
				.getServletContext());
	}
}
