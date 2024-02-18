package org.example.particlesimulator;

public class ParticleApproximation {
    public final double[] position;
    public final int[] speciesContained;
    public final double[] attractionFactor;

    ParticleApproximation(double[] position, int[] speciesContained){
        this.position = position;
        this.speciesContained = speciesContained;
        attractionFactor = new double[7];

        // here I am calculating the cumulative attraction factor that the particle would have
        // if any other particle want to calculate the force with it
        // for example a yellow particle could consider this particle as attractive
        // while a red one could consider it as repulsive
        for (int i = 0; i < 7; i++) {       // where 7 is the number of species
            attractionFactor[i] = 0;

            // if I was particle with species i (0) what is my attraction to this approximated particle?
            for (int j = 0; j < 7; j++) {
                attractionFactor[i] += AttractionMatrix.attractionMatrix[i][j] * speciesContained[j];
            }
        }
        
        
    }
    

}
