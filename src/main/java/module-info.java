module org.jmc.pintfinder {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.jmc.pintfinder to javafx.fxml;
    exports org.jmc.pintfinder;
}