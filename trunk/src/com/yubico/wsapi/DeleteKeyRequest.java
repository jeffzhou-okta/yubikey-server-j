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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DeleteKeyRequest extends KeyRequest {
	private final static Logger log = Logger.getLogger(DeleteKeyRequest.class);

	public DeleteKeyRequest(Map map) throws InvalidMessageException {
		super(map);
	}

	public Response process() {
		String id = getIdentifier();
		Client c = Client.lookup(id);
		if (c == null) {
			log.info("no client '" + id + "' available");
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_NO_SUCH_CLIENT);
			return DeleteKeyResponse.create(map);
		}

		if (!isSigned() || !signatureVerifies(c)) {
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_BAD_SIGNATURE);
			return DeleteKeyResponse.create(map);
		}

		if (!c.checkPerms(this)) {
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_OPERATION_NOT_ALLOWED);
			return DeleteKeyResponse.create(map);
		}

		String keyId = getKeyId();
		Yubikey yk = Yubikey.lookup(keyId);
		if (yk == null) {
			log.info("no yubikey '" + keyId + "' available");
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_NO_SUCH_YUBIKEY);
			return DeleteKeyResponse.create(map);
		}

		if (!(id.equals(yk.getClientId()))) {
			log.info("client '" + id + "' is not owner of key '" + keyId + "'");
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_OPERATION_NOT_ALLOWED);
			return DeleteKeyResponse.create(map);
		}

		try {
			Database.getDefault().deleteKey(yk);
			map.put(Constants.STATUS, Constants.OK);
		} catch (SQLException e) {
			log.warn("while deleting key " + keyId);
			log.warn(e);
			map.put(Constants.STATUS, Constants.E_BACKEND_ERROR);
		}
		return DeleteKeyResponse.create(c, map);
	}

	public String toString() {
		return "[DeleteKeyRequest " + super.toString() + "]";
	}
}
