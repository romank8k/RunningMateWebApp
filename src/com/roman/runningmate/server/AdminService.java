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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AdminService extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();

    response.setContentType("text/html");

    PrintWriter out = response.getWriter();
    out.println("<html><body>");

    String thisURL = request.getRequestURI();
    if (request.getUserPrincipal() != null) {
      out.println("<p>Hello, " + request.getUserPrincipal().getName() + "!</p>");
      out.println("<p>Deleting datastore entities (as many as possible before we time out)...</p>");
      out.println("<p><a href=\"" + userService.createLogoutURL(thisURL) + "\">Sign Out</a></p>");
      if (userService.isUserAdmin()) {
        DeleteKindEntities("User", "Run", "Point");
      }
    } else {
      out.println("<p>If you are the application admin, <a href=\"" + userService.createLoginURL(thisURL) + "\">Sign In</a>.</p>");
      return;
    }

    out.println("</body></html>");
    out.close();
  }

  public void DeleteKindEntities(String... kinds) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    for (String kind : kinds) {
      Query query = new Query(kind);
      query.setKeysOnly();
      PreparedQuery preparedQuery = datastore.prepare(query);
      Iterable<Entity> results = preparedQuery.asIterable();
      for (Entity entity : results) {
        datastore.delete(entity.getKey());
      }
    }
  }
}
