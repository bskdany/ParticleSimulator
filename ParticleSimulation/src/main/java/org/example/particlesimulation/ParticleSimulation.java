package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.*;


public class ParticleSimulation{
    private Map<Color, ParticleSpeciesData> PARTICLE_DATA = new HashMap<Color, ParticleSpeciesData>(){{
        put(Color.WHITE, new ParticleSpeciesData(200, 1));
        put(Color.BLUE, new ParticleSpeciesData(200, 1));
        put(Color.GREEN, new ParticleSpeciesData(200, 1));
        put(Color.YELLOW, new ParticleSpeciesData(200, 1));
        put(Color.PINK, new ParticleSpeciesData(200, 1));
        put(Color.ORANGE,new ParticleSpeciesData(200, 1));
        put(Color.CORAL,new ParticleSpeciesData(200, 1));
    }};
    private final List<Particle> particles = new ArrayList<>();
    public static double[][] ATTRACTION_MATRIX;
    public static int CANVAS_WIDTH;
    public static int CANVAS_HEIGHT;
    public static double UPDATE_RATE_MS;
    private final GraphicsContext gc;
    private Timeline timeline;
    public static double FRICTION = 0.04;
    public static double MAX_ATTRACTION_DISTANCE = 100;
    public static double ATTRACTION_RELATIVE_DISTANCE_CUTOUT = 0.3; // 30%
    public static int FORCE_MULTIPLIER = 5;
    public static double WRAP_DIRECTION_LIMIT_HEIGHT;
    public static double WRAP_DIRECTION_LIMIT_WIDTH;

    ParticleSimulation(GraphicsContext gc, int canvasWidth, int canvasHeight, double updateTimeMs){
        CANVAS_WIDTH = canvasWidth;
        CANVAS_HEIGHT = canvasHeight;
        UPDATE_RATE_MS = updateTimeMs;
        WRAP_DIRECTION_LIMIT_WIDTH = CANVAS_WIDTH - MAX_ATTRACTION_DISTANCE - 1;
        WRAP_DIRECTION_LIMIT_HEIGHT = CANVAS_HEIGHT - MAX_ATTRACTION_DISTANCE - 1;
        this.gc = gc;
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
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                ATTRACTION_MATRIX[i][j] =  Double.parseDouble(decimalFormat.format(random.nextGaussian() * 0.5));
            }
        }
        for (int i = 0; i < size; i++) {
            System.out.print("{");
            for (int j = 0; j < size; j++) {
                System.out.print(ATTRACTION_MATRIX[i][j] + ", ");
            }
            System.out.println("},");
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

    public void setParticleQuantity(int quantity, Color color){
        timeline.stop();
        ParticleSpeciesData speciesData = PARTICLE_DATA.get(color);
        if(speciesData == null){
            timeline.play();
            return;
        }
        int particleDifference = Math.abs(quantity - speciesData.getQuantity());

        if(quantity > speciesData.getQuantity()){
            for (int i = 0; i < particleDifference; i++) {
                createParticle(color);
            }
        }
        else if (quantity < speciesData.getQuantity()) {
            if (speciesData.getQuantity() != 0){
                for (int i = 0; i < particleDifference; i++) {
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
            }
        }
        PARTICLE_DATA.get(color).setQuantity(quantity);
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

    public int getParticleQuantity(Color color){
        return PARTICLE_DATA.get(color).getQuantity();
    }

    public void setFriction(double value){
        FRICTION = value;
    }

    public double getFriction(){
        return FRICTION;
    }

    public void setForceMultiplier(int value){
        FORCE_MULTIPLIER = value;
    }

    public int getForceMultiplier(){
        return FORCE_MULTIPLIER;
    }

    public void setMinAttractionDistance(double value){
        ATTRACTION_RELATIVE_DISTANCE_CUTOUT = value;
    }

    public double getMinAttractionRelativeDistance(){
        return ATTRACTION_RELATIVE_DISTANCE_CUTOUT;
    }
}