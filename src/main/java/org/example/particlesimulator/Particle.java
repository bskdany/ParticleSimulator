package org.example.particlesimulator;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class Particle {
    public double[] position;
    public double[] velocity;
    public double[] force;
    private double[] previousForce;
    private double rejectionProbability;

    public final double RADIUS;
    public final Color color;
    public final int SPECIES;
    public boolean isMoving;
    private int isMovingCoolDownFrames;
    private int isRogueCoolDownFrames;
    public boolean isRogue;

    Particle(int x, int y, double radius, Color color, double mass, int species){
        this.RADIUS = radius;
        this.SPECIES = species;
        this.color = color;
        this.position = new double[]{x,y};
        this.force = new double[]{0,0};
        this.velocity = new double[]{0,0};
        isMoving = true;
        isRogue = false;
        rejectionProbability = 0;
        previousForce = new double[]{0,0};
        isMovingCoolDownFrames = 0;
        isRogueCoolDownFrames = 0;
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
        double[] newForce = new double[]{0,0};

        OptimizationTracking tracking = OptimizationTracking.getInstance();

        targetParticles.forEach(targetParticle -> {
            tracking.increaseTotalInteractions();

            if(!targetParticle.isMoving && !isMoving){
                return;
            }

            if(ParticleSimulation.REJECT_RANDOM_PARTICLES){
                if(ThreadLocalRandom.current().nextDouble() < rejectionProbability){
                    tracking.increaseRandomRejected();
                    return;
                }
            }

            double directionVectorX = targetParticle.position[0] - position[0];
            double directionVectorY = targetParticle.position[1] - position[1];

            if(directionVectorX > ParticleSimulation.wrapDirectionLimitWidth){  // warp left
                directionVectorX -= ParticleSimulation.CANVAS_WIDTH -1;
            }
            else if(directionVectorX < -ParticleSimulation.wrapDirectionLimitWidth){ // warp right
                directionVectorX += ParticleSimulation.CANVAS_WIDTH +1;
            }

            if(directionVectorY > ParticleSimulation.wrapDirectionLimitHeight){ // warp top
                directionVectorY -= ParticleSimulation.CANVAS_HEIGHT -1;
            }
            else if(directionVectorY < -ParticleSimulation.wrapDirectionLimitHeight){ // warp bottom
                directionVectorY += ParticleSimulation.CANVAS_HEIGHT +1;
            }

            double distance = Math.sqrt(directionVectorX * directionVectorX + directionVectorY * directionVectorY) / ParticleSimulation.maxAttractionDistance;

            if(distance > 1.0) {
                OptimizationTracking.getInstance().increaseDiscardedOutOfRange();
                return;
            }


            OptimizationTracking.getInstance().increaseUsedInCalculation();

            double attractionFactor = AttractionMatrix.attractionMatrix[SPECIES][targetParticle.SPECIES];

            double magnitude = calculateAttractionForce(distance, attractionFactor);
            double[] normalisedDirectionVector = normalizeVector(directionVectorX, directionVectorY);

            newForce[0] += normalisedDirectionVector[0] * magnitude * ParticleSimulation.maxAttractionDistance;
            newForce[1] += normalisedDirectionVector[1] * magnitude * ParticleSimulation.maxAttractionDistance;
        });

        if(ParticleSimulation.REJECT_RANDOM_PARTICLES){
            // computing prediction for next force calculation
            double rejectionProbabilityThreshold = 15;
            double forceXPredictionPercentage = Math.abs(100 - (100 / previousForce[0]) * newForce[0]);    // 0 means the same, 10 means 10% of the values are off
            double forceYPredictionPercentage = Math.abs(100 - (100 / previousForce[1]) * newForce[1]);

            double rejectionProbabilityMaxValue = 50;

            // if the current force and the previous have less than rejectionProbability difference
            // I can then assume that the force calculation after will be similar
            // if it is indeed similar then I can use a random selection of neighbour particles instead of all neighbours
            //
            if(forceXPredictionPercentage < rejectionProbabilityThreshold && forceYPredictionPercentage < rejectionProbabilityThreshold){
                if(rejectionProbability < rejectionProbabilityMaxValue){
                    rejectionProbability += 3;
                }
                // setting force in case we are under the threshold
                force[0] = previousForce[0];
                force[1] = previousForce[1];

            }
            else{
                if(rejectionProbability > 0){
                    rejectionProbability = 0;
                }

                // setting force in case we are over the threshold and something went wrong, like if the particle has to change direction
                force[0] = (previousForce[0] + newForce[0]) / 2;
                force[1] = (previousForce[1] + newForce[1]) / 2;

            }

//        force[0] = (newForce[0] + previousForce[0]) / 2;
//        force[1] = (newForce[1] + previousForce[1]) / 2;

            previousForce[0] = force[0];
            previousForce[1] = force[1];
        }
        else{
            force[0] = newForce[0];
            force[1] = newForce[1];
        }

    }

    public void simulate(double updateTime){
        double accelerationX;
        double accelerationY;
        // Not using the force multiplier if the particle is rogue
        if(isRogue){
            // F = m / a
            accelerationX = force[0];
            accelerationY = force[1];
        }
        else{
            accelerationX = force[0] * ParticleSimulation.forceMultiplier;
            accelerationY = force[1] * ParticleSimulation.forceMultiplier;
        }

        velocity[0] *= ParticleSimulation.friction;
        velocity[1] *= ParticleSimulation.friction;

        velocity[0] += accelerationX * updateTime;
        velocity[1] += accelerationY * updateTime;

        double[] deltaPosition = new double[]{0,0};
        deltaPosition[0] = velocity[0] * updateTime;
        deltaPosition[1] = velocity[1] * updateTime;

        isMovingCoolDownFrames -= 1;
        // if the particle speed is past the threshold
        if(Math.abs(deltaPosition[0]) + Math.abs(deltaPosition[1]) > RADIUS / 5){
            // if at the last cycle the particle was not moving
            if(!isMoving){
                // set the number of frames that need to be waited before the particle can be not moving again
                isMovingCoolDownFrames = 3;
            }
            isMoving = true;
        }
        else{
            if(isMovingCoolDownFrames<0){
                isMoving = false;
            }
        }

        // if the particle speed is past the threshold
        if(Math.abs(deltaPosition[0]) + Math.abs(deltaPosition[1]) > RADIUS * 10){
            // if at the last cycle the particle was not moving
            if(!isRogue){
                // set the number of frames that need to be waited before the particle can be not moving again
                isRogueCoolDownFrames = 3;
            }
            isRogue = true;
        }
        else{
            if(isRogueCoolDownFrames<0){
                isRogue = false;
            }
        }

        position[0] += deltaPosition[0];
        position[1] += deltaPosition[1];
    }

    public static double[] normalizeVector(double directionVectorX, double directionVectorY) {
        double magnitude = Math.sqrt(directionVectorX * directionVectorX + directionVectorY * directionVectorY);
        if(magnitude < 1e-10){ // where 1e-10 is the tollerance, this is more efficient than the comparison to 0 because of floating point
            return new double[]{directionVectorX, directionVectorY};
        }
        return new double[]{directionVectorX/magnitude, directionVectorY/magnitude};
    }

    public static double calculateAttractionForce(double relativeDistance, double attractionFactor){
        if(relativeDistance < ParticleSimulation.attractionRelativeDistanceCutout){
            return relativeDistance / ParticleSimulation.attractionRelativeDistanceCutout - 1;
        } else if (relativeDistance < 1.0) {
            return (-Math.abs(relativeDistance - ParticleSimulation.attractionRelativeDistanceCutout - 0.5) + 0.5 ) * 2 * attractionFactor;
        }
        return 0;
    }

    private void calculateVectorWrap(double[] directionVector){
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

    public static double[] getParticleDirectionVector(double[] sourceParticle, double[] destinationParticle){
        return new double[]{destinationParticle[0]-sourceParticle[0], destinationParticle[1]-sourceParticle[1]};
    }
}