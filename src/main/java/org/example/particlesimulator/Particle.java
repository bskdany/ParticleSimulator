package org.example.particlesimulator;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Particle {
    public double[] position;
    public final double RADIUS;
    public Color color;
    public final double MASS;
    public final int SPECIES;
    private final double[] deltaPosition;
    private double[] force;
    public double[] velocity;
    public boolean mixedSpecies;
    public boolean isMoving;
    public static int particleMissRate = 0;
    public static int particleChecks = 0;
    public int[] containingSpecies;
    double[] directionVector = new double[2];
    public double[] attractionFactor;
    public int containingSpeciesCount;
    Particle(int x, int y, double radius, Color color, double mass, int species){
        this.RADIUS = radius;
        this.MASS = mass;
        this.SPECIES = species;
        this.color = color;
        this.position = new double[]{x,y};
        this.deltaPosition = new double[]{0, 0};
        this.force = new double[]{0,0};
        this.velocity = new double[]{0,0};
        this.mixedSpecies = false;
        attractionFactor = new double[7];
        containingSpeciesCount = 1;
        isMoving = true;
    }
    Particle(double[] position, int[] containingSpecies){
        this.position = position;
        this.mixedSpecies = true;
        this.containingSpecies = containingSpecies;
        this.deltaPosition = new double[]{0, 0};
        this.force = new double[]{0,0};
        this.velocity = new double[]{0,0};
        RADIUS = 0;
        MASS = 0;
        SPECIES = 0;
        // here I am calculating the cumulative attraction factor that the particle would have
        // if any other particle want to calculate the force with it
        // for example a yellow particle could consider this particle as attractive
        // while a red one could consider it as repulsive
        attractionFactor = new double[7];

        for (int i = 0; i < 7; i++) {       // where 7 is the number of species
            attractionFactor[i] = 0;

            // if I was particle with species i (0) what is my attraction to this approximated particle?
            for (int j = 0; j < 7; j++) {
                attractionFactor[i] += AttractionMatrix.attractionMatrix[i][j] * containingSpecies[j];
            }
            containingSpeciesCount += containingSpecies[i];
        }
    }
    /**
     * Copy constructor for the particle
     * @param original the original particle
     */
    Particle(Particle original){
        this.position = original.position.clone();
        this.RADIUS = original.RADIUS;
        this.color = original.color;
        this.MASS = original.MASS;
        this.SPECIES = original.SPECIES;
        this.deltaPosition = original.deltaPosition.clone();
        this.force = original.force.clone();
        this.velocity = original.velocity.clone();
        this.directionVector = new double[]{0,0};
        this.mixedSpecies = original.mixedSpecies;
        this.containingSpecies = original.containingSpecies;
        this.attractionFactor = original.attractionFactor;
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

    public static double[] calculateCumulativeParticleForce(Particle sourceParticle){
        double[] force = new double[2];
        ParticleSimulation.particleGridMap.getParticleAround(sourceParticle).forEach(targetParticle -> {
            if(!sourceParticle.isMoving && !targetParticle.isMoving){
                return;
            }

            double[] directionVector = getParticleDirectionVector(sourceParticle, targetParticle);

            if(Math.abs(directionVector[0]) > ParticleSimulation.maxAttractionDistance && Math.abs(directionVector[0]) < ParticleSimulation.wrapDirectionLimitWidth){
                return;
            }
            if(Math.abs(directionVector[1]) > ParticleSimulation.maxAttractionDistance && Math.abs(directionVector[1]) < ParticleSimulation.wrapDirectionLimitHeight){
                return;
            }

            Particle.calculateVectorWrap(directionVector);

            // length of the relative distance
            double distance = Math.sqrt(directionVector[0] * directionVector[0] + directionVector[1] * directionVector[1]) / ParticleSimulation.maxAttractionDistance;

            if(distance > 1) {
                return;
            }

            double attractionFactor;
            if(targetParticle.mixedSpecies){
                attractionFactor = targetParticle.attractionFactor[sourceParticle.SPECIES];
            }
            else {
                attractionFactor = AttractionMatrix.attractionMatrix[sourceParticle.SPECIES][targetParticle.SPECIES];

            }

            double magnitude =  calculateAttractionForce(distance, attractionFactor);
            double[] normalisedDirectionVector = normalizeVector(directionVector);

            force[0] += normalisedDirectionVector[0] * magnitude * ParticleSimulation.maxAttractionDistance * targetParticle.containingSpeciesCount;
            force[1] += normalisedDirectionVector[1] * magnitude * ParticleSimulation.maxAttractionDistance * targetParticle.containingSpeciesCount;
        });
        return force;
    }

    public static double[] calculateSpeciesParticleForce(Particle sourceParticle, ArrayList<Integer> keysToCells, int targetSpecie){
        double[] force = new double[2];
        ParticleSimulation.particleGridMap.getParticlesAtKeysOfSpecie(keysToCells, targetSpecie).forEach(targetParticle -> {
            double[] directionVector = getParticleDirectionVector(sourceParticle, targetParticle);

            if(Math.abs(directionVector[0]) > ParticleSimulation.maxAttractionDistance && Math.abs(directionVector[0]) < ParticleSimulation.wrapDirectionLimitWidth){
                return;
            }
            if(Math.abs(directionVector[1]) > ParticleSimulation.maxAttractionDistance && Math.abs(directionVector[1]) < ParticleSimulation.wrapDirectionLimitHeight){
                return;
            }

            Particle.calculateVectorWrap(directionVector);

            // length of the relative distance
            double distance = Math.sqrt(directionVector[0] * directionVector[0] + directionVector[1] * directionVector[1]) / ParticleSimulation.maxAttractionDistance;

            if(distance > 1) {
                return;
            }

            double attractionFactor;
            if(targetParticle.mixedSpecies){
                attractionFactor = targetParticle.attractionFactor[sourceParticle.SPECIES];
            }
            else {
                attractionFactor = AttractionMatrix.attractionMatrix[sourceParticle.SPECIES][targetParticle.SPECIES];

            }

            double magnitude =  calculateAttractionForce(distance, attractionFactor);
            double[] normalisedDirectionVector = normalizeVector(directionVector);

            force[0] += normalisedDirectionVector[0] * magnitude * ParticleSimulation.maxAttractionDistance * targetParticle.containingSpeciesCount;
            force[1] += normalisedDirectionVector[1] * magnitude * ParticleSimulation.maxAttractionDistance * targetParticle.containingSpeciesCount;
        });
        return force;
    }

    public void simulate(){
        force[0] = 0;
        force[1] = 0;

//        ParticleForceCache particleForceCache = ParticleForceCache.getInstance();
//
//        // 7 configurations, one for each species
//        ArrayList<Integer>[] particleConfiguration = particleForceCache.encodeParticlesConfiguration(this);
//
//        for (int i = 0; i < particleConfiguration.length; i++) {
//            if(particleConfiguration[i].isEmpty()){
//                continue;
//            }
//
//            double[] cachedForce = particleForceCache.getCachedConfiguration(particleConfiguration[i], i);
//
//            if(cachedForce == null){
//                // I need to calculate the force for only the particles that are not cached
//                double[] calculatedForce = calculateSpeciesParticleForce(this, particleConfiguration[i], i);
//                particleForceCache.addConfigurationToCache(particleConfiguration[i], i, calculatedForce);
//                force[0] += calculatedForce[0];
//                force[1] += calculatedForce[1];
//            }
//            else{
//                force[0] += cachedForce[0];
//                force[1] += cachedForce[1];
//            }
//        }

        force = calculateCumulativeParticleForce(this);

        // all particles move towards the center slowly
//        double[] vectorTowardsCenter = normalizeVector(new double[] {(( ParticleSimulation.CANVAS_WIDTH / 2) - position[0]), ( ParticleSimulation.CANVAS_HEIGHT / 2) - position[1]});
//        force[0] += vectorTowardsCenter[0] * ParticleSimulation.CENTRAL_ATTRACTION_MULTIPLIER;
//        force[1] += vectorTowardsCenter[1] * ParticleSimulation.CENTRAL_ATTRACTION_MULTIPLIER;

        // F = m / a
        double accelerationX = force[0] * ParticleSimulation.forceMultiplier / MASS;
        double accelerationY = force[1] * ParticleSimulation.forceMultiplier / MASS;

        velocity[0] *= ParticleSimulation.friction;
        velocity[1] *= ParticleSimulation.friction;

        velocity[0] += accelerationX * ParticleSimulation.UPDATE_RATE_MS / 1000;
        velocity[1] += accelerationY * ParticleSimulation.UPDATE_RATE_MS / 1000;

        deltaPosition[0] = velocity[0] * ParticleSimulation.UPDATE_RATE_MS / 1000;
        deltaPosition[1] = velocity[1] * ParticleSimulation.UPDATE_RATE_MS / 1000;

        if(deltaPosition[0] > RADIUS * 10){
            // explode
        }

        if(deltaPosition[0]*deltaPosition[1]>0){
            isMoving = true;
        }
        else{
            isMoving = false;
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

    private static double[] calculateVectorWrap(double[] directionVector){
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
        return directionVector;
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