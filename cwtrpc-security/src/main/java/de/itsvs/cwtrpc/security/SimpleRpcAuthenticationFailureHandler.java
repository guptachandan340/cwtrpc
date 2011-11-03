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

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class SimpleRpcAuthenticationFailureHandler extends
		AbstractRpcAuthenticationFailureHandler {
	private final Log log = LogFactory
			.getLog(SimpleRpcAuthenticationFailureHandler.class);

	private boolean includeExceptionMessage;

	private Class<? extends Exception> defaultExceptionClass;

	private Map<Class<? extends AuthenticationException>, Class<? extends Exception>> exceptionClassMappings;

	public boolean isIncludeExceptionMessage() {
		return includeExceptionMessage;
	}

	public void setIncludeExceptionMessage(boolean includeExceptionMessage) {
		this.includeExceptionMessage = includeExceptionMessage;
	}

	public Class<? extends Exception> getDefaultExceptionClass() {
		return defaultExceptionClass;
	}

	public void setDefaultExceptionClass(
			Class<? extends Exception> defaultExceptionClass) {
		this.defaultExceptionClass = defaultExceptionClass;
	}

	public Map<Class<? extends AuthenticationException>, Class<? extends Exception>> getExceptionClassMappings() {
		return exceptionClassMappings;
	}

	public void setExceptionClassMappings(
			Map<Class<? extends AuthenticationException>, Class<? extends Exception>> exceptionClassMappings) {
		this.exceptionClassMappings = exceptionClassMappings;
	}

	protected Map<Class<? extends AuthenticationException>, Class<? extends Exception>> createDefaultExceptionClassMappings() {
		return createPackageExceptionClassMappings();
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (getDefaultExceptionClass() == null) {
			log.info("Default exception has not been specified, using default");
			setDefaultExceptionClass(getPackageDefaultExceptionClass());
		}
		if (getExceptionClassMappings() == null) {
			log.info("Exception mappings have not been specified, using default");
			setExceptionClassMappings(createDefaultExceptionClassMappings());
		}
	}

	@Override
	public Exception lookupRemoteExceptionFor(HttpServletRequest request,
			AuthenticationException exception) {
		Class<? extends Exception> remoteExceptionClass = null;

		if (getExceptionClassMappings() != null) {
			final Class<? extends AuthenticationException> exceptionClass;

			exceptionClass = exception.getClass();
			for (Map.Entry<Class<? extends AuthenticationException>, Class<? extends Exception>> entry : getExceptionClassMappings()
					.entrySet()) {
				if (entry.getKey().isAssignableFrom(exceptionClass)) {
					if (log.isDebugEnabled()) {
						log.debug("Exception mapping for class "
								+ exceptionClass.getName() + " is: "
								+ entry.getValue().getName());
					}
					remoteExceptionClass = entry.getValue();
					break;
				}
			}
		}
		if (remoteExceptionClass == null) {
			if (log.isDebugEnabled()) {
				log.debug("Exception mapping does not contain mapping for class "
						+ exception.getClass().getName()
						+ ", using default: "
						+ getDefaultExceptionClass().getName());
			}
			remoteExceptionClass = getDefaultExceptionClass();
		}

		return createRemoteException(request, exception, remoteExceptionClass);
	}

	protected Exception createRemoteException(HttpServletRequest request,
			AuthenticationException exception,
			Class<? extends Exception> remoteExceptionClass) {
		final Constructor<? extends Exception> constructor;
		Exception remoteException;

		if (isIncludeExceptionMessage()) {
			constructor = getMessageConstructor(remoteExceptionClass);
		} else {
			constructor = null;
		}

		try {
			if (constructor != null) {
				if (log.isDebugEnabled()) {
					log.debug("Creating remote exception "
							+ remoteExceptionClass.getName()
							+ " with message of original exception");
				}
				remoteException = BeanUtils.instantiateClass(constructor,
						new Object[] { exception.getMessage() });
			} else {
				remoteException = BeanUtils
						.instantiateClass(remoteExceptionClass);
			}
		} catch (BeanInstantiationException e) {
			log.error("Could not create remote exception: "
					+ remoteExceptionClass.getName(), e);
			remoteException = null;
		}

		return remoteException;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Exception> Constructor<T> getMessageConstructor(
			Class<T> exception) {
		for (Constructor<?> constructor : exception.getConstructors()) {
			if ((constructor.getParameterTypes().length == 1)
					&& String.class.equals(constructor.getParameterTypes()[0])) {
				return ((Constructor<T>) constructor);
			}
		}
		return null;
	}
}
