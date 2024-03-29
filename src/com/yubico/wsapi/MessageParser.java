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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class MessageParser
{
  //private final static Logger log = Logger.getLogger(MessageParser.class);

  static char newline = '\n';

	/**
	 * Unrolls a message as a string. This string will use the
	 * <code>name:value</code> format of the specification. See also
	 * {@link #toUrlString()}.
	 * 
	 * @return the message as a string.
	 */
  static String toPostString (Message message)
  {
    return toStringDelimitedBy (message, ":", newline);
  }

	/**
	 * Unrolls a message as a string. This string will use encoding suitable for
	 * URLs. See also {@link #toPostString()}.
	 * 
	 * @return the message as a string.
	 */
  static String toUrlString (Message message)
  {
    return toStringDelimitedBy (message, "=", '&');
  }

  private static String toStringDelimitedBy (Message message, String kvDelim,
					     char lineDelim)
  {
    Map map = message.toMap ();
    Set set = map.entrySet ();
    StringBuffer sb = new StringBuffer ();
    try
    {
      for (Iterator iter = set.iterator (); iter.hasNext ();)
	{
	  Map.Entry mapEntry = (Map.Entry) iter.next ();
	  String key = (String) mapEntry.getKey ();
	  String value = (String) mapEntry.getValue ();

	  if (lineDelim == newline)
	    {
	      sb.append (key + kvDelim + value);
	      sb.append (lineDelim);
	    }
	  else
	    {
	      sb.append (URLEncoder.encode (key, "UTF-8") + kvDelim
			 + URLEncoder.encode (value, "UTF-8"));
	      if (iter.hasNext ())
		{
		  sb.append (lineDelim);
		}
	    }

	}
      return sb.toString ();
    }
    catch (UnsupportedEncodingException e)
    {
      // should not happen
      throw new RuntimeException ("Internal error");
    }
  }

  static int numberOfNewlines (String query) throws IOException
  {
    BufferedReader br = new BufferedReader (new StringReader (query));
    int n = 0;
    while (br.readLine () != null)
      {
	n += 1;
      }
    // log.warn ("number of lines="+n+" for "+query);
    return n;
  }

  public static Map urlEncodedToMap (String query)
    throws UnsupportedEncodingException
  {
    Map map = new HashMap ();
    if (query == null)
      {
	return map;
      }
    StringTokenizer st = new StringTokenizer (query, "?&=", true);
    String previous = null;
    while (st.hasMoreTokens ())
      {
	String current = st.nextToken ();
	if ("?".equals (current) || "&".equals (current))
	  {
	    // ignore
	  }
	else if ("=".equals (current))
	  {
	    String name = URLDecoder.decode (previous, "UTF-8");
	    if (st.hasMoreTokens ())
	      {
		String value = URLDecoder.decode (st.nextToken (), "UTF-8");
		if (isGoodValue (value))
		  {
		    map.put (name, value);
		  }
	      }
	  }
	else
	  {
	    previous = current;
	  }
      }
    return map;
  }

  private static boolean isGoodValue (String value)
  {
    if ("&".equals (value))
      {
	return false;
      }
    // more tests here perchance
    return true;
  }

  public static Map postedToMap (String query) throws IOException
  {
    Map map = new HashMap ();
    if (query == null)
      {
	return map;
      }
    BufferedReader br = new BufferedReader (new StringReader (query));
    int n = 0;
    String s = br.readLine ();
    while (s != null)
      {
	int index = s.indexOf ("=");
	if (index != -1)
	  {
	    String name = s.substring (0, index);
	    String value = s.substring (index + 1, s.length ());
	    if (name != null && value != null)
	      {
		map.put (name, value);
	      }
	  }
	s = br.readLine ();
      }
    return map;
  }

}
