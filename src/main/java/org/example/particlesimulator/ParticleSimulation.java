package org.example.particlesimulator;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;


public class ParticleSimulation{
    private final SimulationTimeline simulationTimeline;
    private Map<Color, ParticleSpeciesData> particleData;
    private List<Particle> particles = new ArrayList<>();
    public static double CANVAS_WIDTH;
    public static double CANVAS_HEIGHT;
    private final GraphicsContext gc;
    private AnimationTimer timer;
    private SidebarController controller;
    public static int CENTRAL_ATTRACTION_MULTIPLIER;
    public static double maxAttractionDistance;
    public static double attractionRelativeDistanceCutout;
    public static int forceMultiplier;
    public static double wrapDirectionLimitHeight;
    public static double wrapDirectionLimitWidth;
    private final AttractionMatrix attractionMatrix;
    private long lastUpdateTime;
    public static ParticleGridMap particleGridMap;
    private long lastFpsShowTime;
    private int updateCount = 0;
    private int maxParticleId = 0;

    ParticleSimulation(Canvas canvas){
        particleData = new LinkedHashMap<Color, ParticleSpeciesData>(){{
            put(Color.RED, new ParticleSpeciesData(Configs.DEFAULT_PARTICLE_COUNT, Configs.PARTICLE_RADIUS));
            put(Color.PINK, new ParticleSpeciesData(Configs.DEFAULT_PARTICLE_COUNT, Configs.PARTICLE_RADIUS));
            put(Color.ORANGE, new ParticleSpeciesData(Configs.DEFAULT_PARTICLE_COUNT, Configs.PARTICLE_RADIUS));
            put(Color.YELLOW, new ParticleSpeciesData(Configs.DEFAULT_PARTICLE_COUNT, Configs.PARTICLE_RADIUS));
            put(Color.LIME, new ParticleSpeciesData(Configs.DEFAULT_PARTICLE_COUNT, Configs.PARTICLE_RADIUS));
            put(Color.CYAN,new ParticleSpeciesData(Configs.DEFAULT_PARTICLE_COUNT, Configs.PARTICLE_RADIUS));
            put(Color.WHITE,new ParticleSpeciesData(Configs.DEFAULT_PARTICLE_COUNT, Configs.PARTICLE_RADIUS));
        }};
        gc = canvas.getGraphicsContext2D();
        CANVAS_WIDTH = canvas.getWidth();
        CANVAS_HEIGHT = canvas.getHeight();
        wrapDirectionLimitWidth = CANVAS_WIDTH - maxAttractionDistance - 1;
        wrapDirectionLimitHeight = CANVAS_HEIGHT - maxAttractionDistance - 1;

        maxAttractionDistance = Configs.DEFAULT_MAX_ATTRACTION_DISTANCE;
        forceMultiplier = Configs.DEFAULT_FORCE_MULTIPLIER;
        attractionRelativeDistanceCutout = Configs.DEFAULT_MIN_ATTRACTION_DISTANCE_RELATIVE;

        simulationTimeline = new SimulationTimeline();
        attractionMatrix = new AttractionMatrix(particleData.size());
        particleGridMap = new ParticleGridMap(CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    public void initContent() {
        attractionMatrix.generateDefaultAttractionMatrix();
        initParticles();
        simulationTimeline.add(new ParticleSimulationData(attractionMatrix.getSeed(), ParticleSpeciesData.deepCopy(particleData), Particle.deepCloneList(particles), AttractionMatrix.attractionMatrix, Configs.PARTICLE_FRICTION, this.maxAttractionDistance, this.attractionRelativeDistanceCutout, this.forceMultiplier));
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
            for (int i = 0; i < Configs.DEFAULT_PARTICLE_COUNT; i++) {
                createParticle(speciesData.getKey());
            }
        }
        OptimizationTracking.getInstance().setParticleCount(particles.size());
    }

    public void update(){
        timer = new AnimationTimer() {
            static private boolean simulationBufferReady = false;

            private void simulate(double timeUpdate){
                particleGridMap.update((ArrayList<Particle>) particles);

                particleGridMap.getParticlesPositionHashMap().stream().parallel().forEach(particles -> {
                    particles.forEach(particle -> {
                        particle.calculateCumulativeForce(particleGridMap.getParticleAround(particle));
                    });
                });

                particles.forEach(particle -> particle.simulate(timeUpdate));

                particles.forEach(particle -> {
                    particle.adjustPositionWrapping();
                    if(Configs.USE_IMMOBILE_OPTIMIZATION){
                        particle.finalizeIsMovingVariable();
                    }
                });

//                OptimizationTracking.getInstance().setImmobile(particles.size() - (int) particles.stream().filter(particle -> particle.isMoving).count());
            }
            private void display(){
                clearCanvas();

                particles.forEach(particle -> {
                    drawParticle(particle);
                });

                updateCount ++;
//                ParticleForceCache.getInstance().clearCache();

                if(System.currentTimeMillis() - simulationTimeline.lastSaveMs > SimulationTimeline.timeToSaveMs){
                    simulationTimeline.add(new ParticleSimulationData(attractionMatrix.getSeed(), ParticleSpeciesData.deepCopy(particleData), Particle.deepCloneList(particles), AttractionMatrix.attractionMatrix, Configs.PARTICLE_FRICTION, maxAttractionDistance, attractionRelativeDistanceCutout, forceMultiplier));
                }
            }

            @Override
            public void handle(long now) {
                long elapsedTimeFromLastUpdate = now - lastUpdateTime;      // all this stuff is in nanoseconds
                long elapsedTimeFromLastSecond = now - lastFpsShowTime;

                if(Configs.CAP_FPS){
                    double targetTimeUpdate = (double) 1000 / Configs.TARGET_FPS; // milliseconds

                    if(!simulationBufferReady){
                        simulate(targetTimeUpdate/1000);
                        simulationBufferReady = true;
                    }

                    if(elapsedTimeFromLastUpdate >= (double) Configs.TARGET_FPS * 1_000_000){
                        display();
                        simulationBufferReady = false;
                        lastUpdateTime = now;
                    }

                }
                else{
                    // calculating the update time at every update
                    double timeUpdate = (double) elapsedTimeFromLastUpdate / 1_000_000_000;
                        if(timeUpdate >= 2){
                        // this is needed because at the beginning elapsed time is 0
                        timeUpdate = (double) 1_000 / Configs.TARGET_FPS / 1000;
                    }
                    lastUpdateTime = now;
                    simulate(timeUpdate);
                    display();

                }

                if(elapsedTimeFromLastSecond > 1_000_000_000){
                    System.out.println("FPS:        "  + updateCount);
                    controller.setFPSCounter(updateCount);
                    OptimizationTracking.getInstance().showOptimizationData();
                    lastFpsShowTime = now;

                    if(Configs.USE_DYNAMIC_PARTICLE_COUNT){
                        if(Configs.DYNAMIC_PARTICLE_COUNT_GRACE_PERIOD <= 0){
                            if(updateCount < Configs.DYNAMIC_PARTICLE_COUNT_FPS_THRESHOLD){
                                addParticleQuantity(-500, Color.BLACK, true);
                                controller.updateParticleCount();
                            }
                        }
                        else{
                            Configs.DYNAMIC_PARTICLE_COUNT_GRACE_PERIOD-=1;
                        }

                    }

                    updateCount = 0;
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
        particles.add(new Particle(particleX, particleY, particleRadius, color, 1, species, maxParticleId));
        maxParticleId += 1;
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

    public void setController(SidebarController controller){
        this.controller = controller;
    }
}