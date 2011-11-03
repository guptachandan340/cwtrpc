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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultRpcAuthenticationFailureHandler extends
		AbstractRpcAuthenticationFailureHandler {
	private final Log log = LogFactory
			.getLog(DefaultRpcAuthenticationFailureHandler.class);

	public static Expression createExceptionExpression(
			Class<? extends Exception> exceptionClass, boolean includeMessage) {
		final StringBuilder expressionString;
		final ExpressionParser parser;
		final Expression expression;

		expressionString = new StringBuilder();
		expressionString.append("new ");
		expressionString.append(exceptionClass.getCanonicalName());
		expressionString.append('(');
		if (includeMessage) {
			expressionString.append("message");
		}
		expressionString.append(')');

		parser = new SpelExpressionParser();
		expression = parser.parseExpression(expressionString.toString());

		return expression;
	}

	public static Expression createExceptionExpression(
			Class<? extends Exception> exceptionClass) {
		return createExceptionExpression(exceptionClass, false);
	}

	private boolean includeExceptionMessage;

	private Expression defaultExceptionExpression;

	private Map<Class<? extends AuthenticationException>, Expression> exceptionExpressionMappings;

	public boolean isIncludeExceptionMessage() {
		return includeExceptionMessage;
	}

	public void setIncludeExceptionMessage(boolean includeExceptionMessage) {
		this.includeExceptionMessage = includeExceptionMessage;
	}

	public Expression getDefaultExceptionExpression() {
		return defaultExceptionExpression;
	}

	public void setDefaultExceptionExpression(
			Expression defaultExceptionExpression) {
		this.defaultExceptionExpression = defaultExceptionExpression;
	}

	public Map<Class<? extends AuthenticationException>, Expression> getExceptionExpressionMappings() {
		return exceptionExpressionMappings;
	}

	public void setExceptionExpressionMappings(
			Map<Class<? extends AuthenticationException>, Expression> exceptionExpressionMappings) {
		this.exceptionExpressionMappings = exceptionExpressionMappings;
	}

	protected Expression createDefaultExceptionExpression(
			Class<? extends Exception> exceptionClass) {
		return createExceptionExpression(exceptionClass,
				isIncludeExceptionMessage());
	}

	protected Map<Class<? extends AuthenticationException>, Expression> createDefaultExceptionExpressionMappings() {
		final Map<Class<? extends AuthenticationException>, Expression> mappings;

		mappings = new LinkedHashMap<Class<? extends AuthenticationException>, Expression>();
		for (Map.Entry<Class<? extends AuthenticationException>, Class<? extends Exception>> entry : SimpleRpcAuthenticationFailureHandler
				.createPackageExceptionClassMappings().entrySet()) {
			mappings.put(entry.getKey(),
					createDefaultExceptionExpression(entry.getValue()));
		}

		return mappings;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (getDefaultExceptionExpression() == null) {
			log.warn("Default exception expression has not been specified, using default");
			setDefaultExceptionExpression(createDefaultExceptionExpression(getPackageDefaultExceptionClass()));
		}
		if (getExceptionExpressionMappings() == null) {
			log.warn("Exception expression mappings have not been specified, using default");
			setExceptionExpressionMappings(createDefaultExceptionExpressionMappings());
		}
	}

	protected EvaluationContext createEvaluationContext(
			HttpServletRequest request, AuthenticationException exception) {
		final StandardEvaluationContext evaluationContext;

		evaluationContext = new StandardEvaluationContext(exception);
		evaluationContext.setVariable("request", request);

		return evaluationContext;
	}

	@Override
	public Exception lookupRemoteExceptionFor(HttpServletRequest request,
			AuthenticationException exception) {
		Expression remoteExceptionExpression = null;
		Exception remoteException = null;

		if (getExceptionExpressionMappings() != null) {
			final Class<? extends AuthenticationException> exceptionClass;

			exceptionClass = exception.getClass();
			for (Map.Entry<Class<? extends AuthenticationException>, Expression> entry : getExceptionExpressionMappings()
					.entrySet()) {
				if (entry.getKey().isAssignableFrom(exceptionClass)) {
					if (log.isDebugEnabled()) {
						log.debug("Exception mapping for class "
								+ exceptionClass.getName() + " is: "
								+ entry.getValue().getExpressionString());
					}
					remoteExceptionExpression = entry.getValue();
					break;
				}
			}
		}
		if (remoteExceptionExpression == null) {
			if (log.isDebugEnabled()) {
				log.debug("Exception mapping does not contain mapping for class "
						+ exception.getClass().getName()
						+ ", using default: "
						+ getDefaultExceptionExpression().getExpressionString());
			}
			remoteExceptionExpression = getDefaultExceptionExpression();
		}

		try {
			remoteException = remoteExceptionExpression.getValue(
					createEvaluationContext(request, exception),
					Exception.class);
		} catch (EvaluationException e) {
			log.error("Could not create remote exception from expression: "
					+ remoteExceptionExpression.getExpressionString(), e);
			remoteException = null;
		}
		return remoteException;
	}
}
