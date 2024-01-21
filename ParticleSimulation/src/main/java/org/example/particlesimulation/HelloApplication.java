package org.example.particlesimulation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private Particle particle = new Particle(100, 100, 10, Color.BLUE);

    private Pane createContent(){
        Pane root = new Pane();
        root.getChildren().add(particle);
        return root;
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Particle Simulation");
        Pane root = createContent();
        Scene scene = new Scene(root, 500, 500);
        pressedKeyHandling(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void pressedKeyHandling(Scene scene){
        scene.setOnKeyPressed(e -> {
            double speed = 10.0; // Adjust the speed as needed
            KeyCode keyPressed = e.getCode();

            switch (keyPressed) {
                case UP:
                    particle.setCenterY(particle.getCenterY() - speed);
                    break;
                case DOWN:
                    particle.setCenterY(particle.getCenterY() + speed);
                    break;
                case LEFT:
                    particle.setCenterX(particle.getCenterX() - speed);
                    break;
                case RIGHT:
                    particle.setCenterX(particle.getCenterX() + speed);
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