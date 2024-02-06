module org.example.particlesimulation {
    requires javafx.controls;
    requires javafx.fxml;
    requires JavaFastPFOR;


    opens org.example.particlesimulation to javafx.fxml;
    exports org.example.particlesimulation;
}