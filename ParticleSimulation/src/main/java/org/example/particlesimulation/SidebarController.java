package org.example.particlesimulation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class SidebarController {
    private Color selectedSpecies;

    // SIMULATION TAB
    @FXML private Label particleMaxAttractionDistanceLabel; // MAX ATTRACTION DISTANCE
    @FXML private Slider particleMaxAttractionDistanceSlider;

    @FXML private Label particleMinAttractionLabel;         // MIN ATTRACTION DISTANCE
    @FXML private Slider particleMinAttractionSlider;

    @FXML private Label particleFrictionLabel;              // FRICTION
    @FXML private Slider particleFrictionSlider;

    @FXML private Slider particleForceMultiplierSlider;     // FORCE MULTIPLIER
    @FXML private Label particleForceMultiplierLabel;

    // PARTICLE TAB
    @FXML private ChoiceBox<String> speciesChoiceBox;       // SPECIES CHOICE BOX
    @FXML private Spinner<Integer> particleCounterSpinner;  // PARTICLE COUNTER

    private ParticleSimulation simulation;
    private Map<Color, String> colorMap = Util.createColorMap();
    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public void setMainApp(ParticleSimulation simulation){
        this.simulation = simulation;
        // this is the initialisation part that happens after the ParticleSimulation object is created
        // most of the stuff happening here is getting the default values from the simulation and putting them
        // in the fields

        setupParticleSimulationTab();
        setupGeneralSimulationTab();
    }

    @FXML
    protected void handleResetButton() {
        simulation.reset();
    }

    @FXML
    protected void handleResetAttractionMatrixButton(){
        simulation.resetAttractionMatrix();
    }

    private void setupGeneralSimulationTab(){
        // MAX ATTRACTION DISTANCE
        particleMaxAttractionDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setMaxAttractionDistance(newValue.intValue());
            particleMaxAttractionDistanceLabel.setText("Max attraction distance: " + newValue.intValue());
        });

        // MIN ATTRACTION DISTANCE
        particleMinAttractionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double val = Double.parseDouble(decimalFormat.format(newValue));
            simulation.setMinAttractionDistance(val);
            particleMinAttractionLabel.setText("Min attraction distance: " + decimalFormat.format(newValue));
        });

        // FRICTION
        particleFrictionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double val = Double.parseDouble(decimalFormat.format(newValue));
            simulation.setFriction(val);
            particleFrictionLabel.setText("Friction: " + val);
        });

        // FORCE MULTIPLIER
        particleForceMultiplierSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setForceMultiplier(newValue.intValue());
            particleForceMultiplierLabel.setText("Force multiplier: " + newValue.intValue());
        });
    }

    private void setupParticleSimulationTab(){
        // get the colors existing and transform them in color strings
        List<Color> colors = simulation.getParticleColors();
        List<String> entries = new ArrayList<>();
        for(Color color :colors){
            entries.add(colorMap.get(color));
        }
        speciesChoiceBox.setItems(FXCollections.observableArrayList(entries));
        // setting default value of the choice box to be the first color
        speciesChoiceBox.setValue(entries.getFirst());
        // remembering the default species
        selectedSpecies = colors.getFirst();

        // set the default value of the particle counter as the actual particle count
        particleCounterSpinner.getValueFactory().setValue(simulation.getParticleQuantity(selectedSpecies));

        // listener for the choice box
        speciesChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            selectedSpecies = Util.nameToColor(newValue, colorMap);
            particleCounterSpinner.getValueFactory().setValue(simulation.getParticleQuantity(selectedSpecies));
        });

        // PARTICLE COUNT
        particleCounterSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setParticleQuantity(newValue.intValue(), selectedSpecies);
        });
    }
}