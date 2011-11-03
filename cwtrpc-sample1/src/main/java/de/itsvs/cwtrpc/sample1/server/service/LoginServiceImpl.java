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

package de.itsvs.cwtrpc.sample1.server.service;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import de.itsvs.cwtrpc.controller.RemoteServiceContextHolder;
import de.itsvs.cwtrpc.sample1.client.service.LoginService;
import de.itsvs.cwtrpc.security.AuthenticationException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class LoginServiceImpl implements LoginService {
	private final Log log = LogFactory.getLog(LoginServiceImpl.class);

	public String login(String userName, String password)
			throws AuthenticationException {
		final StringBuilder roleNames = new StringBuilder();
		final Authentication auth;

		auth = SecurityContextHolder.getContext().getAuthentication();
		log.info("Login of user '"
				+ auth.getName()
				+ "' (session ID "
				+ RemoteServiceContextHolder.getContext().getServletRequest()
						.getSession().getId() + ")");

		for (GrantedAuthority ga : auth.getAuthorities()) {
			if (roleNames.length() > 0) {
				roleNames.append(", ");
			}
			roleNames.append(ga.getAuthority());
		}

		return roleNames.toString();
	}

	@RolesAllowed("ROLE_SAMPLE")
	public void logout() {
		final Authentication auth;

		auth = SecurityContextHolder.getContext().getAuthentication();
		log.info("Logout of user '" + auth.getName() + "'");
	}
}
