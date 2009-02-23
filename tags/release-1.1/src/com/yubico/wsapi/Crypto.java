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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.apache.tsik.datatypes.Base64;
import java.io.UnsupportedEncodingException;

/**
 * Crypto utilities used in Yubikey validation server
 *
 */
public class Crypto
{
  private final static Logger log = Logger.getLogger (Crypto.class);

  // private static int SALT_LENGTH = 10;
  private static SecureRandom random;
  static
  {
    try
    {
      random = SecureRandom.getInstance ("SHA1PRNG");
      // TODO call setSeed if necessary                       
    } catch (NoSuchAlgorithmException e)
    {
      log.fatal (e);
      throw new RuntimeException ("Cannot init SecureRandom");
    }
  }

  private Crypto ()
  {
  }

  static String sign (Map map, Secret secret) throws IllegalArgumentException
  {
    log.debug ("about to sign " + map);
    StringBuffer sb = new StringBuffer ();
    SortedMap tmp = new TreeMap (map);
    for (Iterator iter = tmp.entrySet ().iterator (); iter.hasNext ();)
      {
	Map.Entry me = (Map.Entry) iter.next ();
	String k = (String) me.getKey ();
	  sb.append (k);
	  sb.append ('=');
	  sb.append ((String) me.getValue ());
	if (iter.hasNext ())
	  {
	    sb.append ('&');
	  }
      }
    return sign (sb.toString (), secret);
  }

  public static byte[] createRandom (byte[]b)
  {
    random.nextBytes (b);
    return b;
  }

  public static String sign (String what, Secret secret)
    throws IllegalArgumentException
  {
    try
    {
      byte[]b = hmacSha1 (secret.toBytes (), what.getBytes ("UTF-8"));
      String sig = Crypto.toString (b);
        log.debug ("signing " + what + " with " + secret + " into " + sig);
        return sig;
    }
    catch (UnsupportedEncodingException e)
    {
      throw new IllegalArgumentException (e);
    }
  }

  private static byte[] hmacSha1 (byte[]key, byte[]text)
    throws IllegalArgumentException
  {
    try
    {
      SecretKey sk = new SecretKeySpec (key, "HMACSHA1");
      Mac m = Mac.getInstance (sk.getAlgorithm ());
        m.init (sk);
        return m.doFinal (text);
    }
    catch (InvalidKeyException e)
    {
      throw new IllegalArgumentException (e);
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new IllegalArgumentException (e);
    }
  }

  static boolean verify (String signature, Map map, Secret secret)
  {
    try
    {
      return sign (map, secret).equals (signature);
    }
    catch (IllegalArgumentException e)
    {
      return false;
    }
  }

  public static byte[] toBytes (String s)
  {
    return Base64.decode (s);
  }

  public static String toString (byte[]b)
  {
    String s = Base64.encode (b);
    s = s.replaceAll ("\n", "");
    return s;
  }
}
