package com.example.finora.controllers;

import com.example.finora.services.CenterService;
import com.example.finora.services.GeoService;
import com.example.finora.services.IpLocationService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CenterMapController {

    @FXML private TextField txtCity;
    @FXML private WebView webView;

    private final GeoService geoService = new GeoService();
    private final IpLocationService ipLocationService = new IpLocationService();

    /**
     * ✅ DTO for the map, to avoid conflict with com.example.finora.entities.Center
     */
    public record MapCenter(int id, String name, String address, double lat, double lng) {}

    private final List<MapCenter> centers = new ArrayList<>();
    private boolean mapReady = false;

    @FXML
    public void initialize() {
        if (webView == null) return;

        // ✅ Load centers from DB -> MapCenter list
        reloadCentersFromDbOrFallback();

        var engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener((obs, oldS, newS) -> {
            if (newS == Worker.State.SUCCEEDED) {
                mapReady = true;

                // optional bridge (if your JS calls window.javaBridge.onGeo / onGeoError)
                try {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaBridge", new JavaBridge());
                } catch (Exception e) {
                    System.err.println("⚠️ JavaBridge init failed: " + e.getMessage());
                }

                if (!centers.isEmpty()) {
                    engine.executeScript("renderCenters(" + toJsonCenters(centers) + ", -1);");
                }
            }
        });

        // ✅ safe resource loading
        URL url = getClass().getResource("/map/centers_map.html");
        engine.load(Objects.requireNonNull(url, "centers_map.html not found in /map").toExternalForm());
    }

    // ==========================================================
    // Actions (matching your FXML)
    // ==========================================================

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

            MapCenter closest = findClosest(centers, lat, lng);
            if (closest == null) {
                showError("Could not find closest center.");
                return;
            }

            webView.getEngine().executeScript("renderCenters(" + toJsonCenters(centers) + "," + closest.id() + ");");
            webView.getEngine().executeScript("setUserLocation(" + lat + "," + lng + ");");

        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onUseMyLocation() {
        if (!mapReady) {
            showError("Map is still loading. Try again in a second.");
            return;
        }
        if (centers.isEmpty()) {
            showError("No centers found.");
            return;
        }

        new Thread(() -> {
            try {
                double[] coords = ipLocationService.getMyLocation();
                double lat = coords[0];
                double lng = coords[1];

                MapCenter closest = findClosest(centers, lat, lng);

                Platform.runLater(() -> {
                    webView.getEngine().executeScript("setUserLocation(" + lat + "," + lng + ");");
                    webView.getEngine().executeScript(
                            "renderCenters(" + toJsonCenters(centers) + "," + (closest != null ? closest.id() : -1) + ");"
                    );
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Location failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onClose() {
        Stage s = (Stage) webView.getScene().getWindow();
        s.close();
    }

    // ==========================================================
    // Optional: Add Center (ONLY if you add a button in FXML)
    // ==========================================================

    @FXML
    private void onAddCenter() {
        Dialog<MapCenter> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Centre");
        dialog.setHeaderText("Ajouter un nouveau centre de formation");

        ButtonType btnSave = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField txtName = new TextField();
        txtName.setPromptText("Nom du centre");

        TextField txtAddress = new TextField();
        txtAddress.setPromptText("Adresse complète");

        TextField txtLat = new TextField();
        txtLat.setPromptText("Latitude (ex: 36.8065)");

        TextField txtLng = new TextField();
        txtLng.setPromptText("Longitude (ex: 10.1815)");

        Button btnGetCoords = new Button("📍 Obtenir coordonnées");
        btnGetCoords.setOnAction(e -> {
            String address = txtAddress.getText() == null ? "" : txtAddress.getText().trim();
            if (address.isEmpty()) {
                showError("Veuillez d'abord entrer une adresse");
                return;
            }
            try {
                double[] coords = geoService.getCoordinates(address);
                txtLat.setText(String.format(Locale.US, "%.6f", coords[0]));
                txtLng.setText(String.format(Locale.US, "%.6f", coords[1]));
                showInfo("Coordonnées obtenues avec succès !");
            } catch (Exception ex) {
                showError("Impossible de géolocaliser cette adresse: " + ex.getMessage());
            }
        });

        grid.add(new Label("Nom du centre:"), 0, 0);
        grid.add(txtName, 1, 0);

        grid.add(new Label("Adresse:"), 0, 1);
        grid.add(txtAddress, 1, 1);
        grid.add(btnGetCoords, 2, 1);

        grid.add(new Label("Latitude:"), 0, 2);
        grid.add(txtLat, 1, 2);

        grid.add(new Label("Longitude:"), 0, 3);
        grid.add(txtLng, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSave) {
                String name = txtName.getText() == null ? "" : txtName.getText().trim();
                String address = txtAddress.getText() == null ? "" : txtAddress.getText().trim();

                if (name.isEmpty() || address.isEmpty()) {
                    showError("Tous les champs sont requis");
                    return null;
                }

                try {
                    double lat = Double.parseDouble(txtLat.getText().trim());
                    double lng = Double.parseDouble(txtLng.getText().trim());
                    return new MapCenter(0, name, address, lat, lng);
                } catch (Exception ex) {
                    showError("Latitude et Longitude doivent être des nombres valides");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::saveCenterToDbAndRefresh);
    }

    private void saveCenterToDbAndRefresh(MapCenter center) {
        try {
            CenterService centerService = new CenterService();

            // ✅ Convert MapCenter -> Entity Center
            // 🔧 If your entity constructor differs, adapt here:
            com.example.finora.entities.Center entity =
                    new com.example.finora.entities.Center(
                            0,
                            center.name(),
                            center.address(),
                            center.lat(),
                            center.lng()
                    );

            centerService.add(entity);

            showInfo("Centre ajouté avec succès !");
            reloadCentersFromDbOrFallback();

            if (mapReady) {
                webView.getEngine().executeScript("renderCenters(" + toJsonCenters(centers) + ", -1);");
            }
        } catch (Exception e) {
            showError("Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================================
    // Load centers from DB (fixes your original addAll errors)
    // ==========================================================

    private void reloadCentersFromDbOrFallback() {
        centers.clear();

        try {
            CenterService centerService = new CenterService();
            List<com.example.finora.entities.Center> dbCenters = centerService.getAll();

            for (com.example.finora.entities.Center c : dbCenters) {
                centers.add(toMapCenter(c));
            }

            System.out.println("✅ " + centers.size() + " centres chargés depuis la DB");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement centres: " + e.getMessage());
            e.printStackTrace();

            centers.add(new MapCenter(1, "FINORA Tunis", "Centre Urbain Nord", 36.8532, 10.2040));
            centers.add(new MapCenter(2, "FINORA Sousse", "Sahloul", 35.8256, 10.5887));
            centers.add(new MapCenter(3, "FINORA Sfax", "Route Lafrane", 34.7406, 10.7603));
        }
    }

    private MapCenter toMapCenter(com.example.finora.entities.Center c) {
        return new MapCenter(
                c.id(),
                c.name(),
                c.address(),
                c.lat(),
                c.lng()
        );
    }
    // ==========================================================
    // JS -> Java Bridge (optional)
    // ==========================================================

    public class JavaBridge {
        public void onGeo(double lat, double lng) {
            Platform.runLater(() -> {
                if (!mapReady || centers.isEmpty()) return;

                MapCenter closest = findClosest(centers, lat, lng);
                if (closest == null) return;

                webView.getEngine().executeScript("renderCenters(" + toJsonCenters(centers) + "," + closest.id() + ");");
                webView.getEngine().executeScript("setUserLocation(" + lat + "," + lng + ");");
            });
        }

        public void onGeoError(String msg) {
            Platform.runLater(() -> showError("Geolocation error: " + msg));
        }
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    private MapCenter findClosest(List<MapCenter> list, double userLat, double userLng) {
        MapCenter best = null;
        double bestD = Double.MAX_VALUE;

        for (MapCenter c : list) {
            double d = haversineKm(userLat, userLng, c.lat(), c.lng());
            if (d < bestD) {
                bestD = d;
                best = c;
            }
        }
        return best;
    }

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

    private String toJsonCenters(List<MapCenter> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            MapCenter c = list.get(i);
            if (i > 0) sb.append(",");

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
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}