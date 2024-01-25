package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ParticleSimulation extends Application {
    private static final int PANE_WIDTH = 1400;
    private static final int PANE_HEIGHT = 700;
//    private static final double UPDATE_RATE_MS = 16.7; // for 60 fps
    private static final double UPDATE_RATE_MS = 33.3; // for 30 fps
    private static final double PARTICLE_RADIUS = 1;
    private static final int PARTICLES_TO_CREATE = 300;
//    private static final Color[] PARTICLE_SPECIES = new Color[]{Color.BLUE};
    private static final Color[] PARTICLE_SPECIES = new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.ORANGE};

//    private Particle testParticle = new Particle((int) (Math.random() * PANE_WIDTH), (int) (Math.random() * PANE_HEIGHT), PARTICLE_RADIUS, Color.BLUE, 1, 0, UPDATE_RATE_MS / 1000, new double[]{0,0}, PANE_WIDTH, PANE_HEIGHT);
    private final List<Particle> particles = new ArrayList<>();
    Pane root = new Pane();

    private Pane createContent(){
        double[][] ATTRACTION_MATRIX = generateAttractionMatrix(PARTICLE_SPECIES.length);
//        particles.add(testParticle);
        for (int j = 0; j < PARTICLE_SPECIES.length; j++) {
            for (int i = 0; i < PARTICLES_TO_CREATE; i++) {
                particles.add(new Particle((int) (Math.random() * PANE_WIDTH), (int) (Math.random() * PANE_HEIGHT), PARTICLE_RADIUS, PARTICLE_SPECIES[j], 1, j, UPDATE_RATE_MS / 1000, ATTRACTION_MATRIX[j], PANE_WIDTH, PANE_HEIGHT));
            }
        }
        root.getChildren().addAll(particles);
        return root;
    }
    @Override
    public void start(Stage stage){
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

                particles.parallelStream().forEach(particle -> particle.simulate(particles));

                for(Particle particle : particles){
                    particle.move();
//                    System.out.println("yes");
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

    private void pressedKeyHandling(Scene scene){
        scene.setOnKeyPressed(e -> {
            KeyCode keyPressed = e.getCode();

            switch (keyPressed) {
                case UP:
                    particles.getFirst().POSITION[1] += -3;
                    break;
                case DOWN:
                    particles.getFirst().POSITION[1] += 3;
                    break;
                case LEFT:
                    particles.getFirst().POSITION[0] += -3;
                    break;
                case RIGHT:
                    particles.getFirst().POSITION[0] += 3;
                    break;
            }
//            testParticle.move();
        });
    }
}