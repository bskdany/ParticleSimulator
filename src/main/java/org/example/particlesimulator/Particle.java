package org.example.particlesimulator;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Particle {
    public double[] position;
    public double[] velocity;
    public double[] force;

    public final double RADIUS;
    public final Color color;
    public final int SPECIES;
    public boolean isMoving;

    Particle(int x, int y, double radius, Color color, double mass, int species){
        this.RADIUS = radius;
        this.SPECIES = species;
        this.color = color;
        this.position = new double[]{x,y};
        this.force = new double[]{0,0};
        this.velocity = new double[]{0,0};
        isMoving = true;
    }

    /**
     * Copy constructor for the particle
     * @param original the original particle
     */
    Particle(Particle original){
        this.position = original.position.clone();
        this.RADIUS = original.RADIUS;
        this.color = original.color;
        this.SPECIES = original.SPECIES;
        this.velocity = original.velocity.clone();
    }

    public void adjustPositionWrapping(){
        if(position[0] < 0){
            position[0] += ParticleSimulation.CANVAS_WIDTH;
        } else if (position[0] > ParticleSimulation.CANVAS_WIDTH) {
            position[0] -= ParticleSimulation.CANVAS_WIDTH - 1;
        }
        if(position[1] < 0){
            position[1] += ParticleSimulation.CANVAS_HEIGHT;
        } else if (position[1] > ParticleSimulation.CANVAS_HEIGHT) {
            position[1] -= ParticleSimulation.CANVAS_HEIGHT -1;
        }
    }

    public void calculateCumulativeForce(Stream<Particle> targetParticles){
        Arrays.fill(force, 0);

        OptimizationTracking tracking = OptimizationTracking.getInstance();

        targetParticles.forEach(targetParticle -> {
//            tracking.increaseTotalInteractions();

            if(!targetParticle.isMoving && !isMoving){
                tracking.increaseImmobileCounter();
                return;
            }

            double[] directionVector = getParticleDirectionVector(this, targetParticle);

            if(Math.abs(directionVector[0]) > ParticleSimulation.maxAttractionDistance && Math.abs(directionVector[0]) < ParticleSimulation.wrapDirectionLimitWidth){
                tracking.increaseDiscardedOutOfRange();
                return;

            }
            if(Math.abs(directionVector[1]) > ParticleSimulation.maxAttractionDistance && Math.abs(directionVector[1]) < ParticleSimulation.wrapDirectionLimitHeight){
                tracking.increaseDiscardedOutOfRange();
                return;
            }

            Particle.calculateVectorWrap(directionVector);

            // length of the relative distance
            double distance = Math.sqrt(directionVector[0] * directionVector[0] + directionVector[1] * directionVector[1]) / ParticleSimulation.maxAttractionDistance;

            if(distance > 1) {
                OptimizationTracking.getInstance().increaseDiscardedOutOfRange();
                return;
            }

            OptimizationTracking.getInstance().increaseUsedInCalculation();

            double attractionFactor = AttractionMatrix.attractionMatrix[SPECIES][targetParticle.SPECIES];

            double magnitude = calculateAttractionForce(distance, attractionFactor);
            double[] normalisedDirectionVector = normalizeVector(directionVector);

            force[0] += normalisedDirectionVector[0] * magnitude * ParticleSimulation.maxAttractionDistance;
            force[1] += normalisedDirectionVector[1] * magnitude * ParticleSimulation.maxAttractionDistance;
        });
    }

    public void simulate(){
        // F = m / a
        double accelerationX = force[0] * ParticleSimulation.forceMultiplier;
        double accelerationY = force[1] * ParticleSimulation.forceMultiplier;

        velocity[0] *= ParticleSimulation.friction;
        velocity[1] *= ParticleSimulation.friction;

        velocity[0] += accelerationX * ParticleSimulation.UPDATE_RATE_MS / 1000;
        velocity[1] += accelerationY * ParticleSimulation.UPDATE_RATE_MS / 1000;

                            // I made this value up
        isMoving = Math.pow(velocity[0] * velocity[1], 2) > 1;

        double[] deltaPosition = new double[]{0,0};
        deltaPosition[0] = velocity[0] * ParticleSimulation.UPDATE_RATE_MS / 1000;
        deltaPosition[1] = velocity[1] * ParticleSimulation.UPDATE_RATE_MS / 1000;

        if(deltaPosition[0] > RADIUS * 10){
            // explode
        }

        position[0] += velocity[0] * ParticleSimulation.UPDATE_RATE_MS / 1000;
        position[1] += velocity[1] * ParticleSimulation.UPDATE_RATE_MS / 1000;
    }

    public static double[] normalizeVector(double[] vector) {
        double magnitude = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
        if(magnitude < 1e-10){ // where 1e-10 is the tollerance, this is more efficient than the comparison to 0 because of floating point
            return vector;
        }
        return new double[]{vector[0]/magnitude, vector[1]/magnitude};
    }

    public static double calculateAttractionForce(double relativeDistance, double attractionFactor){
        if(relativeDistance < ParticleSimulation.attractionRelativeDistanceCutout){
            return relativeDistance / ParticleSimulation.attractionRelativeDistanceCutout - 1;
        } else if (relativeDistance < 1.0) {
            return (-Math.abs(relativeDistance - ParticleSimulation.attractionRelativeDistanceCutout - 0.5) + 0.5 ) * 2 * attractionFactor;
        }
        return 0;
    }

    private static void calculateVectorWrap(double[] directionVector){
        // vector from source to target
        if(directionVector[0] > ParticleSimulation.wrapDirectionLimitWidth){  // warp left
            directionVector[0] = directionVector[0] - ParticleSimulation.CANVAS_WIDTH -1;
        }
        else if(directionVector[0] < -ParticleSimulation.wrapDirectionLimitWidth){ // warp right
            directionVector[0] = directionVector[0] + ParticleSimulation.CANVAS_WIDTH +1;
        }

        if(directionVector[1] > ParticleSimulation.wrapDirectionLimitHeight){ // warp top
            directionVector[1] = directionVector[1] - ParticleSimulation.CANVAS_HEIGHT -1;
        }
        else if(directionVector[1] < -ParticleSimulation.wrapDirectionLimitHeight){ // warp bottom
            directionVector[1] = directionVector[1] + ParticleSimulation.CANVAS_HEIGHT +1;
        }
    }

    public static List<Particle> deepCloneList(List<Particle> originalList){
        List<Particle> newList = new ArrayList<>();
        for(Particle particle : originalList){
            newList.add(new Particle(particle));
        }
        return newList;
    }

    public static double[] getParticleDirectionVector(Particle sourceParticle, Particle destinationParticle){
        return new double[]{destinationParticle.position[0]-sourceParticle.position[0], destinationParticle.position[1]-sourceParticle.position[1]};
    }
}