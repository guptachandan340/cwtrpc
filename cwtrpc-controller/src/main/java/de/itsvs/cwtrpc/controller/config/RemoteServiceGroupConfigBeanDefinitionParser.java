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

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import de.itsvs.cwtrpc.controller.RemoteServiceConfig;
import de.itsvs.cwtrpc.controller.RemoteServiceGroupConfig;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceGroupConfigBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {
	private final BaseServiceConfigParser baseServiceConfigParser;

	private final AutowiredRemoteServiceGroupConfigBeanDefinitionParser autowiredServiceGroupConfigBeanDefinitionParser;

	public RemoteServiceGroupConfigBeanDefinitionParser(
			BaseServiceConfigParser baseServiceConfigParser,
			AutowiredRemoteServiceGroupConfigBeanDefinitionParser autowiredServiceGroupConfigBeanDefinitionParser) {
		this.baseServiceConfigParser = baseServiceConfigParser;
		this.autowiredServiceGroupConfigBeanDefinitionParser = autowiredServiceGroupConfigBeanDefinitionParser;
	}

	public BaseServiceConfigParser getBaseServiceConfigParser() {
		return baseServiceConfigParser;
	}

	public AutowiredRemoteServiceGroupConfigBeanDefinitionParser getAutowiredServiceGroupConfigBeanDefinitionParser() {
		return autowiredServiceGroupConfigBeanDefinitionParser;
	}

	public AbstractBeanDefinition parseNested(Element element,
			ParserContext parserContext) {
		return parseInternal(element, parserContext);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class getBeanClass(Element element) {
		return RemoteServiceGroupConfig.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		final ManagedList<BeanMetadataElement> serviceConfigs;
		final ManagedList<BeanMetadataElement> childGroupConfigs;

		getBaseServiceConfigParser().update(element, parserContext, builder);

		serviceConfigs = new ManagedList<BeanMetadataElement>();
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.SERVICE_ELEMENT)) {
			serviceConfigs.add(createServiceConfigBeanDefinition(child,
					parserContext));
		}
		childGroupConfigs = new ManagedList<BeanMetadataElement>();
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.SERVICE_GROUP_ELEMENT)) {
			childGroupConfigs.add(parseNested(child, parserContext));
		}
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.SERVICE_GROUP_REF_ELEMENT)) {
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
			childGroupConfigs.add(beanReference);
		}
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.AUTOWIRED_SERVICE_GROUP_ELEMENT)) {
			childGroupConfigs
					.add(getAutowiredServiceGroupConfigBeanDefinitionParser()
							.parseNested(child, parserContext));
		}

		builder.addConstructorArgValue(serviceConfigs);
		builder.addConstructorArgValue(childGroupConfigs);
	}

	protected AbstractBeanDefinition createServiceConfigBeanDefinition(
			Element element, ParserContext parserContext) {
		final String serviceName;
		final BeanDefinitionBuilder bdd;
		final String relativePath;

		serviceName = element.getAttribute(XmlNames.SERVICE_REF_ATTR);
		if (!StringUtils.hasText(serviceName)) {
			parserContext.getReaderContext().error(
					"Service reference must not be empty",
					parserContext.extractSource(element));
		}

		bdd = BeanDefinitionBuilder
				.rootBeanDefinition(RemoteServiceConfig.class);
		if (parserContext.isDefaultLazyInit()) {
			bdd.setLazyInit(true);
		}
		bdd.getRawBeanDefinition().setSource(
				parserContext.extractSource(element));
		bdd.addConstructorArgValue(serviceName);

		if (element.hasAttribute(XmlNames.SERVICE_INTERFACE_ATTR)) {
			bdd.addPropertyValue("serviceInterface",
					element.getAttribute(XmlNames.SERVICE_INTERFACE_ATTR));
		}

		if (element.hasAttribute(XmlNames.RELATIVE_PATH_ATTR)) {
			relativePath = element.getAttribute(XmlNames.RELATIVE_PATH_ATTR);
			if (!StringUtils.hasText(relativePath)) {
				parserContext.getReaderContext().error(
						"Relative path must not be empty",
						parserContext.extractSource(element));
			}
			bdd.addPropertyValue("relativePath", relativePath);
		}

		getBaseServiceConfigParser().update(element, parserContext, bdd);

		return bdd.getBeanDefinition();
	}
}
