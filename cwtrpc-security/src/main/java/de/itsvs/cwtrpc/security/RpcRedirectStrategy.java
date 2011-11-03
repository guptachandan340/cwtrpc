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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.util.Assert;

import de.itsvs.cwtrpc.core.CwtRpcUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class RpcRedirectStrategy implements RedirectStrategy {
	private final Log log = LogFactory.getLog(RpcRedirectStrategy.class);

	protected static final String CONTENT_TYPE = "text/plain";

	protected static final String CHARACTER_ENCODING = "UTF-8";

	protected static final String DEFAULT_RESPONSE_TEXT = "";

	private boolean error;

	private int statusCode = HttpServletResponse.SC_BAD_REQUEST;

	private String text;

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		Assert.isTrue((statusCode > 0), "'statusCode' must not be negative");
		this.statusCode = statusCode;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void sendRedirect(HttpServletRequest request,
			HttpServletResponse response, String url) throws IOException {
		final String responseText;

		responseText = createResponseText(request, response, url);

		response.reset();
		addNoCacheResponseHeaders(request, response);
		if (isError()) {
			if (log.isDebugEnabled()) {
				log.debug("Instead of redirecting to url '" + url + "' error "
						+ getStatusCode() + " will be sent to client");
			}
			if (responseText.length() > 0) {
				response.sendError(getStatusCode(), responseText);
			} else {
				response.sendError(getStatusCode());
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Instead of redirecting to url '" + url
						+ "' status code will be set to " + getStatusCode());
			}
			response.setContentType(CONTENT_TYPE);
			response.setCharacterEncoding(CHARACTER_ENCODING);
			response.setStatus(getStatusCode());
			writeResponseText(request, response, CHARACTER_ENCODING, url);
			response.flushBuffer();
		}
	}

	protected void writeResponseText(HttpServletRequest request,
			HttpServletResponse response, String characterEncoding,
			String responseText) throws IOException {
		final byte[] responseBytes;

		responseBytes = responseText.getBytes(characterEncoding);
		response.setContentLength(responseBytes.length);
		response.getOutputStream().write(responseBytes);
	}

	protected String createResponseText(HttpServletRequest request,
			HttpServletResponse response, String url) {
		final String text;

		text = getText();
		return ((text != null) ? text : DEFAULT_RESPONSE_TEXT);
	}

	protected void addNoCacheResponseHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		CwtRpcUtils.addNoCacheResponseHeaders(request, response);
	}
}
