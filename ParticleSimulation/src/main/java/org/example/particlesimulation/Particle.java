package org.example.particlesimulation;

import javafx.scene.paint.Color;

import java.util.List;

public class Particle {
    public double MASS;
    public int SPECIES;
    private final double[] FORCE = {0,0};
    public double[] POSITION = {0, 0};
    public double[] VELOCITY = {0,0};
    public final double RADIUS;
    private final double[] DELTA_POSITION = {0,0};
    public Color COLOR;
    double[] directionVector = new double[2];
    Particle(int x, int y, double radius, Color color, double mass, int species){
        POSITION[0] = x;
        POSITION[1] = y;
        RADIUS = radius;
        COLOR = color;
        MASS = mass;
        SPECIES = species;
    }

    public void adjustPositionWrapping(){
        if(POSITION[0] < 0){
            POSITION[0] += ParticleSimulation.CANVAS_WIDTH + 1;
        } else if (POSITION[0] > ParticleSimulation.CANVAS_WIDTH) {
            POSITION[0] -= ParticleSimulation.CANVAS_WIDTH - 1;
        }
        if(POSITION[1] < 0){
            POSITION[1] += ParticleSimulation.CANVAS_HEIGHT +1;
        } else if (POSITION[1] > ParticleSimulation.CANVAS_HEIGHT) {
            POSITION[1] -= ParticleSimulation.CANVAS_HEIGHT -1;
        }
    }
    public void simulate(List<Particle> particles){
        FORCE[0] = 0;
        FORCE[1] = 0;
        for(Particle particle : particles){
            if(particle != this){
                directionVector[0] = particle.POSITION[0] - POSITION[0];
                directionVector[1] = particle.POSITION[1] - POSITION[1];

                if(Math.abs(directionVector[0]) > ParticleSimulation.MAX_ATTRACTION_DISTANCE && Math.abs(directionVector[0]) < ParticleSimulation.WRAP_DIRECTION_LIMIT_WIDTH){
                    continue;
                }
                if(Math.abs(directionVector[1]) > ParticleSimulation.MAX_ATTRACTION_DISTANCE && Math.abs(directionVector[1]) < ParticleSimulation.WRAP_DIRECTION_LIMIT_HEIGHT){
                    continue;
                }

                double[] directionVector = calculateVectorWrap();

                // length of the relative distance
                double distance = Math.sqrt(directionVector[0] * directionVector[0] + directionVector[1] * directionVector[1]) / ParticleSimulation.MAX_ATTRACTION_DISTANCE;

                if(distance > 1) {
                    continue;
                }
                double attractionFactor = ParticleSimulation.ATTRACTION_MATRIX[SPECIES][particle.SPECIES];
                double magnitude =  calculateAttractionForce(distance, attractionFactor);
                double[] normalisedDirectionVector = normalizeVector(directionVector);

                FORCE[0] += normalisedDirectionVector[0] * magnitude * ParticleSimulation.MAX_ATTRACTION_DISTANCE;
                FORCE[1] += normalisedDirectionVector[1] * magnitude * ParticleSimulation.MAX_ATTRACTION_DISTANCE;
            }
        }
        // all particles move towards the center slowly
        double[] vectorTowardsCenter = normalizeVector(new double[] {(( ParticleSimulation.CANVAS_WIDTH / 2) - POSITION[0]), ( ParticleSimulation.CANVAS_HEIGHT / 2) - POSITION[1]});
        FORCE[0] += vectorTowardsCenter[0];
        FORCE[1] += vectorTowardsCenter[1];

        // F = m / a
        double accelerationX = FORCE[0] * ParticleSimulation.FORCE_MULTIPLIER / MASS;
        double accelerationY = FORCE[1] * ParticleSimulation.FORCE_MULTIPLIER / MASS;

        VELOCITY[0] *= ParticleSimulation.FRICTION;
        VELOCITY[1] *= ParticleSimulation.FRICTION;

        VELOCITY[0] += accelerationX * ParticleSimulation.UPDATE_RATE_MS / 1000;
        VELOCITY[1] += accelerationY * ParticleSimulation.UPDATE_RATE_MS / 1000;

        DELTA_POSITION[0] = VELOCITY[0] * ParticleSimulation.UPDATE_RATE_MS / 1000;
        DELTA_POSITION[1] = VELOCITY[1] * ParticleSimulation.UPDATE_RATE_MS / 1000;

        if(DELTA_POSITION[0] > RADIUS * 10){
            // explode
        }

        POSITION[0] += VELOCITY[0] * ParticleSimulation.UPDATE_RATE_MS / 1000;
        POSITION[1] += VELOCITY[1] * ParticleSimulation.UPDATE_RATE_MS / 1000;

    }

    private static double[] normalizeVector(double[] vector) {
        double magnitude = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
        if(magnitude != 0){
            return new double[]{vector[0] / magnitude, vector[1] / magnitude};
        }
        return vector;
    }

    private double calculateAttractionForce(double relativeDistance, double attractionFactor){
        if(relativeDistance < ParticleSimulation.ATTRACTION_RELATIVE_DISTANCE_CUTOUT){
            return relativeDistance / ParticleSimulation.ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 1;
        } else if (relativeDistance < 1.0) {
            return (-Math.abs(relativeDistance - ParticleSimulation.ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 0.5) + 0.5 ) * 2 * attractionFactor;
        }
        return 0;
    }

    private double[] calculateVectorWrap(){
        // vector from source to target
        if(directionVector[0] > ParticleSimulation.WRAP_DIRECTION_LIMIT_WIDTH){  // warp left
            directionVector[0] = directionVector[0] - ParticleSimulation.CANVAS_WIDTH -1;
        }
        else if(directionVector[0] < -ParticleSimulation.WRAP_DIRECTION_LIMIT_WIDTH){ // warp right
            directionVector[0] = directionVector[0] + ParticleSimulation.CANVAS_WIDTH +1;
        }

        if(directionVector[1] > ParticleSimulation.WRAP_DIRECTION_LIMIT_HEIGHT){ // warp top
            directionVector[1] = directionVector[1] - ParticleSimulation.CANVAS_HEIGHT -1;
        }
        else if(directionVector[1] < -ParticleSimulation.WRAP_DIRECTION_LIMIT_HEIGHT){ // warp bottom
            directionVector[1] = directionVector[1] + ParticleSimulation.CANVAS_HEIGHT +1;
        }
        return directionVector;
    }

}