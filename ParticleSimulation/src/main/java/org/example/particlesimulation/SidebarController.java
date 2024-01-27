package org.example.particlesimulation;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.paint.Color;

public class SidebarController {
    private ParticleSimulation simulation;

    @FXML
    private Slider particleMaxAttractionDistanceSlider;

    @FXML
    private Spinner<Integer> particleCounterSpinner;

    @FXML
    private void initialize(){
        particleMaxAttractionDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setMaxAttractionDistance(newValue.intValue());
        });

        particleCounterSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setParticleQuantity(newValue.intValue(), Color.WHITE);
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