package org.example.particlesimulation;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;

public class SidebarController {
    private ParticleSimulation simulation;

    @FXML
    private Slider particleMaxAttractionDistanceSlider;

    @FXML
    private void initialize(){
        particleMaxAttractionDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setMaxAttractionDistance(newValue.intValue());
        });
    }

    public void setMainApp(ParticleSimulation simulation){
        this.simulation = simulation;
    }
    @FXML
    protected void handleResetButton() {
        simulation.reset();
    }

    @FXML
    protected void handleResetAttractionMatrixButton(){
        simulation.resetAttractionMatrix();
    }
}