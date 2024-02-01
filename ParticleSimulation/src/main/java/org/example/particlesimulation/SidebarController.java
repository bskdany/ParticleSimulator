package org.example.particlesimulation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class SidebarController {
    private Color selectedSpecies;
    private Boolean areAllSpeciesSelected;

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
//    @FXML private Spinner<Integer> particleCounterSpinner;  // PARTICLE COUNTER
    @FXML private GridPane attractionGrid;
    @FXML private CheckBox selectAllCheck;                  // SELECT ALL CHECK BOX
    @FXML private Label particleCountLabel;

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
    private void handleResetButton() {
        simulation.reset();
    }

    @FXML
    private void handleResetAttractionMatrixButton(){
        simulation.resetAttractionMatrix();
        generateAttractionGrid();
    }

    @FXML private void handleStopButton(){
        simulation.stop();
    }

    @FXML private void handleStartButton(){
        simulation.start();
    }

    @FXML private void increaseParticlesButton(){
        simulation.setParticleQuantity(10, selectedSpecies, areAllSpeciesSelected);
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
    }

    @FXML private void decreaseParticlesButton(){
        simulation.setParticleQuantity(-10, selectedSpecies, areAllSpeciesSelected);
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
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

        // INITIALIZING FIELDS
        selectedSpecies = colors.getFirst();
        areAllSpeciesSelected = false;
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies,areAllSpeciesSelected));

        // listener for the choice box
        speciesChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            selectedSpecies = Util.nameToColor(newValue, colorMap);
            particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
        });

        // SELECT ALL CHECK
        selectAllCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            areAllSpeciesSelected = newValue;
            particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
            speciesChoiceBox.setDisable(newValue); // disables the color selection when all are selected
        });

        generateAttractionGrid();
    }

    private void generateAttractionGrid(){
        // ATTRACTION GRID
        for (int i = 0 ; i < ParticleSimulation.ATTRACTION_MATRIX.length ; i++) {
            for (int j = 0; j < ParticleSimulation.ATTRACTION_MATRIX.length; j++) {
                double value = ParticleSimulation.ATTRACTION_MATRIX[i][j];

                Label label = new Label(String.valueOf(value));
                label.setFont(new Font(9));
                Color backgroundColor = Util.mapValueToColor(value);
                label.setBackground(Background.fill(backgroundColor));
                label.setPrefSize(100,100);

                GridPane.setConstraints(label, i+1, j+1);
                attractionGrid.getChildren().add(label);
            }
        }
    }
}