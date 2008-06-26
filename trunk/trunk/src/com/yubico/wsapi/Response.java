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
 * Base of all response messages.
 */
public abstract class Response extends Message {
	static Logger log = Logger.getLogger(Response.class);

	Response(Map map) {
		super(map);
	}

	public Map toMap() {
		return super.toMap();
	}

	public String getStatus() {
		return (String) map.get(Constants.STATUS);
	}

	void putStatus(String status) {
		map.put(Constants.STATUS, status);
	}

	void putTimestamp(String timeStamp) {
		map.put(Constants.TIMESTAMP, timeStamp);
	}

	public String toString() {
		return "[Response " + super.toString() + "]";
	}
}
