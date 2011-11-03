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
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import de.itsvs.cwtrpc.controller.token.DefaultXsrfTokenService;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class BaseServiceConfigParser extends AbstractSingleBeanDefinitionParser {
	private final Log log = LogFactory.getLog(BaseServiceConfigParser.class);

	public void update(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		update(element, parserContext, builder, null);
	}

	public void update(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder, String defaultRpcTokenValidatorName) {
		final Object responseCompressionEnabled;
		final Object rpcTokenProtectionEnabled;
		final String rpcTokenValidatorName;

		if (element.hasAttribute(XmlNames.RESPONSE_COMPRESSION_ENABLED_ATTR)) {
			responseCompressionEnabled = element
					.getAttribute(XmlNames.RESPONSE_COMPRESSION_ENABLED_ATTR);
			builder.addPropertyValue("responseCompressionEnabled",
					responseCompressionEnabled);
		}
		if (element.hasAttribute(XmlNames.RPC_TOKEN_PROTECTION_ENABLED_ATTR)) {
			rpcTokenProtectionEnabled = element
					.getAttribute(XmlNames.RPC_TOKEN_PROTECTION_ENABLED_ATTR);
			builder.addPropertyValue("rpcTokenProtectionEnabled",
					rpcTokenProtectionEnabled);

		}

		if (element.hasAttribute(XmlNames.RPC_TOKEN_VALIDATOR_REF_ATTR)) {
			rpcTokenValidatorName = element
					.getAttribute(XmlNames.RPC_TOKEN_VALIDATOR_REF_ATTR);
			if (!StringUtils.hasText(rpcTokenValidatorName)) {
				parserContext.getReaderContext().error(
						"RPC token validator reference must not be empty",
						parserContext.extractSource(element));
			}
			builder.addPropertyValue("rpcTokenValidatorName",
					rpcTokenValidatorName);
		} else {
			rpcTokenValidatorName = defaultRpcTokenValidatorName;
		}

		/*
		 * Register default RPC token validator only if default configuration is
		 * available (not inherited configuration values from other levels).
		 */
		if ((defaultRpcTokenValidatorName != null)
				&& defaultRpcTokenValidatorName.equals(rpcTokenValidatorName)) {
			createDefaultRpcTokenValidatorBeanDefinition(element,
					parserContext, rpcTokenValidatorName);
		}
	}

	protected void createDefaultRpcTokenValidatorBeanDefinition(
			Element element, ParserContext parserContext,
			String rpcTokenValidatorName) {
		if (!parserContext.getRegistry().containsBeanDefinition(
				rpcTokenValidatorName)) {
			final RootBeanDefinition bd;

			if (log.isInfoEnabled()) {
				log.info("Registering default RPC token validator with name '"
						+ rpcTokenValidatorName + "'");
			}
			bd = new RootBeanDefinition(DefaultXsrfTokenService.class);
			bd.setSource(parserContext.extractSource(element));
			bd.setLazyInit(true);
			parserContext.registerBeanComponent(new BeanComponentDefinition(bd,
					rpcTokenValidatorName));
		}
	}
}
