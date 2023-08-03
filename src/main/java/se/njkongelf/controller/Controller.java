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
import javafx.util.converter.LongStringConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private Path path;
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
        try {
            readBackupFile();
        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
        }
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
            backup_File();
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

    private void backup_File() throws IOException {
        if (timelist.size() > 0) {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + File.separator + "Timetracker" + File.separator + "Timetracked_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")) + ".ttb");
            for (LocalDateTime s : timelist) {
                fileWriter.write(s.toEpochSecond(ZoneOffset.UTC) + "\n");
            }
            fileWriter.flush();
            fileWriter.close();
        }
    }

    private void readBackupFile() throws IOException {
        String file = System.getProperty("user.home")
                + File.separator
                + "Timetracker"
                + File.separator
                + "Timetracked_"
                + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
                + ".ttb";
        if (Files.exists(Paths.get(file), LinkOption.NOFOLLOW_LINKS)) {
            FileReader stream = new FileReader(file);
            BufferedReader streamReader = new BufferedReader(stream);
            List<String> stringList = streamReader.lines().toList();
            stringList.stream().forEach(s -> {
                LongStringConverter longStringConverter = new LongStringConverter();
                timelist.add(LocalDateTime
                        .ofEpochSecond(longStringConverter.fromString(s), 0, ZoneOffset.UTC));
            });
            timelist.stream().forEach(time -> {
                listview.add(time
                        .format(DateTimeFormatter
                                .ofPattern("HH:mm:ss YYYY-MM-dd")));
            });
            long time = 0;

            if (timelist.size() > 0) {
                calculatedTime.set(time);
                starWorktime();
            }
            if (timelist.size() % 2 == 0) {
                time=calcEvenlist(time);
                calculatedTime.set(time);
                starWorktime();
                trackedTime.setText(LocalDateTime
                        .ofEpochSecond(time, 0, ZoneOffset.UTC)
                        .format(DateTimeFormatter
                                .ofPattern("HH:mm:ss")));
            }else{
                time=calcUnEvenList(time);
                calculatedTime.set(time);
                trackedTime.setText(LocalDateTime
                        .ofEpochSecond(time, 0, ZoneOffset.UTC)
                        .format(DateTimeFormatter
                                .ofPattern("HH:mm:ss")));
            }
        }
    }
    private long calcEvenlist(long time){
        for (int i = timelist.size(); i >= 2; i = i - 2) {
            long a = timelist.get(i - 1).toEpochSecond(ZoneOffset.UTC);
            long b = timelist.get(i - 2).toEpochSecond(ZoneOffset.UTC);
            time = time + (a - b);
        }
        return time;
    }
    private long calcUnEvenList(long time){
        if (timelist.size() >1){
            time=LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)-timelist.get(timelist.size()-1).toEpochSecond(ZoneOffset.UTC);
            for (int i = timelist.size()-1; i >= 2; i = i - 2) {
                long a = timelist.get(i - 1).toEpochSecond(ZoneOffset.UTC);
                long b = timelist.get(i - 2).toEpochSecond(ZoneOffset.UTC);
                time = time + (a - b);
            }
        }else{
            time=LocalDateTime.now()
                    .toEpochSecond(ZoneOffset.UTC)-timelist.get(timelist.size()-1).toEpochSecond(ZoneOffset.UTC);
        }
        return time;
    }
}