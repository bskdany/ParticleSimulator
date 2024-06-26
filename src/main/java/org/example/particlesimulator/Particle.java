package org.example.particlesimulator;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
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
    private boolean isMovingBuffer;
    public boolean isMoving;
    private int isMovingCoolDownFrames;
    public int id;

    Particle(int x, int y, double radius, Color color, double mass, int species, int id){
        this.RADIUS = radius;
        this.SPECIES = species;
        this.color = color;
        this.position = new double[]{x,y};
        this.force = new double[]{0,0};
        this.velocity = new double[]{0,0};
        isMoving = true;
        isMovingBuffer = true;
        rejectionProbability = 0;
        previousForce = new double[]{0,0};
        isMovingCoolDownFrames = 0;
        this.id = id;
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
        this.id = original.id;
        this.isMovingCoolDownFrames = original.isMovingCoolDownFrames;
        this.isMoving = original.isMoving;
        this.isMovingBuffer = original.isMovingBuffer;
        this.force = original.force.clone();
        this.previousForce = original.previousForce.clone();
        this.rejectionProbability = original.rejectionProbability;
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

        AtomicInteger totalInteractions = new AtomicInteger();
        AtomicInteger discardedRandom = new AtomicInteger();
        AtomicInteger discardedRange = new AtomicInteger();
        AtomicInteger usedInCalculation = new AtomicInteger();
        AtomicInteger discardedImmobile = new AtomicInteger();

        double randomVar = ThreadLocalRandom.current().nextDouble();

        targetParticles.forEach(targetParticle -> {
            if(id == targetParticle.id){
                return;
            }

            totalInteractions.addAndGet(1);

            if(!targetParticle.isMoving && !isMoving){
                discardedImmobile.addAndGet(1);
                return;
            }

            if(Configs.REJECT_RANDOM_PARTICLES_OPTIMIZATION){
                if(targetParticle.id * randomVar % 100 < rejectionProbability * 100){
                    discardedRandom.addAndGet(1);
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
                discardedRange.addAndGet(1);
                return;
            }

            usedInCalculation.addAndGet(1);

            double attractionFactor = AttractionMatrix.attractionMatrix[SPECIES][targetParticle.SPECIES];

            double magnitude = calculateAttractionForce(distance, attractionFactor);
            double[] normalisedDirectionVector = normalizeVector(directionVectorX, directionVectorY);

            newForce[0] += normalisedDirectionVector[0] * magnitude * ParticleSimulation.maxAttractionDistance;
            newForce[1] += normalisedDirectionVector[1] * magnitude * ParticleSimulation.maxAttractionDistance;
        });

        OptimizationTracking tracking = OptimizationTracking.getInstance();
        tracking.increaseTotalInteractions(totalInteractions.intValue());
        tracking.increaseRandomRejected(discardedRandom.intValue());
        tracking.increaseDiscardedOutOfRange(discardedRange.intValue());
        tracking.increaseUsedInCalculation(usedInCalculation.intValue());
        tracking.increaseImmobile(discardedImmobile.intValue());

        if(Configs.REJECT_RANDOM_PARTICLES_OPTIMIZATION){
            // computing prediction for next force calculation
            double rejectionProbabilityThreshold = 30;
            double forceXPredictionPercentage = Math.abs(100 - (100 / previousForce[0]) * newForce[0]);    // 0 means the same, 10 means 10% of the values are off
            double forceYPredictionPercentage = Math.abs(100 - (100 / previousForce[1]) * newForce[1]);

            double rejectionProbabilityMaxValue = 80;

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

        accelerationX = force[0] * ParticleSimulation.forceMultiplier;
        accelerationY = force[1] * ParticleSimulation.forceMultiplier;

        velocity[0] *= Configs.PARTICLE_FRICTION;
        velocity[1] *= Configs.PARTICLE_FRICTION;

        velocity[0] += accelerationX * updateTime;
        velocity[1] += accelerationY * updateTime;

        double[] deltaPosition = new double[]{0,0};
        deltaPosition[0] = velocity[0] * updateTime;
        deltaPosition[1] = velocity[1] * updateTime;

        double totalMovementInSecond = Math.abs(deltaPosition[0])*updateTime*1000 + Math.abs(deltaPosition[1])*updateTime*1000;

        if(Configs.CAP_PARTICLE_SPEED){
            if(totalMovementInSecond > ParticleSimulation.maxAttractionDistance * 2){
                // movement is too strong, needs to be restricted
                double ratio = ParticleSimulation.maxAttractionDistance * 2 / totalMovementInSecond;
                deltaPosition[0] *= ratio;
                deltaPosition[1] *= ratio;
            }
        }

        if(Configs.USE_IMMOBILE_OPTIMIZATION){
            isMovingCoolDownFrames -= 1;
            // if the particle deltaX or deltaY position in the next second if going to be more than 2 units, then it is moving
            if(totalMovementInSecond > ParticleSimulation.maxAttractionDistance * ParticleSimulation.attractionRelativeDistanceCutout / ParticleSimulation.forceMultiplier * 2){
                // if at the last cycle the particle was not moving
                if(!isMoving){
                    // set the number of frames that need to be waited before the particle can be not moving again
                    isMovingCoolDownFrames = 3;
                }
                isMovingBuffer = true;
            }
            else{
                if(isMoving){
                    if(isMovingCoolDownFrames<0){
                        isMovingBuffer = false;
//                        deltaPosition[0] = 0;
//                        deltaPosition[1] = 0;
                    }
                }
            }
        }

        position[0] += deltaPosition[0];
        position[1] += deltaPosition[1];
    }

    public static double[] normalizeVector(double directionVectorX, double directionVectorY) {
        double magnitude = Math.sqrt(directionVectorX * directionVectorX + directionVectorY * directionVectorY);
        if(magnitude < 1e-10){ // where 1e-10 is the tolerance, this is more efficient than the comparison to 0 because of floating point
            return new double[]{directionVectorX, directionVectorY};
        }
        return new double[]{directionVectorX/magnitude, directionVectorY/magnitude};
    }

    public static double calculateAttractionForce(double relativeDistance, double attractionFactor){
        if(relativeDistance < ParticleSimulation.attractionRelativeDistanceCutout){
            return - Math.pow(Configs.REPULSION_MULTIPLIER * relativeDistance / ParticleSimulation.attractionRelativeDistanceCutout - Configs.REPULSION_MULTIPLIER, 2);
        } else if (relativeDistance < 1.0) {
            return (-Math.abs(relativeDistance - ParticleSimulation.attractionRelativeDistanceCutout - 0.5) + 0.5 ) * attractionFactor;
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

    public void finalizeIsMovingVariable(){
        isMoving = isMovingBuffer;
    }
}