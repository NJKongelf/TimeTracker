module TimeTracker {
    requires javafx.controls;
    requires javafx.fxml;

    opens se.njkongelf.controller to javafx.fxml;
    opens se.njkongelf to javafx.fxml;

    exports se.njkongelf;
}