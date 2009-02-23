/*
 * Copyright 2008, 2009 Yubico
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

import java.util.Date;

public class Yubikey
{
  private Secret secret;

  private Date created;

  private Date accessed;

  private String tokenId;

  private String userId;

  private String clientId;

  private boolean active;

  private int counter;

  private int low;

  private int high;

  private int sessionUse;

  public Yubikey (Date created, boolean active, Date accessed, Secret secret,
		  String clientId, String tokenId, String userId, int counter,
		  int high, int low, int sessionUse)
  {
    this.secret = secret;
    this.active = active;
    this.created = created;
    this.accessed = accessed;
    this.tokenId = tokenId;
    this.clientId = clientId;
    this.userId = userId;
    this.counter = counter;
    this.high = high;
    this.low = low;
    this.sessionUse = sessionUse;
  }

  static Yubikey lookup (String tokenId)
  {
    Database db = Database.getDefault ();
    return db.getYubikeyOnTokenId (tokenId);
  }

  static String createRandomTokenId ()
  {
    byte[]b = new byte[6];
    return Crypto.toString (Crypto.createRandom (b));
  }

  static String createRandomUserId ()
  {
    byte[]b = new byte[6];
    return Crypto.toString (Crypto.createRandom (b));
  }

  // public static String createRandom()
  // {
  // String s = UUID.generate().toString();
  // String s2 = UUID.generate().toString();
  // String res = Crypto.sign(s, Secret.fromAscii(s2));
  // return Secret.fromBase64(res);
  // }

  void updateLastSeen (int counter, int high, int low, int sessionUse)
  {
    this.counter = counter;
    this.high = high;
    this.low = low;
    this.sessionUse = sessionUse;
    this.accessed = new java.util.Date ();
  }

  public int getSessionCounter ()
  {
    return counter;
  }

  public int getTimestampHigh ()
  {
    return high;
  }

  public int getTimestampLow ()
  {
    return low;
  }

  public int getSessionUse ()
  {
    return sessionUse;
  }

  Date getCreated ()
  {
    return created;
  }

  Date getAccessed ()
  {
    return accessed;
  }

  boolean getActive ()
  {
    return active;
  }

  Secret getSecret ()
  {
    return secret;
  }

  String getTokenId ()
  {
    return tokenId;
  }

  String getUserId ()
  {
    return userId;
  }

  String getClientId ()
  {
    return clientId;
  }

  void setTokenId (String tokenId)
  {
    this.tokenId = tokenId;
  }

  void setUserId (String userId)
  {
    this.userId = userId;
  }

  public String toString ()
  {
    return "[Yubikey secret=" + secret
      + ", token ID=" + tokenId
      + ", token UID=" + userId
      + ", last counter=" + counter
      + ", high=" + high
      + ", low=" + low + ", session use=" + sessionUse + "]";
  }
}
