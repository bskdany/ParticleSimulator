package org.example.particlesimulation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.text.DecimalFormat;
import java.util.*;

public class SidebarController {
    private Color selectedSpecies;
    private Boolean areAllSpeciesSelected;

    @FXML private Button playPauseButton;

    @FXML private Label particleMaxAttractionDistanceLabel; // MAX ATTRACTION DISTANCE
    @FXML private Slider particleMaxAttractionDistanceSlider;
    @FXML private Label particleMinAttractionLabel;         // MIN ATTRACTION DISTANCE
    @FXML private Slider particleMinAttractionSlider;
    @FXML private Slider particleForceMultiplierSlider;     // FORCE MULTIPLIER
    @FXML private Label particleForceMultiplierLabel;
    @FXML private Slider timelineSlider;
    @FXML private Label timelineLabel;

    @FXML private ChoiceBox<String> speciesChoiceBox;       // SPECIES CHOICE BOX
    @FXML private GridPane attractionGrid;
    @FXML private CheckBox selectAllCheck;                  // SELECT ALL CHECK BOX
    @FXML private Label particleCountLabel;
    @FXML private Slider particleAttractionValueSlider;
    @FXML private Button killAllParticlesButton;
    @FXML private TextField seedInput;

    private Label activeAttractionGridLabel;
    private int[] activeAttractionLabelCoordinates = {0,0};
    private ParticleSimulation simulation;
    private Map<Color, String> colorMap;
    private boolean disableTimelineValueListener = false;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public void setMainApp(ParticleSimulation simulation){
        this.simulation = simulation;
        // this is the initialisation part that happens after the ParticleSimulation object is created
        // most of the stuff happening here is getting the default values from the simulation and putting them
        // in the fields
        setupParticleSimulationTab();
        setupGeneralSimulationTab();
    }

    @FXML void updateAllElements(){
        particleMaxAttractionDistanceSlider.setValue(ParticleSimulation.getMaxAttractionDistance());
        particleMinAttractionSlider.setValue(ParticleSimulation.getAttractionRelativeDistanceCutout());
        particleForceMultiplierSlider.setValue(ParticleSimulation.getForceMultiplier());
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies,false));
        updateAttractionMatrix();
    }
    @FXML void resetDefaultSettingsButton(){
        // Simulation
        simulation.setMaxAttractionDistance(100);
        simulation.setMinAttractionDistance(0.3);
        simulation.setForceMultiplier(5);

        // Particle
        simulation.generateDefaultAttractionMatrix();
        updateAttractionMatrix();
        simulation.initParticles();
        particleCountLabel.setText("Particle count: 200");

        updateAllElements();
    }
    @FXML void handleKillAllParticlesButton(){
        if(!areAllSpeciesSelected){
            simulation.addParticleQuantity(-simulation.getParticleQuantity(selectedSpecies, false), selectedSpecies, false);
            particleCountLabel.setText("Particle count: 0");
        }
    }
    @FXML private void handleResetButton() {
        simulation.reset();
    }
    private void updateAttractionMatrix(){
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
    @FXML private void handleDefaultAttractionMatrixButton(){
        simulation.resetDefaultAttractionMatrix();
        updateAttractionMatrix();
        seedInput.setText(simulation.seed);
    }
    @FXML private void handleRandomAttractionMatrixButton(){
        simulation.resetRandomAttractionMatrix();
        updateAttractionMatrix();
        seedInput.setText(simulation.seed);
    }
    @FXML void handlePlayPauseButton(){
        if(Objects.equals(playPauseButton.getText(), "Play")){
            simulation.start();
            playPauseButton.setText("Pause");
        } else{
            simulation.stop();
            playPauseButton.setText("Play");
        }
    }
    @FXML private void increaseParticlesButton(){
        simulation.addParticleQuantity(10, selectedSpecies, areAllSpeciesSelected);
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
    }
    @FXML private void decreaseParticlesButton(){
        simulation.addParticleQuantity(-10, selectedSpecies, areAllSpeciesSelected);
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

        // FORCE MULTIPLIER
        particleForceMultiplierSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setForceMultiplier(newValue.intValue());
            particleForceMultiplierLabel.setText("Force multiplier: " + newValue.intValue());
        });

        DecimalFormat timeFormatter = new DecimalFormat("0.0");

        // TIMELINE SLIDER
        timelineSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(!disableTimelineValueListener){
                simulation.peekRewind(100 - newValue.intValue());
                updateAllElements();

                timelineLabel.setText("Go back " + timeFormatter.format(((double) SimulationTimeline.timeToSaveMs * (100 - newValue.intValue()))/ 1000 ) + " sec");
            }
            else{
                disableTimelineValueListener = false;
                timelineLabel.setText("Go back 0 seconds");
            }
        });

        timelineSlider.setOnMouseReleased(e -> {
            simulation.finalizeRewind();
            disableTimelineValueListener = true;
            timelineSlider.setValue(100);
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

        // SEED INPUT
        seedInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!simulation.setAttractionMatrixFromSeed(newValue)){
                seedInput.setStyle("-fx-border-color: red;");
            }
            else {
                seedInput.setStyle("-fx-border-color: transparent;");
                updateAttractionMatrix();
            }
        });
        seedInput.setText(simulation.seed);
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