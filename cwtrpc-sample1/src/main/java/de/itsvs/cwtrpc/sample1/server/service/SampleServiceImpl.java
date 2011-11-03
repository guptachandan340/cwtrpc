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

import de.itsvs.cwtrpc.sample1.client.service.SampleService;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class SampleServiceImpl implements SampleService {
	private final Log log = LogFactory.getLog(SampleServiceImpl.class);

	private int infoCount;

	public String getInfo() {
		final StringBuilder info = new StringBuilder();
		final Authentication auth;
		boolean first;

		auth = SecurityContextHolder.getContext().getAuthentication();
		log.info("User '" + auth.getName() + "' is requesting info");

		info.append("Number of Requests: " + (++infoCount) + "\n");
		info.append("User Name: " + auth.getName() + "\n");
		info.append("Roles: ");

		first = true;
		for (GrantedAuthority ga : auth.getAuthorities()) {
			if (!first) {
				info.append(", ");
			}
			first = false;
			info.append(ga.getAuthority());
		}

		return info.toString();
	}

	@RolesAllowed("ROLE_DEMO")
	public String getDemoInfo() {
		log.info("User with role ROLE_DEMO is requesting demo info");

		return "This is an information that is only available\n"
				+ "for users that have role ROLE_DEMO.\n\n"
				+ "You have this role and can see this message\n"
				+ "that is returned from by the remote service!";
	}
}
