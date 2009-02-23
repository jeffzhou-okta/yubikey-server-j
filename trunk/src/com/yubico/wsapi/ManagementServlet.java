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
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class ManagementServlet extends WsApiServlet {
	private static final long serialVersionUID = 2873429797623L;

	static Logger log = Logger.getLogger(ManagementServlet.class);

	private ManageRequestFactory mrf;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ManageRequestFactory mrf = ManageRequestFactory.getDefault();
		this.mrf = mrf;
	}

	public void doQuery(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.debug("in ManagementServlet");
		Map map = req.getParameterMap();
		print(resp, mrf.createFrom(map).process());
	}
}
