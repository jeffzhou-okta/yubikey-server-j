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
import org.apache.log4j.Logger;
import com.yubico.base.Token;
import com.yubico.base.Pof;
import java.security.GeneralSecurityException;

public class KeySubsystem {
	private final static Logger log = Logger.getLogger(KeySubsystem.class);

	public static final int OK = 1;

	public static final int REPLAY_ERROR = 2;

	public static final int FORMAT_ERROR = 3;

	public static final int OTHER_ERROR = 4;

	/**
	 * Expect otp to be > 32 length. 
	 * It consists of token id + the random otp itself
	 * @return OK(1) or error codes(>1)
	 */
	static int checkOtp(String otp) {
		otp = otp.toLowerCase();
		int len = otp.length();
		if (len <= 32) {
			log.info("OTP too short " + otp);
			return FORMAT_ERROR;
		}
		int split = len - 32;
		try {
			return checkOtp(Modhex.decode(otp.substring(0, split)), otp
					.substring(split));
		} catch (Exception e) {
			log.info("While checking otp=" + otp);
			log.info(e);
			return OTHER_ERROR;
		}
	}

	/**
	 * @return OK(1) or error codes(>1)
	 */
	static int checkOtp(byte[] tokenId, String otp) throws Exception {
		Database db = Database.getDefault();
		Yubikey yubikey = db.getYubikeyOnTokenId(tokenId);

		byte[] key = yubikey.getSecret().toBytes();
		log.debug("secret=" + yubikey.getSecret());
		log.debug("otp=" + otp);

		Token t;
		try {
			t = Pof.parse(otp, key);
		} catch (GeneralSecurityException e) {
			log.info(e);
			return FORMAT_ERROR;
		}

		int sessionCounter = toInt(t.getSessionCounter());
		int seenSessionCounter = yubikey.getSessionCounter();
		int scDiff = seenSessionCounter - sessionCounter;

		int hi = t.getTimestampHigh() & 0xff;
		int seenHi = yubikey.getTimestampHigh();
		int hiDiff = seenHi - hi;

		int lo = toInt(t.getTimestampLow());
		int seenLo = yubikey.getTimestampLow();
		int loDiff = seenLo - lo;

		if (scDiff > 0) {
			log.info("session counter less than last seen");
			log.info("session counter=" + sessionCounter);
			log.info("seen session counter=" + seenSessionCounter);
			log.info("Incoming otp=" + t);
			log.info("Current info=" + yubikey);
			return REPLAY_ERROR;
		}
		if (scDiff == 0 && hiDiff > 0) {
			log.info("high timestamp less than last seen");
			log.info("high=" + hi);
			log.info("seen high=" + seenHi);
			log.info("Incoming otp=" + t);
			log.info("Current info=" + yubikey);
			//return REPLAY_ERROR;
		}
		if (scDiff == 0 && hiDiff == 0 && loDiff > 0) {
			log.info("low timestamp less than last seen");
			log.info("low=" + lo);
			log.info("seen low=" + seenLo);
			log.info("Incoming otp=" + t);
			log.info("Current info=" + yubikey);
			//return REPLAY_ERROR;
		}
		if (scDiff == 0 && hiDiff == 0 && loDiff == 0) {
			log.info("Replayed otp detected");
			log.info("Incoming otp=" + t);
			log.info("Current info=" + yubikey);
			return REPLAY_ERROR;
		}

		yubikey.updateLastSeen(sessionCounter, hi, lo);
		db.updateYubikeyOnTokenId(tokenId, yubikey);

		return OK;
	}

	private static int toInt(byte[] arr) {
		int low = arr[0] & 0xff;
		int high = arr[1] & 0xff;
		return (int) (high << 8 | low);
	}
}
