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

    private final HashMap<Integer, double[]>[] positionToForceCache = new HashMap[7];


    private ParticleForceCache(){
        for (int i = 0; i < particleForceCache.length; i++) {
            particleForceCache[i] = new HashMap<>();
        }

        for (int i = 0; i < positionToForceCache.length; i++) {
            positionToForceCache[i] = new HashMap<>();
        }
    }

    public void clearCache(){
        for(HashMap<Integer, double[]> map : positionToForceCache){
            map.clear();
        }
    }

    public void cacheForce(int species, double[] position, double[] force){
        positionToForceCache[species].put(positionToKey(position), force);
    }

    public double[] getCachedForce(int species, double[] position){
        return positionToForceCache[species].get(positionToKey(position));
    }

    private static int positionToKey(double[] position){
        int radiusSize = 3;
        return ((int) position[0]/radiusSize * ((int) ParticleSimulation.CANVAS_HEIGHT / radiusSize) + (int) position[1] / radiusSize);
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






