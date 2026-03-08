module com.univ.scheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens com.univ.scheduler.controller to javafx.fxml;
    opens com.univ.scheduler.model to javafx.base;

    exports com.univ.scheduler;
}