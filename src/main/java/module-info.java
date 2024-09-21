module BurritoKing {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.testfx; // Include TestFX if needed for testing

    opens aus.restaurant to javafx.fxml;
    opens aus.restaurant.controllers to javafx.fxml, org.testfx;
    opens aus.restaurant.models to javafx.base;

    exports aus.restaurant;
    exports aus.restaurant.controllers;
    exports aus.restaurant.models;
}
