package org.example.particlesimulation;

import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Util {

    // based on a color like "white" returns Color.WHITE from the colors class
    public static Color nameToColor(String string, Map<Color, String> colorMap){
        for(Map.Entry<Color, String> entry :colorMap.entrySet()){
            if(Objects.equals(entry.getValue(), string)){
                return entry.getKey();
            }
        }
        return Color.BLACK;
    }

    // god why do I have to do this
    // whatever this is is getting the fields from the class color and matches them to string to access for later
    public static Map<Color, String> createColorMap() {
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
}
