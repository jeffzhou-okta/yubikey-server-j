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

import org.apache.log4j.Logger;

/** 
 * The response to an AddClientRequest
 * @see Class AddClientRequest
 */
public class AddClientResponse extends Response {
	static Logger log = Logger.getLogger(AddClientResponse.class);

	AddClientResponse(Map map) {
		super(map);
	}

	static AddClientResponse create(Map map) {
		return create(null, map);
	}

	public String getNonce() {
		return (String) map.get(Constants.NONCE);
	}

	public String getClientId() {
		return (String) map.get(Constants.CLIENT_ID);
	}

	public String getSharedSecret() {
		return (String) map.get(Constants.SHARED_SECRET);
	}

	static AddClientResponse create(Client signer, Map map) {
		AddClientResponse r = new AddClientResponse(map);
		if (signer != null) {
			String ts = DateUtils.getTimeStamp();
			r.putTimestamp(ts);
			r.sign(signer.getSecret());
		}
		return r;
	}

	public String toString() {
		return "[AddClientResponse " + super.toString() + "]";
	}
}
