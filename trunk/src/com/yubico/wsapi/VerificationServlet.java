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

import java.io.IOException;
// import java.io.PrintWriter;
// import java.net.URLEncoder;
// import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
// import javax.servlet.ServletConfig;
// import javax.servlet.ServletContext;
// import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The servlet that handles an incoming verification request, mapped from
 * web.xml
 */
public class VerificationServlet extends WsApiServlet {
	private static final long serialVersionUID = 2873488797623L;

	public void doQuery(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Map map = req.getParameterMap();
		print(resp, VerifyRequestFactory.getDefault().createFrom(map).process());
	}
}
