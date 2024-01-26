package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private static final int PARTICLES_TO_CREATE = 100 ;
//    private static final Color[] PARTICLE_SPECIES = new Color[]{Color.BLUE};
    private static final Color[] PARTICLE_SPECIES = new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.ORANGE, Color.CORAL};

//    private Particle testParticle = new Particle((int) (Math.random() * PANE_WIDTH), (int) (Math.random() * PANE_HEIGHT), PARTICLE_RADIUS, Color.BLUE, 1, 0, UPDATE_RATE_MS / 1000, new double[]{0,0}, PANE_WIDTH, PANE_HEIGHT);
    private final List<Particle> particles = new ArrayList<>();
    private final int PANE_WIDTH;
    private final int PANE_HEIGHT;
    private final double UPDATE_RATE_MS;
    private final Pane root;

    private Timeline timeline;

    ParticleSimulation(Pane root, int paneWidth, int paneHeight, double updateTimeMs){
        PANE_WIDTH = paneWidth;
        PANE_HEIGHT = paneHeight;
        UPDATE_RATE_MS = updateTimeMs;
        this.root = root;
    }

    public void initContent(){
        root.getChildren().clear();
        particles.clear();

        double[][] ATTRACTION_MATRIX = generateAttractionMatrix(PARTICLE_SPECIES.length);
        for (int j = 0; j < PARTICLE_SPECIES.length; j++) {
            for (int i = 0; i < PARTICLES_TO_CREATE; i++) {
                particles.add(new Particle((int) (Math.random() * PANE_WIDTH), (int) (Math.random() * PANE_HEIGHT), PARTICLE_RADIUS, PARTICLE_SPECIES[j], 1, j, UPDATE_RATE_MS / 1000, ATTRACTION_MATRIX[j], PANE_WIDTH, PANE_HEIGHT));
            }
        }
        root.getChildren().addAll(particles);
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
                    for(Particle particle : particles){
                        particle.move();
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void reset(){
        timeline.stop();
        initContent();
        update();
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