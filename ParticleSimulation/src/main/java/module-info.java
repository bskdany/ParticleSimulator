module org.example.particlesimulation {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.particlesimulation to javafx.fxml;
    exports org.example.particlesimulation;
}