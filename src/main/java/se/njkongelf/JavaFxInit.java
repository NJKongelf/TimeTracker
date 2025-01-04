package se.njkongelf;

import feign.Feign;
import feign.codec.Decoder;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.njkongelf.controller.Controller;
import se.njkongelf.feign.InternetCheckGoogle;
import se.njkongelf.model.Model;

import java.io.IOException;

public class JavaFxInit extends Application {
  private Controller controller;
  private Scene scene;
  private Model model;

  private Parent loadFXML(String fxml) throws IOException {
    model = new Model();
    FXMLLoader fxmlLoader = new FXMLLoader();
    fxmlLoader.setController(new Controller(model));
    controller = fxmlLoader.getController();
    controller.setInternetCheckGoogle(Feign.builder().target(InternetCheckGoogle.class,"http://www.google.com"));
    fxmlLoader.setLocation(TimeTracker.class.getResource("/" + fxml + ".fxml"));
    return fxmlLoader.load();
  }

  @Override
  public void start(Stage stage) throws IOException {
    scene = new Scene(loadFXML("Main"));
    stage.setTitle("Time Tracker");
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

  public static void main(String[] args) {
    Application.launch(JavaFxInit.class,args);
  }
}
