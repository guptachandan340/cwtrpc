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

package de.itsvs.cwtrpc.controller.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.server.Util;
import com.google.gwt.user.server.rpc.NoXsrfProtect;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.XsrfProtect;

import de.itsvs.cwtrpc.controller.RemoteServiceContextHolder;
import de.itsvs.cwtrpc.core.CwtRpcException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultXsrfTokenService implements XsrfTokenService,
		RpcTokenValidator, RpcTokenGenerator {
	private final Log log = LogFactory.getLog(DefaultXsrfTokenService.class);

	public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";

	protected static final String MD5_MESSAGE_DIGEST_ALGORITHM_NAME = "MD5";

	private static final Md5ThreadLocal md5ThreadLocal = new Md5ThreadLocal(
			MD5_MESSAGE_DIGEST_ALGORITHM_NAME);

	private final String sessionCookieName;

	public DefaultXsrfTokenService() {
		this(DEFAULT_SESSION_COOKIE_NAME);
	}

	public DefaultXsrfTokenService(String sessionCookieName) {
		this.sessionCookieName = sessionCookieName;
	}

	public String getSessionCookieName() {
		return sessionCookieName;
	}

	public XsrfToken getNewXsrfToken() {
		final HttpServletRequest request;

		request = RemoteServiceContextHolder.getContext().getServletRequest();
		if (request == null) {
			throw new IllegalStateException(
					"Method must only be invoked in context of "
							+ "remote service controller");
		}

		return generateToken(request);
	}

	public XsrfToken generateToken(HttpServletRequest request)
			throws RpcTokenException {
		final byte[] sessionCookieBytes;
		final String token;

		Assert.notNull(request, "'request' must not be null");

		sessionCookieBytes = getCookieBytes(request, getSessionCookieName());
		if ((sessionCookieBytes == null) || (sessionCookieBytes.length == 0)) {
			throw new RpcTokenException(
					"Request does not contain required valid session cookie "
							+ getSessionCookieName());
		}

		token = getMd5HexString(sessionCookieBytes);
		if (log.isDebugEnabled()) {
			log.debug("Generated token '" + token + "'");
		}

		return new XsrfToken(token);
	}

	public boolean shouldValidateToken(HttpServletRequest servletRequest,
			RPCRequest rpcRequest) {
		return Util.isMethodXsrfProtected(rpcRequest.getMethod(),
				XsrfProtect.class, NoXsrfProtect.class, RpcToken.class);
	}

	public void validateToken(HttpServletRequest servletRequest,
			RPCRequest rpcRequest) throws RpcTokenException {
		final RpcToken token;
		final XsrfToken xsrfToken;
		final XsrfToken expectedXsrfToken;

		Assert.notNull(servletRequest, "'servletRequest' must not be null");
		Assert.notNull(rpcRequest, "'rpcRequest' must not be null");

		token = rpcRequest.getRpcToken();
		if (token == null) {
			throw new RpcTokenException(
					"Request does not contain required XSRF token");
		}
		if (!(token instanceof XsrfToken)) {
			throw new RpcTokenException(
					"RPC token is not required XSRF token ["
							+ token.getClass().getName() + "]");
		}
		xsrfToken = (XsrfToken) token;

		expectedXsrfToken = generateToken(servletRequest);
		if (log.isDebugEnabled()) {
			log.debug("Validating received token '" + xsrfToken.getToken()
					+ "' against expected token '"
					+ expectedXsrfToken.getToken() + "'");
		}
		if (!expectedXsrfToken.getToken().equals(xsrfToken.getToken())) {
			throw new RpcTokenException(
					"Received XSRF token does not match expected token");
		}
	}

	protected static byte[] getCookieBytes(HttpServletRequest request,
			String cookieName) {
		final Cookie cookie;

		cookie = Util.getCookie(request, cookieName, false);
		if ((cookie == null) || (cookie.getValue() == null)) {
			/* request contains invalid cookie */
			return null;
		}

		return cookie.getValue().getBytes();
	}

	protected static String getMd5HexString(byte[] bytes) {
		final MessageDigest md;
		final StringBuilder value;

		md = md5ThreadLocal.get();
		md.reset();
		md.update(bytes);

		value = new StringBuilder();
		for (byte b : md.digest()) {
			final int val = b & 0xff;

			if (val < 16) {
				value.append('0');
			}
			value.append(Integer.toHexString(val));
		}

		return value.toString();
	}

	protected static class Md5ThreadLocal extends ThreadLocal<MessageDigest> {
		private final String algorithm;

		public Md5ThreadLocal(String algorithm) {
			this.algorithm = algorithm;
		}

		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance(algorithm);
			} catch (NoSuchAlgorithmException e) {
				throw new CwtRpcException("'" + algorithm
						+ "' message digest algorithm "
						+ "has not been registered", e);
			}
		}
	}
}
