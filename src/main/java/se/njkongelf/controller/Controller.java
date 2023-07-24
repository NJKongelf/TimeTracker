package se.njkongelf.controller;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    private List<LocalDateTime> timelist;
    private Stage stage;

    private ExecutorService threadpool = Executors.newFixedThreadPool(2);
    private ObservableList<String> listview;

    private String oldClockvalue;
    private ObjectProperty<ObservableList<String>> listObjectProperty;
    @FXML
    private ListView<String> label;

    @FXML
    private TextField clock;

    public Controller() {

    }

    @FXML
    public void handleButton(ActionEvent event) {
        timelist.add(LocalDateTime.now());
        listview.add(timelist.get(timelist.size() - 1).format(DateTimeFormatter.ofPattern("HH:mm:ss YYYY-MM-dd")));
        int items = label.getItems().size();
        label.scrollTo(items);
        label.refresh();
    }

    @FXML
    public void exitOnclick(ActionEvent event) {
        stage.setAlwaysOnTop(false);
        threadpool.shutdownNow();
        stage.close();
    }

    public void initialize() {
        timelist = new ArrayList<>();
        listview = FXCollections.observableArrayList();
        label.itemsProperty().setValue(listview);
        oldClockvalue= currentTime();
        startClock(threadpool);
    }

    protected void startClock(ExecutorService threadpool) {

            threadpool.submit( new Task() {
                @Override
                protected Object call() throws Exception {
                    while (!threadpool.isShutdown()){
                        updateClock(clock);
                    }
                    return null;
                }
            });

    }

    protected void updateClock(TextField clock) {

        if(!clock.getText().equals(currentTime()))
            clock.setText(currentTime());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private String currentTime(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}