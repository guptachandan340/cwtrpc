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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.itsvs.cwtrpc.core.pattern.Pattern;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class AutowiredRemoteServiceGroupConfig extends RemoteServiceGroupConfig
		implements ApplicationContextAware, InitializingBean {
	private ApplicationContext applicationContext;

	private Collection<String> basePackages = Collections.emptyList();

	private Collection<Pattern> includeFilters = Collections.emptyList();

	private Collection<Pattern> excludeFilters = Collections.emptyList();

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Collection<String> getBasePackages() {
		return basePackages;
	}

	public void setBasePackages(Collection<String> basePackages) {
		this.basePackages = basePackages;
	}

	public Collection<Pattern> getIncludeFilters() {
		return includeFilters;
	}

	public void setIncludeFilters(Collection<Pattern> includeFilters) {
		this.includeFilters = includeFilters;
	}

	public Collection<Pattern> getExcludeFilters() {
		return excludeFilters;
	}

	public void setExcludeFilters(Collection<Pattern> excludeFilters) {
		this.excludeFilters = excludeFilters;
	}

	public void afterPropertiesSet() {
		final Log log;
		final ListableBeanFactory beanFactory;
		final String[] beanNames;
		final List<RemoteServiceConfig> serviceConfigs;

		log = LogFactory.getLog(AutowiredRemoteServiceGroupConfig.class);
		log.debug("Searching for remote services");

		beanFactory = getApplicationContext();
		beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				beanFactory, RemoteService.class, true, false);
		if (beanNames.length == 0) {
			return;
		}

		serviceConfigs = new ArrayList<RemoteServiceConfig>();
		for (String beanName : beanNames) {
			final Class<?> beanType;
			final Class<?> serviceInterface;

			beanType = beanFactory.getType(beanName);
			serviceInterface = getRemoteServiceInterface(beanType);
			if (serviceInterface != null) {
				if (serviceInterface
						.getAnnotation(RemoteServiceRelativePath.class) != null) {
					if (isAcceptedServiceInterface(serviceInterface)) {
						if (log.isDebugEnabled()) {
							log.debug("Adding service '" + beanName + "'");
						}
						serviceConfigs.add(new RemoteServiceConfig(beanName));
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Ignoring service '" + beanName
								+ "' since service does not specify "
								+ "remote service relative path");
					}
				}
			} else {
				if (log.isInfoEnabled()) {
					log.info("Ignoring service '" + beanName
							+ "' since it implements multiple "
							+ "remote service interfaces");
				}
			}
		}

		setServiceConfigs(serviceConfigs);
	}

	protected Class<?> getRemoteServiceInterface(Class<?> beanType) {
		Class<?> foundInterface = null;

		for (Class<?> c : beanType.getInterfaces()) {
			if (RemoteService.class.isAssignableFrom(c)) {
				if (foundInterface != null) {
					/* interface must be unique */
					return null;
				}
				foundInterface = c;
			}
		}

		return foundInterface;
	}

	protected boolean isAcceptedServiceInterface(Class<?> serviceInterface) {
		final String className;

		className = serviceInterface.getName();

		/* if no base package has been specified, all packages are accepted */
		if ((getBasePackages() != null) && !getBasePackages().isEmpty()) {
			boolean found = false;

			for (String basePackage : getBasePackages()) {
				if (className.startsWith(basePackage)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		/* if no include filter has been specified, all are included */
		if ((getIncludeFilters() != null) && !getIncludeFilters().isEmpty()) {
			boolean found = false;

			for (Pattern filter : getIncludeFilters()) {
				if (filter.matches(className)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		if ((getExcludeFilters() != null) && !getExcludeFilters().isEmpty()) {
			for (Pattern filter : getExcludeFilters()) {
				if (filter.matches(className)) {
					return false;
				}
			}
		}

		return true;
	}
}
