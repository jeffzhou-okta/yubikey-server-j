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
import java.util.Map;

import org.apache.log4j.Logger;

abstract public class RequestFactory {
	private final static Logger log = Logger.getLogger(RequestFactory.class);

	RequestFactory() {
	}

	/**
	 * Creates a request from a map.
	 * 
	 * @param map
	 *            the map to parse.
	 * @return a suitable request from the input data.
	 */
	public Request createFrom(Map map) {
		Map tmp = new HashMap();
		tmp.putAll(map);
		tmp = FactoryUtil.normalize(tmp);
		try {
			return generate(tmp);
		} catch (InvalidMessageException e) {
			log.info(e);
			try {
				return new ErrorRequest(e.getReason(), e.getExtraInfo());
			} catch (InvalidMessageException e2) {
				log.warn("This should never happen");
				throw new RuntimeException(e2);
			}
		}
	}

	abstract Request generate(Map normalizedMap) throws InvalidMessageException;
}
