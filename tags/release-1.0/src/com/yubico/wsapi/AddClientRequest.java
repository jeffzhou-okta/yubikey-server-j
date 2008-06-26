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

/** 
 * The request to add a Client (a YubiKey issuer). If you run your own web site and 
 * you issue Yubikeys to your users from there, you need only 1 Client in the database, 
 * that's your company itself.
 *  
 * @see Class AddClientResponse
 */

public class AddClientRequest extends Request {
	private final static Logger log = Logger.getLogger(AddClientRequest.class);

	String getEmail() {
		return (String) map.get(Constants.EMAIL);
	}

	String getNonce() {
		return (String) map.get(Constants.NONCE);
	}

	String getVerifyOtpPerm() {
		return (String) map.get(Constants.VERIFY_OTP);
	}

	String getAddClientsPerm() {
		return (String) map.get(Constants.ADD_CLIENT);
	}

	String getDeleteClientsPerm() {
		return (String) map.get(Constants.DELETE_CLIENT);
	}

	String getAddKeysPerm() {
		return (String) map.get(Constants.ADD_KEY);
	}

	String getDeleteKeysPerm() {
		return (String) map.get(Constants.DELETE_KEY);
	}

	private Perms perms;

	public void checkIsValid() throws InvalidMessageException {
		if (getOperation() == null) {
			throw new InvalidMessageException(Constants.E_MISSING_PARAMETER,
					Constants.OPERATION);
		}
		if (getIdentifier() == null) {
			throw new InvalidMessageException(Constants.E_MISSING_PARAMETER,
					Constants.IDENTIFIER);
		}
		if (getNonce() == null) {
			throw new InvalidMessageException(Constants.E_MISSING_PARAMETER,
					Constants.NONCE);
		}
		this.perms = Perms.checkValidPerms(getVerifyOtpPerm(),
				getAddClientsPerm(), getDeleteClientsPerm(), getAddKeysPerm(),
				getDeleteKeysPerm());
		if (perms == null) {
			throw new InvalidMessageException(Constants.E_INCORRECT_PARAMETER,
					Constants.PERMISSIONS);
		}
	}

	public AddClientRequest(Map map) throws InvalidMessageException {
		super(map);
	}

	public Response process() {
		String id = getIdentifier();
		Client c = Client.lookup(id);
		if (c == null) {
			log.info("no client '" + id + "' available");
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_NO_SUCH_CLIENT);
			return AddClientResponse.create(map);
		}

		if (!isSigned() || !signatureVerifies(c)) {
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_BAD_SIGNATURE);
			return AddClientResponse.create(map);
		}

		if (!c.checkPerms(this)) {
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_OPERATION_NOT_ALLOWED);
			return AddClientResponse.create(c, map);
		}

		if (!perms.allowedBy(c.getPerms())) {
			Map map = new HashMap();
			map.put(Constants.STATUS, Constants.E_OPERATION_NOT_ALLOWED);
			return AddClientResponse.create(c, map);
		}

		String email = getEmail();
		Secret secret = c.createSecret(email);

		Client newClient = new Client(new java.util.Date(), true, email,
				secret, perms);
		Map map = new HashMap();
		map.put(Constants.OPERATION, getOperation());
		map.put(Constants.NONCE, getNonce());
		map.put(Constants.SHARED_SECRET, Crypto.toString(newClient.getSecret()
				.toBytes()));
		try {
			String clientId = Database.getDefault().addClient(newClient);
			map.put(Constants.STATUS, Constants.OK);
			map.put(Constants.CLIENT_ID, clientId);
		} catch (SQLException e) {
			log.warn(e);
			map.put(Constants.STATUS, Constants.E_BACKEND_ERROR);
		}
		return AddClientResponse.create(c, map);
	}

	public String toString() {
		return "[AddClientRequest " + super.toString() + "]";
	}
}
