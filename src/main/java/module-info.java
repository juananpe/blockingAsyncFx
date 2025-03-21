module eus.ehu.asyncdemo {
    requires javafx.controls;
    requires javafx.fxml;

    opens eus.ehu.asyncdemo to javafx.fxml;
    exports eus.ehu.asyncdemo;
}