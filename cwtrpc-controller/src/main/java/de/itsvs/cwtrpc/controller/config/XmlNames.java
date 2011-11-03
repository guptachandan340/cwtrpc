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

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public interface XmlNames {
	String SERIALIZATION_POLICY_PROVIDER_ELEMENT = "serialization-policy-provider";

	String CONTROLLER_ELEMENT = "controller";

	String MODULE_ELEMENT = "module";

	String SERVICE_GROUP_ELEMENT = "service-group";

	String SERVICE_ELEMENT = "service";

	String SERVICE_GROUP_REF_ELEMENT = "service-group-ref";

	String AUTOWIRED_SERVICE_GROUP_ELEMENT = "autowired-service-group";

	String MODULE_REF_ELEMENT = "module-ref";

	String INCLUDE_FILTER_ELEMENT = "include-filter";

	String EXCLUDE_FILTER_ELEMENT = "exclude-filter";

	String CACHE_CONTROL_ELEMENT = "cache-control";

	String URI_ELEMENT = "uri";

	String ID_ATTR = "id";

	String MODULE_NAME_ATTR = "module-name";

	String SERVICE_REF_ATTR = "service-ref";

	String SERVICE_INTERFACE_ATTR = "service-interface";

	String BEAN_REFERENCE_NAME_ATTR = "name";

	String SERIALIZATION_POLICY_PROVIDER_REF_ATTR = "serialization-policy-provider-ref";

	String RPC_TOKEN_PROTECTION_ENABLED_ATTR = "rpc-token-protection-enabled";

	String RPC_TOKEN_VALIDATOR_REF_ATTR = "rpc-token-validator-ref";

	String BASE_PACKAGES_ATTR = "base-packages";

	String EXPRESSION_ATTR = "expression";

	String TYPE_ATTR = "type";

	String RESPONSE_COMPRESSION_ENABLED_ATTR = "response-compression-enabled";

	String METHOD_ATTR = "method";

	String PUBLIC_ATTR = "public";

	String PRIVATE_ATTR = "private";

	String NO_CACHE_ATTR = "no-cache";

	String NO_STORE_ATTR = "no-store";

	String NO_TRANSFORM_ATTR = "no-transform";

	String MUST_REVALIDATE_ATTR = "must-revalidate";

	String PROXY_REVALIDATE_ATTR = "proxy-revalidate";

	String MAX_AGE_ATTR = "max-age";

	String S_MAXAGE_ATTR = "s-maxage";

	String EXPIRES_ATTR = "expires";

	String PRAGMA_NO_CACHE_ATTR = "pragma-no-cache";

	String LOWER_CASE_MATCH_ATTR = "lower-case-match";

	String DEFAULTS_ENABLED_ATTR = "defaults-enabled";

	String CACHE_MAX_AGE_ATTR = "cache-max-age";

	String RELATIVE_PATH_ATTR = "relative-path";

	String VALUE_ATTR = "value";
}
