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
import java.io.File;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import com.meterware.servletunit.ServletRunner;
import com.yubico.wsapi.Constants;
import com.yubico.wsapi.Crypto;
import com.yubico.wsapi.Database;
import com.yubico.wsapi.MessageParser;
import com.yubico.wsapi.Response;
import com.yubico.wsapi.Secret;
import com.yubico.wsapi.VerificationRequest;
import com.yubico.wsapi.VerificationResponse;
import com.yubico.wsapi.Yubikey;

public class VerifyTests extends TestCase
{
  ServletRunner sr;
  static Logger log = Logger.getLogger (VerifyTests.class);

  public VerifyTests (String name)
  {
    super (name);
  }

  protected void setUp () throws Exception
  {
    super.setUp ();
    sr = new ServletRunner (new File ("web.xml"));
  }

  protected void tearDown () throws Exception
  {
    super.tearDown ();
    sr.shutDown ();
  }

  public static Test suite ()
  {
    TestSuite suite = new TestSuite (VerifyTests.class);

    TestSetup wrapper = new TestSetup (suite) {
      protected void setUp (){
			      // org.apache.log4j.BasicConfigurator.configure();
			      }
    };
    return wrapper;
  }


  public void testStrungGoodOtp () throws Exception
  {
    VerifyTests.resetYubikey ();
    String msg = "id=1&otp=vvvvvvcucrlcietctckflvnncdgckubflugerlnr";
    VerificationRequest vr
      = new VerificationRequest (MessageParser.urlEncodedToMap (msg));

    Response response = Utils.sendVerify (sr, vr);

      assertTrue (response instanceof VerificationResponse);
    Secret s = Secret.fromBase64 ("571dmZQ9MJ5T983eDqhuOplnHk8=");
      assertTrue (response.verifySignature (s));

    String result = response.getStatus ();
      assertTrue (Constants.OK.equals (result));
  }

  public void testGoodOtpCAPS_OTP () throws Exception
  {
    log.info ("-----------testGoodOtpCAPS_OTP()");
    VerifyTests.resetYubikey ();
    Map map = Utils.readFile ("1.txt");
    VerificationRequest vr = new VerificationRequest (map);

    Response response = Utils.sendVerify (sr, vr);

      assertTrue (response instanceof VerificationResponse);
    Secret s = Secret.fromBase64 ("571dmZQ9MJ5T983eDqhuOplnHk8=");
      assertTrue (response.verifySignature (s));

    String result = response.getStatus ();
      assertTrue (Constants.OK.equals (result));
  }

  // should be detected as a replay
  //
  public void testBadOtp () throws Exception
  {
    Map map = Utils.readFile ("1.txt");
    VerificationRequest vr = new VerificationRequest (map);

    Response response = Utils.sendVerify (sr, vr);
    Secret s = Secret.fromBase64 ("571dmZQ9MJ5T983eDqhuOplnHk8=");
      assertTrue (response.verifySignature (s));

      assertTrue (response instanceof VerificationResponse);
    String result = response.getStatus ();
      assertTrue (Constants.E_REPLAYED_OTP.equals (result));
  }

  // since this test uses only one token, we need to
  // reset the count in the database. 
  //
  private static void resetYubikey () throws Exception
  {
    Yubikey yk = new Yubikey (new java.util.Date (), true,
			      new java.util.Date (),
			      Secret.
			      fromModHex ("vfundlgjfhitfuvccjlhirkdrgfttggt"),
			      "1", "1", "1", 0, 0, 0, 0);
    String b64 = Crypto.toString (Secret.fromModHex ("vvvvvvcu").toBytes ());
      Database.getDefault ().updateYubikeyOnTokenId (b64, yk);
  }

  public void testWrongClient () throws Exception
  {
    VerifyTests.resetYubikey ();
    Map map = Utils.readFile ("2.txt");
    VerificationRequest vr = new VerificationRequest (map);

    Response response = Utils.sendVerify (sr, vr);
      assertTrue (response instanceof VerificationResponse);
    String result = response.getStatus ();
      assertTrue (Constants.E_NO_SUCH_CLIENT.equals (result));
  }

//     public void testIncorrectlySignedGoodOtp() throws Exception
//     {
//      Map map = Utils.readFile("2.txt");
//      VerificationRequest vr = new VerificationRequest(map);
//      vr.sign(Secret.fromAscii("incorrect_secret"));

//      Response response = Utils.sendVerify(sr, vr);
//      Secret s = Secret.fromAscii("secret2");
//      assertTrue(response.verifySignature(s));

//      assertTrue(response instanceof VerificationResponse);
//      String result = response.getStatus();
//      assertTrue(Constants.E_BAD_SIGNATURE.equals(result));
//     }

  public void testMissingOtp () throws Exception
  {
    String msg = "id=1&ot=";
    Map map = MessageParser.urlEncodedToMap (msg);

    Response response = Utils.sendVerify (sr, map);

      assertTrue (response instanceof VerificationResponse);
    String result = response.getStatus ();
      assertTrue (Constants.E_MISSING_PARAMETER.equals (result));
  }

}
