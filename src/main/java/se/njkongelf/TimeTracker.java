package se.njkongelf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class TimeTracker extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
//        scene = new Scene(loadFXML("primary"));
//        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
//        stage.setScene(scene);
//        stage.show();
        String javaVersion = "17";
        String javafxVersion = "17";
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Scene scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TimeTracker.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
