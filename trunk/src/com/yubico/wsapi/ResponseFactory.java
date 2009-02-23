package com.yubico.wsapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

abstract public class ResponseFactory
{
  private final static Logger log = Logger.getLogger (ResponseFactory.class);

    ResponseFactory ()
  {
  }

    /**
     * Creates a response from a map.
     *
     * @param map the map to parse.
     * @return a suitable request from the input data.
     */
  public Response createFrom (Map map)
  {
    Map tmp = new HashMap ();
    tmp.putAll (map);
    tmp = FactoryUtil.normalize (tmp);
    try
    {
      return generate (tmp);
    }
    catch (Exception e)
    {
      log.info (e);
      return new ErrorResponse (tmp);
    }
  }

  abstract Response generate (Map normalizedMap) throws Exception;
}
