package org.example.particlesimulator;
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
        colorMap = Util.createColorMap();

        // get the colors existing and transform them in color strings
        List<Color> colors = new ArrayList<>(colorMap.keySet());
        List<String> entries = new ArrayList<>();
        for(Color color :colors){
            entries.add(colorMap.get(color));
        }
        speciesChoiceBox.setItems(FXCollections.observableArrayList(entries));
        // setting default value of the choice box to be the first color
        speciesChoiceBox.setValue(entries.getFirst());

        selectedSpecies = colors.getFirst();
        areAllSpeciesSelected = false;
        particleAttractionValueSlider.setDisable(true);

        setupSpeciesSelector();
        setupAllParticlesCheck();
        setupAttractionSlider();
        setupSeedInput();
        setupMaxAttractionDistance();
        setupMinAttractionDistance();
        setupForceMultiplier();
        setupTimelineSlider();
        generateAttractionGrid();
        updateSeedInput();
    }
    private void setupSpeciesSelector(){
        speciesChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            selectedSpecies = Util.nameToColor(newValue, colorMap);
            particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
        });
    }
    private void setupAllParticlesCheck(){
        // SELECT ALL CHECK
        selectAllCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            areAllSpeciesSelected = newValue;
            updateParticleCount();
            speciesChoiceBox.setDisable(newValue); // disables the color selection when all are selected
        });
        selectAllCheck.setSelected(true);
    }
    private void setupAttractionSlider(){
        particleAttractionValueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.getAttractionMatrix().setAttractionMatrixValue(activeAttractionLabelCoordinates, Double.parseDouble(decimalFormat.format(newValue)));
            updateGridAtCoordinate(Double.parseDouble(decimalFormat.format(newValue)), activeAttractionLabelCoordinates);
        });

        particleAttractionValueSlider.setOnMouseReleased((e) -> {
            simulation.getAttractionMatrix().calculateSeed();
            updateSeedInput();
        });
    }
    private void setupTimelineSlider(){
        DecimalFormat timeFormatter = new DecimalFormat("0.0");
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
    private void setupForceMultiplier(){
        particleForceMultiplierSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ParticleSimulation.forceMultiplier = newValue.intValue();
            particleForceMultiplierLabel.setText("Force multiplier: " + newValue.intValue());
        });
        particleForceMultiplierSlider.setValue(Configs.DEFAULT_FORCE_MULTIPLIER);
    }
    private void setupMinAttractionDistance(){
        particleMinAttractionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double val = Double.parseDouble(decimalFormat.format(newValue));
            ParticleSimulation.attractionRelativeDistanceCutout = val;
            particleMinAttractionLabel.setText("Min attraction distance: " + decimalFormat.format(newValue));
        });
        particleMinAttractionSlider.setValue(Configs.DEFAULT_MIN_ATTRACTION_DISTANCE_RELATIVE);
    }
    private void setupMaxAttractionDistance(){
        particleMaxAttractionDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ParticleSimulation.maxAttractionDistance = newValue.intValue();
            ParticleSimulation.particleGridMap.generateCellSize();
            particleMaxAttractionDistanceLabel.setText("Max attraction distance: " + newValue.intValue());
        });
        particleMaxAttractionDistanceSlider.setValue(Configs.DEFAULT_MAX_ATTRACTION_DISTANCE);
    }
    private void setupSeedInput(){
        seedInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!simulation.getAttractionMatrix().setAttractionMatrixFromSeed(newValue)){
                seedInput.setStyle("-fx-border-color: red;");
            }
            else {
                seedInput.setStyle("-fx-border-color: transparent;");
                updateAttractionMatrix();
            }
        });
    }


    private void updateSeedInput(){
        seedInput.setText(simulation.getAttractionMatrix().getSeed());
    }
    private void updateAttractionMatrix(){
        for (int i = 0; i < AttractionMatrix.attractionMatrix.length; i++) {
            for (int j = 0; j < AttractionMatrix.attractionMatrix.length; j++) {
                updateGridAtCoordinate(AttractionMatrix.attractionMatrix[i][j], new int[]{i,j});
            }
        }
        if(activeAttractionGridLabel != null){
            activeAttractionGridLabel.setBorder(null);
        }
        activeAttractionLabelCoordinates[0] = -1;
        activeAttractionLabelCoordinates[1] = -1;
        particleAttractionValueSlider.setDisable(true);
    }
    private void updateMaxAttractionDistance(){
        particleMaxAttractionDistanceSlider.setValue(ParticleSimulation.maxAttractionDistance);
    }
    private void updateMinAttractionDistance(){
        particleMinAttractionSlider.setValue(ParticleSimulation.getAttractionRelativeDistanceCutout());
    }
    private void updateForceMultiplier(){
        particleForceMultiplierSlider.setValue(ParticleSimulation.forceMultiplier);
    }
    private void updateParticleCount(){
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies,areAllSpeciesSelected));
    }
    void updateAllElements(){
        updateMaxAttractionDistance();
        updateMinAttractionDistance();
        updateForceMultiplier();
        updateAttractionMatrix();
        updateSeedInput();
        updateParticleCount();
    }
    @FXML void resetDefaultSettingsButton(){
        // Simulation
        ParticleSimulation.maxAttractionDistance = Configs.DEFAULT_MAX_ATTRACTION_DISTANCE;
        ParticleSimulation.attractionRelativeDistanceCutout = Configs.DEFAULT_MIN_ATTRACTION_DISTANCE_RELATIVE;
        ParticleSimulation.forceMultiplier = Configs.DEFAULT_FORCE_MULTIPLIER;

        // Particle
        simulation.getAttractionMatrix().generateDefaultAttractionMatrix();
        simulation.initParticles();
        updateAllElements();
    }
    @FXML void handleKillAllParticlesButton(){
        if(!areAllSpeciesSelected){
            simulation.addParticleQuantity(-simulation.getParticleQuantity(selectedSpecies, false), selectedSpecies, false);
            updateParticleCount();
        }
    }
    @FXML private void handleResetButton() {
        simulation.reset();
    }
    @FXML private void handleDefaultAttractionMatrixButton(){
        simulation.getAttractionMatrix().generateDefaultAttractionMatrix();
        updateAttractionMatrix();
        seedInput.setText(simulation.getAttractionMatrix().getSeed());
    }
    @FXML private void handleRandomAttractionMatrixButton(){
        simulation.getAttractionMatrix().generateRandomAttractionMatrix();
        updateAttractionMatrix();
        updateSeedInput();
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
        simulation.addParticleQuantity(100, selectedSpecies, areAllSpeciesSelected);
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
    }
    @FXML private void decreaseParticlesButton(){
        simulation.addParticleQuantity(-100, selectedSpecies, areAllSpeciesSelected);
        particleCountLabel.setText("Particle count: " + simulation.getParticleQuantity(selectedSpecies, areAllSpeciesSelected));
    }
    private void updateGridAtCoordinate(double value, int[] coordinates){
        int matrixWidth = AttractionMatrix.attractionMatrix.length;
        int position = (matrixWidth * 2) + coordinates[0] * matrixWidth + coordinates[1] ;
        Label labelToUpdate = (Label) attractionGrid.getChildren().get(position);
        labelToUpdate.setText(String.valueOf(value));
        Color backgroundColor = Util.mapValueToColor(value);
        labelToUpdate.setBackground(Background.fill(backgroundColor));
    }
    private void generateAttractionGrid(){
        // ATTRACTION GRID
        for (int i = 0; i < AttractionMatrix.attractionMatrix.length; i++) {
            for (int j = 0; j < AttractionMatrix.attractionMatrix.length; j++) {
                double value = AttractionMatrix.attractionMatrix[i][j];

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
                        particleAttractionValueSlider.setValue(simulation.getAttractionMatrix().getAttractionMatrixValueAt(activeAttractionLabelCoordinates));

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