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

import java.util.Map;

public class ManageResponseFactory extends ResponseFactory
{
    public static ManageResponseFactory getDefault()
    {
	return new ManageResponseFactory();
    }

    Response generate(Map normalizedMap) throws Exception
    {
	String op = (String) normalizedMap.get(Constants.OPERATION);
	if (Constants.ADD_CLIENT.equals(op)){
	    return new AddClientResponse(normalizedMap);
	} 
	else if (Constants.ADD_KEY.equals(op)){
	    return new AddKeyResponse(normalizedMap);
	} 
	else if (Constants.DELETE_KEY.equals(op)){
	    return new DeleteKeyResponse(normalizedMap);
	} 
	else {
	    throw new RuntimeException(op+" is nyi in "+normalizedMap);
	    //return new ErrorResponse(normalizedMap);
	}
    }

//     public void setup(String password)
//     {
//  	this.password = password;
//     }

}

