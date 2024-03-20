package org.example.particlesimulator;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class OptimizationTracking {
    private static OptimizationTracking instance;
    private final AtomicInteger immobileParticles = new AtomicInteger();
    private final AtomicInteger discardedDueToOutOfRange = new AtomicInteger();
    private final AtomicInteger usedInCalculation = new AtomicInteger();
    private final AtomicInteger totalParticleInteractions = new AtomicInteger();
    private final AtomicInteger numberOfParticlesAveraged = new AtomicInteger();
    private final AtomicInteger randomRejectedParticles = new AtomicInteger();
    private int numberOfParticles;
    private int updateCounter;
    private int totalParticleCount;
    OptimizationTracking(){
        resetValues();
    }

    public void increaseImmobile(int num){
        immobileParticles.addAndGet(num);
    }
    public void setImmobile(int num){immobileParticles.addAndGet(num);}
    public void increaseDiscardedOutOfRange(int num){
        discardedDueToOutOfRange.addAndGet(num);
    }
    public void increaseUsedInCalculation(int num){
        usedInCalculation.addAndGet(num);
    }
    public void increaseParticlesAveraged(){numberOfParticlesAveraged.addAndGet(1);}
    public void increaseTotalInteractions(int num){totalParticleInteractions.addAndGet(num);}
    public void increaseRandomRejected(int num){randomRejectedParticles.addAndGet(num);}
    public void increaseUpdate(){updateCounter+=1;}
    public void setParticleCount(int num){totalParticleCount = num;}
    public void showOptimizationData(){
        System.out.println("Averaged    " + calculatePercentage(numberOfParticlesAveraged.intValue(), numberOfParticles * updateCounter));
        System.out.println("Immobile    " + calculatePercentage(immobileParticles.intValue(), totalParticleInteractions.intValue()));
        System.out.println("Rejected    " + calculatePercentage(randomRejectedParticles.intValue(), totalParticleInteractions.intValue()));
        System.out.println("Range       " + calculatePercentage(discardedDueToOutOfRange.intValue(), totalParticleInteractions.intValue()));
        System.out.println("Used        " + calculatePercentage(usedInCalculation.intValue(), totalParticleInteractions.intValue()));
        System.out.println();

        resetValues();
    }

    private String calculatePercentage(int value, int relative){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format((double) value / relative * 100) + "%";
    }

    private void resetValues(){
        immobileParticles.set(0);
        discardedDueToOutOfRange.set(0);
        usedInCalculation.set(0);
        totalParticleInteractions.set(0);
        numberOfParticlesAveraged.set(0);
        numberOfParticles = 1500 * 7;
        updateCounter = 0;
        randomRejectedParticles.set(0);
    }

    public static OptimizationTracking getInstance() {
        if (instance == null) {
            instance = new OptimizationTracking();
        }
        return instance;
    }

}
