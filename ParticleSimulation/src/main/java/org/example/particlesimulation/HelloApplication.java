package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Vector;

public class HelloApplication extends Application {
    private static final int PANE_WIDTH = 500;
    private static final int PANE_HEIGHT = 500;
    private static final double UPDATE_RATE_MS = 16.7; // for 60 fps

    private static final int PARTICLE_RADIUS = 10;
    private Particle particle = new Particle(100, 100, PARTICLE_RADIUS, Color.BLUE);
    private Wall wallTop = new Wall(0, -10, PANE_WIDTH, 10);
    private Wall wallRight = new Wall(PANE_WIDTH, 0, 10, PANE_HEIGHT);
    private Wall wallBottom = new Wall(0, PANE_HEIGHT, PANE_WIDTH, 10);
    private Wall wallLeft = new Wall(-10, 0, 10, PANE_HEIGHT);

    private Wall testWall = new Wall(200, 100, 70, 150);
    private Wall[] walls = {wallBottom, wallLeft, wallTop, wallRight, testWall};

    Pane root = new Pane();


    private Pane createContent(){
        root.getChildren().add(particle);
        root.getChildren().add(wallTop);
        root.getChildren().add(wallRight);
        root.getChildren().add(wallBottom);
        root.getChildren().add(wallLeft);
        root.getChildren().add(testWall);
//        root.requestLayout();
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

        startUpdate();

    }

    private void startUpdate(){

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(UPDATE_RATE_MS), actionEvent -> {
                particle.move();
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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
        public static final double mass = 1;
        public double[] direction = {1,1};
        public double[] speed = {3,3};

        Particle(int x, int y, int radius, Color color){
            super(x,y,radius,color);
            setCenterX(x);
            setCenterY(y);
        }

        public void move(){

            for (int i = 0; i < walls.length; i++) {
                if(getBoundsInParent().intersects(walls[i].getBoundsInParent())){
                    System.out.println("Collision detected");

                    Rectangle rectangle = walls[i];

                    // the coordinate where the intersection happened is just the particle x and y coordinates
                    // and the radius going in some direction

                    double particleX = getCenterX();
                    double particleY = getCenterY();

                    double rectangleX = rectangle.getX();
                    double rectangleY = rectangle.getY();
                    double rectangleWidth = rectangle.getWidth();
                    double rectangleHeight = rectangle.getHeight();
                    double rectangleOriginX = rectangleX + rectangleWidth / 2;
                    double rectangleOriginY = rectangleY + rectangleHeight / 2;

                    double deltaXRectangleParticle = rectangleOriginX - particleX;
                    double deltaYRectangleParticle = rectangleOriginY - particleY;

                    double collisionOriginX = particleX;
                    double collisionOriginY = particleY;
                    double[] normalVector = {0,0};

                    if(deltaXRectangleParticle > rectangleWidth / 2){
                        collisionOriginX = particleX + (deltaXRectangleParticle - rectangleWidth/2);
                        normalVector[0] = -1;
                    } else if (deltaXRectangleParticle < -(rectangleWidth / 2)) {
                        collisionOriginX = particleX + (deltaXRectangleParticle  + rectangleWidth/2);
                        normalVector[0] = 1;
                    }

                    if(deltaYRectangleParticle > rectangleHeight / 2){
                        collisionOriginY = particleY + (deltaYRectangleParticle - rectangleHeight/2);
                        normalVector[1] = -1;
                    } else if (deltaYRectangleParticle < -(rectangleHeight / 2)) {
                        collisionOriginY = particleY + (deltaYRectangleParticle + rectangleHeight/2);
                        normalVector[1] = 1;
                    }

                    Circle collisionPlaceholder = new Circle(collisionOriginX, collisionOriginY, 3,Color.RED );
                    root.getChildren().add(collisionPlaceholder);

                    double particleAngleImpactRAD = Math.atan2(direction[0], direction[1]);
//                    double
                    direction = calculateReflectionVector(direction, normalVector);


//                    System.out.println(particleAngleImpactRAD);



                }
            }
            setCenterX(getCenterX() + speed[0] * direction[0]);
            setCenterY(getCenterY() + speed[1] * direction[1]);
        }

        public static double[] normalizeVector(double[] vector) {
            double magnitude = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
            return new double[]{vector[0] / magnitude, vector[1] / magnitude};
        }

        public static double[] calculateReflectionVector(double[] directionVector, double[] normalVector) {
            // Normalize the direction vector
            directionVector = normalizeVector(directionVector);

            // Calculate the dot product
            double dotProduct = directionVector[0] * normalVector[0] + directionVector[1] * normalVector[1];

            // Calculate the reflection vector
            double[] reflectionVector = {
                    directionVector[0] - 2 * dotProduct * normalVector[0],
                    directionVector[1] - 2 * dotProduct * normalVector[1]
            };

            // Normalize the reflection vector
            reflectionVector = normalizeVector(reflectionVector);

            return reflectionVector;
        }

    }

    private class Wall extends Rectangle {
        public static final double mass = 1;

        Wall(int x, int y, int width, int height){
            super(x, y, width, height);
        }
    }


}