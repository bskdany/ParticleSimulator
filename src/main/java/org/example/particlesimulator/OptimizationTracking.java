package org.example.particlesimulator;

import java.text.DecimalFormat;

public class OptimizationTracking {
    private static OptimizationTracking instance;
    private int calculationSavedByCaching;
    private int calculationSavedByImmobileParticle;
    private int discardedDueToOutOfRange;
    private int usedInCalculation;
    private int totalParticleStream;
    OptimizationTracking(){
        resetValues();
    }

    public void increaseCachingCounter(){
        calculationSavedByCaching++;
    }
    public void increaseImmobileCounter(){
        calculationSavedByImmobileParticle++;
    }
    public void increaseDiscardedOutOfRange(){
        discardedDueToOutOfRange++;
    }
    public void increaseUsedInCalculation(){
        usedInCalculation++;
    }
    public void increaseTotal(){totalParticleStream++;}

    public void showOptimizationData(){
        System.out.println("Optimizations");
        System.out.println("Caching     " + calculatePercentage(calculationSavedByCaching));
        System.out.println("Immobile    " + calculatePercentage(calculationSavedByImmobileParticle));
        System.out.println("Range       " + calculatePercentage(discardedDueToOutOfRange));
        System.out.println("Used        " + calculatePercentage(usedInCalculation));
        System.out.println();

        resetValues();
    }

    private String calculatePercentage(int value){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format((double) value / totalParticleStream * 100) + "%";
    }

    private void resetValues(){
        calculationSavedByCaching = 0;
        calculationSavedByImmobileParticle = 0;
        discardedDueToOutOfRange = 0;
        usedInCalculation = 0;
        totalParticleStream = 1;
    }

    public static OptimizationTracking getInstance() {
        if (instance == null) {
            instance = new OptimizationTracking();
        }
        return instance;
    }
}
