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

package de.itsvs.cwtrpc.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gwt.user.server.rpc.RPCServletUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class UnexpectedErrorFilter implements Filter {
	private final Log log = LogFactory.getLog(UnexpectedErrorFilter.class);

	private ServletContext servletContext;

	public ServletContext getServletContext() {
		return servletContext;
	}

	protected void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void init(FilterConfig config) throws ServletException {
		setServletContext(config.getServletContext());
	}

	public void destroy() {
		/* nothing to be done */
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			processUnexpectedFailure((HttpServletRequest) request,
					(HttpServletResponse) response, e);
		}
	}

	protected void processUnexpectedFailure(HttpServletRequest request,
			HttpServletResponse response, Throwable e) {
		log.error("Unexpected error while processing service request", e);

		if (!response.isCommitted()) {
			response.reset();
			/*
			 * Since unexpected failure does a non cachable status code, we do
			 * not need to initialize caching.
			 */
			RPCServletUtils.writeResponseForUnexpectedFailure(
					getServletContext(), response, e);
		}
	}

}
