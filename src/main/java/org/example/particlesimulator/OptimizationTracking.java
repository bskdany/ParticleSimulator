package org.example.particlesimulator;

import java.text.DecimalFormat;

public class OptimizationTracking {
    private static OptimizationTracking instance;
    private int calculationSavedByCaching;
    private int immobileParticles;
    private int rogueParticles;
    private int discardedDueToOutOfRange;
    private int usedInCalculation;
    private int totalParticleInteractions;
    private int numberOfParticlesAveraged;
    private int randomRejectedParticles;
    private int numberOfParticles;
    private int updateCounter;
    private int totalParticleCount;
    private int collisionCount;
    OptimizationTracking(){
        resetValues();
    }

    public void increaseCachingCounter(){
        calculationSavedByCaching++;
    }
    public void increaseImmobileCounter(){
        immobileParticles++;
    }
    public void increaseRogueParticles(){rogueParticles++;}
    public void setImmobile(int num){immobileParticles+=num;}
    public void setRogue(int num){rogueParticles+=num;}
    public void increaseCollision(){collisionCount++;}

    public void increaseDiscardedOutOfRange(){
        discardedDueToOutOfRange++;
    }
    public void increaseUsedInCalculation(){
        usedInCalculation++;
    }
    public void increaseParticlesAveraged(){numberOfParticlesAveraged++;}
    public void increaseTotalInteractions(){totalParticleInteractions++;}
    public void increaseRandomRejected(){randomRejectedParticles++;}
    public void increaseUpdate(){updateCounter++;}
    public void setParticleCount(int num){totalParticleCount = num;}
    public void showOptimizationData(){
        System.out.println("Caching     " + calculatePercentage(calculationSavedByCaching, totalParticleInteractions));
        System.out.println("Immobile    " + calculatePercentage(immobileParticles, totalParticleCount * updateCounter));
        System.out.println("Rogue       " + calculatePercentage(rogueParticles, totalParticleCount * updateCounter));
        System.out.println("Averaged    " + calculatePercentage(numberOfParticlesAveraged, numberOfParticles * updateCounter));
        System.out.println("Range       " + calculatePercentage(discardedDueToOutOfRange, totalParticleInteractions));
        System.out.println("Used        " + calculatePercentage(usedInCalculation, totalParticleInteractions));
        System.out.println("Rejected    " + calculatePercentage(randomRejectedParticles, totalParticleInteractions));
        System.out.println("Collisions  " + calculatePercentage(collisionCount, totalParticleCount*updateCounter));
        System.out.println();

        resetValues();
    }

    private String calculatePercentage(int value, int relative){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format((double) value / relative * 100) + "%";
    }

    private void resetValues(){
        calculationSavedByCaching = 0;
        rogueParticles = 0;
        immobileParticles = 0;
        discardedDueToOutOfRange = 0;
        usedInCalculation = 0;
        totalParticleInteractions = 1;
        numberOfParticlesAveraged = 0;
        numberOfParticles = 1500 * 7;
        updateCounter = 0;
        randomRejectedParticles = 0;
        collisionCount = 0;
    }

    public static OptimizationTracking getInstance() {
        if (instance == null) {
            instance = new OptimizationTracking();
        }
        return instance;
    }

}
