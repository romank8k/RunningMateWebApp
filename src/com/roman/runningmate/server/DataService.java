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
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class DataService extends HttpServlet {
  private static final long serialVersionUID = 1L;

  // TODO: Would be best to insert using transactions. To avoid timing out, split up transactions by run.
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    if (request.getUserPrincipal() != null) {
      response.setContentType("text/html");

      addUserRuns(user, request.getParameter("runs"));

      PrintWriter out = response.getWriter();
      out.println("<html><body>");
      out.println(request.getParameter("runs")); // TODO: For debugging...need to return a status code for success/failure.
      out.println("</body></html>");
      out.close();
    } else {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  // A user can also use a proxy to get his/her run data, and embed the AJAX interface on their own site (using their unique access ID to request the run data without logging in).
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {    
    User user = isAuthorized(request);

    // TODO: For testing on the dev app server. Insert some sample runs.
    String serverInfo = getServletContext().getServerInfo();
    String test = request.getParameter("test");
    if (serverInfo.split("/")[0].equals("Google App Engine Development") && test != null && user != null && UserServiceFactory.getUserService().isUserAdmin()) {
      String sampleJson = "{\"runs\":[{\"time_start\":\"1284551781547\",\"time_end\":\"1284564270811\",\"distance\":\"15940.823425022181\",\"coordinates\":[{\"latitude\":\"40.61426103115082\",\"longitude\":\"-73.97027134895325\",\"elevation\":\"-11.0\",\"time_elapsed\":\"0\"},{\"latitude\":\"40.614577531814575\",\"longitude\":\"-73.969686627388\",\"elevation\":\"-19.0\",\"time_elapsed\":\"60231\"},{\"latitude\":\"40.61501204967499\",\"longitude\":\"-73.96902143955231\",\"elevation\":\"-19.0\",\"time_elapsed\":\"60691\"},{\"latitude\":\"40.61590254306793\",\"longitude\":\"-73.96923065185547\",\"elevation\":\"-24.0\",\"time_elapsed\":\"60081\"},{\"latitude\":\"40.616825222969055\",\"longitude\":\"-73.96937549114227\",\"elevation\":\"-22.0\",\"time_elapsed\":\"60937\"},{\"latitude\":\"40.61773180961609\",\"longitude\":\"-73.96939694881439\",\"elevation\":\"-24.0\",\"time_elapsed\":\"60038\"},{\"latitude\":\"40.61867594718933\",\"longitude\":\"-73.96959006786346\",\"elevation\":\"-23.0\",\"time_elapsed\":\"60980\"},{\"latitude\":\"40.6196254491806\",\"longitude\":\"-73.96971881389618\",\"elevation\":\"-21.0\",\"time_elapsed\":\"60044\"},{\"latitude\":\"40.62095046043396\",\"longitude\":\"-73.96998167037964\",\"elevation\":\"-23.0\",\"time_elapsed\":\"60944\"},{\"latitude\":\"40.62269389629364\",\"longitude\":\"-73.97027671337128\",\"elevation\":\"-21.0\",\"time_elapsed\":\"60002\"},{\"latitude\":\"40.624104738235474\",\"longitude\":\"-73.97055566310883\",\"elevation\":\"-26.0\",\"time_elapsed\":\"60019\"},{\"latitude\":\"40.62438905239105\",\"longitude\":\"-73.97030889987946\",\"elevation\":\"-23.0\",\"time_elapsed\":\"60934\"},{\"latitude\":\"40.62518835067749\",\"longitude\":\"-73.97043764591217\",\"elevation\":\"-26.0\",\"time_elapsed\":\"60019\"},{\"latitude\":\"40.626073479652405\",\"longitude\":\"-73.97060930728912\",\"elevation\":\"-25.0\",\"time_elapsed\":\"60978\"},{\"latitude\":\"40.62710881233215\",\"longitude\":\"-73.97081851959229\",\"elevation\":\"-20.0\",\"time_elapsed\":\"60008\"},{\"latitude\":\"40.62875032424927\",\"longitude\":\"-73.97107064723969\",\"elevation\":\"-25.0\",\"time_elapsed\":\"60014\"},{\"latitude\":\"40.6297105550766\",\"longitude\":\"-73.97127985954285\",\"elevation\":\"-15.0\",\"time_elapsed\":\"60986\"},{\"latitude\":\"40.63040792942047\",\"longitude\":\"-73.97141396999359\",\"elevation\":\"-22.0\",\"time_elapsed\":\"60014\"},{\"latitude\":\"40.631293058395386\",\"longitude\":\"-73.9715963602066\",\"elevation\":\"-19.0\",\"time_elapsed\":\"60979\"},{\"latitude\":\"40.63216745853424\",\"longitude\":\"-73.97173583507538\",\"elevation\":\"-26.0\",\"time_elapsed\":\"60008\"},{\"latitude\":\"40.63327252864838\",\"longitude\":\"-73.97190749645233\",\"elevation\":\"-20.0\",\"time_elapsed\":\"60008\"},{\"latitude\":\"40.63434541225433\",\"longitude\":\"-73.97213280200958\",\"elevation\":\"-14.0\",\"time_elapsed\":\"61000\"},{\"latitude\":\"40.634592175483704\",\"longitude\":\"-73.97221863269806\",\"elevation\":\"-12.0\",\"time_elapsed\":\"60103\"},{\"latitude\":\"40.63547194004059\",\"longitude\":\"-73.9723527431488\",\"elevation\":\"-18.0\",\"time_elapsed\":\"60001\"},{\"latitude\":\"40.63617467880249\",\"longitude\":\"-73.97249758243561\",\"elevation\":\"-20.0\",\"time_elapsed\":\"60929\"},{\"latitude\":\"40.63695788383484\",\"longitude\":\"-73.9726209640503\",\"elevation\":\"-19.0\",\"time_elapsed\":\"60160\"},{\"latitude\":\"40.637837648391724\",\"longitude\":\"-73.97278726100922\",\"elevation\":\"-26.0\",\"time_elapsed\":\"61440\"},{\"latitude\":\"40.63875496387482\",\"longitude\":\"-73.97299647331238\",\"elevation\":\"-11.0\",\"time_elapsed\":\"60419\"},{\"latitude\":\"40.640305280685425\",\"longitude\":\"-73.97329688072205\",\"elevation\":\"-14.0\",\"time_elapsed\":\"60978\"},{\"latitude\":\"40.64142644405365\",\"longitude\":\"-73.97350072860718\",\"elevation\":\"-15.0\",\"time_elapsed\":\"60052\"},{\"latitude\":\"40.64272999763489\",\"longitude\":\"-73.97372603416443\",\"elevation\":\"-7.0\",\"time_elapsed\":\"60993\"},{\"latitude\":\"40.643577575683594\",\"longitude\":\"-73.97392988204956\",\"elevation\":\"-15.0\",\"time_elapsed\":\"60932\"},{\"latitude\":\"40.64439296722412\",\"longitude\":\"-73.97403717041016\",\"elevation\":\"-7.0\",\"time_elapsed\":\"60046\"},{\"latitude\":\"40.64510107040405\",\"longitude\":\"-73.97403180599213\",\"elevation\":\"-11.0\",\"time_elapsed\":\"60979\"},{\"latitude\":\"40.64599692821503\",\"longitude\":\"-73.97419810295105\",\"elevation\":\"-6.0\",\"time_elapsed\":\"60004\"},{\"latitude\":\"40.64684987068176\",\"longitude\":\"-73.97436439990997\",\"elevation\":\"3.0\",\"time_elapsed\":\"60001\"}]}]}";
      addUserRuns(user, sampleJson);
    }

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    if (user != null) {
      String requestType = request.getParameter("request_type");
      if (requestType != null) {
        if (requestType.equals("get_runs")) {
          String result = getRuns(user);
          out.println(result);
        } else if(requestType.equals("get_last_synced_run_time")) {
          out.println(getLastSyncedRunTime(user));
        } else if (requestType.equals("get_coordinates")) {
          String runId = request.getParameter("run_id");
          if (runId != null) {
            String result = getCoordinates(user, runId);
            out.println(result);
          }
        } else if (requestType.equals("remove_runs")) {
          String runIdsJson = request.getParameter("run_ids");
          if (runIdsJson != null) {
            removeRuns(user, runIdsJson);
          }
        } else if(requestType.equals("merge_runs")) {
          String runIdsJson = request.getParameter("run_ids");
          if (runIdsJson != null) {
            mergeRuns(user, runIdsJson);
          }
        } else if (requestType.equals("clear_runs")) {
          UserService userService = UserServiceFactory.getUserService();
          String thisURL = request.getRequestURI();
          out.println("<html><body>");
          out.println("<p>Hello, " + request.getUserPrincipal().getName() + "!</p>");
          out.println("<p>Deleting your runs (as many as possible before we time out)...</p>");
          out.println("<p><a href=\"" + userService.createLogoutURL(thisURL) + "\">Sign Out</a></p>");
          out.println("</body></html>");
          clearRuns(user);
        } else {
          // Unrecognized request type.
        }
      }
    }

    out.close();
  }

  // Authorized requests are those that either specify a valid access ID or a proper authentication cookie (managed by Google).
  // Returns an authorized User or null.
  public User isAuthorized(HttpServletRequest request) {
    UserService userService = UserServiceFactory.getUserService();

    User user = null;
    String accessId = request.getParameter("access_id");
    if (accessId != null) {
      Query userQuery = new Query("User");
      userQuery.addFilter("access_id", FilterOperator.EQUAL, accessId);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity userEntity = datastore.prepare(userQuery).asSingleEntity();
      if (userEntity != null) {
        user = (User) userEntity.getProperty("user");
      }
    } else {
      user = userService.getCurrentUser();
    }

    return user;
  }

  // TODO: Need a merge operation, so we can merge several runs into one.
  public void mergeRuns(User user, String json) {
  }

  public void removeRuns(User user, String json) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userKey = KeyFactory.createKey("User", user.getUserId());

    try {
      JSONParser parser = new JSONParser();
      JSONArray runIdsArray = (JSONArray) parser.parse(json);
      for (Object runIdObj : runIdsArray) {
        String runId = (String) runIdObj;
        Key runKey = KeyFactory.createKey(userKey, "Run", Long.parseLong(runId));

        Query pointsQuery = new Query("Point", runKey);
        pointsQuery.setKeysOnly();
        PreparedQuery preparedPointsQuery = datastore.prepare(pointsQuery);
        for (Entity point : preparedPointsQuery.asIterable()) {
          datastore.delete(point.getKey());
        }
        datastore.delete(runKey);
      }
    } catch (ParseException e) {
    }
  }

  public void clearRuns(User user) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userKey = KeyFactory.createKey("User", user.getUserId());
    Query runsQuery = new Query("Run", userKey);
    runsQuery.setKeysOnly();
    PreparedQuery preparedRunsQuery = datastore.prepare(runsQuery);
    for (Entity run : preparedRunsQuery.asIterable()) {
      Query pointsQuery = new Query("Point", run.getKey());
      pointsQuery.setKeysOnly();
      PreparedQuery preparedPointsQuery = datastore.prepare(pointsQuery);
      for (Entity point : preparedPointsQuery.asIterable()) {
        datastore.delete(point.getKey());
      }
      datastore.delete(run.getKey());
    }
  }

  @SuppressWarnings("unchecked")
  public String getCoordinates(User user, String runId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userKey = KeyFactory.createKey("User", user.getUserId());
    Key runKey = KeyFactory.createKey(userKey, "Run", Long.parseLong(runId));

    // TODO: This is optional. Only if we need some run information.
    /*Entity run;
    try {
      run = datastore.get(runKey);
      User storedUser = (User) run.getProperty("user");
    } catch (EntityNotFoundException e) {
      // Key does not exist.
    }*/

    Query pointsQuery = new Query("Point", runKey);
    pointsQuery.addSort("point_num", SortDirection.ASCENDING);
    PreparedQuery preparedQuery = datastore.prepare(pointsQuery);

    JSONArray runCoordinatesArray = new JSONArray();
    for (Entity point : preparedQuery.asIterable()) {
      GeoPt coordinate = (GeoPt) point.getProperty("coordinate");
      Long timeElapsed = (Long) point.getProperty("time_elapsed");
      Double elevation = (Double) point.getProperty("elevation");

      JSONObject pointObject = new JSONObject();
      pointObject.put("latitude", ((Float) coordinate.getLatitude()).toString());
      pointObject.put("longitude", ((Float) coordinate.getLongitude()).toString());
      pointObject.put("time_elapsed", timeElapsed.toString());
      pointObject.put("elevation", elevation.toString());

      runCoordinatesArray.add(pointObject);
    }

    JSONObject runsObject = new JSONObject();
    runsObject.put("run_coordinates", runCoordinatesArray);

    return runsObject.toJSONString();
  }

  @SuppressWarnings("unchecked")
  public String getRuns(User user) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userKey = KeyFactory.createKey("User", user.getUserId());
    Query runsQuery = new Query("Run", userKey);
    runsQuery.addSort("time_start", SortDirection.DESCENDING);
    PreparedQuery preparedQuery = datastore.prepare(runsQuery);

    JSONArray runsArray = new JSONArray();
    for (Entity run : preparedQuery.asIterable()) {
      Long timeStart = (Long) run.getProperty("time_start");
      Long timeEnd = (Long) run.getProperty("time_end");
      Double distance = (Double) run.getProperty("distance");

      JSONObject runsObject = new JSONObject();
      runsObject.put("time_start", timeStart.toString());
      runsObject.put("time_end", timeEnd.toString());
      runsObject.put("distance", distance.toString());
      runsObject.put("run_id", ((Long) run.getKey().getId()).toString());
      runsObject.put("run_key", KeyFactory.keyToString(run.getKey()));

      runsArray.add(runsObject);
    }

    JSONObject runsObject = new JSONObject();
    runsObject.put("runs", runsArray);
    return runsObject.toJSONString();
  }

  public long getLastSyncedRunTime(User user) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userKey = KeyFactory.createKey("User", user.getUserId());
    Query runsQuery = new Query("Run", userKey);
    runsQuery.addSort("time_start", SortDirection.DESCENDING);
    PreparedQuery preparedQuery = datastore.prepare(runsQuery);

    for (Entity run : preparedQuery.asIterable()) {
      Long timeStart = (Long) run.getProperty("time_start");
      return timeStart;
    }
    return Long.MAX_VALUE;
  }

  void addUserRuns(User user, String json) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key userKey = KeyFactory.createKey("User", user.getUserId());
    Entity userEntity;
    try {
      userEntity = datastore.get(userKey);
    } catch (EntityNotFoundException e) {
      // We generate a unique, random "access ID" for this user, so that his/her runs can be queried for without authentication.
      // We don't use the Google one, since it potentially leaks information about the user (other App Engine/Google services also use the same ID).
      // We must make sure the ID is unique.
      Query userQuery;
      UUID randId;
      do {
        randId = UUID.randomUUID();
        userQuery = new Query("User");
        userQuery.addFilter("access_id", FilterOperator.EQUAL, randId.toString());
      } while (datastore.prepare(userQuery).asSingleEntity() != null);

      userEntity = new Entity(userKey);
      userEntity.setProperty("user", user);
      userEntity.setProperty("access_id", randId.toString());
      datastore.put(userEntity);
    }

    try {
      JSONParser parser = new JSONParser();

      JSONObject jsonObj = (JSONObject) parser.parse(json);
      JSONArray runsArray = (JSONArray) jsonObj.get("runs");

      for (Object runObj : runsArray) {
        JSONObject runJsonObj = (JSONObject) runObj;

        Long timeStart = Long.parseLong((String) runJsonObj.get("time_start"));
        Long timeEnd = Long.parseLong((String) runJsonObj.get("time_end"));
        Double distance = Double.parseDouble((String) runJsonObj.get("distance"));

        Entity run = new Entity("Run", userKey);
        run.setProperty("time_start", timeStart);
        run.setProperty("time_end", timeEnd);
        run.setProperty("distance", distance);

        Key runKey = datastore.put(run);

        JSONArray coordinatesArray = (JSONArray) runJsonObj.get("coordinates");
        int count = 0;
        for (Object coordinateObj : coordinatesArray) {
          JSONObject coordinateJsonObj = (JSONObject) coordinateObj;

          Float latitude = Float.parseFloat((String) coordinateJsonObj.get("latitude"));
          Float longitude = Float.parseFloat((String) coordinateJsonObj.get("longitude"));
          Float elevation = Float.parseFloat((String) coordinateJsonObj.get("elevation"));
          Long timeElapsed = Long.parseLong((String) coordinateJsonObj.get("time_elapsed"));

          GeoPt coordinate = new GeoPt(latitude, longitude);

          Entity point = new Entity("Point", runKey);
          point.setProperty("coordinate", coordinate);
          point.setProperty("elevation", elevation);
          point.setProperty("time_elapsed", timeElapsed);
          point.setProperty("point_num", count); // Assume that we send points in the same order that we actually ran.

          datastore.put(point);
          count++;
        }
      }
    } catch (ParseException e) {
    }
  }
}
