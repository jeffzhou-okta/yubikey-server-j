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

// java -cp ./out/wsapi.jar:./resources/tsik.jar:./resources/log4j-1.2.13.jar com.yubico.wsapi.tools.AddKey test/data/m1a.txt  571dmZQ9MJ5T983eDqhuOplnHk8= http://api.yubico.com/wsapi/add_client
/**
 * Add a new YubiKey from command line.
 */
public class AddKey {
	public static void main(String[] argv) throws Exception {
		if (argv.length != 3) {
			System.err
					.println("Usage: AddKey <filename> <shared secret> <dest>");
			return;
		}
		Properties p = new Properties();
		File f = new File(argv[0]);
		p.load(new FileInputStream(f));

		p.put(Constants.OPERATION, Constants.ADD_KEY);
		byte[] b = new byte[20];
		p.put(Constants.NONCE, Crypto.toString(Crypto.createRandom(b)));
		p.put(Constants.TIMESTAMP, new java.util.Date().toString());
		new AddKey(p, argv[1], argv[2]);
	}

	public AddKey(Map map, String sharedKey, String dest) throws Exception {
		AddKeyRequest req = new AddKeyRequest(map);
		Secret s = Secret.fromBase64(sharedKey);
		req.sign(s);

		AddKeyResponse resp = AddKey.send(req.toMap(), dest);
		if (!resp.verifySignature(s)) {
			System.err.println("Signature doesn't verify");
		}
		String result = resp.getStatus();
		if (!result.equals(Constants.OK)) {
			System.err.println("Result is not OK");
		}
		System.out.println("tokenId="
				+ AddKey.toString(Crypto.toBytes(resp.getTokenId())));
		System.out.println("userId="
				+ AddKey.toString(Crypto.toBytes(resp.getUserId())));
		System.out.println("key="
				+ AddKey.toString(Crypto.toBytes(resp.getSharedSecret())));
	}

	static String toString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i += 1) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(Integer.toHexString(b[i] & 0xFF));
		}
		return sb.toString();
	}

	public static AddKeyResponse send(Map map, String dest) throws Exception {
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
		return (AddKeyResponse) ManageResponseFactory.getDefault().createFrom(
				prop);
	}
}
