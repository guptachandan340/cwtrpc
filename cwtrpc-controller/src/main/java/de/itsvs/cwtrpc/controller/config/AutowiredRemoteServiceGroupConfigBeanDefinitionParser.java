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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import de.itsvs.cwtrpc.controller.AutowiredRemoteServiceGroupConfig;
import de.itsvs.cwtrpc.core.pattern.MatcherType;
import de.itsvs.cwtrpc.core.pattern.PatternFactory;
import de.itsvs.cwtrpc.core.pattern.PatternType;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class AutowiredRemoteServiceGroupConfigBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {
	public static final PatternType DEFAULT_PATTERN_TYPE = PatternType.REGEX;

	private final BaseServiceConfigParser baseServiceConfigParser;

	public AutowiredRemoteServiceGroupConfigBeanDefinitionParser(
			BaseServiceConfigParser baseServiceConfigParser) {
		this.baseServiceConfigParser = baseServiceConfigParser;
	}

	public BaseServiceConfigParser getBaseServiceConfigParser() {
		return baseServiceConfigParser;
	}

	public AbstractBeanDefinition parseNested(Element element,
			ParserContext parserContext) {
		return parseInternal(element, parserContext);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class getBeanClass(Element element) {
		return AutowiredRemoteServiceGroupConfig.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		final String[] basePackages;
		ManagedList<BeanDefinition> filters;

		getBaseServiceConfigParser().update(element, parserContext, builder);

		if (element.hasAttribute(XmlNames.BASE_PACKAGES_ATTR)) {
			basePackages = StringUtils.tokenizeToStringArray(
					element.getAttribute(XmlNames.BASE_PACKAGES_ATTR),
					ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
			builder.addPropertyValue("basePackages", basePackages);
		}

		filters = new ManagedList<BeanDefinition>();
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.INCLUDE_FILTER_ELEMENT)) {
			filters.add(parseFilter(child, parserContext));
		}
		if (!filters.isEmpty()) {
			builder.addPropertyValue("includeFilters", filters);
		}

		filters = new ManagedList<BeanDefinition>();
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.EXCLUDE_FILTER_ELEMENT)) {
			filters.add(parseFilter(child, parserContext));
		}
		if (!filters.isEmpty()) {
			builder.addPropertyValue("excludeFilters", filters);
		}
	}

	protected BeanDefinition parseFilter(Element element,
			ParserContext parserContext) {
		final BeanDefinitionBuilder bdd;
		final Object type;
		final String expression;

		bdd = BeanDefinitionBuilder.rootBeanDefinition(PatternFactory.class);
		bdd.getRawBeanDefinition().setSource(
				parserContext.extractSource(element));
		if (parserContext.isDefaultLazyInit()) {
			bdd.setLazyInit(true);
		}

		if (element.hasAttribute(XmlNames.TYPE_ATTR)) {
			type = element.getAttribute(XmlNames.TYPE_ATTR);
		} else {
			type = DEFAULT_PATTERN_TYPE;
		}

		expression = element.getAttribute(XmlNames.EXPRESSION_ATTR);
		if (!StringUtils.hasText(expression)) {
			parserContext.getReaderContext().error(
					"Filter expression must not be empty",
					parserContext.extractSource(element));
		}

		bdd.setFactoryMethod("compile");
		bdd.addConstructorArgValue(type);
		bdd.addConstructorArgValue(MatcherType.PACKAGE);
		bdd.addConstructorArgValue(expression);

		return bdd.getBeanDefinition();
	}
}
