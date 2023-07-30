package se.njkongelf.controller;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
    @FXML
    private Spinner<Integer> workingHours;
    private Property<SpinnerValueFactory<Integer>> workingHoursValueProperty;
    private SpinnerValueFactory<Integer> workingHoursValue;
    private List<LocalDateTime> timelist;
    private AtomicBoolean runtrackedTime;
    private Stage stage;
    private AtomicLong calculatedTime;
    private ExecutorService threadpool = Executors.newFixedThreadPool(2);
    private ObservableList<String> listview;
    private SimpleStringProperty clockString;
    private SimpleStringProperty trackedTimeString;

    public Controller() {
    }

    public void initialize() {
        timelist = new ArrayList<>();
        listview = FXCollections.observableArrayList();
        label.itemsProperty().setValue(listview);
        startClock(threadpool);
        calculatedTime = new AtomicLong(0);
        runtrackedTime = new AtomicBoolean(false);
        trackedTimeString = new SimpleStringProperty();
        clockString = new SimpleStringProperty();
        clock.textProperty().bindBidirectional(clockString);
        trackedTime.textProperty().bindBidirectional(trackedTimeString);
    }

    public void handleButton(ActionEvent event) {
        timelist.add(LocalDateTime.now());
        listview.add(timelist.get(timelist.size() - 1).format(DateTimeFormatter.ofPattern("HH:mm:ss YYYY-MM-dd")));
        //   if(!(size % 2 == 0))
        starWorktime();
        int items = label.getItems().size();
        label.scrollTo(items);
        label.refresh();
    }

    public void exitOnclick(ActionEvent event) {
        try {
            printToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage.setAlwaysOnTop(false);
        runtrackedTime.set(false);
        threadpool.shutdownNow();
        stage.close();
    }

    protected void startClock(ExecutorService threadpool) {
        threadpool.submit(new Task() {
            @Override
            protected Object call() throws Exception {
                while (!threadpool.isShutdown()) {
                    updateClock();
                }
                return null;
            }
        });
    }

    protected void updateClock() {
        clockString.set(currentTime());
    }

    private void starWorktime() {

        timeWorked.setVisible(true);
        if (start_stop.getText().equals("Start")) {
            start_stop.setText("Stop");
            runtrackedTime.set(false);
            workTime(threadpool);
        } else {
            start_stop.setText("Start");
            runtrackedTime.set(true);
        }
    }

    protected void updateWorktime(String time) {
        trackedTimeString.set(time);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private String currentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void workTime(ExecutorService threadpool) {
        threadpool.submit(new Task() {
            @Override
            protected Object call() throws Exception {
                while (!runtrackedTime.get()) {
                    String s = LocalDateTime.ofEpochSecond(calculatedTime.get(), 0, ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    updateWorktime(s);
                    Thread.sleep(1000);
                    calculatedTime.incrementAndGet();
                }
                return null;
            }
        });
    }

    private void printToFile() throws IOException {
        if (timelist.size() > 0) {
            FileWriter fileWriter = new FileWriter("Timetracked_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")) + ".txt");
            for (LocalDateTime s : timelist) {
                fileWriter.write(s.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n");
            }
            fileWriter.write("Tracked time: " + LocalDateTime.ofEpochSecond(calculatedTime.get(), 0, ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            fileWriter.flush();
            fileWriter.close();
        }
    }

}