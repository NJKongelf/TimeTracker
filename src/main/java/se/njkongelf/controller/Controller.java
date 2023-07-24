package se.njkongelf.controller;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Controller {

    private List<LocalDateTime> timelist;
    private Stage stage;

    private ObservableList<String> listview;

    private ObjectProperty<ObservableList<String>> listObjectProperty;
    @FXML
    private ListView<String> label;

    public Controller() {

    }

    @FXML
    public void handleButton(ActionEvent event) {
        timelist.add(LocalDateTime.now());
        listview.add(timelist.get(timelist.size()-1).format(DateTimeFormatter.ofPattern("HH:mm:ss YYYY-MM-dd")));
        int items = label.getItems().size();
        label.scrollTo(items);
        label.refresh();
    }

    @FXML
    public void exitOnclick(ActionEvent event){
        stage.setAlwaysOnTop(false);
        stage.close();
    }
    public void initialize() {
    timelist = new ArrayList<>();
    listview = FXCollections.observableArrayList();
    label.itemsProperty().setValue(listview);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}