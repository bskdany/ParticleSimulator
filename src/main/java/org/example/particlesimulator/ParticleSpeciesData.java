package org.example.particlesimulator;

import javafx.scene.paint.Color;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParticleSpeciesData {
    private int quantity;
    private double radius;

    ParticleSpeciesData(int quantity, double radius) {
        this.quantity = quantity;
        this.radius = radius;
    }

    ParticleSpeciesData(ParticleSpeciesData original){
        this.quantity = original.getQuantity();
        this.radius = original.getRadius();
    }

    public static Map<Color, ParticleSpeciesData> deepCopy(Map<Color, ParticleSpeciesData> original){
        Map<Color, ParticleSpeciesData> newMap = new LinkedHashMap<Color, ParticleSpeciesData>();
        for (Map.Entry<Color, ParticleSpeciesData> entry : original.entrySet()){
            ParticleSpeciesData newData = new ParticleSpeciesData(entry.getValue());
            newMap.put(entry.getKey(), newData);
        }
        return newMap;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}

