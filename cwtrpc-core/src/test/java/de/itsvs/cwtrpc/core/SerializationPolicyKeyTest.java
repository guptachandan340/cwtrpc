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

import org.junit.Assert;
import org.junit.Test;

import de.itsvs.cwtrpc.core.DefaultExtendedSerializationPolicyProvider.SerializationPolicyKey;

/**
 * @author Volker Schmidt
 * @since 0.9
 */
public class SerializationPolicyKeyTest {
	@Test
	public void test1() {
		SerializationPolicyKey key1;
		SerializationPolicyKey key2;

		key1 = new SerializationPolicyKey("/test1/test2", "xyzabc");
		key2 = new SerializationPolicyKey("/test1/test2", "xyzabc");

		Assert.assertEquals(key1, key1);
		Assert.assertEquals(key1, key2);
		Assert.assertEquals(key1.hashCode(), key2.hashCode());
	}

	@Test
	public void test2() {
		SerializationPolicyKey key1;
		SerializationPolicyKey key2;

		key1 = new SerializationPolicyKey("/test1/test2", "xyzabc");
		key2 = new SerializationPolicyKey("/test1/test3", "xyzabc");

		Assert.assertFalse(key1.equals(key2));
	}

	@Test
	public void test3() {
		SerializationPolicyKey key1;
		SerializationPolicyKey key2;

		key1 = new SerializationPolicyKey("/test1/test2", "xyzabc");
		key2 = new SerializationPolicyKey("/test1/test2", "xyzabd");

		Assert.assertFalse(key1.equals(key2));
	}
}
