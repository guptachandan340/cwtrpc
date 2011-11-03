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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import de.itsvs.cwtrpc.security.AbstractRpcAuthenticationFailureHandler;
import de.itsvs.cwtrpc.security.AccountExpiredException;
import de.itsvs.cwtrpc.security.AccountStatusException;
import de.itsvs.cwtrpc.security.AuthenticationException;
import de.itsvs.cwtrpc.security.AuthenticationServiceException;
import de.itsvs.cwtrpc.security.BadCredentialsException;
import de.itsvs.cwtrpc.security.CredentialsExpiredException;
import de.itsvs.cwtrpc.security.DisabledException;
import de.itsvs.cwtrpc.security.LockedException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class AbstractRpcAuthenticationFailureHandlerTest {
	@Test
	public void testGetPackageDefaultExceptionClass1() {
		Assert.assertEquals(
				getCwtRpcSecurityExceptionClass(AuthenticationException.class
						.getSimpleName()),
				AbstractRpcAuthenticationFailureHandler
						.getPackageDefaultExceptionClass());
	}

	@Test
	public void testCreatePackageExceptionClassMappings1() {
		assertNullExceptionClass(AccountStatusException.class, null);
		assertExceptionClass(AccountExpiredException.class, null);
		assertExceptionClass(CredentialsExpiredException.class, null);
		assertExceptionClass(DisabledException.class, null);
		assertExceptionClass(LockedException.class, null);
		assertNullExceptionClass(
				AuthenticationCredentialsNotFoundException.class, null);
		assertExceptionClass(AuthenticationServiceException.class, null);
		assertExceptionClass(BadCredentialsException.class, null);
		assertNullExceptionClass(InsufficientAuthenticationException.class,
				null);
		assertNullExceptionClass(NonceExpiredException.class,
				"web.authentication.www");
		assertNullExceptionClass(
				PreAuthenticatedCredentialsNotFoundException.class,
				"web.authentication.preauth");
		assertNullExceptionClass(ProviderNotFoundException.class, null);
		assertNullExceptionClass(RememberMeAuthenticationException.class,
				"web.authentication.rememberme");
		assertNullExceptionClass(CookieTheftException.class,
				"web.authentication.rememberme");
		assertNullExceptionClass(InvalidCookieException.class,
				"web.authentication.rememberme");
		assertNullExceptionClass(SessionAuthenticationException.class,
				"web.authentication.session");
		assertNullExceptionClass(UsernameNotFoundException.class,
				"core.userdetails");
	}

	public void assertExceptionClass(Class<?> simpleNameClass,
			String subPackageName) {
		Assert.assertEquals(
				getCwtRpcSecurityExceptionClass(simpleNameClass.getSimpleName()),
				AbstractRpcAuthenticationFailureHandler
						.createPackageExceptionClassMappings().get(
								getSpringSecurityExceptionClass(
										simpleNameClass.getSimpleName(),
										subPackageName)));
	}

	public void assertNullExceptionClass(Class<?> simpleNameClass,
			String subPackageName) {
		Assert.assertNull(AbstractRpcAuthenticationFailureHandler
				.createPackageExceptionClassMappings()
				.get(getSpringSecurityExceptionClass(
						simpleNameClass.getSimpleName(), subPackageName)));
	}

	public static Class<?> getSpringSecurityExceptionClass(
			String simpleClassName, String subPackageName) {
		final StringBuilder name;

		name = new StringBuilder();
		name.append("org.springframework.security.");
		if (subPackageName != null) {
			name.append(subPackageName);
		} else {
			name.append("authentication");
		}
		name.append('.');
		name.append(simpleClassName);

		try {
			return AbstractRpcAuthenticationFailureHandlerTest.class
					.getClassLoader().loadClass(name.toString());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Class<?> getCwtRpcSecurityExceptionClass(
			String simpleClassName) {
		try {
			return AbstractRpcAuthenticationFailureHandlerTest.class
					.getClassLoader().loadClass(
							"de.itsvs.cwtrpc.security." + simpleClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
