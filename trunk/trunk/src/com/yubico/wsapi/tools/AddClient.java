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
package com.yubico.wsapi.tools;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Map;
import java.net.URLEncoder;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Properties;
import com.yubico.wsapi.*;

/** 
 * Add a new client (YubiKey issuer) from command line. 
 * <br>
 * Eg. java -cp ./out/wsapi.jar;./resources/tsik.jar;./resources/log4j-1.2.13.jar com.yubico.wsapi.tools.AddClient test\data\m1a.txt TWIWuqIJKVWhXPbVuxEiHv5GSA0= http://my_org/wsapi/add_client 
 * */
public class AddClient {
	public static void main(String[] argv) throws Exception {
		if (argv.length != 3) {
			System.err
					.println("Usage: AddClient <filename> <shared secret> <dest_url>");
			return;
		}
		Properties p = new Properties();
		File f = new File(argv[0]);
		p.load(new FileInputStream(f));

		p.put(Constants.OPERATION, Constants.ADD_CLIENT);
		byte[] b = new byte[20];
		p.put(Constants.NONCE, Crypto.toString(Crypto.createRandom(b)));
		p.put(Constants.TIMESTAMP, new java.util.Date().toString());
		new AddClient(p, argv[1], argv[2]);
	}

	public AddClient(Map map, String sharedKey, String dest) throws Exception {
		AddClientRequest req = new AddClientRequest(map);
		Secret s = Secret.fromBase64(sharedKey);
		req.sign(s);

		AddClientResponse resp = AddClient.send(req.toMap(), dest);
		if (!resp.verifySignature(s)) {
			System.err.println("Signature doesn't verify");
		}
		String result = resp.getStatus();
		if (!result.equals(Constants.OK)) {
			System.err.println("Result is not OK");
		}
		System.out.println("client id=" + resp.getClientId());
		System.out.println("key (base64)=" + resp.getSharedSecret());
		System.out.println("key (byte[])="
				+ AddKey.toString(Crypto.toBytes(resp.getSharedSecret())));
	}

	public static AddClientResponse send(Map map, String dest) throws Exception {
		String toSend = "";

		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry) iter.next();
			String key = URLEncoder.encode((String) me.getKey(), "UTF-8");
			String value = URLEncoder.encode((String) me.getValue(), "UTF-8");
			toSend += key + "=" + value;
			if (iter.hasNext()) {
				toSend += "&";
			}
		}

		BufferedReader in = null;
		StringBuffer contents = new StringBuffer();
		try {
			URL url = new URL(dest + "?" + toSend);
			System.out.println(url);
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			in = new BufferedReader(new InputStreamReader(connection
					.getInputStream()));
			String str;			
			while ((str = in.readLine()) != null) {
				System.out.println(str);
				contents.append(str + "\n");
			}
		} finally {
			if (in != null)
				in.close();
		}

		Properties prop = new Properties();
		try {
			byte[] b = contents.toString().getBytes("UTF-8");
			prop.load(new ByteArrayInputStream(b));
		} catch (IOException e) {
			throw new RuntimeException("Could not parse name/value input:" + e);
		}
		Response resp = ManageResponseFactory.getDefault().createFrom(prop);
		System.out.println(resp);
		return (AddClientResponse) resp;
	}
}
