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

import java.util.Map;

/**
 * Base class of all kinds of incoming requests
 */
public abstract class Request extends Message {
	Request(Map map) throws InvalidMessageException {
		super(map);
		checkIsValid();
	}

	public Map toMap() {
		return super.toMap();
	}

	boolean isSigned() {
		return getHash() != null;
	}

	boolean signatureVerifies(Client c) {
		return verifySignature(c.getSecret());
	}

	String getNonce() {
		return (String) map.get(Constants.NONCE);
	}

	String getOperation() {
		return (String) map.get(Constants.OPERATION);
	}

	String getIdentifier() {
		return (String) map.get(Constants.IDENTIFIER);
	}

	String getOtp() {
		return (String) map.get(Constants.OTP);
	}

	String get(String key) {
		return (String) map.get(key);
	}

	public abstract void checkIsValid() throws InvalidMessageException;

	public abstract Response process();

	public String toString() {
		return "[Request " + super.toString() + "]";
	}
}
