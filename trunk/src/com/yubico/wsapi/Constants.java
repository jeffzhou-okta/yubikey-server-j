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

public class Constants
{
  public static String ADD_CLIENT = "add_client";
  public static String ADD_KEY = "add_key";
  public static String CLIENT_ID = "client_id";
  public static String DELETE_CLIENT = "delete_client";
  public static String DELETE_KEY = "delete_key";
  public static String EMAIL = "email";
  public static String EXTRA_INFO = "info";
  public static String HMAC = "h";
  public static String IDENTIFIER = "id";
  public static String KEY_ID = "key_id";
  public static String NONCE = "nonce";
  public static String OK = "OK";
  public static String OPERATION = "operation";
  public static String OTP = "otp";
  public static String PERMISSIONS = "permissions";
  public static String RESULT = "result";
  public static String SHARED_SECRET = "shared_secret";
  public static String STATUS = "status";
  public static String TIMESTAMP = "t";
  public static String TOKEN_ID = "token_id";
  public static String USER_ID = "user_id";
  public static String VERIFY_OTP = "verify_otp";

  public static String E_BACKEND_ERROR = "BACKEND_ERROR";
  public static String E_BAD_OTP = "BAD_OTP";
  public static String E_BAD_SIGNATURE = "BAD_SIGNATURE";
  public static String E_INCORRECT_PARAMETER = "INCORRECT_PARAMETER";
  public static String E_MISSING_PARAMETER = "MISSING_PARAMETER";
  public static String E_NO_SUCH_CLIENT = "NO_SUCH_CLIENT";
  public static String E_NO_SUCH_YUBIKEY = "NO_SUCH_YUBIKEY";
  public static String E_NO_SUCH_OPERATION = "NO_SUCH_OPERATION";
  public static String E_OPERATION_NOT_ALLOWED = "OPERATION_NOT_ALLOWED";
  //public static String E_POLICY_VIOLATION = "POLICY_VIOLATION";
  public static String E_REPLAYED_OTP = "REPLAYED_OTP";
  public static String E_UNKNOWN_ERROR = "UNKNOWN_ERROR";
}
