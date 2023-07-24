package se.njkongelf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.njkongelf.controller.Controller;

import java.io.IOException;

public class TimeTracker extends Application {
    private Controller controller;
    private  Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Main"));
        //   scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        controller.setStage(stage);
        stage.show();
        stage.setAlwaysOnTop(true);
    }

//    static void setRoot(String fxml) throws IOException {
//        scene.setRoot(loadFXML(fxml));
//    }

    private Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setController( new Controller());
        controller = fxmlLoader.getController();
        fxmlLoader.setLocation(TimeTracker.class.getResource("/" + fxml + ".fxml"));
        //   fxmlLoader.setLocation(TimeTracker.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
