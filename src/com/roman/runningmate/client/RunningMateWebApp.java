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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.event.PolylineClickHandler;
import com.google.gwt.maps.client.event.PolylineMouseOverHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.roman.runningmate.shared.LoginStatus;

public class RunningMateWebApp implements EntryPoint {
  private UserDataServiceAsync userDataService = GWT.create(UserDataService.class);

  private ScrollPanel dailyRuns = new ScrollPanel();
  private ScrollPanel monthlyRuns = new ScrollPanel();
  private ScrollPanel yearlyRuns = new ScrollPanel();
  private ScrollPanel settings = new ScrollPanel();

  private MapWidget map = null;

  private HashMap<Integer, LinkedList<Polyline>> runsMapping = new HashMap<Integer, LinkedList<Polyline>>();

  @Override
  public void onModuleLoad() {
    String accessId = Window.Location.getParameter("access_id");
    if (accessId != null) {
      Settings.setRunsUrlAccessId(accessId);
      Settings.setRunCoordinatesUrlAccessId(accessId);
      loadUi(false);
    } else {
      getLoginStatus();
    }
  }

  private void getLoginStatus() {
    userDataService.getLoginStatus(Settings.getLoginRedirectUrl(), new AsyncCallback<LoginStatus>() {
      public void onFailure(Throwable caught) {
        // TODO: Handle.
      }

      // TODO: Need a logout URL for logged in people.
      public void onSuccess(LoginStatus loginStatus) {
        if (!loginStatus.isLoggedIn()) {
          Window.Location.assign(loginStatus.getUrl());
        } else {
          loadUi(true);
        }
      }
    });
  }

  public void loadUi(final boolean showSettingsPanel) {
    Maps.loadMapsApi(Settings.getGoogleMapsApiKey(), "2", false, new Runnable() {
      @Override
      public void run() {
        // TODO: Need a different custom location for each user.
        LatLng newYorkCity = LatLng.newInstance(40.661560, -73.968201);

        map = new MapWidget(newYorkCity, 12);
        map.addMapType(MapType.getSatelliteMap());
        map.addMapType(MapType.getPhysicalMap());
        map.addControl(new MapTypeControl());

        map.addControl(new LargeMapControl3D());
        map.setScrollWheelZoomEnabled(true);

        LayoutPanel dailyStatsContainer = new LayoutPanel();
        dailyStatsContainer.add(dailyRuns);
        LayoutPanel monthlyStatsContainer = new LayoutPanel();
        monthlyStatsContainer.add(monthlyRuns);
        LayoutPanel yearlyStatsContainer = new LayoutPanel();
        yearlyStatsContainer.add(yearlyRuns);
        LayoutPanel settingsContainer = new LayoutPanel();
        settingsContainer.add(settings);

        if (Settings.getSmallerLayout()) {
          dailyStatsContainer.setWidgetLeftRight(dailyRuns, 10, Unit.PCT, 10, Unit.PCT);
          dailyStatsContainer.setWidgetTopBottom(dailyRuns, 10, Unit.PCT, 10, Unit.PCT);

          monthlyStatsContainer.setWidgetLeftRight(monthlyRuns, 10, Unit.PCT, 10, Unit.PCT);
          monthlyStatsContainer.setWidgetTopBottom(monthlyRuns, 10, Unit.PCT, 10, Unit.PCT);

          yearlyStatsContainer.setWidgetLeftRight(yearlyRuns, 10, Unit.PCT, 10, Unit.PCT);
          yearlyStatsContainer.setWidgetTopBottom(yearlyRuns, 10, Unit.PCT, 10, Unit.PCT);

          settingsContainer.setWidgetLeftRight(settings, 10, Unit.PCT, 10, Unit.PCT);
          settingsContainer.setWidgetTopBottom(settings, 10, Unit.PCT, 10, Unit.PCT);
        }

        // Can also use lazy panels.
        TabLayoutPanel statsPanel = new TabLayoutPanel(2, Unit.EM);
        statsPanel.add(dailyStatsContainer, "Daily Stats");
        statsPanel.add(monthlyStatsContainer, "Monthly Stats");
        statsPanel.add(yearlyStatsContainer, "Yearly Stats");
        if (showSettingsPanel) {
          statsPanel.add(settingsContainer, "Settings");
        }

        if (showSettingsPanel) {
          populateSettings();
        }

        LayoutPanel mapContainer = new LayoutPanel();
        mapContainer.setStylePrimaryName("MapWidgetWrapper");
        mapContainer.add(map);
        if (Settings.getSmallerLayout()) {
          mapContainer.setWidgetLeftRight(map, 10, Unit.PCT, 10, Unit.PCT);
          mapContainer.setWidgetTopBottom(map, 10, Unit.PCT, 10, Unit.PCT);
        }

        LayoutPanel container = new LayoutPanel();
        container.add(statsPanel);
        container.add(mapContainer);

        container.setWidgetLeftWidth(statsPanel, 0, Unit.PCT, 50, Unit.PCT);
        container.setWidgetRightWidth(mapContainer, 0, Unit.PCT, 50, Unit.PCT);

        container.setWidgetTopHeight(mapContainer, 2, Unit.EM, 100, Unit.PCT);
        container.setWidgetTopBottom(mapContainer, 2, Unit.EM, 0, Unit.EM);

        if (Settings.getEmbedded()) {
          // For embedding into an existing layout.
          container.setSize("940px", "600px");
          RootPanel.get("main").add(container);
        } else {
          // To use as a standalone application.
          RootLayoutPanel rp = RootLayoutPanel.get();
          rp.add(container);
        }

        getRuns();
      }
    });
  }

  public void generateMap(Integer runId, String jsonResult) {
    ArrayList<LatLng> path = new ArrayList<LatLng>();

    JSONValue jsonValue = JSONParser.parseStrict(jsonResult);
    JSONArray runsArray = jsonValue.isObject().get("run_coordinates").isArray();

    double minLat = Double.POSITIVE_INFINITY;
    double minLng = Double.POSITIVE_INFINITY;

    double maxLat = Double.NEGATIVE_INFINITY;
    double maxLng = Double.NEGATIVE_INFINITY;

    for (int i = 0; i < runsArray.size(); i++) {
      double lat = Double.parseDouble(runsArray.get(i).isObject().get("latitude").isString().stringValue());
      if (lat < minLat) {
        minLat = lat;
      }
      if (lat > maxLat) {
        maxLat = lat;
      }

      double lng = Double.parseDouble(runsArray.get(i).isObject().get("longitude").isString().stringValue());
      if (lng < minLng) {
        minLng = lng;
      }
      if (lng > maxLng) {
        maxLng = lng;
      }

      path.add(LatLng.newInstance(lat, lng));
    }

    LatLng latLngArr[] = new LatLng[path.size()];
    int i = 0;
    for (LatLng currLatLng : path) {
      // map.addOverlay(new Marker(currLatLng));
      latLngArr[i++] = currLatLng;
    }

    final Polyline polyline = new Polyline(latLngArr);

    polyline.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
      @Override
      public void onMouseOver(PolylineMouseOverEvent event) {
      }
    });

    polyline.addPolylineClickHandler(new PolylineClickHandler() {
      @Override
      public void onClick(PolylineClickEvent event) {
        double lengthInMeters = polyline.getLength();

        InfoWindowContent content = new InfoWindowContent(new HTML("<b>Path distance: " + metersToMiles(lengthInMeters) + " miles</b>"));
        map.getInfoWindow().open(event.getLatLng(), content);
      }
    });

    LinkedList<Polyline> polyList = runsMapping.get(runId);
    if (polyList != null) {
      polyList.add(polyline);
    } else {
      polyList = new LinkedList<Polyline>();
      polyList.add(polyline);
      runsMapping.put(runId, polyList);
    }

    map.addOverlay(polyline);
    map.setZoomLevel(13);
    map.panTo(LatLng.newInstance((maxLat + minLat) / 2.0, (maxLng + minLng) / 2.0));
  }

  // As an optimization we can just hide the Polylines for runs that were unchecked. And if they're checked again, we can just unhide the polyline.
  private void removePath(int runId) {
    LinkedList<Polyline> polyList = runsMapping.get(runId);
    if (polyList != null) {
      Polyline polyline = polyList.remove();
      map.removeOverlay(polyline);
      if (polyList.isEmpty()) {
        runsMapping.remove(runId);
      }
    }
  }

  public void getMapCoordinates(final int runId) {
    RemoteRequest.requestGetURI(new CommandResponse() {
      @Override
      public void executeOnSuccess(String responseTextSuccess) {
        generateMap(runId, responseTextSuccess);
      }

      @Override
      public void executeOnFailure(String responseTextFailure) {
      }

    }, Settings.getRunCoordinatesUrl(runId));
  }

  public void getRuns() {
    RemoteRequest.requestGetURI(new CommandResponse() {
      @Override
      public void executeOnSuccess(String responseTextSuccess) {
        buildRunsTable(responseTextSuccess);
        buildMonthlyRunsTable(responseTextSuccess);
        buildYearlyRunsTable(responseTextSuccess);
      }

      @Override
      public void executeOnFailure(String responseTextFailure) {
      }
    }, Settings.getRunsUrl());
  }

  String formatTimeElapsed(long millisElapsed) {
    long seconds_elapsed = convertMillisecondsToSeconds(millisElapsed);
    long hours = seconds_elapsed / 60 / 60;
    seconds_elapsed -= hours * 60 * 60;
    long minutes = seconds_elapsed / 60;
    seconds_elapsed -= minutes * 60;
    long seconds = seconds_elapsed;

    String timeElapsed = hours + " hr : " + (minutes < 10 ? "0" : "") + minutes + " min : " + (seconds < 10 ? "0" : "") + seconds + " sec";
    return timeElapsed;
  }

  public void buildRunsTable(String jsonResult) {
    JSONValue jsonValue = JSONParser.parseStrict(jsonResult);
    JSONArray runsArray = jsonValue.isObject().get("runs").isArray();

    final Grid grid = new Grid(runsArray.size() + 1, 6);
    grid.setStylePrimaryName("RunsTable");

    // Mapping of row number to run ID, where the row number if the array index.
    final ArrayList<Integer> runIds = new ArrayList<Integer>();

    grid.setHTML(0, 0, "<b>Toggle Path</b>");
    grid.setHTML(0, 1, "<b>Day</b>");
    grid.setHTML(0, 2, "<b>Start Time</b>");
    grid.setHTML(0, 3, "<b>Stop Time</b>");
    grid.setHTML(0, 4, "<b>Total Run Time</b>");
    grid.setHTML(0, 5, "<b>Distance</b>");

    DateTimeFormat dayFormat = DateTimeFormat.getFormat("EEE, MMM dd, yyyy");
    DateTimeFormat timeFormat = DateTimeFormat.getFormat("hh:mm:ss aa");

    for (int i = 0; i < runsArray.size(); i++) {
      int runId = Integer.parseInt((runsArray.get(i).isObject().get("run_id").isString().stringValue()));

      long timeStart = Long.parseLong((runsArray.get(i).isObject().get("time_start").isString().stringValue()));
      long timeEnd = Long.parseLong((runsArray.get(i).isObject().get("time_end").isString().stringValue()));
      double distance = Double.parseDouble((runsArray.get(i).isObject().get("distance").isString().stringValue()));

      final int row = i + 1;
      runIds.add(runId);
      grid.setText(row, 0, "Run " + i);

      final String showText = "Show";
      final String hideText = "Hide";

      Button button = new Button(showText, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Button clickedButton = (Button) event.getSource();

          int runId = runIds.get(row - 1);

          if (clickedButton.getText().equalsIgnoreCase(showText)) {
            getMapCoordinates(runId);
            clickedButton.setText(hideText);
          } else {
            removePath(runId);
            clickedButton.setText(showText);
          }
        }
      });

      grid.setWidget(row, 0, button);
      grid.setText(row, 1, dayFormat.format(new Date(timeStart)));
      grid.setText(row, 2, timeFormat.format(new Date(timeStart)));
      grid.setText(row, 3, timeFormat.format(new Date(timeEnd)));

      grid.setText(row, 4, formatTimeElapsed(timeEnd - timeStart));

      grid.setText(row, 5, Double.toString(((int) (metersToMiles(distance) * 100)) / 100.0) + " miles");
    }

    dailyRuns.setWidget(grid);
  }

  public Grid buildAggregatedDataTable(String jsonResult, String labelFormat, String labelName) {
    JSONValue jsonValue = JSONParser.parseStrict(jsonResult);
    JSONArray runsArray = jsonValue.isObject().get("runs").isArray();

    DateTimeFormat aggregatedFormat = DateTimeFormat.getFormat(labelFormat);

    final ArrayList<AggregatedRunData> aggregatedData = new ArrayList<AggregatedRunData>();

    AggregatedRunData currData = new AggregatedRunData("");
    for (int i = 0; i < runsArray.size(); i++) {
      int runId = Integer.parseInt((runsArray.get(i).isObject().get("run_id").isString().stringValue()));

      long timeStart = Long.parseLong((runsArray.get(i).isObject().get("time_start").isString().stringValue()));
      long timeEnd = Long.parseLong((runsArray.get(i).isObject().get("time_end").isString().stringValue()));
      double distance = Double.parseDouble((runsArray.get(i).isObject().get("distance").isString().stringValue()));

      String label = aggregatedFormat.format(new Date(timeStart));

      // Found a new label.
      if (!label.equals(currData.getLabel())) {
        if (!currData.getLabel().isEmpty()) {
          aggregatedData.add(currData);
        }
        currData = new AggregatedRunData(label);
      }
      currData.addRun(runId, timeStart, timeEnd, distance);
    }
    aggregatedData.add(currData);

    final Grid grid = new Grid(aggregatedData.size() + 1, 4);
    grid.setStylePrimaryName("RunsTable");

    grid.setHTML(0, 0, "<b>Toggle Paths</b>");
    grid.setHTML(0, 1, "<b>" + labelName + "</b>");
    grid.setHTML(0, 2, "<b>Total Time</b>");
    grid.setHTML(0, 3, "<b>Total Distance</b>");

    for (int i = 0; i < aggregatedData.size(); i++) {
      final String showText = "Show All";
      final String hideText = "Hide All";

      final int currDataRow = i;

      Button button = new Button(showText, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Button clickedButton = (Button) event.getSource();

          if (clickedButton.getText().equalsIgnoreCase(showText)) {
            for (int currRunId : aggregatedData.get(currDataRow).getRunIds()) {
              getMapCoordinates(currRunId);
            }

            clickedButton.setText(hideText);
          } else {
            for (int currRunId : aggregatedData.get(currDataRow).getRunIds()) {
              removePath(currRunId);
            }

            clickedButton.setText(showText);
          }
        }
      });

      grid.setWidget(i + 1, 0, button);
      grid.setText(i + 1, 1, aggregatedData.get(currDataRow).getLabel());
      grid.setText(i + 1, 2, formatTimeElapsed(aggregatedData.get(currDataRow).getTotalTime()));
      grid.setText(i + 1, 3, Double.toString(((int) (metersToMiles(aggregatedData.get(currDataRow).getTotalDistance()) * 100)) / 100.0) + " miles");
    }

    return grid;
  }

  public void buildMonthlyRunsTable(String jsonResult) {
    monthlyRuns.setWidget(buildAggregatedDataTable(jsonResult, "MMM yyyy", "Month"));
  }

  public void buildYearlyRunsTable(String jsonResult) {
    yearlyRuns.setWidget(buildAggregatedDataTable(jsonResult, "yyyy", "Year"));
  }

  public void populateSettings() {
    final HTML accessIdLabel = new HTML();
    userDataService.getAccessId(new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        accessIdLabel.setHTML("<p>" + caught.getMessage() + "</p>");
      }

      @Override
      public void onSuccess(String result) {
        accessIdLabel.setHTML("<p>Access Key: " + result + "</p><p><a href=\"" + Settings.getAccessIdUrl(result) + "\">My Run Stats Page</a></p>");
      }
    });

    settings.setWidget(accessIdLabel);
  }

  public long convertMillisecondsToSeconds(long milliseconds) {
    return milliseconds / 1000;
  }

  public double asTheCrowFliesDistance(LatLng pointA, LatLng pointB) {
    // http://en.wikipedia.org/wiki/Great-circle_distance
    return 0;
  }

  public double metersToMiles(double meters) {
    return meters * 0.000621371192;
  }
}
