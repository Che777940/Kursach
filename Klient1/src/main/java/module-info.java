module klient {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.logging;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    exports InterfaceMenu;
    opens ScoringWindowParameters to javafx.base;
    opens CreditApplications to javafx.base;
    opens FinancialInfoManagement to javafx.base;
}