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

import org.apache.log4j.Logger;
/**
 * 
 * Each Client has its permissions
 *
 */
public class Perms {
	static Logger log = Logger.getLogger(Perms.class);

	private boolean verifyOtp;

	private boolean addClients;

	private boolean deleteClients;

	private boolean addKeys;

	private boolean deleteKeys;

	// static Perms defaultPerms = new Perms(true, false, false, true, true);
	static Perms nullPerms = new Perms(false, false, false, false, false);

	Perms(boolean verifyOtp, boolean addClients, boolean deleteClients,
			boolean addKeys, boolean deleteKeys) {
		this.verifyOtp = verifyOtp;
		this.addClients = addClients;
		this.deleteClients = deleteClients;
		this.addKeys = addKeys;
		this.deleteKeys = deleteKeys;
	}

	public boolean getVerifyOtp() {
		return verifyOtp;
	}

	public boolean getAddClients() {
		return addClients;
	}

	public boolean getDeleteClients() {
		return deleteClients;
	}

	public boolean getAddKeys() {
		return addKeys;
	}

	public boolean getDeleteKeys() {
		return deleteKeys;
	}

	/**
	 * Returns a perms if the values are syntactically correct.
	 */
	static Perms checkValidPerms(String verifyOtp, String addClient,
			String deleteClient, String addKey, String deleteKey) {
		log.debug("checkValidPerms " + verifyOtp + ", " + addClient + ", "
				+ deleteClient + ", " + addKey + ", " + deleteKey);

		boolean vo = false, ac = false, dc = false, ak = false, dk = false;
		int n = 0;
		if ((null == verifyOtp) || ("true".equals(verifyOtp))
				|| ("false".equals(verifyOtp))) {
			n += 1;
			vo = "true".equals(verifyOtp);
		}
		if ((null == addClient) || ("true".equals(addClient))
				|| ("false".equals(addClient))) {
			n += 1;
			ac = "true".equals(addClient);
		}
		if ((null == deleteClient) || ("true".equals(deleteClient))
				|| ("false".equals(deleteClient))) {
			n += 1;
			dc = "true".equals(deleteClient);
		}
		if ((null == addKey) || ("true".equals(addKey))
				|| ("false".equals(addKey))) {
			n += 1;
			ak = "true".equals(addKey);
		}
		if ((null == deleteKey) || ("true".equals(deleteKey))
				|| ("false".equals(deleteKey))) {
			n += 1;
			dk = "true".equals(deleteKey);
		}
		if (n == 5) {
			return new Perms(vo, ac, dc, ak, dk);
		} else {
			return null;
		}
	}

	public boolean check(Request request) {
		log.info("Checking " + request + " against " + this);

		if (request instanceof VerificationRequest) {
			return verifyOtp;
		} else if (request instanceof AddClientRequest) {
			return addClients;
		} else if (request instanceof AddKeyRequest) {
			return addKeys;
		} else if (request instanceof DeleteKeyRequest) {
			return deleteKeys;
		} else {
			log.warn("Unhandled request " + request + " in perms check");
		}
		return false;
	}

	/**
	 * Checks whether this perms is allowed by another perms. This perms is
	 * allowed if it is a subset of the other perms.
	 */
	public boolean allowedBy(Perms perms) {
		log.info("Checking " + this + " against " + perms);

		if (getVerifyOtp() && !perms.getVerifyOtp()) {
			return false;
		}
		if (getAddClients() && !perms.getAddClients()) {
			return false;
		}
		if (getDeleteClients() && !perms.getDeleteClients()) {
			return false;
		}
		if (getAddKeys() && !perms.getAddKeys()) {
			return false;
		}
		if (getDeleteKeys() && !perms.getDeleteKeys()) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "[Perms verify otp=" + verifyOtp + ", add clients=" + addClients
				+ ", delete clients=" + deleteClients + ", add keys=" + addKeys
				+ ", delete keys=" + deleteKeys + "]";
	}
}
