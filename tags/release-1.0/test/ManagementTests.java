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
import com.yubico.wsapi.AddClientRequest;
import com.yubico.wsapi.AddClientResponse;
import com.yubico.wsapi.AddKeyRequest;
import com.yubico.wsapi.AddKeyResponse;
import com.yubico.wsapi.Constants;
import com.yubico.wsapi.DeleteKeyRequest;
import com.yubico.wsapi.Response;
import com.yubico.wsapi.Secret;

public class ManagementTests extends TestCase
{
    ServletRunner sr;
    static Logger log = Logger.getLogger(ManagementTests.class);

    public ManagementTests(String name) 
    {
	super(name);
    }

    protected void setUp() throws Exception 
    {
	super.setUp();
    	sr = new ServletRunner(new File("web.xml"));
    }

    protected void tearDown() throws Exception 
    {
	super.tearDown();
	sr.shutDown();
    }

    public static Test suite() 
    {
        TestSuite suite = new TestSuite(ManagementTests.class);

	TestSetup wrapper = new TestSetup(suite) {
	   protected void setUp() {
	       // org.apache.log4j.BasicConfigurator.configure();
	   }
        };
	return wrapper;
    }

    static Secret secret = Secret.fromBase64("571dmZQ9MJ5T983eDqhuOplnHk8=");

    static String sharedKey;
    static String clientId;
    static String tokenId;

    public void testAddClient() throws Exception
    {
	log.info("-----------testAddClient()");

	Map map = Utils.readFile("m1a.txt");
	AddClientRequest req = new AddClientRequest(map);
	req.sign(secret);	

	Response resp = Utils.sendManagement(sr, req);
	assertTrue(resp.verifySignature(secret));
	String result = resp.getStatus();
	assertTrue(result, result.equals(Constants.OK));

	AddClientResponse response = (AddClientResponse) resp;
	assertTrue(response.getNonce().equals("1970-01-01T00:00:00Z"));
	ManagementTests.sharedKey = response.getSharedSecret();
	ManagementTests.clientId = response.getClientId();
    }

    public void testAddKey() throws Exception
    {
	log.info("-------------testAddKey()");
	Map map = Utils.readFile("m2a.txt");
	map.put(Constants.IDENTIFIER, ManagementTests.clientId);
	AddKeyRequest req = new AddKeyRequest(map);
	Secret mySecret = Secret.fromBase64(sharedKey);	
	req.sign(mySecret);	

	Response resp = Utils.sendManagement(sr, req);
	assertTrue(resp.verifySignature(mySecret));
	String result = resp.getStatus();
	assertTrue(result, result.equals(Constants.OK));
 
	AddKeyResponse response = (AddKeyResponse) resp;
	assertTrue(response.getNonce().equals("1970-01-01T00:00:00Z"));
	ManagementTests.tokenId = response.getTokenId();
    }

    public void testDeleteKey() throws Exception
    {
	log.info("-------------testDeleteKey()");
	Map map = Utils.readFile("m3a.txt");
	map.put(Constants.IDENTIFIER, ManagementTests.clientId);
	map.put(Constants.KEY_ID, ManagementTests.tokenId);
	DeleteKeyRequest req = new DeleteKeyRequest(map);
	Secret mySecret = Secret.fromBase64(sharedKey);	
	req.sign(mySecret);	

	Response resp = Utils.sendManagement(sr, req);
	assertTrue(resp.verifySignature(mySecret));
	String result = resp.getStatus();
	assertTrue(result, result.equals(Constants.OK));
    }

    public void testAddClientWithoutAddClientPerm() throws Exception
    {
	log.info("-------------testAddClientWithout()");
	Map map = Utils.readFile("m1a.txt");
	map.put(Constants.IDENTIFIER, ManagementTests.clientId);
	map.put(Constants.EMAIL, "testclient2@example.com");
	AddClientRequest req = new AddClientRequest(map);
	Secret mySecret = Secret.fromBase64(sharedKey);	
	req.sign(mySecret);	

	Response resp = Utils.sendManagement(sr, req);
	assertTrue(resp.verifySignature(mySecret));
	String result = resp.getStatus();
	assertTrue(result, result.equals(Constants.E_OPERATION_NOT_ALLOWED));
    }

    public void testAddClientWithAddClientPerm() throws Exception
    {
	log.info("-------------testAddClientWith()");
	Map map = Utils.readFile("m1a.txt");
	map.put(Constants.DELETE_CLIENT, "true");
	map.put(Constants.EMAIL, "testclient3@example.com");
	AddClientRequest req = new AddClientRequest(map);
	req.sign(secret);	

	Response resp = Utils.sendManagement(sr, req);
	assertTrue(resp.verifySignature(secret));
	String result = resp.getStatus();
	assertTrue(result, result.equals(Constants.OK));

	AddClientResponse response = (AddClientResponse) resp;
	assertTrue(response.getNonce().equals("1970-01-01T00:00:00Z"));
	ManagementTests.sharedKey = response.getSharedSecret();
	ManagementTests.clientId = response.getClientId();
    }

}
