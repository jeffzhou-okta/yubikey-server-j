/*
 * Copyright 2008, 2009 Yubico
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
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/** 
 * The base class of all servlets used in the Yubikey validation server
 */
abstract public class WsApiServlet extends HttpServlet {
	private static final long serialVersionUID = 2873429387623L;

	private ServletContext ctx = null;

	/** Data retrieved from web.xml */
	private static String DB_USER = "db_user";

	/** Data retrieved from web.xml */
	private static String DB_PASSWORD = "db_password";

	/** Data retrieved from web.xml */
	private static String DB_URL = "db_url";

	static Logger log = Logger.getLogger(WsApiServlet.class);

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ctx = config.getServletContext();

		String user = config.getInitParameter(WsApiServlet.DB_USER);
		check(WsApiServlet.DB_USER, user);

		String password = config.getInitParameter(WsApiServlet.DB_PASSWORD);
		check(WsApiServlet.DB_PASSWORD, password);

		String url = config.getInitParameter(WsApiServlet.DB_URL);
		check(WsApiServlet.DB_URL, url);

		try {
			Database.setup(user, password, url);
		} catch (SQLException e) {
			throw new ServletException(e);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		} catch (InstantiationException e) {
			throw new ServletException(e);
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		}
	}

	void check(String what, String found) throws ServletException {
		log.debug("check: " + what + "," + found);
		if (found == null || "".equals(found)) {
			throw new ServletException("Missing required param: '" + what + "'");
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doQuery(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doQuery(request, response);
	}

	void print(HttpServletResponse resp, Response response) throws IOException {

		resp.setHeader("Content-Type", "text/plain");
		PrintWriter out = resp.getWriter();
		Map map = response.toMap();
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry) iter.next();
			// String key = URLEncoder.encode((String) me.getKey(), "UTF-8");
			// String value = URLEncoder.encode((String) me.getValue(),
			// "UTF-8");
			String key = (String) me.getKey();
			String value = (String) me.getValue();
			out.println(key + "=" + value);
		}
		out.println();
	}

	abstract public void doQuery(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException;
}
