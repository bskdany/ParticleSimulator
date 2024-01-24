package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class HelloApplication extends Application {
    private static final int PANE_WIDTH = 500;
    private static final int PANE_HEIGHT = 500;
    private static final double UPDATE_RATE_MS = 16.7; // for 60 fps
    private static final int PARTICLE_RADIUS = 10;
    private static final int PARTICLES_TO_CREATE = 10;
    private static final Color[] PARTICLE_SPECIES = new Color[]{Color.BLUE, Color.RED, Color.GREEN};
    private double[][] ATTRACTION_MATRIX = new double[PARTICLE_SPECIES.length][PARTICLE_SPECIES.length];
    private ArrayList<Particle> particles = new ArrayList<>();

    Pane root = new Pane();

    private Pane createContent(){
        ATTRACTION_MATRIX = generateAttractionMatrix(PARTICLE_SPECIES.length);

        for (int j = 0; j < PARTICLE_SPECIES.length; j++) {
            for (int i = 0; i < PARTICLES_TO_CREATE; i++) {
                particles.add(new Particle((int) (Math.random() * PANE_WIDTH), (int) (Math.random() * PANE_HEIGHT), PARTICLE_RADIUS, PARTICLE_SPECIES[j], 1, j));
            }
        }

        root.getChildren().addAll(particles);
        return root;
    }
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Particle Simulation");
        Pane root = createContent();
        Scene scene = new Scene(root, PANE_HEIGHT, PANE_WIDTH);
        stage.setScene(scene);
        stage.show();
        startUpdate();
    }

    private void startUpdate(){

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(UPDATE_RATE_MS), actionEvent -> {
                for(Particle particle : particles){
                    particle.simulate();
                }
                for(Particle particle : particles){
                    particle.move();
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static void main(String[] args) {
        launch();
    }

    public static double[][] generateAttractionMatrix(int size){
        double[][] attractionMatrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                attractionMatrix[i][j] = Math.random();
            }
        }
        return attractionMatrix;
    }


    private class Particle extends Circle {
        public double MASS;
        public int SPECIES;
        public double[] POSITION = {0, 0};
        public final double MAX_ATTRACTION_DISTANCE = 400;
        public final double ATTRACTION_RELATIVE_DISTANCE_CUTOUT = 0.3;
        public double[] VELOCITY = {0,0};
        public double FRICTION = 0.04;
        public double deltaTime = UPDATE_RATE_MS / 1000;
        public double FORCE_MULTIPLIER = 40;
        Particle(int x, int y, int radius, Color color, double mass, int species){
            super(x,y,radius,color);
            POSITION[0] = x;
            POSITION[1] = y;
            MASS = mass;
            SPECIES = species;
        }
        public void move(){
            setCenterX(POSITION[0]);
            setCenterY(POSITION[1]);
        }
        public void simulate(){
            double[] SUM_FORCE = {0,0};
            for(Particle particle : particles){
                if(particle != this){
                    double[] result =  calculateAttractionForceNew(this, particle);
                    SUM_FORCE[0] += result[0];
                    SUM_FORCE[1] += result[1];
                }
            }
            // F = m / a
            double accelerationX = SUM_FORCE[0] * FORCE_MULTIPLIER / MASS;
            double accelerationY = SUM_FORCE[1] * FORCE_MULTIPLIER / MASS;

            VELOCITY[0] *= FRICTION;
            VELOCITY[1] *= FRICTION;

            VELOCITY[0] += accelerationX * deltaTime;
            VELOCITY[1] += accelerationY * deltaTime;

            POSITION[0] += VELOCITY[0] * deltaTime;
            POSITION[1] += VELOCITY[1] * deltaTime;
        }

        private static double[] normalizeVector(double[] vector) {
            double magnitude = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
            if(magnitude != 0){
                return new double[]{vector[0] / magnitude, vector[1] / magnitude};
            }
            return vector;
        }
//        private static double[] calculateReflectionVector(double[] directionVector, double[] normalVector) {
//            // Normalize the direction vector
//            directionVector = normalizeVector(directionVector);
//
//            // Calculate the dot product
//            double dotProduct = directionVector[0] * normalVector[0] + directionVector[1] * normalVector[1];
//
//            // Calculate the reflection vector
//            double[] reflectionVector = {
//                    directionVector[0] - 2 * dotProduct * normalVector[0],
//                    directionVector[1] - 2 * dotProduct * normalVector[1]
//            };
//
//            // Normalize the reflection vector
//            reflectionVector = normalizeVector(reflectionVector);
//
//            return reflectionVector;
//        }
//        private void handleCollisionWalls(){
//
//            for (Wall wall : walls) {
//                if (getBoundsInParent().intersects(wall.getBoundsInParent())) {
//
//                    double particleX = getCenterX();
//                    double particleY = getCenterY();
//
//                    double rectangleX = wall.getX();
//                    double rectangleY = wall.getY();
//                    double rectangleWidth = wall.getWidth();
//                    double rectangleHeight = wall.getHeight();
//                    double rectangleOriginX = rectangleX + rectangleWidth / 2;
//                    double rectangleOriginY = rectangleY + rectangleHeight / 2;
//
//                    double deltaXRectangleParticle = rectangleOriginX - particleX;
//                    double deltaYRectangleParticle = rectangleOriginY - particleY;
//
//                    double collisionOriginX = particleX;
//                    double collisionOriginY = particleY;
//                    double[] normalVector = {0, 0};
//
//                    if (deltaXRectangleParticle > rectangleWidth / 2) {
//                        collisionOriginX = particleX + (deltaXRectangleParticle - rectangleWidth / 2);
//                        normalVector[0] = -1;
//                    } else if (deltaXRectangleParticle < -(rectangleWidth / 2)) {
//                        collisionOriginX = particleX + (deltaXRectangleParticle + rectangleWidth / 2);
//                        normalVector[0] = 1;
//                    }
//
//                    if (deltaYRectangleParticle > rectangleHeight / 2) {
//                        collisionOriginY = particleY + (deltaYRectangleParticle - rectangleHeight / 2);
//                        normalVector[1] = -1;
//                    } else if (deltaYRectangleParticle < -(rectangleHeight / 2)) {
//                        collisionOriginY = particleY + (deltaYRectangleParticle + rectangleHeight / 2);
//                        normalVector[1] = 1;
//                    }
//
//                    Circle collisionPlaceholder = new Circle(collisionOriginX, collisionOriginY, 3, Color.RED);
//                    root.getChildren().add(collisionPlaceholder);
//
//                    FORCE = calculateReflectionVector(FORCE, normalVector);
//                }
//            }
//        }
//        private double[] calculateAttractionForce(Particle source, Particle target){
//            // vector from source to target
//            double[] directionVector = {target.getCenterX() - source.getCenterX(), target.getCenterY() - source.getCenterY()};
//            // length of the distance
//            double distance = Math.sqrt(Math.pow(directionVector[0],2) + Math.pow(directionVector[1], 2));
//            // F = G * (m1 * m2) / r^2
//            double magnitude =  ATTRACTION * (source.MASS * target.MASS) / (distance * distance);
//
//            return normalizeVector(new double[]{directionVector[0] * magnitude, directionVector[1] * magnitude});
//        }
        private double[] calculateAttractionForceNew(Particle source, Particle target){
            // vector from source to target
            double[] directionVector = {target.getCenterX() - source.getCenterX(), target.getCenterY() - source.getCenterY()};
            // length of the distance
            double distance = Math.sqrt(Math.pow(directionVector[0],2) + Math.pow(directionVector[1], 2));

            double relativeDistance = distance / MAX_ATTRACTION_DISTANCE;

            double magnitude = 0;
            double attractionFactor = ATTRACTION_MATRIX[SPECIES][target.SPECIES];

            if(relativeDistance < ATTRACTION_RELATIVE_DISTANCE_CUTOUT){
                magnitude = relativeDistance / ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 1;
            } else if (relativeDistance < 1.0) {
                magnitude = (-Math.abs(relativeDistance - ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 0.5) + 0.5 ) * 2 * attractionFactor;
            }
            else if( 1 < relativeDistance){
                magnitude = 0;
            }

            double[] normalisedDirectionVector = normalizeVector(directionVector);

            return new double[]{normalisedDirectionVector[0] * magnitude * MAX_ATTRACTION_DISTANCE, normalisedDirectionVector[1] * magnitude * MAX_ATTRACTION_DISTANCE};
        }
    }
    private class Wall extends Rectangle {
        public static final double mass = 1;

        Wall(int x, int y, int width, int height){
            super(x, y, width, height);
        }
    }
}