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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

public class RemoteRequest {
  private static RequestCallback setUpCallback(final CommandResponse whatToDo) {
    RequestCallback callback = new RequestCallback() {
      @Override
      public void onError(Request request, Throwable e) {
        whatToDo.executeOnFailure(e.getMessage());
      }

      @Override
      public void onResponseReceived(Request request, Response response) {
        if (response.getStatusCode() == Response.SC_OK) {
          whatToDo.executeOnSuccess(response.getText());
        } else {
          whatToDo.executeOnFailure("Server Response Headers Not 200 (Header Code: " + response.getStatusCode() + ")" + response.getStatusText()
              + response.getText());
        }
      }
    };

    return callback;
  }

  private static void sendRequest(String requestData, RequestBuilder builder, RequestCallback callback, CommandResponse whatToDo) {
    try {
      builder.sendRequest(requestData, callback);
    } catch (RequestException e) {
      whatToDo.executeOnFailure(e.getMessage());
    }
  }

  public static void requestPostURI(final CommandResponse whatToDo, final String uri, final String postParameter, final String postValue) {
    RequestCallback callback = setUpCallback(whatToDo);
    String postData = URL.encode(postParameter) + "=" + URL.encode(postValue);
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, uri);
    builder.setHeader("Content-type", "application/x-www-form-urlencoded");
    builder.setHeader("Connection", "close");
    sendRequest(postData, builder, callback, whatToDo);
  }

  public static void requestGetURI(final CommandResponse whatToDo, final String uri) {
    RequestCallback callback = setUpCallback(whatToDo);
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, uri);
    builder.setHeader("Connection", "close");
    sendRequest(null, builder, callback, whatToDo);
  }
}
