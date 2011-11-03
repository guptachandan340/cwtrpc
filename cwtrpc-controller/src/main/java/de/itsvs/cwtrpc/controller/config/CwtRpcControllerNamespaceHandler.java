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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class CwtRpcControllerNamespaceHandler extends NamespaceHandlerSupport {
	public void init() {
		final BaseServiceConfigParser baseServiceParser;
		final AutowiredRemoteServiceGroupConfigBeanDefinitionParser autowiredServiceGroupParser;
		final RemoteServiceGroupConfigBeanDefinitionParser serviceGroupParser;
		final RemoteServiceModuleConfigBeanDefinitionParser moduleParser;

		baseServiceParser = new BaseServiceConfigParser();
		autowiredServiceGroupParser = new AutowiredRemoteServiceGroupConfigBeanDefinitionParser(
				baseServiceParser);
		serviceGroupParser = new RemoteServiceGroupConfigBeanDefinitionParser(
				baseServiceParser, autowiredServiceGroupParser);
		moduleParser = new RemoteServiceModuleConfigBeanDefinitionParser(
				baseServiceParser, serviceGroupParser);

		registerBeanDefinitionParser(XmlNames.CACHE_CONTROL_ELEMENT,
				new CacheControlConfigBeanDefinitionParser());
		registerBeanDefinitionParser(
				XmlNames.SERIALIZATION_POLICY_PROVIDER_ELEMENT,
				new SerializationPolicyProviderBeanDefinitionParser());
		registerBeanDefinitionParser(XmlNames.CONTROLLER_ELEMENT,
				new RemoteServiceControllerConfigBeanDefinitionParser(
						baseServiceParser, moduleParser));
		registerBeanDefinitionParser(XmlNames.MODULE_ELEMENT, moduleParser);
		registerBeanDefinitionParser(XmlNames.SERVICE_GROUP_ELEMENT,
				serviceGroupParser);
		registerBeanDefinitionParser(XmlNames.AUTOWIRED_SERVICE_GROUP_ELEMENT,
				autowiredServiceGroupParser);
	}
}
