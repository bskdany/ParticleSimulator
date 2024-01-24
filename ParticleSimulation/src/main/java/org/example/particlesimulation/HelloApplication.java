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
    private static final int PANE_WIDTH = 1400;
    private static final int PANE_HEIGHT = 800;
    private static final double UPDATE_RATE_MS = 16.7; // for 60 fps
    private static final int PARTICLE_RADIUS = 2;
    private static final int PARTICLES_TO_CREATE = 100;
    private static final Color[] PARTICLE_SPECIES = new Color[]{Color.WHITE, Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.PINK};
    private double[][] ATTRACTION_MATRIX = new double[PARTICLE_SPECIES.length][PARTICLE_SPECIES.length];
    private Particle testParticle = new Particle(100,100, 10, Color.GRAY, 1, 0);
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
//        root.getChildren().add(testParticle);
        return root;
    }
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Particle Simulation");
        Pane root = createContent();
        Scene scene = new Scene(root, PANE_WIDTH, PANE_HEIGHT, Color.BLACK);
        stage.setScene(scene);
        pressedKeyHandling(scene);
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
//        double[][] attractionMatrix = {
//                {-0.08107187750636491, 0.7359548417603424, -0.7100706611540691, 0.11666661944421064, -0.6008519290416389},
//                {-0.26172083436753957, 0.7197511005998479, 0.5704957933274387, 0.6064088193212608, -0.32064612038964324},
//                {-0.5936838674310888, 0.641235332032943, 0.8091235092568326, 0.3394994361766158, 1.0727223378844815},
//                {0.6002815957745079, 0.8131670263871386, 0.940292618860865, -0.5353126027886103, -0.2067875926053041},
//                {-0.6201721480615886, 0.3606964499070032, -0.28842948154868375, 0.13856416475485311, 0.8414390975408145},
//        };

//        double[][] attractionMatrix1 = {
//                {-0.19972632532133072, -0.5001391299558663, -0.10840563685155946, -0.384214674293719, 0.9411458756781387, 0.2694121348527364},
//                {0.788322083332805 ,-0.7012069591040996, -0.4177407685768749, 0.27599904544074205, 0.8860377279526489, 0.34098896723600103},
//                {0.2915985323790543, -0.7214472571507881, -0.4825329121349121, 0.4639146221234215, 0.1709788181295474, 0.8928580692014204},
//                {0.44923748885076364, -0.8768560022521236, -0.8757866089178984, 0.6343599209613223, 0.3341514842854024, 0.1932410412783373},
//                { -0.35597831399761726, -0.5977302675436847, 0.23154757222996059, -0.8964292357171652, 0.12310856453395991, 0.881597942632326},
//                {0.39746840312635856, 0.011938335083090501, 0.9780512319276281, 1.076062105543309, -0.13918867491302145, -0.3793896399713822}
//        };

        double[][] attractionMatrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // generate values between -0.9 and 1.1
                attractionMatrix[i][j] = Math.random() * 2 - 0.9;
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(attractionMatrix[i][j] + " ");
            }
            System.out.println("");
        }

        return attractionMatrix;
    }

    private void pressedKeyHandling(Scene scene){
        scene.setOnKeyPressed(e -> {
            KeyCode keyPressed = e.getCode();

            switch (keyPressed) {
                case UP:
                    testParticle.POSITION[1] += -3;
                    break;
                case DOWN:
                    testParticle.POSITION[1] += 3;
                    break;
                case LEFT:
                    testParticle.POSITION[0] += -3;
                    break;
                case RIGHT:
                    testParticle.POSITION[0] += 3;
                    break;
            }
            testParticle.move();
        });
    }

    private class Particle extends Circle {
        public double MASS;
        public int SPECIES;
        public double[] POSITION = {0, 0};
        public final double MAX_ATTRACTION_DISTANCE = 70;
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
            if(POSITION[0] <= 0){
                POSITION[0] += PANE_WIDTH;
            } else if (POSITION[0] >= PANE_WIDTH) {
                POSITION[0] -= PANE_WIDTH;
            }
            if(POSITION[1] <= 0){
                POSITION[1] += PANE_HEIGHT;
            } else if (POSITION[1] >= PANE_HEIGHT) {
                POSITION[1] -= PANE_HEIGHT;
            }
            setCenterX(POSITION[0]);
            setCenterY(POSITION[1]);
        }
        public void simulate(){
            double[] SUM_FORCE = {0,0};
            for(Particle particle : particles){
                if(particle != this){
                    double[] directionVector = calculateDirectionVector(particle);

                    // length of the distance
                    double distance = Math.sqrt(Math.pow(directionVector[0],2) + Math.pow(directionVector[1], 2)) / MAX_ATTRACTION_DISTANCE;

                    if(distance < 1){
                        double attractionFactor = ATTRACTION_MATRIX[SPECIES][particle.SPECIES];
                        double magnitude =  calculateAttractionForce(distance, attractionFactor);
                        double[] normalisedDirectionVector = normalizeVector(directionVector);

                        SUM_FORCE[0] += normalisedDirectionVector[0] * magnitude * MAX_ATTRACTION_DISTANCE;
                        SUM_FORCE[1] += normalisedDirectionVector[1] * magnitude * MAX_ATTRACTION_DISTANCE;
                    }
                }
            }
            // all particles move towards the center slowly
            double[] vectorTowardsCenter = normalizeVector(new double[] {(((double) PANE_WIDTH / 2) - POSITION[0]), ((double) PANE_HEIGHT / 2) - POSITION[1]});
            SUM_FORCE[0] += vectorTowardsCenter[0] *10;
            SUM_FORCE[1] += vectorTowardsCenter[1] *10;

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
        private double calculateAttractionForce(double relativeDistance, double attractionFactor){
            if(relativeDistance < ATTRACTION_RELATIVE_DISTANCE_CUTOUT){
                return relativeDistance / ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 1;
            } else if (relativeDistance < 1.0) {
                return (-Math.abs(relativeDistance - ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 0.5) + 0.5 ) * 2 * attractionFactor;
            }
            return 0;
        }

        private double[] calculateDirectionVector(Particle target){
            // vector from source to target
            double[] directionVector = {(target.POSITION[0] - POSITION[0]), (target.POSITION[1] - POSITION[1])};

            if(directionVector[0] < -(PANE_WIDTH - MAX_ATTRACTION_DISTANCE)){
                directionVector[0] = PANE_WIDTH + directionVector[0];
            }

            if(directionVector[1] < -(PANE_HEIGHT - MAX_ATTRACTION_DISTANCE)){
                directionVector[1] = PANE_HEIGHT + directionVector[1];
            }
            return directionVector;
        }
    }
    private class Wall extends Rectangle {
        public static final double mass = 1;

        Wall(int x, int y, int width, int height){
            super(x, y, width, height);
        }
    }
}