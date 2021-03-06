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
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import de.itsvs.cwtrpc.core.DefaultExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProvider;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class SerializationPolicyProviderBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {
	@SuppressWarnings("rawtypes")
	@Override
	protected Class getBeanClass(Element element) {
		return DefaultExtendedSerializationPolicyProvider.class;
	}

	@Override
	protected String resolveId(Element element,
			AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		final String id;

		id = element.getAttribute(ID_ATTRIBUTE);
		return StringUtils.hasText(id) ? id
				: ExtendedSerializationPolicyProvider.DEFAULT_BEAN_ID;
	}
}
