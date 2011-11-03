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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public abstract class AbstractRpcAuthenticationFailureHandler extends
		AbstractRpcFailureHandler implements AuthenticationFailureHandler {
	private final Log log = LogFactory
			.getLog(AbstractRpcAuthenticationFailureHandler.class);

	static Class<? extends Exception> getPackageDefaultExceptionClass() {
		return de.itsvs.cwtrpc.security.AuthenticationException.class;
	}

	static Map<Class<? extends AuthenticationException>, Class<? extends Exception>> createPackageExceptionClassMappings() {
		final Map<Class<? extends AuthenticationException>, Class<? extends Exception>> mappings;

		mappings = new LinkedHashMap<Class<? extends AuthenticationException>, Class<? extends Exception>>();
		mappings.put(
				org.springframework.security.authentication.BadCredentialsException.class,
				de.itsvs.cwtrpc.security.BadCredentialsException.class);
		mappings.put(
				org.springframework.security.authentication.AccountExpiredException.class,
				de.itsvs.cwtrpc.security.AccountExpiredException.class);
		mappings.put(
				org.springframework.security.authentication.CredentialsExpiredException.class,
				de.itsvs.cwtrpc.security.CredentialsExpiredException.class);
		mappings.put(
				org.springframework.security.authentication.DisabledException.class,
				de.itsvs.cwtrpc.security.DisabledException.class);
		mappings.put(
				org.springframework.security.authentication.LockedException.class,
				de.itsvs.cwtrpc.security.LockedException.class);
		mappings.put(
				org.springframework.security.authentication.AuthenticationServiceException.class,
				de.itsvs.cwtrpc.security.AuthenticationServiceException.class);

		return mappings;
	}

	public abstract Exception lookupRemoteExceptionFor(
			HttpServletRequest request, AuthenticationException exception);

	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		Exception remoteException = null;

		log.debug("Handling authentication failure", exception);

		remoteException = lookupRemoteExceptionFor(request, exception);
		if (remoteException != null) {
			writeExpectedException(request, response, remoteException);
		} else {
			writeUnexpectedException(request, response, exception);
		}
	}
}
