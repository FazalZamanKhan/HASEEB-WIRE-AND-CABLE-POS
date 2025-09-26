module com.cablemanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires itextpdf;
    requires java.desktop;

    opens com.cablemanagement to javafx.fxml;
    opens com.cablemanagement.views to javafx.fxml, javafx.graphics;
    opens com.cablemanagement.views.pages to javafx.base, javafx.fxml;
    opens com.cablemanagement.model to javafx.base, javafx.fxml;
    
    exports com.cablemanagement;
    exports com.cablemanagement.views;
    exports com.cablemanagement.model;
    exports com.cablemanagement.database;
    exports com.cablemanagement.invoice;
    
}
