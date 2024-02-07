module org.example.parclesimulator {
    requires javafx.controls;
    requires javafx.fxml;
    opens org.example.particlesimulation to javafx.fxml;
    exports org.example.particlesimulation;
}