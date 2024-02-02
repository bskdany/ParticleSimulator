package org.example.particlesimulation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    @FXML private Slider particleAttractionValueSlider;
    private Label activeAttractionGridLabel;
    private int[] activeAttractionLabelCoordinates = {0,0};

    private ParticleSimulation simulation;
    private Map<Color, String> colorMap;
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
        for (int i = 0; i < ParticleSimulation.ATTRACTION_MATRIX.length; i++) {
            for (int j = 0; j < ParticleSimulation.ATTRACTION_MATRIX.length; j++) {
                updateGrid(ParticleSimulation.ATTRACTION_MATRIX[i][j], new int[]{i,j});
            }
        }
        if(activeAttractionGridLabel != null){
            activeAttractionGridLabel.setBorder(null);
        }
        activeAttractionLabelCoordinates[0] = -1;
        activeAttractionLabelCoordinates[1] = -1;
        particleAttractionValueSlider.setDisable(true);

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
        colorMap = Util.createColorMap();

        // get the colors existing and transform them in color strings
        List<Color> colors = new ArrayList<>(colorMap.keySet());
        List<String> entries = new ArrayList<>();
        for(Color color :colors){
            entries.add(colorMap.get(color));
//            System.out.println(colorMap.get(color));
//            System.out.println(color.getRed() + " " + color.getGreen() + " " + color.getBlue());
        }
        speciesChoiceBox.setItems(FXCollections.observableArrayList(entries));
        // setting default value of the choice box to be the first color
        speciesChoiceBox.setValue(entries.getFirst());

        // INITIALIZING FIELDS
        selectedSpecies = colors.getFirst();
        areAllSpeciesSelected = false;
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies,areAllSpeciesSelected));
        particleAttractionValueSlider.setDisable(true);


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

        // ATTRACTION MATRIX SLIDER
        particleAttractionValueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            if(Math.abs(oldValue.doubleValue() - newValue.doubleValue()) < 0.05){
//                return;
//            }
            simulation.setAttractionMatrixValue(activeAttractionLabelCoordinates, Double.parseDouble(decimalFormat.format(newValue)));

            updateGrid(Double.parseDouble(decimalFormat.format(newValue)), activeAttractionLabelCoordinates);
        });

        generateAttractionGrid();
    }

    private void updateGrid(double value, int[] coordinates){
        int matrixWidth = ParticleSimulation.ATTRACTION_MATRIX.length;
        int position = (matrixWidth * 2) + coordinates[0] * matrixWidth + coordinates[1] ;
        Label labelToUpdate = (Label) attractionGrid.getChildren().get(position);
        labelToUpdate.setText(String.valueOf(value));
        Color backgroundColor = Util.mapValueToColor(value);
        labelToUpdate.setBackground(Background.fill(backgroundColor));
    }

    private void generateAttractionGrid(){
        // ATTRACTION GRID
        for (int i = 0 ; i < ParticleSimulation.ATTRACTION_MATRIX.length; i++) {
            for (int j = 0; j < ParticleSimulation.ATTRACTION_MATRIX.length; j++) {
                double value = ParticleSimulation.ATTRACTION_MATRIX[i][j];

                Label label = new Label(String.valueOf(value));
                label.setFont(new Font(9));
                Color backgroundColor = Util.mapValueToColor(value);
                label.setBackground(Background.fill(backgroundColor));
                label.setPrefSize(100,100);

                int finalI = i;
                int finalJ = j;
                label.setOnMouseClicked(e -> {
                    Label clickedLabel = (Label) e.getSource();

                    // remove the border from the previous active label
                    if(activeAttractionGridLabel != null && activeAttractionGridLabel != label){
                        activeAttractionGridLabel.setBorder(null);
                        activeAttractionLabelCoordinates[0] = -1;
                        activeAttractionLabelCoordinates[1] = -1;
                        particleAttractionValueSlider.setDisable(true);
                    }

                    // if it has no border add border
                    if(clickedLabel.getBorder() == null){
                        clickedLabel.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));
                        activeAttractionGridLabel = clickedLabel;
                        activeAttractionLabelCoordinates[0] = finalI;
                        activeAttractionLabelCoordinates[1] = finalJ;
                        particleAttractionValueSlider.setDisable(false);
                        particleAttractionValueSlider.setValue(simulation.getAttractionMatrixValueAt(activeAttractionLabelCoordinates));

                    }
                    // if it has border remove it
                    else{
                        clickedLabel.setBorder(null);
                        activeAttractionGridLabel = null;
                        activeAttractionLabelCoordinates[0] = -1;
                        activeAttractionLabelCoordinates[1] = -1;
                        particleAttractionValueSlider.setDisable(true);
                    }
                });

                GridPane.setConstraints(label, j+1, i+1); // why rows first? I have no idea
                attractionGrid.getChildren().add(label);
            }
        }
    }
}