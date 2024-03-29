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

public class DeleteKeyResponse extends Response
{
  static Logger log = Logger.getLogger (DeleteKeyResponse.class);

    DeleteKeyResponse (Map map)
  {
    super (map);
  }

  static DeleteKeyResponse create (Map map)
  {
    return create (null, map);
  }

  static DeleteKeyResponse create (Client signer, Map map)
  {
    DeleteKeyResponse r = new DeleteKeyResponse (map);
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
    return "[DeleteKeyResponse " + super.toString () + "]";
  }
}
