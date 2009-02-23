package com.yubico.wsapi;

import java.util.Map;

public class ErrorResponse extends Response
{
  ErrorResponse (Map map)
  {
    super (map);
  }

  void putExtraInfo (String info)
  {
    map.put (Constants.EXTRA_INFO, info);
  }

  public String toString ()
  {
    return "[ErrorResponse " + super.toString () + "]";
  }
}
