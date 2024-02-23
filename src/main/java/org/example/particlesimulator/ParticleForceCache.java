package org.example.particlesimulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ParticleForceCache {
    // singleton
    private static ParticleForceCache instance;

    // because it's hard to encode everything all at once I divide the configuration in pieces
    // SPECIES
    //  0  ->   {{[hashKey, quantity],[...],[...],[],[]} -> TOTAL FORCE} , {{[hashKey, quantity],[...],[...],[],[]} -> TOTAL FORCE...}
    //  1       ...
    //  2       ...
    private final HashMap<ArrayList<Integer>, double[]>[] particleForceCache = new HashMap[7];
    // and yes I am aware hard-coding the number 7 everywhere in my code is not the brightest idea

    private ParticleForceCache(){
        for (int i = 0; i < particleForceCache.length; i++) {
            particleForceCache[i] = new HashMap<>();
        }
    }

    public ArrayList<Integer>[] encodeParticlesConfiguration(Particle sourceParticle){
        ParticleGridMap gridMap = ParticleSimulation.particleGridMap;

        int hashKey = ParticleGridMap.particleToHashKey(sourceParticle);
        List<Integer> keysToNeighbours = gridMap.getKeysToNeighbours(hashKey);

        ArrayList<Integer>[] encodedConfiguration = new ArrayList[7];
        for (int i = 0; i < encodedConfiguration.length; i++) {
            encodedConfiguration[i] = new ArrayList<>();
        }

        for(int key : keysToNeighbours){
            List<Particle> neighbourParticlesAtKey = gridMap.getParticlesAtKey(key);
            if (neighbourParticlesAtKey == null){
                continue;
            }

            for(Particle neighbourParticle :neighbourParticlesAtKey){

                // assuming there is only one particle per cell
                encodedConfiguration[neighbourParticle.SPECIES].add(key);
            }
        }

        return encodedConfiguration;
    }

    public double[] getCachedConfiguration(ArrayList<Integer> configuration, int species){
        return particleForceCache[species].get(configuration);
    }

    public void addConfigurationToCache(ArrayList<Integer> configuration, int species, double[] force){
        particleForceCache[species].put(configuration, force);
    }


    public static ParticleForceCache getInstance(){
        if (instance == null){
            instance = new ParticleForceCache();
        }
        return instance;
    }
}






