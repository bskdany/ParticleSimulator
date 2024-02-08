module org.example.parclesimulator {
    requires javafx.controls;
    requires javafx.fxml;
    opens org.example.particlesimulator to javafx.fxml;
    exports org.example.particlesimulator;
}