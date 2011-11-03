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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import de.itsvs.cwtrpc.controller.RemoteServiceModuleConfig;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RemoteServiceModuleConfigBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {
	private final BaseServiceConfigParser baseServiceConfigParser;

	private final RemoteServiceGroupConfigBeanDefinitionParser serviceGroupConfigBeanDefinitionParser;

	public RemoteServiceModuleConfigBeanDefinitionParser(
			BaseServiceConfigParser baseServiceConfigParser,
			RemoteServiceGroupConfigBeanDefinitionParser serviceGroupConfigBeanDefinitionParser) {
		this.baseServiceConfigParser = baseServiceConfigParser;
		this.serviceGroupConfigBeanDefinitionParser = serviceGroupConfigBeanDefinitionParser;
	}

	public BaseServiceConfigParser getBaseServiceConfigParser() {
		return baseServiceConfigParser;
	}

	public RemoteServiceGroupConfigBeanDefinitionParser getServiceGroupConfigBeanDefinitionParser() {
		return serviceGroupConfigBeanDefinitionParser;
	}

	public AbstractBeanDefinition parseNested(Element element,
			ParserContext parserContext) {
		return parseInternal(element, parserContext);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class getBeanClass(Element element) {
		return RemoteServiceModuleConfig.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		final String name;

		name = element.getAttribute(XmlNames.MODULE_NAME_ATTR);
		if (!StringUtils.hasText(name)) {
			parserContext.getReaderContext().error(
					"Module name must not be empty",
					parserContext.extractSource(element));
		}

		builder.addConstructorArgValue(name);
		builder.addConstructorArgValue(getServiceGroupConfigBeanDefinitionParser()
				.parseNested(element, parserContext));
		getBaseServiceConfigParser().update(element, parserContext, builder);
	}
}
