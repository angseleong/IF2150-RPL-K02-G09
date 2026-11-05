package sehati;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Titik masuk aplikasi SEHATI.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // TODO(#3): ganti dengan cangkang aplikasi dan LoginForm.
        stage.setTitle("SEHATI");
        stage.setScene(new Scene(new StackPane(new Label("SEHATI")), 1280, 800));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
