package org.example.particlesimulator;

public class OptimizationTracking {
    private static OptimizationTracking instance;
    int calculationSavedByCaching;
    int calculationSavedByImmobileParticle;
    OptimizationTracking(){
        calculationSavedByCaching = 0;
        calculationSavedByImmobileParticle = 0;
    }

    public void increaseCachingCounter(){
        calculationSavedByCaching++;
    }

    public void increaseImmobileCounter(){
        calculationSavedByImmobileParticle++;
    }


    public void showOptimizationData(){
        System.out.println("\nOptimizations");
        System.out.println("Caching     " + calculationSavedByCaching);
        System.out.println("Immobile    " + calculationSavedByImmobileParticle);

        resetValues();
    }

    private void resetValues(){
        calculationSavedByCaching = 0;
        calculationSavedByImmobileParticle = 0;
    }

    public static OptimizationTracking getInstance() {
        if (instance == null) {
            instance = new OptimizationTracking();
        }
        return instance;
    }
}
