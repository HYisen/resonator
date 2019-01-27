module net.alexhyisen.resonator.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.alexhyisen.resonator.utility;
    requires net.alexhyisen.resonator.core;

    opens net.alexhyisen.resonator.gui to javafx.fxml, javafx.graphics;
    exports net.alexhyisen.resonator.gui;
}