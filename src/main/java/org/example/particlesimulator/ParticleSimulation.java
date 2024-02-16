package org.example.particlesimulator;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;


public class ParticleSimulation{
    private final SimulationTimeline simulationTimeline;
    private final int DEFAULT_PARTICLE_COUNT = 1500;
    private final double RADIUS = 0.5;
    private Map<Color, ParticleSpeciesData> particleData = new LinkedHashMap<Color, ParticleSpeciesData>(){{
        put(Color.RED, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, RADIUS));
        put(Color.PINK, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, RADIUS));
        put(Color.ORANGE, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, RADIUS));
        put(Color.YELLOW, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, RADIUS));
        put(Color.LIME, new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, RADIUS));
        put(Color.CYAN,new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, RADIUS));
        put(Color.WHITE,new ParticleSpeciesData(DEFAULT_PARTICLE_COUNT, RADIUS));
    }};
    private List<Particle> particles = new ArrayList<>();
    public static double CANVAS_WIDTH;
    public static double CANVAS_HEIGHT;
    private final int SIMULATION_FPS = 60;
    public static double UPDATE_RATE_MS;
    private final long UPDATE_RATE_NANOSEC;
    private final GraphicsContext gc;
    private AnimationTimer timer;
    public static double friction = 0.04;
    public static double maxAttractionDistance = 30;
    public static double attractionRelativeDistanceCutout = 0.3; // 30%
    public static int forceMultiplier = 5;
    public static double wrapDirectionLimitHeight;
    public static double wrapDirectionLimitWidth;
    private final AttractionMatrix attractionMatrix;
    private long lastUpdateTime = 0;
    public static ParticleGridMap particleGridMap;

    ParticleSimulation(Canvas canvas){
        gc = canvas.getGraphicsContext2D();
        UPDATE_RATE_MS = (double) 1000 / SIMULATION_FPS;
        UPDATE_RATE_NANOSEC = (long) 1_000_000_000 / SIMULATION_FPS;
        CANVAS_WIDTH = canvas.getWidth();
        CANVAS_HEIGHT = canvas.getHeight();
        wrapDirectionLimitWidth = CANVAS_WIDTH - maxAttractionDistance - 1;
        wrapDirectionLimitHeight = CANVAS_HEIGHT - maxAttractionDistance - 1;
        simulationTimeline = new SimulationTimeline();
        attractionMatrix = new AttractionMatrix(particleData.size());
        particleGridMap = new ParticleGridMap(CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    public void initContent() {
        attractionMatrix.generateDefaultAttractionMatrix();
        initParticles();
        simulationTimeline.add(new ParticleSimulationData(attractionMatrix.getSeed(), ParticleSpeciesData.deepCopy(particleData), Particle.deepCloneList(particles), AttractionMatrix.attractionMatrix, this.friction, this.maxAttractionDistance, this.attractionRelativeDistanceCutout, this.forceMultiplier));
    }
    public void stop(){
        timer.stop();
    }
    public void start(){
        timer.start();
    }
    public void initParticles(){
        particles.clear();
        for(Map.Entry<Color, ParticleSpeciesData> speciesData : particleData.entrySet()){
            for (int i = 0; i < DEFAULT_PARTICLE_COUNT; i++) {
                createParticle(speciesData.getKey());
            }
        }
    }
    public void update(){
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // limit the animation to only work at 30fps
                long elapsedTime = now - lastUpdateTime;

                particleGridMap.update(particles);

                if(elapsedTime >= UPDATE_RATE_NANOSEC){
                    particles.parallelStream().forEach(particle -> particle.simulate());
                    clearCanvas();
                    particles.forEach(particle -> {
                        particle.adjustPositionWrapping();
                        drawParticle(particle);
                    });

                    if(System.currentTimeMillis() - simulationTimeline.lastSaveMs > SimulationTimeline.timeToSaveMs){
                        simulationTimeline.add(new ParticleSimulationData(attractionMatrix.getSeed(), ParticleSpeciesData.deepCopy(particleData), Particle.deepCloneList(particles), AttractionMatrix.attractionMatrix, friction, maxAttractionDistance, attractionRelativeDistanceCutout, forceMultiplier));
                    }
                    lastUpdateTime = now;
                }
            }
        };
        timer.start();
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
        particleData = data.PARTICLE_DATA;
        AttractionMatrix.attractionMatrix =  data.ATTRACTION_MATRIX;
        attractionMatrix.setSeed(data.seed);
        friction = data.FRICTION;
        maxAttractionDistance = data.MAX_ATTRACTION_DISTANCE;
        attractionRelativeDistanceCutout = data.ATTRACTION_RELATIVE_DISTANCE_CUTOUT;
        forceMultiplier = data.FORCE_MULTIPLIER;
        wrapDirectionLimitWidth = data.WRAP_DIRECTION_LIMIT_WIDTH;
        wrapDirectionLimitHeight = data.WRAP_DIRECTION_LIMIT_HEIGHT;

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
        ParticleSpeciesData speciesData = particleData.get(color);
        if(speciesData == null){
            return;
        }
        if(quantity > 0){
            for (int i = 0; i < quantity; i++) {
                createParticle(color);
            }
            particleData.get(color).setQuantity(speciesData.getQuantity() + quantity);
        }
        else if (quantity < 0) {
            if (speciesData.getQuantity() >= -quantity){
                for (int i = 0; i > quantity; i--) {
                    Particle particleToRemove = null;
                    for(Particle particle :particles){
                        if (particle.color == color){
                            particleToRemove = particle;
                            break;
                        }
                    }
                    if(particleToRemove != null){
                        particles.remove(particleToRemove);
                    }
                }
                particleData.get(color).setQuantity(speciesData.getQuantity() + quantity);
            }
        }
    }
    public void addParticleQuantity(int quantity, Color color, boolean areAllSelected){
        stop();
        if(areAllSelected) {
            for (Color species : particleData.keySet()) {
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
        double particleRadius = particleData.get(color).getRadius();
        int species = 0;
        for(Color key : particleData.keySet()){
            if(key == color){
                break;
            }
            species++;
        }
        particles.add(new Particle(particleX, particleY, particleRadius, color, 1, species));
    }
    public void drawParticle(Particle particle) {
        gc.setFill(particle.color);
        double radius = particle.RADIUS;
        gc.fillOval(particle.position[0] - radius, particle.position[1] - radius, 2 * radius, 2 * radius);
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
            return particleData.get(color).getQuantity();
        }
    }
    public static double getAttractionRelativeDistanceCutout() {
        return attractionRelativeDistanceCutout;
    }
    public AttractionMatrix getAttractionMatrix(){
        return attractionMatrix;
    }
}