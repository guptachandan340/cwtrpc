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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.gwt.user.client.rpc.RpcRequestBuilder;

import de.itsvs.cwtrpc.core.RpcSessionInvalidationPolicy;
import de.itsvs.cwtrpc.core.CwtRpcUtils;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class CwtRpcUtilsTest {
	@Test
	public void testContainsStrongName1() {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getHeader(EasyMock
						.eq(RpcRequestBuilder.STRONG_NAME_HEADER)))
				.andReturn("ABC").atLeastOnce();
		EasyMock.replay(request);

		Assert.assertTrue(CwtRpcUtils.containsStrongName(request));

		EasyMock.verify(request);
	}

	@Test
	public void testContainsStrongName2() {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getHeader(EasyMock
						.eq(RpcRequestBuilder.STRONG_NAME_HEADER)))
				.andReturn(null).atLeastOnce();
		EasyMock.replay(request);

		Assert.assertFalse(CwtRpcUtils.containsStrongName(request));

		EasyMock.verify(request);
	}

	@Test
	public void testGetDefaultRpcSessionInvalidationPolicy() {
		RpcSessionInvalidationPolicy policy;

		policy = CwtRpcUtils.getDefaultRpcSessionInvalidationPolicy();
		Assert.assertFalse(policy.isInvalidateAfterInvocation());
		Assert.assertFalse(policy.isInvalidateOnExpectedException());
		Assert.assertFalse(policy.isInvalidateOnUnexpectedException());
	}

	@Test
	public void testSaveRpcSessionInvalidationPolicy() {
		RpcSessionInvalidationPolicy policy;
		HttpServletRequest request;

		policy = EasyMock.createMock(RpcSessionInvalidationPolicy.class);
		request = EasyMock.createMock(HttpServletRequest.class);
		request.setAttribute(EasyMock
				.eq(CwtRpcUtils.RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME),
				EasyMock.same(policy));

		EasyMock.replay(policy);
		EasyMock.replay(request);

		CwtRpcUtils.saveRpcSessionInvalidationPolicy(request, policy);

		EasyMock.verify(policy);
		EasyMock.verify(request);
	}

	@Test
	public void testIsRpcSessionInvalidationPolicySet1() {
		RpcSessionInvalidationPolicy policy;
		HttpServletRequest request;

		policy = EasyMock.createMock(RpcSessionInvalidationPolicy.class);
		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME)))
				.andReturn(policy).atLeastOnce();

		EasyMock.replay(policy);
		EasyMock.replay(request);

		Assert.assertTrue(CwtRpcUtils
				.isRpcSessionInvalidationPolicySet(request));

		EasyMock.verify(policy);
		EasyMock.verify(request);
	}

	@Test
	public void testIsRpcSessionInvalidationPolicySet2() {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME)))
				.andReturn(null).atLeastOnce();

		EasyMock.replay(request);

		Assert.assertFalse(CwtRpcUtils
				.isRpcSessionInvalidationPolicySet(request));

		EasyMock.verify(request);
	}

	@Test
	public void testGetRpcSessionInvalidationPolicy1() {
		RpcSessionInvalidationPolicy policy;
		HttpServletRequest request;

		policy = EasyMock.createMock(RpcSessionInvalidationPolicy.class);
		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME)))
				.andReturn(policy).atLeastOnce();

		EasyMock.replay(policy);
		EasyMock.replay(request);

		Assert.assertSame(policy,
				CwtRpcUtils.getRpcSessionInvalidationPolicy(request));

		EasyMock.verify(policy);
		EasyMock.verify(request);
	}

	@Test
	public void testGetRpcSessionInvalidationPolicy2() {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.RPC_SESSION_INVALIDATION_POLICY_ATTR_NAME)))
				.andReturn(null).atLeastOnce();

		EasyMock.replay(request);

		Assert.assertSame(CwtRpcUtils.getDefaultRpcSessionInvalidationPolicy(),
				CwtRpcUtils.getRpcSessionInvalidationPolicy(request));

		EasyMock.verify(request);
	}

	@Test
	public void testAddNoCacheResponseHeaders() {
		HttpServletRequest request;
		HttpServletResponse response;

		request = EasyMock.createMock(HttpServletRequest.class);
		response = EasyMock.createMock(HttpServletResponse.class);

		response.setHeader(EasyMock.eq("Cache-Control"),
				EasyMock.eq("no-cache, no-store, no-transform"));
		response.setDateHeader(EasyMock.eq("Expires"), EasyMock.eq(-1L));
		response.setHeader(EasyMock.eq("Pragma"), EasyMock.eq("no-cache"));

		EasyMock.replay(request);
		EasyMock.replay(response);

		CwtRpcUtils.addNoCacheResponseHeaders(request, response);

		EasyMock.verify(request);
		EasyMock.verify(response);
	}

	@Test
	public void testIsContentRead1() {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.GWT_RPC_REQUEST_CONTENT_ATTR_NAME)))
				.andReturn("abc").atLeastOnce();

		EasyMock.replay(request);

		Assert.assertTrue(CwtRpcUtils.isContentRead(request));

		EasyMock.verify(request);
	}

	@Test
	public void testIsContentRead2() {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.GWT_RPC_REQUEST_CONTENT_ATTR_NAME)))
				.andReturn(null).atLeastOnce();

		EasyMock.replay(request);

		Assert.assertFalse(CwtRpcUtils.isContentRead(request));

		EasyMock.verify(request);
	}

	@Test
	public void testReadContent1() throws IOException, ServletException {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.checkOrder(request, true);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.GWT_RPC_REQUEST_CONTENT_ATTR_NAME)))
				.andReturn(null).atLeastOnce();
		EasyMock.expect(
				request.getHeader(EasyMock
						.eq(RpcRequestBuilder.STRONG_NAME_HEADER)))
				.andReturn("ABC").atLeastOnce();
		EasyMock.checkOrder(request, false);
		EasyMock.expect(request.getContentType()).andReturn("text/x-gwt-rpc")
				.atLeastOnce();
		EasyMock.expect(request.getCharacterEncoding()).andReturn("UTF-8")
				.anyTimes();
		EasyMock.expect(request.getInputStream())
				.andAnswer(new IAnswer<ServletInputStream>() {
					public ServletInputStream answer() throws Throwable {
						return new TestServletInputStream(
								new ByteArrayInputStream("abc xyz"
										.getBytes("UTF-8")));
					}
				}).once();
		EasyMock.checkOrder(request, true);
		request.setAttribute(
				EasyMock.eq(CwtRpcUtils.GWT_RPC_REQUEST_CONTENT_ATTR_NAME),
				EasyMock.eq("abc xyz"));

		EasyMock.replay(request);

		Assert.assertEquals("abc xyz", CwtRpcUtils.readContent(request));

		EasyMock.verify(request);
	}

	@Test
	public void testReadContent2() throws IOException, ServletException {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.GWT_RPC_REQUEST_CONTENT_ATTR_NAME)))
				.andReturn("abc def xyz").atLeastOnce();

		EasyMock.replay(request);

		Assert.assertEquals("abc def xyz", CwtRpcUtils.readContent(request));

		EasyMock.verify(request);
	}

	@Test(expected = SecurityException.class)
	public void testReadContent3() throws IOException, ServletException {
		HttpServletRequest request;

		request = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.checkOrder(request, true);
		EasyMock.expect(
				request.getAttribute(EasyMock
						.eq(CwtRpcUtils.GWT_RPC_REQUEST_CONTENT_ATTR_NAME)))
				.andReturn(null).atLeastOnce();
		EasyMock.expect(
				request.getHeader(EasyMock
						.eq(RpcRequestBuilder.STRONG_NAME_HEADER)))
				.andReturn(null).atLeastOnce();

		EasyMock.replay(request);

		CwtRpcUtils.readContent(request);
	}

	protected static class TestServletInputStream extends ServletInputStream {
		public InputStream inputStream;

		public TestServletInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public int read() throws IOException {
			return inputStream.read();
		}
	}
}
