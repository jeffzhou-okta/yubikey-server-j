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
 * Base of all Yubikey management related requests
 * 
 */
abstract public class KeyRequest extends Request
{
  private final static Logger log = Logger.getLogger (KeyRequest.class);

  public void checkIsValid () throws InvalidMessageException
  {
    if (getOperation () == null)
      {
	log.info ("Missing " + Constants.OPERATION);
	throw new InvalidMessageException (Constants.E_MISSING_PARAMETER,
					   Constants.OPERATION);
      }
    if (getIdentifier () == null)
      {
	log.info ("Missing " + Constants.IDENTIFIER);
	throw new InvalidMessageException (Constants.E_MISSING_PARAMETER,
					   Constants.IDENTIFIER);
      }
    if (getNonce () == null)
      {
	log.info ("Missing " + Constants.NONCE);
	throw new InvalidMessageException (Constants.E_MISSING_PARAMETER,
					   Constants.NONCE);
      }
  }

  String getKeyId ()
  {
    return (String) map.get (Constants.KEY_ID);
  }

  public KeyRequest (Map map) throws InvalidMessageException
  {
    super (map);
  }

  abstract public Response process ();

  public String toString ()
  {
    return "[KeyRequest " + super.toString () + "]";
  }
}
