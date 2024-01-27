package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.*;


public class ParticleSimulation{
    private double PARTICLE_RADIUS = 1;

    private double[][] ATTRACTION_MATRIX;

    private Map<Color, ParticleSpeciesData> PARTICLE_DATA = new HashMap<Color, ParticleSpeciesData>(){{
        put(Color.WHITE, new ParticleSpeciesData(100, 1));
        put(Color.BLUE, new ParticleSpeciesData(100, 1));
        put(Color.GREEN, new ParticleSpeciesData(100, 1));
        put(Color.YELLOW, new ParticleSpeciesData(100, 1));
        put(Color.PINK, new ParticleSpeciesData(100, 1));
        put(Color.ORANGE,new ParticleSpeciesData(100, 1));
        put(Color.CORAL,new ParticleSpeciesData(100, 1));
    }};
    private final List<Particle> particles = new ArrayList<>();
    private final int CANVAS_WIDTH;
    private final int CANVAS_HEIGHT;
    private final double UPDATE_RATE_MS;
    private final GraphicsContext gc;
    private Timeline timeline;

    ParticleSimulation(GraphicsContext gc, int canvasWidth, int canvasHeight, double updateTimeMs){
        this.CANVAS_WIDTH = canvasWidth;
        this.CANVAS_HEIGHT = canvasHeight;
        this.UPDATE_RATE_MS = updateTimeMs;
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
        for(Particle particle :particles){
            particle.setRelativeAttractionMatrix(ATTRACTION_MATRIX[particle.SPECIES]);
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

    public void setParticleQuantity(int quantity, Color color){
        timeline.stop();
        ParticleSpeciesData speciesData = PARTICLE_DATA.get(color);
        if(speciesData == null){
            timeline.play();
            return;
        }
        int particleDifference = Math.abs(quantity - speciesData.getQuantity());
        System.out.println(quantity);

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
        particles.add(new Particle(particleX, particleY, particleRadius, color, 1, species , UPDATE_RATE_MS / 1000,  ATTRACTION_MATRIX[species], 5, CANVAS_WIDTH, CANVAS_HEIGHT));
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

}