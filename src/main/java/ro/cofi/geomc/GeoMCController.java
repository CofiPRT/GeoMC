package ro.cofi.geomc;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GeoMCController {
    private static final double WORLD_WIDTH = 101374;
    private static final double WORLD_HEIGHT = 55295;

    private static final Set<Character> VALID_SUFFIXES = new HashSet<>(Arrays.asList('N', 'S', 'E', 'W'));


    @FXML
    private TextField mcX;
    @FXML
    private TextField mcZ;
    @FXML
    private TextField mcDec;
    @FXML
    private TextField mcDecS;
    @FXML
    private TextField mcSexag;
    @FXML
    private TextField mcSexagS;

    @FXML
    private TextField gLat;
    @FXML
    private TextField gLon;
    @FXML
    private TextField gRes;

    @FXML
    private void initialize() {
        // create a change listener that will update the Minecraft Result text area
        ChangeListener<String> mcListener = (observable, oldValue, newValue) -> {
            // parse X and Z coordinates as doubles
            double x;
            double z;

            try {
                String xStr = mcX.getText().trim();
                String zStr = mcZ.getText().trim();

                // if any string is empty, return
                if (xStr.isEmpty() || zStr.isEmpty()) {
                    mcDec.setText("");
                    mcDecS.setText("");
                    mcSexag.setText("");
                    mcSexagS.setText("");
                    return;
                }

                x = Double.parseDouble(xStr);
                z = Double.parseDouble(zStr);
            } catch (NumberFormatException e) {
                // if the input is not a number, show an error message
                String message = "Invalid input!";
                mcDec.setText(message);
                mcDecS.setText(message);
                mcSexag.setText(message);
                mcSexagS.setText(message);

                // set color to red
                String style = "-fx-text-fill: red;";
                mcDec.setStyle(style);
                mcDecS.setStyle(style);
                mcSexag.setStyle(style);
                mcSexagS.setStyle(style);

                return;
            }

            // set the text of the text areas
            String[] messages = computeMCResult(x, z);
            mcDec.setText(messages[0]);
            mcDecS.setText(messages[1]);
            mcSexag.setText(messages[2]);
            mcSexagS.setText(messages[3]);

            // set color to black
            String style = "-fx-text-fill: black;";
            mcDec.setStyle(style);
            mcDecS.setStyle(style);
            mcSexag.setStyle(style);
            mcSexagS.setStyle(style);
        };

        // add the change listener to the X and Z text fields
        mcX.textProperty().addListener(mcListener);
        mcZ.textProperty().addListener(mcListener);

        // create a change listener that will update the Geo Result text area
        ChangeListener<String> gListener = (observable, oldValue, newValue) -> {
            // parse latitude and longitude as doubles - they may be in decimal or sexagesimal format
            double lat;
            double lon;

            try {
                // trim the input
                String latStr = gLat.getText().trim();
                String lonStr = gLon.getText().trim();

                // if any string is empty, return
                if (latStr.isEmpty() || lonStr.isEmpty()) {
                    gRes.setText("");
                    return;
                }

                lat = parseGeographicCoordinate(latStr);
                lon = parseGeographicCoordinate(lonStr);
            } catch (NumberFormatException e) {
                // if the input is not a number, show an error message
                String message = "Invalid input!";
                gRes.setText(message);

                // set color to red
                gRes.setStyle("-fx-text-fill: red;");

                return;
            }

            gRes.setText(computeGResult(lat, lon));
            gRes.setStyle("-fx-text-fill: black;");
        };

        // add the change listener to the latitude and longitude text fields
        gLat.textProperty().addListener(gListener);
        gLon.textProperty().addListener(gListener);
    }

    private String[] computeMCResult(double x, double z) {
        // compute the latitude and longitude
        double lat = -z * 90 / WORLD_HEIGHT;
        double lon = x * 180 / WORLD_WIDTH;

        // decimal format
        String decimalNotation = String.format("%.5f, %.5f", lat, lon);

        // sexagesimal format - compute minutes and seconds - leave seconds as a double
        int latDeg = (int) lat;
        int latMin = (int) ((lat - latDeg) * 60);
        double latSec = (lat - latDeg - latMin / 60.0) * 3600;

        int lonDeg = (int) lon;
        int lonMin = (int) ((lon - lonDeg) * 60);
        double lonSec = (lon - lonDeg - lonMin / 60.0) * 3600;

        String sexagesimalNotation = String.format(
            "%d° %d' %.2f\", %d° %d' %.2f\"",
            latDeg, latMin, latSec,
            lonDeg, lonMin, lonSec
        );

        // decimal format with suffix - S and W are negative
        String decimalNotationWithSuffix = String.format(
            "%.5f %s, %.5f %s",
            Math.abs(lat), lat < 0 ? "S" : "N",
            Math.abs(lon), lon < 0 ? "W" : "E"
        );

        // sexagesimal format with suffix - S and W are negative
        String sexagesimalNotationWithSuffix = String.format(
            "%d° %d' %.2f\" %s, %d° %d' %.2f\" %s",
            Math.abs(latDeg), Math.abs(latMin), Math.abs(latSec), lat < 0 ? "S" : "N",
            Math.abs(lonDeg), Math.abs(lonMin), Math.abs(lonSec), lon < 0 ? "W" : "E"
        );

        return new String[] {
            decimalNotation,
            decimalNotationWithSuffix,
            sexagesimalNotation,
            sexagesimalNotationWithSuffix
        };
    }

    private double parseGeographicCoordinate(String input) {
        // check if the coordinate ends in a suffix - remove the suffix, and if it is S or W, negate the coordinate
        boolean isNegative = false;

        char lastChar = input.charAt(input.length() - 1);

        if (VALID_SUFFIXES.contains(lastChar)) {
            input = input.substring(0, input.length() - 1).trim();
            isNegative = lastChar == 'S' || lastChar == 'W';
        }

        double coordinate;

        // check if the coordinate is in decimal or sexagesimal format
        if (input.contains("°")) {
            // sexagesimal format
            String[] parts = input.split("[°'\"]");

            if (parts.length != 3) {
                throw new NumberFormatException();
            }

            double deg = Double.parseDouble(parts[0].trim());
            double min = Double.parseDouble(parts[1].trim());
            double sec = Double.parseDouble(parts[2].trim());

            coordinate = deg + min / 60.0 + sec / 3600.0;
        } else {
            // decimal format
            coordinate = Double.parseDouble(input);
        }

        return coordinate * (isNegative ? -1 : 1);
    }

    private String computeGResult(double lat, double lon) {
        // compute the X and Z coordinates
        double x = lon * WORLD_WIDTH / 180;
        double z = -lat * WORLD_HEIGHT / 90;

        // set the result text area
        return String.format("X: %.5f, Z: %.5f", x, z);
    }
}