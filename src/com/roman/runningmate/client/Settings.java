// Copyright (c) 2011, Roman Khmelichek
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//  1. Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//  3. Neither the name of Roman Khmelichek nor the names of its contributors
//     may be used to endorse or promote products derived from this software
//     without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
// EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.roman.runningmate.client;

import com.google.gwt.http.client.URL;

public class Settings {
  public static final boolean DEBUG = false;

  public static final boolean SMALLER_LAYOUT = true;
  public static final boolean EMBEDDED = false;

  public static final String GOOGLE_MAPS_API_KEY = "";

  public static final String BASE_URL = DEBUG ? "http://127.0.0.1:8888" : "http://running-mate.appspot.com";

  public static String LOGIN_REDIRECT_URL = DEBUG ? (BASE_URL + "/RunningMateWebApp.html?gwt.codesvr=127.0.0.1:9997") : (BASE_URL + "/");

  public static String GET_RUNS_PATH = "/data_service?request_type=get_runs";
  public static String GET_COORDINATES_PATH = "/data_service?request_type=get_coordinates";

  public static String RUNS_URL = DEBUG ? (BASE_URL + GET_RUNS_PATH) : (BASE_URL + GET_RUNS_PATH);
  public static String RUN_COORDINATES_URL = DEBUG ? (BASE_URL + GET_COORDINATES_PATH) : (BASE_URL + GET_COORDINATES_PATH);

  public static boolean getSmallerLayout() {
    return SMALLER_LAYOUT;
  }

  public static boolean getEmbedded() {
    return EMBEDDED;
  }

  public static String getGoogleMapsApiKey() {
    return GOOGLE_MAPS_API_KEY;
  }

  public static String getBaseUrl() {
    return BASE_URL;
  }

  public static String getAccessIdUrl(String accessId) {
    return (DEBUG ? (BASE_URL + "/RunningMateWebApp.html?gwt.codesvr=127.0.0.1:9997&") : (BASE_URL + "/?")) + "access_id=" + URL.encode(accessId);
  }

  public static String getLoginRedirectUrl() {
    return LOGIN_REDIRECT_URL;
  }

  public static String getRunsUrl() {
    return RUNS_URL;
  }

  public static void setRunsUrlAccessId(String accessId) {
    RUNS_URL += "&access_id=" + accessId;
  }

  public static String getRunCoordinatesUrl(int runId) {
    return RUN_COORDINATES_URL + "&run_id=" + runId;
  }

  public static void setRunCoordinatesUrlAccessId(String accessId) {
    RUN_COORDINATES_URL += "&access_id=" + accessId;
  }
}
