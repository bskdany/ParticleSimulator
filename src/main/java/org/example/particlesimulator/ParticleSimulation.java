package org.example.particlesimulator;

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
    private final int DEFAULT_PARTICLE_COUNT = 200;
    private Map<Color, ParticleSpeciesData> PARTICLE_DATA = new LinkedHashMap<Color, ParticleSpeciesData>(){{
        put(Color.RED, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, 1));
        put(Color.PINK, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, 1));
        put(Color.ORANGE, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, 1));
        put(Color.YELLOW, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, 1));
        put(Color.LIME, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, 1));
        put(Color.CYAN,new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, 1));
        put(Color.WHITE,new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, 1));
    }};
    private List<Particle> particles = new ArrayList<>();
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
    private final AttractionMatrix attractionMatrix;
    ParticleSimulation(Canvas canvas, double updateTimeMs){
        this.gc = canvas.getGraphicsContext2D();
        UPDATE_RATE_MS = updateTimeMs;
        CANVAS_WIDTH = canvas.getWidth();
        CANVAS_HEIGHT = canvas.getHeight();
        WRAP_DIRECTION_LIMIT_WIDTH = CANVAS_WIDTH - MAX_ATTRACTION_DISTANCE - 1;
        WRAP_DIRECTION_LIMIT_HEIGHT = CANVAS_HEIGHT - MAX_ATTRACTION_DISTANCE - 1;
        simulationTimeline = new SimulationTimeline();
        attractionMatrix = new AttractionMatrix(PARTICLE_DATA.size());
    }
    public void initContent() {
        attractionMatrix.generateRandomAttractionMatrix();
        initParticles();
        simulationTimeline.add(new ParticleSimulationData(attractionMatrix.getSeed(), ParticleSpeciesData.deepCopy(PARTICLE_DATA), Particle.deepCloneList(particles), AttractionMatrix.ATTRACTION_MATRIX, this.FRICTION, this.MAX_ATTRACTION_DISTANCE, this.ATTRACTION_RELATIVE_DISTANCE_CUTOUT, this.FORCE_MULTIPLIER));
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
    public void initParticles(){
        particles.clear();
        for(Map.Entry<Color, ParticleSpeciesData> speciesData : PARTICLE_DATA.entrySet()){
            for (int i = 0; i < DEFAULT_PARTICLE_COUNT; i++) {
                createParticle(speciesData.getKey());
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
                if(System.currentTimeMillis() - simulationTimeline.lastSaveMs > SimulationTimeline.timeToSaveMs){
                    simulationTimeline.add(new ParticleSimulationData(attractionMatrix.getSeed(), ParticleSpeciesData.deepCopy(PARTICLE_DATA), Particle.deepCloneList(particles), AttractionMatrix.ATTRACTION_MATRIX, FRICTION, MAX_ATTRACTION_DISTANCE, ATTRACTION_RELATIVE_DISTANCE_CUTOUT, FORCE_MULTIPLIER));
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
    public void peekRewind(int offset){
        stop();
        ParticleSimulationData data = simulationTimeline.getAt(offset);
        particles = data.particles;
        PARTICLE_DATA = data.PARTICLE_DATA;
        AttractionMatrix.ATTRACTION_MATRIX =  data.ATTRACTION_MATRIX;
        attractionMatrix.setSeed(data.seed);
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
    public void addParticleQuantity(int quantity, Color color, boolean areAllSelected){
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
    public static double getAttractionRelativeDistanceCutout() {
        return ATTRACTION_RELATIVE_DISTANCE_CUTOUT;
    }
    public AttractionMatrix getAttractionMatrix(){
        return attractionMatrix;
    }
}