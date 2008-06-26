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

import java.io.*;
import com.yubico.wsapi.Secret;
import com.yubico.wsapi.Crypto;

/** 
 * Convert a tab-delimited data file into a YubiKey insertion 
 * SQL statement 
 */
public class ConvertPoflog {
	public static void main(String[] argv) throws Exception {
		FileInputStream fstream = new FileInputStream(argv[0]);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			ConvertPoflog.convert(strLine);
		}
		in.close();
	}

	public static void convert(String s) {
		String[] ss = s.split("\t");
		// for (int i = 0; i < ss.length; i += 1) {
		// System.out.println(i+": "+ss[i]);
		// }
		s = "insert into yubikeys (client_id, active, created, "
				+ "accessed, tokenId, userId, secret) values (2, true, " + "'"
				+ ss[0] + "', '" + ss[0] + "', " + "'"
				+ Crypto.toString(Secret.fromModHex(ss[1]).toBytes()) + "', "
				+ "'" + Crypto.toString(Secret.fromModHex(ss[2]).toBytes())
				+ "', " + "'"
				+ Crypto.toString(Secret.fromModHex(ss[3]).toBytes()) + "');";
		System.out.println(s);
	}
}
