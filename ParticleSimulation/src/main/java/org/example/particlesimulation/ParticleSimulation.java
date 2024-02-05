package org.example.particlesimulation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.*;


public class ParticleSimulation{
    private final SimulationTimeline simulationTimeline;
    private Map<Color, ParticleSpeciesData> PARTICLE_DATA = new LinkedHashMap<Color, ParticleSpeciesData>(){{
        put(Color.RED, new ParticleSpeciesData(200, 1));
        put(Color.PINK, new ParticleSpeciesData(200, 1));
        put(Color.ORANGE, new ParticleSpeciesData(200, 1));
        put(Color.YELLOW, new ParticleSpeciesData(200, 1));
        put(Color.LIME, new ParticleSpeciesData(200, 1));
        put(Color.CYAN,new ParticleSpeciesData(200, 1));
        put(Color.WHITE,new ParticleSpeciesData(200, 1));
    }};
    private List<Particle> particles = new ArrayList<>();
    public static double[][] ATTRACTION_MATRIX;
    public static double CANVAS_WIDTH;
    public static double CANVAS_HEIGHT;
    public static double UPDATE_RATE_MS;
    private final GraphicsContext gc;
    private Timeline timeline;
    public static double FRICTION = 0.04;
    public static double MAX_ATTRACTION_DISTANCE = 100;
    public static double ATTRACTION_RELATIVE_DISTANCE_CUTOUT = 0.3; // 30%
    public static int FORCE_MULTIPLIER = 5;
    public static double WRAP_DIRECTION_LIMIT_HEIGHT;
    public static double WRAP_DIRECTION_LIMIT_WIDTH;

    ParticleSimulation(Canvas canvas, double updateTimeMs){
        this.gc = canvas.getGraphicsContext2D();
        UPDATE_RATE_MS = updateTimeMs;
        CANVAS_WIDTH = canvas.getWidth();
        CANVAS_HEIGHT = canvas.getHeight();
        WRAP_DIRECTION_LIMIT_WIDTH = CANVAS_WIDTH - MAX_ATTRACTION_DISTANCE - 1;
        WRAP_DIRECTION_LIMIT_HEIGHT = CANVAS_HEIGHT - MAX_ATTRACTION_DISTANCE - 1;
        simulationTimeline = new SimulationTimeline();
    }

    public void initContent() {
        generateAttractionMatrix();
        initParticles();
        simulationTimeline.add(new ParticleSimulationData(this.PARTICLE_DATA, Particle.deepCloneList(particles), this.ATTRACTION_MATRIX, this.FRICTION, this.MAX_ATTRACTION_DISTANCE, this.ATTRACTION_RELATIVE_DISTANCE_CUTOUT, this.FORCE_MULTIPLIER));
    }

    public void initParticles(){
        particles.clear();
        for(Map.Entry<Color, ParticleSpeciesData> speciesData : PARTICLE_DATA.entrySet()){
            for (int i = 0; i < speciesData.getValue().getQuantity(); i++) {
                createParticle(speciesData.getKey());
            }
        }
    }

    public void generateAttractionMatrix(){
        int size = PARTICLE_DATA.size();
        ATTRACTION_MATRIX = new double[size][size];
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                ATTRACTION_MATRIX[i][j] =  Double.parseDouble(decimalFormat.format(random.nextGaussian() * 0.5));
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if(i == j){
                    ATTRACTION_MATRIX[i][j] = 0.8;
                } else if (i == j+1 || i == j-1) {
                    ATTRACTION_MATRIX[i][j] = 0.4;
                } else if (i == j+2 || i == j-2) {
                    ATTRACTION_MATRIX[i][j] = 0;
                } else if (i == j+3 || i == j-3) {
                    ATTRACTION_MATRIX[i][j] = -0.2;
                } else if (i == j+4 || i == j-4) {
                    ATTRACTION_MATRIX[i][j] = -0.4;
                } else if (i == j+5 || i == j-5) {
                    ATTRACTION_MATRIX[i][j] = -0.6;
                } else if (i == j+6 || i == j-6) {
                    ATTRACTION_MATRIX[i][j] = -0.8;
                }
            }
        }
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
                if(System.currentTimeMillis() - simulationTimeline.lastSaveMs > simulationTimeline.timeToSaveMs){
                    simulationTimeline.add(new ParticleSimulationData(this.PARTICLE_DATA, Particle.deepCloneList(particles), this.ATTRACTION_MATRIX, this.FRICTION, this.MAX_ATTRACTION_DISTANCE, this.ATTRACTION_RELATIVE_DISTANCE_CUTOUT, this.FORCE_MULTIPLIER));
                }
            })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void reset(){
        stop();
        initContent();
        start();
    }

    public void resetAttractionMatrix(){
//        timeline.stop();
        generateAttractionMatrix();
//        timeline.play();
    }

    public void setMaxAttractionDistance(int distance){
        MAX_ATTRACTION_DISTANCE = distance;
    }

    public void peekRewind(int offset){
        stop();
        ParticleSimulationData data = simulationTimeline.getAt(offset);
        particles = data.particles;
        PARTICLE_DATA = data.PARTICLE_DATA;
        ATTRACTION_MATRIX = data.ATTRACTION_MATRIX;
        FRICTION = data.FRICTION;
        MAX_ATTRACTION_DISTANCE = data.MAX_ATTRACTION_DISTANCE;
        ATTRACTION_RELATIVE_DISTANCE_CUTOUT = data.ATTRACTION_RELATIVE_DISTANCE_CUTOUT;
        FORCE_MULTIPLIER = data.FORCE_MULTIPLIER;
        WRAP_DIRECTION_LIMIT_WIDTH = data.WRAP_DIRECTION_LIMIT_WIDTH;
        WRAP_DIRECTION_LIMIT_HEIGHT = data.WRAP_DIRECTION_LIMIT_HEIGHT;

        clearCanvas();
        for(Particle p : particles){
            drawParticle(p);
        }
    }

    public void finalizeRewind(){
        simulationTimeline.setNewAtCurrent();
        start();
    }

    private void handleParticleQuantityCreationHelper(Color color, int quantity){
        ParticleSpeciesData speciesData = PARTICLE_DATA.get(color);
        if(speciesData == null){
            return;
        }
        if(quantity > 0){
            for (int i = 0; i < quantity; i++) {
                createParticle(color);
            }
            PARTICLE_DATA.get(color).setQuantity(speciesData.getQuantity() + quantity);
        }
        else if (quantity < 0) {
            if (speciesData.getQuantity() >= -quantity){
                for (int i = 0; i > quantity; i--) {
                    Particle particleToRemove = null;
                    for(Particle particle :particles){
                        if (particle.COLOR == color){
                            particleToRemove = particle;
                            break;
                        }
                    }
                    if(particleToRemove != null){
                        particles.remove(particleToRemove);
                    }
                }
                PARTICLE_DATA.get(color).setQuantity(speciesData.getQuantity() + quantity);
            }
        }
    }

    public void setParticleQuantity(int quantity, Color color, boolean areAllSelected){
        stop();

        if(areAllSelected) {
            for (Color species : PARTICLE_DATA.keySet()) {
               handleParticleQuantityCreationHelper(species, quantity);
            }
        }
        else{           // if only one particle needs to be removed
            handleParticleQuantityCreationHelper(color, quantity);
        }
        start();
    }

    private void createParticle(Color color){
        int particleX = (int) (Math.random() * CANVAS_WIDTH);
        int particleY = (int) (Math.random() * CANVAS_HEIGHT);
        int particleRadius = PARTICLE_DATA.get(color).getRadius();
        int species = 0;
        for(Color key : PARTICLE_DATA.keySet()){
            if(key == color){
                break;
            }
            species++;
        }
        particles.add(new Particle(particleX, particleY, particleRadius, color, 1, species));
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

    public int getParticleQuantity(Color color, boolean areAllSpeciesSelected){
        if(areAllSpeciesSelected){
            return particles.size();
        }
        else{
            return PARTICLE_DATA.get(color).getQuantity();
        }
    }

    public double getAttractionMatrixValueAt(int[] coordinates){
        return ATTRACTION_MATRIX[coordinates[0]][coordinates[1]];
    }

    public void setForceMultiplier(int value){
        FORCE_MULTIPLIER = value;
    }
    public void setMinAttractionDistance(double value){
        ATTRACTION_RELATIVE_DISTANCE_CUTOUT = value;
    }

    public void setAttractionMatrixValue(int[] coordinates, double value){
        stop();
        ATTRACTION_MATRIX[coordinates[0]][coordinates[1]] = value;
        start();
    }

    public void stop(){
        if(timeline.getStatus() == Animation.Status.RUNNING){
            timeline.pause();
        }
    }

    public void start(){
        if(timeline.getStatus() == Animation.Status.PAUSED){
            timeline.play();
        }
    }
}