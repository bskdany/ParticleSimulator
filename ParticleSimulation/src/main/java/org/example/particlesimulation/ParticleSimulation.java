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
    private Map<Color, ParticleSpeciesData> PARTICLE_DATA = new HashMap<Color, ParticleSpeciesData>(){{
        put(Color.RED, new ParticleSpeciesData(200, 1));
        put(Color.PINK, new ParticleSpeciesData(200, 1));
        put(Color.ORANGE, new ParticleSpeciesData(200, 1));
        put(Color.YELLOW, new ParticleSpeciesData(200, 1));
        put(Color.LIME, new ParticleSpeciesData(200, 1));
        put(Color.CYAN,new ParticleSpeciesData(200, 1));
        put(Color.WHITE,new ParticleSpeciesData(200, 1));
    }};
    private final List<Particle> particles = new ArrayList<>();
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
    }

    public void initContent() {
        generateAttractionMatrix();
        initParticles();
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
//        for (int i = 0; i < size; i++) {
//            System.out.print("{");
//            for (int j = 0; j < size; j++) {
//                System.out.print(ATTRACTION_MATRIX[i][j] + ", ");
//            }
//            System.out.println("},");
//        }
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
        generateAttractionMatrix();
        timeline.play();
    }

    public void setMaxAttractionDistance(int distance){
        MAX_ATTRACTION_DISTANCE = distance;
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
            if (speciesData.getQuantity() > quantity){
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
        timeline.stop();

        if(areAllSelected) {
            for (Color species : PARTICLE_DATA.keySet()) {
               handleParticleQuantityCreationHelper(species, quantity);
            }
        }
        else{           // if only one particle needs to be removed
            handleParticleQuantityCreationHelper(color, quantity);
        }
        timeline.play();
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

    public List<Color> getParticleColors(){
        return new ArrayList<>(PARTICLE_DATA.keySet());
    }
    public int getParticleQuantity(Color color, boolean areAllSpeciesSelected){
        if(areAllSpeciesSelected){
            return particles.size();
        }
        else{
            return PARTICLE_DATA.get(color).getQuantity();
        }
    }
    public void setFriction(double value){
        FRICTION = value;
    }
    public void setForceMultiplier(int value){
        FORCE_MULTIPLIER = value;
    }
    public void setMinAttractionDistance(double value){
        ATTRACTION_RELATIVE_DISTANCE_CUTOUT = value;
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