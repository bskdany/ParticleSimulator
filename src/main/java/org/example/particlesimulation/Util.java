package org.example.particlesimulation;

import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Util {


    // maps a value from -1 to 1 to a Color Fade between Red and Green
    public static Color mapValueToColor(double value) {
        double minHue = 0;
        double maxHue = 100;
        double mappedHue = (value + 1) / 2 * (maxHue - minHue) + minHue;
        return Color.hsb(Math.max(0, Math.min(120, mappedHue)), 1.0, 1.0);
    }

    // based on a color like "white" returns Color.WHITE from the colors class
    public static Color nameToColor(String string, Map<Color, String> colorMap){
        for(Map.Entry<Color, String> entry :colorMap.entrySet()){
            if(Objects.equals(entry.getValue(), string)){
                return entry.getKey();
            }
        }
        return Color.BLACK;
    }

    public static Map<Color, String> createColorMap() {
        Map<Color, String> colorMap = new LinkedHashMap<Color, String>(){{
            put(Color.RED, "Red");
            put(Color.PINK, "Pink");
            put(Color.ORANGE, "Orange");
            put(Color.YELLOW, "Yellow");
            put(Color.LIME, "Green");
            put(Color.CYAN, "Cyan");
            put(Color.WHITE, "White");
        }};

        return colorMap;
    }
}
