package se.njkongelf;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.njkongelf.controller.Controller;

import java.io.IOException;

public class TimeTracker extends Application {
    private Controller controller;
    private  Scene scene;
    public static void main(String[] args) {
        launch();
    }
    private Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setController( new Controller());
        controller = fxmlLoader.getController();
        fxmlLoader.setLocation(TimeTracker.class.getResource("/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Main"));
        stage.setScene(scene);
        controller.setStage(stage);
        stage.show();
        stage.setAlwaysOnTop(true);
    }
    @Override
    public void stop() throws Exception {
        super.stop();
        controller.exitOnclick(new ActionEvent());
    }
}