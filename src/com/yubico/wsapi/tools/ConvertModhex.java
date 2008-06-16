/*
 * Copyright 2008 Yubico
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 *
 * You may obtain a copy of the License at 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */
package com.yubico.wsapi.tools;

import com.yubico.wsapi.Secret;
import com.yubico.wsapi.Crypto;

public class ConvertModhex {
	public static void main(String[] argv) throws Exception {
		Secret s = Secret.fromModHex(argv[0]);
		System.out.println("Hex: " + toString(s.toBytes()));
		System.out.println("Base64: " + Crypto.toString(s.toBytes()));
	}

	static String toString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i += 1) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(Integer.toHexString(b[i] & 0xFF));
		}
		return sb.toString();
	}
}
