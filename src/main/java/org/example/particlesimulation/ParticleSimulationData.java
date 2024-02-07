package org.example.particlesimulation;

import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.particlesimulation.ParticleSimulation.CANVAS_HEIGHT;
import static org.example.particlesimulation.ParticleSimulation.CANVAS_WIDTH;

public class ParticleSimulationData {
    public Map<Color, ParticleSpeciesData> PARTICLE_DATA;
    public List<Particle> particles;
    public final double[][] ATTRACTION_MATRIX;
    public final double FRICTION;
    public final double MAX_ATTRACTION_DISTANCE;
    public final double ATTRACTION_RELATIVE_DISTANCE_CUTOUT;
    public final int FORCE_MULTIPLIER;
    public final double WRAP_DIRECTION_LIMIT_HEIGHT;
    public final double WRAP_DIRECTION_LIMIT_WIDTH;

    ParticleSimulationData(Map<Color, ParticleSpeciesData> particleData, List<Particle> particles, double[][] attractionMatrix, double friction, double maxAttDistance,double relDistanceCutout, int forceMultiplier){
        this.PARTICLE_DATA = new LinkedHashMap<Color, ParticleSpeciesData>(particleData);
        this.particles = new ArrayList<Particle>(particles);
        this.ATTRACTION_MATRIX = attractionMatrix;
        this.FRICTION = friction;
        this.MAX_ATTRACTION_DISTANCE = maxAttDistance;
        this.ATTRACTION_RELATIVE_DISTANCE_CUTOUT = relDistanceCutout;
        this.FORCE_MULTIPLIER = forceMultiplier;
        this.WRAP_DIRECTION_LIMIT_WIDTH = CANVAS_WIDTH - MAX_ATTRACTION_DISTANCE - 1;
        this.WRAP_DIRECTION_LIMIT_HEIGHT = CANVAS_HEIGHT - MAX_ATTRACTION_DISTANCE - 1;
    }
}
