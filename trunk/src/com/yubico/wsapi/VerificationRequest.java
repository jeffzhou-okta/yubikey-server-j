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
 * 
 * OTP verification request
 * 
 */
public class VerificationRequest extends Request {
	private final static Logger log = Logger
			.getLogger(VerificationRequest.class);

	public VerificationRequest(Map map) throws InvalidMessageException {
		super(map);
	}

	public Response process() {
		String id = getIdentifier();
		Client c = Client.lookup(id);
		if (c == null) {
			log.info("no such client=" + id);
			return VerificationResponse.create(c, Constants.E_NO_SUCH_CLIENT);
		}
		if (!c.checkPerms(this)) {
			return VerificationResponse.create(c,
					Constants.E_OPERATION_NOT_ALLOWED);
		}

		if (isSigned() && (!signatureVerifies(c))) {
			return VerificationResponse.create(c, Constants.E_BAD_SIGNATURE);
		}

		int ksStatus = KeySubsystem.checkOtp(getOtp());
		String status = Constants.E_UNKNOWN_ERROR;
		if (ksStatus == KeySubsystem.OK) {
			status = Constants.OK;
		} else if (ksStatus == KeySubsystem.REPLAY_ERROR) {
			status = Constants.E_REPLAYED_OTP;
		} else if (ksStatus == KeySubsystem.FORMAT_ERROR) {
			status = Constants.E_BAD_OTP;
		} else if (ksStatus == KeySubsystem.OTHER_ERROR) {
			status = Constants.E_BAD_OTP;
		}

		return VerificationResponse.create(c, status);
	}

	/**
	 * Check if the request is valid
	 */
	public void checkIsValid() throws InvalidMessageException {
		if (getOtp() == null) {
			throw new InvalidMessageException(Constants.E_MISSING_PARAMETER,
					Constants.OTP);
		}
		if (getIdentifier() == null) {
			throw new InvalidMessageException(Constants.E_MISSING_PARAMETER,
					Constants.IDENTIFIER);
		}
	}

	/**
	 * Sign the response to send back to client
	 */
	public void sign(Secret secret) {
		String ts = DateUtils.getTimeStamp();
		map.put(Constants.TIMESTAMP, ts);
		super.sign(secret);
	}

	public String toString() {
		return "[VerificationRequest " + super.toString() + "]";
	}
}
