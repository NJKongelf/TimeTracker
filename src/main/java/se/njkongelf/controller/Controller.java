package se.njkongelf.controller;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import se.njkongelf.model.Model;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Controller {
    @FXML
    private Button savePath;
    @FXML
    private Button start_stop;
    @FXML
    private Label timeWorked;
    @FXML
    private Label label_OverTime;
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField trackedTime;
    @FXML
    private TextField clock;
    @FXML
    private TextField overTime;
    @FXML
    private Spinner<Integer> workingHours;
    private Property<Integer> workingHoursValueProperty;
    private SpinnerValueFactory<Integer> workingHoursValue;
    private List<LocalDateTime> timelist;
    private AtomicBoolean runtrackedTime;
    private AtomicBoolean runOverTime;
    private Stage stage;
    private AtomicLong calculatedTime;
    private AtomicLong calculatedOverTime;
    private ExecutorService threadpool = Executors.newFixedThreadPool(2);
    private ObservableList<String> listviewObserv;
    private SimpleStringProperty clockString;
    private SimpleStringProperty overTimeString;
    private SimpleStringProperty trackedTimeString;
    private Model model;
    private Properties properties;
    private String settingsfile;

    public Controller(Model model) {
        this.model = model;
    }

    public void initialize() {
        settingsfile = "conf/settings.properties";
        properties = model.readInSettingsFile(settingsfile);
        model.setProperties(properties);
        clockString = new SimpleStringProperty();
        clock.textProperty().bindBidirectional(clockString);
        overTimeString = new SimpleStringProperty();
        overTime.textProperty().bindBidirectional(overTimeString);
        model.setController(this);
        timelist = new ArrayList<>();
        listviewObserv = FXCollections.observableArrayList();
        listView.itemsProperty().setValue(listviewObserv);
        workingHoursValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 24);
        workingHoursValueProperty = new SimpleIntegerProperty().asObject();
        workingHoursValueProperty.setValue(Integer.valueOf(properties.getProperty("workinghours")));
        workingHours.setValueFactory(workingHoursValue);
        workingHoursValue.valueProperty().bindBidirectional(workingHoursValueProperty);
        workingHoursValueProperty.addListener(model.spinngerListner());
        calculatedTime = new AtomicLong(0);
        runtrackedTime = new AtomicBoolean(false);
        runOverTime = new AtomicBoolean(false);
        trackedTimeString = new SimpleStringProperty();
        trackedTime.textProperty().bindBidirectional(trackedTimeString);
        try {
            model.readBackupFile(timelist, listviewObserv, calculatedTime, trackedTime);
        } catch (IOException e) {
        }
        startClock(threadpool);
        setCalculatedOverTime();
    }

    public void handleButton(ActionEvent event) {
        timelist.add(LocalDateTime.now());
        listviewObserv.add(timelist.get(timelist.size() - 1).format(DateTimeFormatter.ofPattern("HH:mm:ss YYYY-MM-dd")));
        starWorktime();
        int items = listView.getItems().size();
        listView.scrollTo(items);
        listView.refresh();
    }

    public void exitOnclick(ActionEvent event) {

        try {
            model.saveSettings(settingsfile);
            model.printToFile(timelist, calculatedTime);
            model.backup_File(timelist);
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

    public void starWorktime() {

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
                    setCalculatedOverTime();
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

    public void saveFileDialog(ActionEvent actionEvent) {
        //Show a file dialog that returns a selected file for opening or null if no file was selected.
        FileChooser fileChooser = new FileChooser ();
        fileChooser.setTitle ("Spara fil");
        fileChooser.setInitialDirectory (new File(System.getProperty ("user.home") + File.separator + "Documents"));
        fileChooser.getExtensionFilters ().addAll (
                new FileChooser.ExtensionFilter ("TXT", "*.txt"));
       // Filehandler filehandler = new Filehandler ();
        File        path        = fileChooser.showSaveDialog (stage);


        //Path can be null if abort was selected
        if (path != null) {
            //We have a valid File object. Use with FileReader or FileWriter
            System.out.println (path.getAbsolutePath ());
          //  filehandler.saveFileSVG (model, path, (int) canvas.getWidth (), (int) canvas.getHeight ());
        } else {
            System.out.println ("no file");
        }
    }
    protected void setCalculatedOverTime(){
      Integer hours = workingHoursValueProperty.getValue();
      Integer seconds = (hours*60)*60;

      if(calculatedTime.get() >= seconds){
          long time = calculatedTime.get() - seconds;
          runOverTime.set(true);
          overTimeString.set(LocalDateTime.ofEpochSecond(time,0,ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
          label_OverTime.setVisible(true);
          overTime.setVisible(true);
      }else{
          runOverTime.set(false);
          label_OverTime.setVisible(false);
          overTime.setVisible(false);
      }
    }
}