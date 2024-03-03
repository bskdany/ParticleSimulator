package org.example.particlesimulator;

import java.util.ArrayList;

public class ParticleCluster{
    ParticleCluster(ArrayList<Particle> cluster){

    }



//    ParticleCluster(ArrayList<Particle> cluster){
//
//        // here I am calculating the cumulative attraction factor that the particle would have
//        // if any other particle want to calculate the force with it
//        // for example a yellow particle could consider this particle as attractive
//        // while a red one could consider it as repulsive
//        localAttractionFactor = new double[7];
//
//        for (int i = 0; i < 7; i++) {       // where 7 is the number of species
//            localAttractionFactor[i] = 0;
//
//            // if I was particle with species i (0) what is my attraction to this approximated particle?
//            for (int j = 0; j < 7; j++) {
//                localAttractionFactor[i] += AttractionMatrix.attractionMatrix[i][j] * containingSpecies[j];
//            }
//            containingSpeciesCount += containingSpecies[i];
//        }
//    }
}
