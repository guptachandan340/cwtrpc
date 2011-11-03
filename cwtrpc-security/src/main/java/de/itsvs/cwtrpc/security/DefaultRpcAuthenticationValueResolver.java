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

package de.itsvs.cwtrpc.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultRpcAuthenticationValueResolver implements
		RpcAuthenticationValueResolver {
	private final Log log = LogFactory
			.getLog(DefaultRpcAuthenticationValueResolver.class);

	private int parameterIndex;

	private boolean trimValue = true;

	public DefaultRpcAuthenticationValueResolver() {
		super();
	}

	public DefaultRpcAuthenticationValueResolver(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

	public int getParameterIndex() {
		return parameterIndex;
	}

	public void setParameterIndex(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

	public boolean isTrimValue() {
		return trimValue;
	}

	public void setTrimValue(boolean trimValue) {
		this.trimValue = trimValue;
	}

	public String resolveValue(Object[] parameters) {
		final Object value;
		String convertedValue;

		if ((parameters == null) || (parameters.length <= getParameterIndex())) {
			if (log.isWarnEnabled()) {
				log.warn("Number of parameters " + parameters.length
						+ " is smaller than requested parameter index "
						+ getParameterIndex());
			}
			return null;
		}

		value = parameters[getParameterIndex()];
		if (value == null) {
			return null;
		}

		convertedValue = value.toString();
		if (isTrimValue()) {
			convertedValue = convertedValue.trim();
		}

		return convertedValue;
	}
}
