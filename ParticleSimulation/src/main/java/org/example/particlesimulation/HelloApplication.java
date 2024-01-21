package org.example.particlesimulation;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private static final int PANE_WIDTH = 500;
    private static final int PANE_HEIGHT = 500;

    private static final int PARTICLE_RADIUS = 10;
    private Particle particle = new Particle(100, 100, PARTICLE_RADIUS, Color.BLUE);

    private Pane createContent(){
        Pane root = new Pane();
        root.getChildren().add(particle);
        return root;
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Particle Simulation");
        Pane root = createContent();
        Scene scene = new Scene(root, PANE_HEIGHT, PANE_WIDTH);
        pressedKeyHandling(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void pressedKeyHandling(Scene scene){
        scene.setOnKeyPressed(e -> {
            double speed = 10.0; // Adjust the speed as needed
            KeyCode keyPressed = e.getCode();
            double particleCenterY = particle.getCenterY();
            double particleCenterX = particle.getCenterX();

            switch (keyPressed) {
                case UP:
                    if(particleCenterY > PARTICLE_RADIUS){
                        particle.setCenterY(particle.getCenterY() - speed);
                    }
                    break;
                case DOWN:
                    if(particleCenterY < PANE_HEIGHT - PARTICLE_RADIUS){
                        particle.setCenterY(particle.getCenterY() + speed);
                    }
                    break;
                case LEFT:
                    if(particleCenterX > PARTICLE_RADIUS){
                        particle.setCenterX(particle.getCenterX() - speed);
                    }
                    break;
                case RIGHT:
                    if(particleCenterX < PANE_WIDTH - PARTICLE_RADIUS){
                        particle.setCenterX(particle.getCenterX() + speed);
                    }
                    break;
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }

    private class Particle extends Circle {
        Particle(int x, int y, int radius, Color color){
            super(x,y,radius,color);
            setCenterX(x);
            setCenterY(y);
        }


    }

}