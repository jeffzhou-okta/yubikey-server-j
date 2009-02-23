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
 * The response to an AddKeyRequest.
 * 
 * This is used in combination with the provisioning process. The secret is returned 
 * back to the caller (the provisioning party) to implant into the YubiKey with the 
 * Yubikey configuration tool provided by Yubico.
 *  
 * @see Class AddKeyRequest
 */
public class AddKeyResponse extends Response
{
  static Logger log = Logger.getLogger (AddKeyResponse.class);

    AddKeyResponse (Map map)
  {
    super (map);
  }

  public String getNonce ()
  {
    return (String) map.get (Constants.NONCE);
  }

  public String getTokenId ()
  {
    return (String) map.get (Constants.TOKEN_ID);
  }

  public String getUserId ()
  {
    return (String) map.get (Constants.USER_ID);
  }

  public String getSharedSecret ()
  {
    return (String) map.get (Constants.SHARED_SECRET);
  }

  static AddKeyResponse create (Map map)
  {
    return create (null, map);
  }

  static AddKeyResponse create (Client signer, Map map)
  {
    AddKeyResponse r = new AddKeyResponse (map);
    if (signer != null)
      {
	String ts = DateUtils.getTimeStamp ();
	r.putTimestamp (ts);
	r.sign (signer.getSecret ());
      }
    return r;
  }

  public String toString ()
  {
    return "[AddKeyResponse " + super.toString () + "]";
  }
}
