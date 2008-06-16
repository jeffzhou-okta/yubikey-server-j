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
package com.yubico.wsapi;

import com.yubico.base.Modhex;
import java.io.UnsupportedEncodingException;

/**
 * 
 *
 */
public class Secret {
	private byte[] secret;

	public Secret(byte[] secret) {
		this.secret = secret;
	}

	public static Secret fromAscii(String s) throws IllegalArgumentException {
		try {
			return new Secret(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static Secret fromBase64(String s) {
		return new Secret(Crypto.toBytes(s));
	}

	public static Secret fromModHex(String s) {
		return new Secret(Modhex.decode(s));
	}

	public static Secret createRandom() {
		byte[] b = new byte[16];
		return new Secret(Crypto.createRandom(b));
	}

	public byte[] toBytes() {
		return secret;
	}

	public String toString() {
		return "[Secret key=" + Crypto.toString(secret) + "]";
	}
}
