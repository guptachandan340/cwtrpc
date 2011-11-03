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

package de.itsvs.cwtrpc.core;

import javax.servlet.ServletContext;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class ExtendedContextLoaderListener extends ContextLoaderListener {
	@Override
	public WebApplicationContext initWebApplicationContext(
			ServletContext servletContext) {
		CwtRpcUtils.preloadContextClasses(servletContext);
		return super.initWebApplicationContext(servletContext);
	}
}
