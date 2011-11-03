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

import javax.servlet.http.HttpServletRequest;

import org.junit.Ignore;

import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.server.rpc.RPCRequest;

import de.itsvs.cwtrpc.controller.token.RpcTokenValidator;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
@Ignore
public class TestRpcTokenValidator1Impl implements RpcTokenValidator {
	public boolean shouldValidateToken(HttpServletRequest servletRequest,
			RPCRequest rpcRequest) {
		return false;
	}

	public void validateToken(HttpServletRequest servletRequest,
			RPCRequest rpcRequest) throws RpcTokenException {
		/* nothing to be done */
	}
}
