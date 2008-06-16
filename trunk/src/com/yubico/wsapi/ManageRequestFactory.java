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

public class ManageRequestFactory extends RequestFactory {
	private final static Logger log = Logger
			.getLogger(ManageRequestFactory.class);

	public static ManageRequestFactory getDefault() {
		return new ManageRequestFactory();
	}

	public Request generate(Map normalizedMap) throws InvalidMessageException {
		log.debug("generate() normalizedMap=" + normalizedMap);
		String op = (String) normalizedMap.get(Constants.OPERATION);
		if (Constants.ADD_CLIENT.equals(op)) {
			return new AddClientRequest(normalizedMap);
		} else if (Constants.ADD_KEY.equals(op)) {
			return new AddKeyRequest(normalizedMap);
		} else if (Constants.DELETE_KEY.equals(op)) {
			return new DeleteKeyRequest(normalizedMap);
		} else {
			if (op == null)
				op = "<none>";
			throw new InvalidMessageException(Constants.E_NO_SUCH_OPERATION, op);
		}
		/*
		 * } else if (DELETE_CLIENT.equals(op)){ return new
		 * DeleteClientRequest.(normalizedMap); } else if (ADD_KEY.equals(op)){
		 * return new AddKeyRequest.(normalizedMap); } else if
		 * (DELETE_KEY.equals(op)){ return new DeleteKeyRequest.(normalizedMap); }
		 * else { return new ErrorRequest(normalizedMap, E_NO_SUCH_OPERATION); }
		 */
	}

	// public void setup(String password)
	// {
	// this.password = password;
	// }

}
