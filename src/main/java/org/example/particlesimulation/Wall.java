package org.example.particlesimulation;

import javafx.scene.shape.Rectangle;

public class Wall extends Rectangle {
    public static final double mass = 1;

    Wall(int x, int y, int width, int height){
        super(x, y, width, height);
    }
}