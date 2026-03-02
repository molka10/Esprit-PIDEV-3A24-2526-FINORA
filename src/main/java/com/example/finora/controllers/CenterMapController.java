package com.example.finora.controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import com.example.finora.services.GeoService;
import com.example.finora.services.IpLocationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CenterMapController {

    @FXML
    private TextField txtCity;
    @FXML
    private WebView webView;

    private final GeoService geoService = new GeoService();
    private final IpLocationService ipLocationService = new IpLocationService();

    private final List<Center> centers = new ArrayList<>();

    private boolean mapReady = false;

    @FXML
    public void initialize() {
        if (webView == null)
            return;

        centers.add(new Center(1, "FINORA Tunis", "Centre Urbain Nord", 36.8532, 10.2040));
        centers.add(new Center(2, "FINORA Sousse", "Sahloul", 35.8256, 10.5887));
        centers.add(new Center(3, "FINORA Sfax", "Route Lafrane", 34.7406, 10.7603));

        var engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener((obs, oldS, newS) -> {
            if (newS == Worker.State.SUCCEEDED) {
                mapReady = true;

                // ✅ connect window.javaBridge for JS -> Java callbacks
                try {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaBridge", new JavaBridge());
                } catch (Exception ignored) {
                }

                // ✅ draw centers initially
                if (!centers.isEmpty()) {
                    engine.executeScript("renderCenters(" + toJsonCenters(centers) + ", -1);");
                }
            }
        });

        engine.load(getClass().getResource("/map/centers_map.html").toExternalForm());
    }

    @FXML
    private void onUseMyLocation() {
        if (!mapReady) {
            showError("Map is still loading. Try again in a second.");
            return;
        }

        new Thread(() -> {
            try {
                double[] coords = ipLocationService.getMyLocation();
                double lat = coords[0];
                double lng = coords[1];

                Center closest = findClosest(centers, lat, lng);

                Platform.runLater(() -> {
                    webView.getEngine().executeScript("setUserLocation(" + lat + "," + lng + ");");
                    webView.getEngine()
                            .executeScript("renderCenters(" + toJsonCenters(centers) + "," + closest.id() + ");");
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Location failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onFind() {
        try {
            if (!mapReady) {
                showError("Map is still loading. Try again in a second.");
                return;
            }

            String city = (txtCity == null || txtCity.getText() == null) ? "" : txtCity.getText().trim();
            if (city.isEmpty()) {
                showError("Please enter a city.");
                return;
            }

            if (centers.isEmpty()) {
                showError("No centers found.");
                return;
            }

            double[] coords = geoService.getCoordinates(city);
            double lat = coords[0];
            double lng = coords[1];

            Center closest = findClosest(centers, lat, lng);
            if (closest == null) {
                showError("Could not find closest center.");
                return;
            }

            String centersJson = toJsonCenters(centers);

            // ✅ render + highlight closest
            webView.getEngine().executeScript("renderCenters(" + centersJson + "," + closest.id() + ");");

            // ✅ show user on map (NOW the JS function exists)
            webView.getEngine().executeScript("setUserLocation(" + lat + "," + lng + ");");

        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void onClose() {
        Stage s = (Stage) webView.getScene().getWindow();
        s.close();
    }

    // =======================
    // JS -> Java Bridge
    // =======================
    public class JavaBridge {
        public void onGeo(double lat, double lng) {
            Platform.runLater(() -> {
                if (!mapReady || centers.isEmpty())
                    return;

                Center closest = findClosest(centers, lat, lng);
                if (closest == null)
                    return;

                webView.getEngine()
                        .executeScript("renderCenters(" + toJsonCenters(centers) + "," + closest.id() + ");");
                webView.getEngine().executeScript("setUserLocation(" + lat + "," + lng + ");");
            });
        }

        public void onGeoError(String msg) {
            Platform.runLater(() -> showError("Geolocation error: " + msg));
        }
    }

    // =======================
    // Distance + Helpers
    // =======================

    private Center findClosest(List<Center> list, double userLat, double userLng) {
        Center best = null;
        double bestD = Double.MAX_VALUE;

        for (Center c : list) {
            double d = haversineKm(userLat, userLng, c.lat(), c.lng());
            if (d < bestD) {
                bestD = d;
                best = c;
            }
        }
        return best;
    }

    // ✅ Haversine distance in KM
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String toJsonCenters(List<Center> list) {
        // ✅ simple safe JSON builder
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Center c = list.get(i);
            if (i > 0)
                sb.append(",");
            sb.append("{")
                    .append("\"id\":").append(c.id()).append(",")
                    .append("\"name\":\"").append(escape(c.name())).append("\",")
                    .append("\"address\":\"").append(escape(c.address())).append("\",")
                    .append("\"lat\":").append(String.format(Locale.US, "%.6f", c.lat())).append(",")
                    .append("\"lng\":").append(String.format(Locale.US, "%.6f", c.lng()))
                    .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ✅ Small model
    public record Center(int id, String name, String address, double lat, double lng) {
    }
}