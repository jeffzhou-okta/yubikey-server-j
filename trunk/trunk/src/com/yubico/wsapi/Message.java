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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Message is the base class that defines all request and response messages.
 */
public abstract class Message {
	static Logger log = Logger.getLogger(Message.class);

	Map map;

	Message(Map m) {
		map = new HashMap();
		map.putAll(m);
	}

	/**
	 * Returns a map representation of this message.
	 * 
	 * @return the map.
	 */
	public Map toMap() {
		Map tmp = new HashMap();
		tmp.putAll(map);
		return tmp;
	}

	public String toGetString() {
		StringBuffer sb = new StringBuffer();
		Iterator iter = toMap().entrySet().iterator();
		while (iter.hasNext()) {
			try {
				Map.Entry me = (Map.Entry) iter.next();
				sb.append(URLEncoder.encode((String) me.getKey(), "UTF-8"));
				sb.append("=");
				sb.append(URLEncoder.encode((String) me.getValue(), "UTF-8"));
				if (iter.hasNext()) {
					sb.append("&");
				}
			} catch (UnsupportedEncodingException e) {
				// ignore
			}
		}
		return sb.toString();
	}

	public String getHash() {
		return (String) map.get(Constants.HMAC);
	}

	public void setHash(String hash) {
		map.put(Constants.HMAC, hash);
	}

	public String getTimestamp() {
		return (String) map.get(Constants.TIMESTAMP);
	}

	public boolean verifySignature(Secret secret) {
		log.debug("message.verify, map=" + map + ", secret=" + secret);
		String hash = getHash();
		Map map = toMap();
		map.remove(Constants.HMAC);
		log.debug("message.verify, map=" + map + ", hash=" + hash);
		return Crypto.verify(hash, map, secret);
	}

	public void sign(Secret secret) {
		Map map = toMap();
		map.remove(Constants.HMAC);
		log.debug("message.sign, map=" + map);
		setHash(Crypto.sign(map, secret));
	}

	public String toString() {
		return "[Message map=" + map + "]";
	}
}
