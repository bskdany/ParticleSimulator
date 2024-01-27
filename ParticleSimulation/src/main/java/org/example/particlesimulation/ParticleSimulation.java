package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ParticleSimulation{
    private static final double PARTICLE_RADIUS = 1;
    private static final int PARTICLES_TO_CREATE = 350 ;
    private static final Color[] PARTICLE_SPECIES = new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.ORANGE, Color.CORAL};
    private final List<Particle> particles = new ArrayList<>();
    private final int PANE_WIDTH;
    private final int PANE_HEIGHT;
    private final double UPDATE_RATE_MS;
    private final GraphicsContext gc;
    private Timeline timeline;

    ParticleSimulation(GraphicsContext gc, int paneWidth, int paneHeight, double updateTimeMs){
        PANE_WIDTH = paneWidth;
        PANE_HEIGHT = paneHeight;
        UPDATE_RATE_MS = updateTimeMs;
        this.gc = gc;
    }

    public void initContent(){

        particles.clear();

        double[][] ATTRACTION_MATRIX = generateAttractionMatrix(PARTICLE_SPECIES.length);
        for (int j = 0; j < PARTICLE_SPECIES.length; j++) {
            for (int i = 0; i < PARTICLES_TO_CREATE; i++) {
                particles.add(new Particle((int) (Math.random() * PANE_WIDTH), (int) (Math.random() * PANE_HEIGHT), PARTICLE_RADIUS, PARTICLE_SPECIES[j], 1, j, UPDATE_RATE_MS / 1000,  ATTRACTION_MATRIX[j], 5, PANE_WIDTH, PANE_HEIGHT));
            }
        }
    }
    public static double[][] generateAttractionMatrix(int size){
        double[][] attractionMatrix = new double[size][size];
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                attractionMatrix[i][j] =  Double.parseDouble(decimalFormat.format(random.nextGaussian() * 0.5));
            }
        }
        for (int i = 0; i < size; i++) {
            System.out.print("{");
            for (int j = 0; j < size; j++) {
                System.out.print(attractionMatrix[i][j] + ", ");
            }
            System.out.println("},");
        }
        return attractionMatrix;
    }

    public void update(){
        timeline = new Timeline(
                new KeyFrame(Duration.millis(UPDATE_RATE_MS), actionEvent -> {
                    particles.parallelStream().forEach(particle -> particle.simulate(particles));
                    clearCanvas();
                    for(Particle particle : particles){
                        particle.adjustPositionWrapping();
                        drawParticle(particle);
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void reset(){
        timeline.stop();
        initContent();
        timeline.play();
    }

    public void resetAttractionMatrix(){
        timeline.stop();
        double[][] newAttractionMatrix = generateAttractionMatrix(PARTICLE_SPECIES.length);

        int counter = 0;
        int attractionMatrixCounter = 0;
        for(Particle particle : particles){
            particle.setRelativeAttractionMatrix(newAttractionMatrix[attractionMatrixCounter]);
            if(counter > PARTICLES_TO_CREATE){
                attractionMatrixCounter++;
                counter = 0;
            }
            counter ++;
        }
        timeline.play();
    }

    public void setMaxAttractionDistance(int distance){
        timeline.stop();
        for(Particle particle : particles){
            particle.setMaxAttractionDistance(distance);
        }
        timeline.play();
    }

    public void drawParticle(Particle particle) {
        gc.setFill(particle.COLOR);
        double radius = particle.RADIUS;
        gc.fillOval(particle.POSITION[0] - radius, particle.POSITION[1] - radius, 2 * radius, 2 * radius);
    }

    public void clearCanvas(){
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }


//    private void pressedKeyHandling(Scene scene){
//        scene.setOnKeyPressed(e -> {
//            KeyCode keyPressed = e.getCode();
//
//            switch (keyPressed) {
//                case UP:
//                    particles.getFirst().POSITION[1] += -3;
//                    break;
//                case DOWN:
//                    particles.getFirst().POSITION[1] += 3;
//                    break;
//                case LEFT:
//                    particles.getFirst().POSITION[0] += -3;
//                    break;
//                case RIGHT:
//                    particles.getFirst().POSITION[0] += 3;
//                    break;
//            }
////            testParticle.move();
//        });
//    }
}