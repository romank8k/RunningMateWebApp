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

package com.roman.runningmate.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.roman.runningmate.client.UserDataService;
import com.roman.runningmate.shared.LoginStatus;
import com.roman.runningmate.shared.UserNotFoundException;

@SuppressWarnings("serial")
public class UserDataServiceImpl extends RemoteServiceServlet implements UserDataService {
  @Override
  public LoginStatus getLoginStatus(String redirectUrl) {
    UserService userService = UserServiceFactory.getUserService();

    String url;
    boolean loggedIn;

    if (getThreadLocalRequest().getUserPrincipal() != null) {
      url = userService.createLogoutURL(redirectUrl);
      loggedIn = true;
    } else {
      url = userService.createLoginURL(redirectUrl);
      loggedIn = false;
    }

    return new LoginStatus(loggedIn, url);
  }

  @Override
  public String getAccessId() throws UserNotFoundException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key userKey = KeyFactory.createKey("User", user.getUserId());
    Entity userEntity;
    try {
      userEntity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      throw new UserNotFoundException("User not found.");
    }

    return (String) userEntity.getProperty("access_id");
  }
}
