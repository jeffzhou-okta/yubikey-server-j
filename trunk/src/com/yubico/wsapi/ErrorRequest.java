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

import java.util.HashMap;

public class ErrorRequest extends Request {
	// private final static Logger log = Logger.getLogger(ErrorRequest.class);
	private String reason;

	private String info;

	ErrorRequest(String reason, String info) throws InvalidMessageException {
		super(new HashMap());
		this.reason = reason;
		this.info = info;
	}

	public void checkIsValid() throws InvalidMessageException {
	}

	public Response process() {
		ErrorResponse r = new ErrorResponse(new HashMap());
		r.putStatus(reason);
		r.putExtraInfo(info);
		return r;
	}

	public String toString() {
		return "[ErrorRequest " + super.toString() + "]";
	}
}
