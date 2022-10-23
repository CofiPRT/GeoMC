package ro.cofi.geomc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GeoMCApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GeoMCApplication.class.getResource("geomc-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("GeoMC Coordinate Converter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}