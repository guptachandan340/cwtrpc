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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import de.itsvs.cwtrpc.controller.RemoteServiceControllerConfig;
import de.itsvs.cwtrpc.core.DefaultExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProvider;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceControllerConfigBeanDefinitionParser implements
		BeanDefinitionParser {
	private final Log log = LogFactory
			.getLog(RemoteServiceControllerConfigBeanDefinitionParser.class);

	private final BaseServiceConfigParser baseServiceConfigParser;

	private final RemoteServiceModuleConfigBeanDefinitionParser serviceModuleConfigBeanDefinitionParser;

	public RemoteServiceControllerConfigBeanDefinitionParser(
			BaseServiceConfigParser baseServiceConfigParser,
			RemoteServiceModuleConfigBeanDefinitionParser serviceModuleConfigBeanDefinitionParser) {
		this.baseServiceConfigParser = baseServiceConfigParser;
		this.serviceModuleConfigBeanDefinitionParser = serviceModuleConfigBeanDefinitionParser;
	}

	public BaseServiceConfigParser getBaseServiceConfigParser() {
		return baseServiceConfigParser;
	}

	public RemoteServiceModuleConfigBeanDefinitionParser getServiceModuleConfigBeanDefinitionParser() {
		return serviceModuleConfigBeanDefinitionParser;
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		final CompositeComponentDefinition compositeDef;
		final BeanDefinitionBuilder bdd;
		final AbstractBeanDefinition beanDefinition;
		String id;

		compositeDef = new CompositeComponentDefinition(element.getTagName(),
				parserContext.extractSource(element));
		parserContext.pushContainingComponent(compositeDef);

		bdd = BeanDefinitionBuilder
				.rootBeanDefinition(RemoteServiceControllerConfig.class);
		bdd.getRawBeanDefinition().setSource(
				parserContext.extractSource(element));
		if (parserContext.isDefaultLazyInit()) {
			bdd.setLazyInit(true);
		}

		bdd.addConstructorArgReference(getSerializationPolicyProviderBeanRef(
				element, parserContext).getBeanName());
		bdd.addPropertyValue("serviceModuleConfigs",
				parseModules(element, parserContext));
		getBaseServiceConfigParser()
				.update(element,
						parserContext,
						bdd,
						RemoteServiceControllerConfig.DEFAULT_RPC_VALIDATOR_SERVICE_NAME);

		id = element.getAttribute(XmlNames.ID_ATTR);
		if (!StringUtils.hasText(id)) {
			id = RemoteServiceControllerConfig.DEFAULT_BEAN_ID;
		}
		beanDefinition = bdd.getBeanDefinition();
		parserContext.registerBeanComponent(new BeanComponentDefinition(
				beanDefinition, id));

		parserContext.popAndRegisterContainingComponent();
		return beanDefinition;
	}

	protected ManagedList<BeanMetadataElement> parseModules(Element element,
			ParserContext parserContext) {
		final ManagedList<BeanMetadataElement> modules;

		modules = new ManagedList<BeanMetadataElement>();
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.MODULE_ELEMENT)) {
			modules.add(getServiceModuleConfigBeanDefinitionParser()
					.parseNested(child, parserContext));
		}
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.MODULE_REF_ELEMENT)) {
			final String name;
			final RuntimeBeanReference beanReference;

			name = child.getAttribute(XmlNames.BEAN_REFERENCE_NAME_ATTR);
			if (!StringUtils.hasText(name)) {
				parserContext.getReaderContext().error(
						"Module reference must not be empty",
						parserContext.extractSource(element));
			}
			beanReference = new RuntimeBeanReference(name);
			beanReference.setSource(parserContext.extractSource(element));
			modules.add(beanReference);
		}

		return modules;
	}

	protected BeanReference getSerializationPolicyProviderBeanRef(
			Element element, ParserContext parserContext) {
		final String serializationPolicyProviderName;

		if (element
				.hasAttribute(XmlNames.SERIALIZATION_POLICY_PROVIDER_REF_ATTR)
				&& !ExtendedSerializationPolicyProvider.DEFAULT_BEAN_ID
						.equals(element
								.getAttribute(XmlNames.SERIALIZATION_POLICY_PROVIDER_REF_ATTR))) {
			serializationPolicyProviderName = element
					.getAttribute(XmlNames.SERIALIZATION_POLICY_PROVIDER_REF_ATTR);
		} else {
			serializationPolicyProviderName = ExtendedSerializationPolicyProvider.DEFAULT_BEAN_ID;
			if (!parserContext.getRegistry().containsBeanDefinition(
					serializationPolicyProviderName)) {
				final RootBeanDefinition bd;

				if (log.isInfoEnabled()) {
					log.info("Registering default serialization policy provider with name '"
							+ serializationPolicyProviderName + "'");
				}
				bd = new RootBeanDefinition(
						DefaultExtendedSerializationPolicyProvider.class);
				bd.setSource(parserContext.extractSource(element));
				if (parserContext.isDefaultLazyInit()) {
					bd.setLazyInit(true);
				}
				parserContext
						.registerBeanComponent(new BeanComponentDefinition(bd,
								serializationPolicyProviderName));
			}
		}

		return new RuntimeBeanReference(serializationPolicyProviderName);
	}
}
