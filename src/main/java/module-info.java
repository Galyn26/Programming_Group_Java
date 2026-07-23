module com.example.programming_group_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // Gives JDBC permissions for MySQL connection

    opens com.example.programming_group_java to javafx.fxml, javafx.graphics;
    exports com.example.programming_group_java;
}