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

/**
 * A response to an OTP verification request
 */
public class VerificationResponse extends Response
{
  static Logger log = Logger.getLogger (VerificationResponse.class);

    VerificationResponse (Map map)
  {
    super (map);
  }

  static VerificationResponse create (Client signer, String status)
  {
    Map map = new HashMap ();
    map.put (Constants.STATUS, status);
    return createResponse (signer, map);
  }

  private static VerificationResponse createResponse (Client signer, Map map)
  {
    VerificationResponse vr = new VerificationResponse (map);
    log.debug ("client signer=" + signer);
    if (signer != null)
      {
	String ts = DateUtils.getTimeStamp ();
	vr.putTimestamp (ts);
	vr.sign (signer.getSecret ());
      }
    return vr;
  }

  public boolean isOk ()
  {
    return Constants.OK.equals (getStatus ());
  }

  public String toString ()
  {
    return "[VerificationResponse " + super.toString () + "]";
  }
}
