module TimeTracker {
    requires javafx.controls;
    requires javafx.fxml;

    opens se.njkongelf to javafx.fxml;

    exports se.njkongelf;
}