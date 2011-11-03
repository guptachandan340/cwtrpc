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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import de.itsvs.cwtrpc.controller.CacheControlConfig;
import de.itsvs.cwtrpc.controller.CacheControlUriConfig;
import de.itsvs.cwtrpc.core.pattern.MatcherType;
import de.itsvs.cwtrpc.core.pattern.PatternFactory;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class CacheControlConfigBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {
	@Override
	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		if (element.hasAttribute(ID_ATTRIBUTE)) {
			return super.resolveId(element, definition, parserContext);
		}
		return CacheControlConfig.DEFAULT_BEAN_ID;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class getBeanClass(Element element) {
		return CacheControlConfig.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		final ManagedList<AbstractBeanDefinition> uriConfigs;

		update(element, builder, XmlNames.LOWER_CASE_MATCH_ATTR,
				"lowerCaseMatch");
		update(element, builder, XmlNames.DEFAULTS_ENABLED_ATTR,
				"defaultsEnabled");
		update(element, builder, XmlNames.CACHE_MAX_AGE_ATTR, "cacheMaxAge");

		uriConfigs = new ManagedList<AbstractBeanDefinition>();
		for (Element child : DomUtils.getChildElementsByTagName(element,
				XmlNames.URI_ELEMENT)) {
			uriConfigs.add(createUriConfigBeanDefinition(child, parserContext));
		}
		if (!uriConfigs.isEmpty()) {
			builder.addPropertyValue("uriConfigs", uriConfigs);
		}
	}

	protected AbstractBeanDefinition createUriConfigBeanDefinition(
			Element element, ParserContext parserContext) {
		final BeanDefinitionBuilder bdd;

		bdd = BeanDefinitionBuilder
				.rootBeanDefinition(CacheControlUriConfig.class);
		if (parserContext.isDefaultLazyInit()) {
			bdd.setLazyInit(true);
		}
		bdd.getRawBeanDefinition().setSource(
				parserContext.extractSource(element));
		bdd.addConstructorArgValue(createUriPattern(element, parserContext));

		if (element.hasAttribute(XmlNames.SERVICE_INTERFACE_ATTR)) {
			bdd.addPropertyValue("serviceInterface",
					element.getAttribute(XmlNames.SERVICE_INTERFACE_ATTR));
		}

		update(element, bdd, XmlNames.METHOD_ATTR, "method");
		update(element, bdd, XmlNames.PUBLIC_ATTR, "publicContent");
		update(element, bdd, XmlNames.PRIVATE_ATTR, "privateContent");
		update(element, bdd, XmlNames.NO_CACHE_ATTR, "noCache");
		update(element, bdd, XmlNames.NO_STORE_ATTR, "noStore");
		update(element, bdd, XmlNames.NO_TRANSFORM_ATTR, "noTransform");
		update(element, bdd, XmlNames.MUST_REVALIDATE_ATTR, "mustRevalidate");
		update(element, bdd, XmlNames.PROXY_REVALIDATE_ATTR, "proxyRevalidate");
		update(element, bdd, XmlNames.MAX_AGE_ATTR, "maxAge");
		update(element, bdd, XmlNames.S_MAXAGE_ATTR, "sharedMaxage");
		update(element, bdd, XmlNames.EXPIRES_ATTR, "expires");
		update(element, bdd, XmlNames.PRAGMA_NO_CACHE_ATTR, "pragmaNoCache");

		return bdd.getBeanDefinition();
	}

	protected void update(Element element, BeanDefinitionBuilder builder,
			String attributeName, String propertyName) {
		if (element.hasAttribute(attributeName)) {
			builder.addPropertyValue(propertyName,
					element.getAttribute(attributeName));
		}
	}

	protected AbstractBeanDefinition createUriPattern(Element element,
			ParserContext parserContext) {
		final String value;
		final Object type;
		final RootBeanDefinition beanDefinition;

		value = element.getAttribute(XmlNames.VALUE_ATTR);
		if (!StringUtils.hasText(value)) {
			parserContext.getReaderContext().error(
					"URI value must not be empty",
					parserContext.extractSource(element));
		}
		if (element.hasAttribute(XmlNames.TYPE_ATTR)) {
			type = element.getAttribute(XmlNames.TYPE_ATTR);
		} else {
			type = CacheControlUriConfig.DEFAULT_PATTERN_TYPE;
		}

		beanDefinition = new RootBeanDefinition(PatternFactory.class);
		beanDefinition.setSource(parserContext.extractSource(element));
		if (parserContext.isDefaultLazyInit()) {
			beanDefinition.setLazyInit(true);
		}
		beanDefinition.setFactoryMethodName("compile");

		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				0, type);
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				1, MatcherType.URI);
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				2, value);

		return beanDefinition;
	}
}
