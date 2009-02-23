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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A request from a Client to add a new key. A new key entry is generated with a
 * randomized new secret. 
 * 
 * This is used in combination with the provisioning process. The secret is returned 
 * back to the caller (the provisioning party) to implant into the YubiKey with the 
 * Yubikey configuration tool provided by Yubico. 
 */
public class AddKeyRequest extends KeyRequest
{
  private final static Logger log = Logger.getLogger (AddKeyRequest.class);

  public AddKeyRequest (Map map) throws InvalidMessageException
  {
    super (map);
  }

  public Response process ()
  {

    String id = getIdentifier ();
    Client c = Client.lookup (id);

    if (c == null)
      {
	log.info ("no client '" + id + "' available");
	Map map = new HashMap ();
	  map.put (Constants.STATUS, Constants.E_NO_SUCH_CLIENT);
	  return AddKeyResponse.create (map);
      }

    if (!isSigned () || !signatureVerifies (c))
      {
	Map map = new HashMap ();
	map.put (Constants.STATUS, Constants.E_BAD_SIGNATURE);
	return AddKeyResponse.create (map);
      }

    if (!c.checkPerms (this))
      {
	Map map = new HashMap ();
	map.put (Constants.STATUS, Constants.E_OPERATION_NOT_ALLOWED);
	return AddKeyResponse.create (map);
      }

    Secret secret = Secret.createRandom ();
    String tokenId = Yubikey.createRandomTokenId ();
    String userId = Yubikey.createRandomUserId ();

    Yubikey key = new Yubikey (new java.util.Date (), true,
			       new java.util.Date (), secret, id,
			       tokenId, userId, 0, 0, 0, 0);
    Map map = new HashMap ();
    map.put (Constants.OPERATION, getOperation ());
    map.put (Constants.NONCE, getNonce ());
    // try three times -- the IDs we randomly generate may
    // be used!
    boolean success = false;
    for (int i = 0; i < 3; i += 1)
      {
	try
	{
	  String keyId = Database.getDefault ().addKey (key);

	  map.put (Constants.STATUS, Constants.OK);
	  map.put (Constants.TOKEN_ID, key.getTokenId ());
	  map.put (Constants.USER_ID, key.getUserId ());
	  map.put (Constants.SHARED_SECRET,
		   Crypto.toString (key.getSecret ().toBytes ()));
	  success = true;
	  break;
	}
	catch (SQLException e)
	{
	  log.info ("Could not add key. Trying again");
	  log.info (e);
	  key.setTokenId (Yubikey.createRandomTokenId ());
	  key.setUserId (Yubikey.createRandomUserId ());
	}
      }
    if (!success)
      {
	map.put (Constants.STATUS, Constants.E_BACKEND_ERROR);
      }
    return AddKeyResponse.create (c, map);
  }

  public String toString ()
  {
    return "[AddKeyRequest " + super.toString () + "]";
  }
}
