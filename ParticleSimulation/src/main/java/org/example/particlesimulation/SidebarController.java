package org.example.particlesimulation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.*;

public class SidebarController {
    private ParticleSimulation simulation;
    private Color selectedSpecies;

    @FXML
    private Slider particleMaxAttractionDistanceSlider;

    @FXML
    private ChoiceBox<String> speciesChoiceBox;

    @FXML
    private Spinner<Integer> particleCounterSpinner;

    private Map<Color, String> colorMap = createColorMap();


    public void setMainApp(ParticleSimulation simulation){
        this.simulation = simulation;

        // this is the initialisation part that happens after the ParticleSimulation object is created
        // most of the stuff happening here is getting the default values from the simulation and putting them
        // in the fields


        // get the colors existing and transform them in color strings
        List<Color> colors = simulation.getParticleColors();
        List<String> entries = new ArrayList<>();
        for(Color color :colors){
            entries.add(colorToName(color));
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
            selectedSpecies = nameToColor(newValue);
            particleCounterSpinner.getValueFactory().setValue(simulation.getParticleQuantity(selectedSpecies));
        });

        particleMaxAttractionDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setMaxAttractionDistance(newValue.intValue());
        });

        particleCounterSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            simulation.setParticleQuantity(newValue.intValue(), selectedSpecies);
        });
    }

    private String colorToName(Color color) {
        return colorMap.getOrDefault(color, color.toString());
    }

    private Color nameToColor(String string){
        for(Map.Entry<Color, String> entry :colorMap.entrySet()){
            if(Objects.equals(entry.getValue(), string)){
                return entry.getKey();
            }
        }
        return Color.BLACK;
    }


    // god why do I have to do this
    // whatever this is is getting the fields from the class color and matches them to string to access for later
    private static Map<Color, String> createColorMap() {
        Map<Color, String> colorMap = new HashMap<>();

        Field[] fields = Color.class.getFields();

        for (Field field : fields) {
            if (field.getType() == Color.class) {
                try {
                    Color color = (Color) field.get(null); // null because it's a static field
                    String colorName = field.getName().toLowerCase();
                    colorMap.put(color, colorName);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return colorMap;
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