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

import javax.servlet.http.HttpServletRequest;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.gwt.user.server.rpc.SerializationPolicy;

import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.ExtendedSerializationPolicyProviderDelegate;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class ExtendedSerializationPolicyProviderDelegateTest {
	@Test
	public void test1() {
		ExtendedSerializationPolicyProvider provider;
		HttpServletRequest request;
		SerializationPolicy policy;
		ExtendedSerializationPolicyProviderDelegate delegate;

		provider = EasyMock
				.createMock(ExtendedSerializationPolicyProvider.class);
		request = EasyMock.createMock(HttpServletRequest.class);
		policy = EasyMock.createMock(SerializationPolicy.class);

		EasyMock.expect(
				provider.getSerializationPolicy(EasyMock.same(request),
						EasyMock.eq("http://demo/xyz/def/"),
						EasyMock.eq("h83fnddf"))).andReturn(policy).once();

		EasyMock.replay(provider);
		EasyMock.replay(request);
		EasyMock.replay(policy);

		delegate = new ExtendedSerializationPolicyProviderDelegate(provider,
				request);
		Assert.assertSame(provider, delegate.getProvider());
		Assert.assertSame(request, delegate.getRequest());

		Assert.assertSame(policy, delegate.getSerializationPolicy(
				"http://demo/xyz/def/", "h83fnddf"));

		EasyMock.verify(provider);
		EasyMock.verify(request);
		EasyMock.verify(policy);
	}
}
