package se.njkongelf.controller;


import feign.FeignException;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import se.njkongelf.TimeTracker;
import se.njkongelf.db.services.TimeSheetService;
import se.njkongelf.feign.InternetCheckGoogle;
import se.njkongelf.model.BackUpFileHandler;
import se.njkongelf.model.Model;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
  private Label on_off_line;
  @FXML
  private ListView<String> listView;
  @FXML
  private ListView<String> listViewEdit;
  @FXML
  private TextField trackedTime;
  @FXML
  private TextField clock;
  @FXML
  private TextField timeEditField;
  @FXML
  private TextField overTime;
  @FXML
  private Spinner<Integer> workingHours;
  private Property<Integer> workingHoursValueProperty;
  private SpinnerValueFactory<Integer> workingHoursValue;
  private List<LocalDateTime> timelist;
  private AtomicBoolean runtrackedTime;
  private AtomicBoolean runOverTime;
  @Setter
  private Stage stage;
  private AtomicInteger listIndex;
  private AtomicLong calculatedTime;
  private AtomicLong calculatedOverTime;
  private ExecutorService threadpool = Executors.newFixedThreadPool(2);
  private ObservableList<String> listviewObserv;
  private ObservableList<String> listviewEditObserv;
  private SimpleStringProperty clockString;
  private SimpleStringProperty overTimeString;
  private SimpleStringProperty trackedTimeString;
  private Model model;
  private Properties properties;
  private String settingsfile;
  private ConfigurableApplicationContext context;
  private BackUpFileHandler backUpFileHandler;
  @Setter
  private InternetCheckGoogle internetCheckGoogle;

  public Controller(Model model) {
    this.model = model;
    this.backUpFileHandler = new BackUpFileHandler();
  }

  public void initialize() {
    settingsfile = "conf/settings.properties";
    properties = model.readInSettingsFile(settingsfile);
    model.setProperties(properties);
    context = TimeTracker.getContext();
    model.setDbonline(false);
    model.setFileHandler(backUpFileHandler);
    model.setLogger((Logger) context.getBean("logBean"));
    clockString = new SimpleStringProperty();
    clock.textProperty().bindBidirectional(clockString);
    overTimeString = new SimpleStringProperty();
    overTime.textProperty().bindBidirectional(overTimeString);
    model.setController(this);
    model.setTimeSheetService(context.getBean(TimeSheetService.class));
    timelist = new ArrayList<>();
    listviewObserv = FXCollections.observableArrayList();
    listviewEditObserv = FXCollections.observableArrayList();
    listView.itemsProperty().setValue(listviewObserv);
    listViewEdit.itemsProperty().setValue(listviewEditObserv);
    workingHoursValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 24);
    workingHoursValueProperty = new SimpleIntegerProperty().asObject();
    workingHoursValueProperty.setValue(Integer.valueOf(properties.getProperty("workinghours")));
    workingHours.setValueFactory(workingHoursValue);
    workingHoursValue.valueProperty().bindBidirectional(workingHoursValueProperty);
    workingHoursValueProperty.addListener(model.spinngerListner());
    calculatedTime = new AtomicLong(0);
    listIndex = new AtomicInteger(0);
    runtrackedTime = new AtomicBoolean(false);
    runOverTime = new AtomicBoolean(false);
    trackedTimeString = new SimpleStringProperty();
    trackedTime.textProperty().bindBidirectional(trackedTimeString);
    try {
      model.readBackupFile(timelist, listviewObserv, calculatedTime, trackedTime);
    } catch (IOException ignored) {
    }
    listviewEditObserv.addAll(listviewObserv);
    listViewEdit.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    listViewEdit.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        timeEditField.setText(newValue);
        listIndex.set(listViewEdit.getSelectionModel().getSelectedIndex());
   //     System.out.println(listIndex.get());
      }
    });
    Platform.runLater(() -> {
      if (model.isDbonline()) {
        on_off_line.setText("ONLINE");
        on_off_line.setTextFill(Paint.valueOf("#09f507"));
      }else {
        on_off_line.setTextFill(Paint.valueOf("#f50707"));
        on_off_line.setText("OFFLINE");
      }
    });
    Platform.runLater(() ->{
      List<String> oldBackupFiles= backUpFileHandler.backupFilesDates();
      if (oldBackupFiles.isEmpty()){
        System.out.println("no old files to process");
      }else{
        model.handleOldBackupFiles(oldBackupFiles);
      }
    });
    // TODO Fixa Azure Devops connection
//    Platform.runLater( () -> {
//      try {
//        internetCheckGoogle.internetcheck();
//        System.out.println("Internet available");
//      }catch (FeignException e){
//        System.out.println("No internet service available");
//      }
//    });
    startClock(threadpool);
    setCalculatedOverTime();
  }

  public void handleButton(ActionEvent event) {
    timelist.add(LocalDateTime.now());
    listviewObserv.add(timelist.get(timelist.size() - 1).format(DateTimeFormatter.ofPattern("HH:mm:ss YYYY-MM-dd")));
    listviewEditObserv.add(timelist.get(timelist.size() - 1).format(DateTimeFormatter.ofPattern("HH:mm:ss YYYY-MM-dd")));
    starWorktime();
    int items = listView.getItems().size();
    listView.scrollTo(items);
    listView.refresh();
    listViewEdit.refresh();
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
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Spara fil");
    fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Documents"));
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("TXT", "*.txt"));
    // Filehandler filehandler = new Filehandler ();
    File path = fileChooser.showSaveDialog(stage);


    //Path can be null if abort was selected
    if (path != null) {
      //We have a valid File object. Use with FileReader or FileWriter
      System.out.println(path.getAbsolutePath());
      //  filehandler.saveFileSVG (model, path, (int) canvas.getWidth (), (int) canvas.getHeight ());
    } else {
      System.out.println("no file");
    }
  }

  protected void setCalculatedOverTime() {
    Integer hours = workingHoursValueProperty.getValue();
    Integer seconds = (hours * 60) * 60;

    if (calculatedTime.get() >= seconds) {
      long time = calculatedTime.get() - seconds;
      runOverTime.set(true);
      overTimeString.set(LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
      label_OverTime.setVisible(true);
      overTime.setVisible(true);
    } else {
      runOverTime.set(false);
      label_OverTime.setVisible(false);
      overTime.setVisible(false);
    }
  }
}
