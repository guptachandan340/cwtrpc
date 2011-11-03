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

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;

import de.itsvs.cwtrpc.core.DefaultExtendedSerializationPolicyProvider;
import de.itsvs.cwtrpc.core.CwtRpcException;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class DefaultExtendedSerializationPolicyProviderTest {
	@Test
	public void testGetModuleBasePath1() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		Assert.assertEquals("/testapp/testmod/", provider.getModuleBasePath(
				request, "https://160.56.27.220/testapp/testmod/"));

		EasyMock.verify(request);
	}

	@Test(expected = IncompatibleRemoteServiceException.class)
	public void testGetModuleBasePath2() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.getModuleBasePath(request,
				":https:xyz////160.56.27.220:abc/testapp/testmod/");
	}

	@Test
	public void testGetRelativeModuleBasePath1() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		Assert.assertEquals("/testmod/", provider.getRelativeModuleBasePath(
				request, "/testapp/testmod/"));

		EasyMock.verify(request);
	}

	@Test
	public void testGetRelativeModuleBasePath2() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("").atLeastOnce();

		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		Assert.assertEquals("/testmod/",
				provider.getRelativeModuleBasePath(request, "/testmod/"));

		EasyMock.verify(request);
	}

	@Test(expected = IncompatibleRemoteServiceException.class)
	public void testGetRelativeModuleBasePath4() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.getRelativeModuleBasePath(request, "/testapp2/testmod/");
	}

	@Test(expected = IncompatibleRemoteServiceException.class)
	public void testGetRelativeModuleBasePath5() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp2")
				.atLeastOnce();

		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.getRelativeModuleBasePath(request, "/testapp/testmod/");
	}

	@Test(expected = SecurityException.class)
	public void testGetRelativeModuleBasePath6() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.getRelativeModuleBasePath(request, "/testapp/testmod/../app/");
	}

	@Test(expected = IncompatibleRemoteServiceException.class)
	public void testGetRelativeModuleBasePath7() {
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.getRelativeModuleBasePath(request, "/testapp/testmod");
	}

	@Test
	public void testGetPolicyPath1() {
		DefaultExtendedSerializationPolicyProvider provider;

		provider = new DefaultExtendedSerializationPolicyProvider();
		Assert.assertEquals(
				"/testmod/CA2D1779E3A55A9700F6B99DD75BFFXY.gwt.rpc", provider
						.getPolicyPath("/testmod/",
								"CA2D1779E3A55A9700F6B99DD75BFFXY"));
	}

	@Test(expected = SecurityException.class)
	public void testGetPolicyPath2() {
		DefaultExtendedSerializationPolicyProvider provider;

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.getPolicyPath("/testmod/",
				"../CA2D1779E3A55A9700F6B99DD75BFFXY");
	}

	@Test
	public void testLoadSerializationPolicy1() throws Exception {
		ServletContext servletContext;
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;
		SerializationPolicy policy;
		TypeNameObfuscator standardPolicy;

		servletContext = EasyMock.createMock(ServletContext.class);
		EasyMock.expect(
				servletContext.getResourceAsStream(EasyMock
						.eq("/testmod/CA2D1779E3A55A9700F6B99DD75BFFF4.gwt.rpc")))
				.andAnswer(new IAnswer<InputStream>() {
					public InputStream answer() throws Throwable {
						return getClass().getResourceAsStream(
								"/de/itsvs/cwtrpc/core/"
										+ "CA2D1779E3A55A9700F6B99DD75BFFF4"
										+ ".gwt.rpc");
					}
				}).once();
		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(servletContext);
		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.setServletContext(servletContext);
		provider.afterPropertiesSet();
		policy = provider.loadSerializationPolicy(request, "/testapp/testmod/",
				"CA2D1779E3A55A9700F6B99DD75BFFF4");
		Assert.assertTrue(policy instanceof TypeNameObfuscator);

		standardPolicy = (TypeNameObfuscator) policy;
		Assert.assertEquals("java.lang.String/8004016611",
				standardPolicy.getTypeIdForClass(String.class));

		EasyMock.verify(servletContext);
		EasyMock.verify(request);
	}

	@Test
	public void testLoadSerializationPolicy2() throws Exception {
		ServletContext servletContext;
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		servletContext = EasyMock.createMock(ServletContext.class);
		EasyMock.expect(
				servletContext.getResourceAsStream(EasyMock
						.eq("/testmod/CA2D1779E3A55A9700F6B99DD75BFFF4.gwt.rpc")))
				.andReturn(null).once();
		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(servletContext);
		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.setServletContext(servletContext);
		provider.afterPropertiesSet();
		try {
			provider.loadSerializationPolicy(request, "/testapp/testmod/",
					"CA2D1779E3A55A9700F6B99DD75BFFF4");
			Assert.fail("Exception expected since file does not exist");
		} catch (IncompatibleRemoteServiceException e) {
			/* expected exception */
		}

		EasyMock.verify(servletContext);
		EasyMock.verify(request);
	}

	@Test
	public void testLoadSerializationPolicy3() throws Exception {
		ServletContext servletContext;
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;

		servletContext = EasyMock.createMock(ServletContext.class);
		EasyMock.expect(
				servletContext.getResourceAsStream(EasyMock
						.eq("/testmod/ERROR779E3A55A9700F6B99DD75ERROR.gwt.rpc")))
				.andAnswer(new IAnswer<InputStream>() {
					public InputStream answer() throws Throwable {
						return getClass().getResourceAsStream(
								"/de/itsvs/cwtrpc/core/"
										+ "ERROR779E3A55A9700F6B99DD75ERROR"
										+ ".gwt.rpc");
					}
				}).once();
		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(servletContext);
		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.setServletContext(servletContext);
		provider.afterPropertiesSet();
		try {
			provider.loadSerializationPolicy(request, "/testapp/testmod/",
					"ERROR779E3A55A9700F6B99DD75ERROR");
			Assert.fail("Exception expected since policy file is invalid");
		} catch (CwtRpcException e) {
			/* expected exception */
		}

		EasyMock.verify(servletContext);
		EasyMock.verify(request);
	}

	@Test
	public void testGetSerializationPolicy1() throws Exception {
		ServletContext servletContext;
		HttpServletRequest request;
		DefaultExtendedSerializationPolicyProvider provider;
		SerializationPolicy policy1;
		SerializationPolicy policy2;

		servletContext = EasyMock.createMock(ServletContext.class);
		EasyMock.expect(
				servletContext.getResourceAsStream(EasyMock
						.eq("/testmod/CA2D1779E3A55A9700F6B99DD75BFFF4.gwt.rpc")))
				.andAnswer(new IAnswer<InputStream>() {
					public InputStream answer() throws Throwable {
						return getClass().getResourceAsStream(
								"/de/itsvs/cwtrpc/core/"
										+ "CA2D1779E3A55A9700F6B99DD75BFFF4"
										+ ".gwt.rpc");
					}
				}).once();
		EasyMock.expect(
				servletContext.getResourceAsStream(EasyMock
						.eq("/test2mod/CA2D1779E3A55A9700F6B99DD75BFFF4.gwt.rpc")))
				.andAnswer(new IAnswer<InputStream>() {
					public InputStream answer() throws Throwable {
						return getClass().getResourceAsStream(
								"/de/itsvs/cwtrpc/core/"
										+ "CA2D1779E3A55A9700F6B99DD75BFFF4"
										+ ".gwt.rpc");
					}
				}).once();
		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/testapp")
				.atLeastOnce();

		EasyMock.replay(servletContext);
		EasyMock.replay(request);

		provider = new DefaultExtendedSerializationPolicyProvider();
		provider.setServletContext(servletContext);
		provider.afterPropertiesSet();

		policy1 = provider.getSerializationPolicy(request,
				"https://160.56.27.220/testapp/testmod/",
				"CA2D1779E3A55A9700F6B99DD75BFFF4");
		Assert.assertNotNull(policy1);

		policy2 = provider.getSerializationPolicy(request,
				"https://160.56.27.220/testapp/test2mod/",
				"CA2D1779E3A55A9700F6B99DD75BFFF4");
		Assert.assertNotSame(policy1, policy2);

		policy2 = provider.getSerializationPolicy(request,
				"https://160.56.27.220/testapp/testmod/",
				"CA2D1779E3A55A9700F6B99DD75BFFF4");
		Assert.assertSame(policy1, policy2);

		policy2 = provider.getSerializationPolicy(request,
				"https://demo:8989/testapp/testmod/",
				"CA2D1779E3A55A9700F6B99DD75BFFF4");
		Assert.assertSame(policy1, policy2);

		EasyMock.verify(servletContext);
		EasyMock.verify(request);
	}
}
