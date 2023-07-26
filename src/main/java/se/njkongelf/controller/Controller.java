package se.njkongelf.controller;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BubbleChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoPeriod;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {
    @FXML
    private Button start_stop;
    @FXML
    private Label timeWorked;
    @FXML
    private ListView<String> label;
    @FXML
    private TextField trackedTime;
    @FXML
    private TextField clock;
    private List<LocalDateTime> timelist;
    private Stage stage;
    private ExecutorService threadpool = Executors.newFixedThreadPool(2);
    private ObservableList<String> listview;;

    public Controller() {

    }

    public void handleButton(ActionEvent event) {
        timelist.add(LocalDateTime.now());
        listview.add(timelist.get(timelist.size() - 1).format(DateTimeFormatter.ofPattern("HH:mm:ss YYYY-MM-dd")));
        int size = timelist.size();
        if(!(size % 2 == 0))
            updateWorkTime(trackedTime);
        int items = label.getItems().size();
        label.scrollTo(items);
        label.refresh();
    }

    public void exitOnclick(ActionEvent event) {
        stage.setAlwaysOnTop(false);
        threadpool.shutdownNow();
        stage.close();
    }

    public void initialize() {
        timelist = new ArrayList<>();
        listview = FXCollections.observableArrayList();
        label.itemsProperty().setValue(listview);
        start_stop.setText("Start");
        startClock(threadpool);
    }
    protected void startClock(ExecutorService threadpool) {

        threadpool.submit(new Task() {
            @Override
            protected Object call() throws Exception {
                while (!threadpool.isShutdown()) {
                    updateClock(clock);
                }
                return null;
            }
        });

    }
    protected void updateClock(TextField clock) {

        if (!clock.getText().equals(currentTime()))
            clock.setText(currentTime());
    }

    protected void updateWorkTime(TextField timer){
        timeWorked.setVisible(true);
        start_stop.setText("Stop");
        System.out.println(timelist.get(timelist.size() - 1));
        System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        //Calc of past time.
        System.out.println(LocalDateTime.ofEpochSecond(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - timelist.get(0).toEpochSecond(ZoneOffset.UTC),0,ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    private String currentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }



}