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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.yubico.wsapi.ManageResponseFactory;
import com.yubico.wsapi.Request;
import com.yubico.wsapi.Response;
import com.yubico.wsapi.VerifyResponseFactory;

public class Utils
{
    static Logger log = Logger.getLogger(ManagementTests.class);
    private static String base = "http://localhost:8080/"; 

    static Response sendManagement(ServletRunner sr, Request r) throws Exception
    {
	return send(sr, r, "manage");
    }

    static Response sendVerify(ServletRunner sr, Request r) throws Exception
    {
	return send(sr, r, "verify");
    }

    static Response sendVerify(ServletRunner sr, Map map) throws Exception
    {
	return send(sr, map, "verify");
    }

    private static Response send(ServletRunner sr, Request r, String s) 
	throws Exception
    {
	return send(sr, r.toMap(), s);
    }

    private static Response send(ServletRunner sr, Map map, String s) 
	throws Exception
    {
	ServletUnitClient sc = sr.newClient();
	WebRequest req = new GetMethodWebRequest(base + s);

	Iterator iter = map.entrySet().iterator();
	while (iter.hasNext()){
	    Map.Entry me = (Map.Entry) iter.next();
	    req.setParameter((String) me.getKey(), 
			     (String) me.getValue());
	}

	log.debug("sending "+req.getQueryString()+" to "+base+s);	
	WebResponse resp = sc.getResponse(req);
	log.debug("receiving '"+resp.getText()+"'");

	map = nvToMap(resp.getText());
	if ("manage".equals(s)){
	    return ManageResponseFactory.getDefault().createFrom(map);
	} else {
	    return VerifyResponseFactory.getDefault().createFrom(map);
	}
    }

    private static Map nvToMap(String nv)
    {
	Properties prop = new Properties();
	try {
	    byte[] b = nv.getBytes("UTF-8");
	    prop.load(new ByteArrayInputStream(b));
	} catch (IOException e) {
	    throw new RuntimeException("Could not parse name/value input:"+e);
	}
	return prop;
    }

    static String readFileAsString(String fileName) throws Exception
    {
	BufferedReader input = null;
	try {
	    File f = new File(fileName);
	    if (!f.exists()){
		throw new IllegalArgumentException("No such file: " 
						   + f.getCanonicalPath());
	    }
	    input = new BufferedReader(new FileReader(f));
	    String line = null;
	    StringBuffer contents = new StringBuffer();
	    while ((line = input.readLine()) != null){
		int n = line.indexOf('=');
		String name = URLEncoder.encode(line.substring(0, n), "UTF-8");
		String value 
		    = URLEncoder.encode(line.substring(n+1, line.length()),
					"UTF-8");
		contents.append(name+"="+value+"?");		
	    }
	    String s = contents.toString();
	    return s.substring(0, s.length());
	} finally {
	    if (input!= null) input.close();
	}
    }

    static Properties readFile(String fileName) throws Exception
    {
	File f = new File(fileName);
	if (!f.exists()){
	    throw new IllegalArgumentException("No such file: " 
					       + f.getCanonicalPath());
	}
	FileInputStream in = null;
	try {
	    Properties prop = new Properties();
	    in = new FileInputStream(f);
	    prop.load(in);
	    return prop;
	} finally {
	    if (in != null) try{in.close();}catch (Exception ignore){}
	}

    }

}
