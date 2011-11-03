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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class UsernamePasswordRpcAuthenticationFilter extends
		AbstractRpcAuthenticationProcessingFilter {
	private RpcAuthenticationValueResolver usernameResolver;

	private RpcAuthenticationValueResolver passwordResolver;

	public RpcAuthenticationValueResolver getUsernameResolver() {
		return usernameResolver;
	}

	public void setUsernameResolver(
			RpcAuthenticationValueResolver usernameResolver) {
		this.usernameResolver = usernameResolver;
	}

	public RpcAuthenticationValueResolver getPasswordResolver() {
		return passwordResolver;
	}

	public void setPasswordResolver(
			RpcAuthenticationValueResolver passwordResolver) {
		this.passwordResolver = passwordResolver;
	}

	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();

		if (getUsernameResolver() == null) {
			setUsernameResolver(new DefaultRpcAuthenticationValueResolver(0));
		}
		if (getPasswordResolver() == null) {
			setPasswordResolver(new DefaultRpcAuthenticationValueResolver(1));
		}
	}

	@Override
	public Authentication createAuthenticationToken(
			HttpServletRequest servletRequest, RPCRequest rpcRequest)
			throws AuthenticationException, IOException, ServletException {
		final UsernamePasswordAuthenticationToken authentication;
		String username;
		String password;

		username = getUsernameResolver().resolveValue(
				rpcRequest.getParameters());
		if (username == null) {
			username = "";
		}
		password = getPasswordResolver().resolveValue(
				rpcRequest.getParameters());
		if (password == null) {
			password = "";
		}

		authentication = new UsernamePasswordAuthenticationToken(username,
				password);
		updateDetails(servletRequest, authentication);

		return authentication;
	}

	protected void updateDetails(HttpServletRequest request,
			UsernamePasswordAuthenticationToken authentication) {
		authentication.setDetails(getAuthenticationDetailsSource()
				.buildDetails(request));
	}
}
