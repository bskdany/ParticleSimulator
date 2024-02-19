package org.example.particlesimulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ParticleForceCache {
    private static final HashMap<Integer[], Double>[] particleForceCache = new HashMap[7];

    public static ArrayList<Integer>[] encodeParticlesConfiguration(Particle sourceParticle){
        ParticleGridMap gridMap = ParticleSimulation.particleGridMap;

        int hashKey = ParticleGridMap.particleToHashKey(sourceParticle);
        List<Integer> keysToNeighbours = gridMap.getKeysToNeighbours(hashKey);

        // because it's hard to encode everything all at once I divide the configuration in pieces

        // SPECIES
        //  0  ->   {[hashKey, quantity],[...],[...],[],[]}     ->  TOTAL FORCE
        //  1       ...
        //  2       ...

        ArrayList<Integer>[] encodedConfiguration = new ArrayList[7];
        // and yes I am aware hard-coding the number 7 everywhere in my code is not the brightest idea

        for (int i = 0; i < keysToNeighbours.size(); i++) {
            List<Particle> neighbourParticlesAtKey = gridMap.getParticlesAtKey(i);
            if (neighbourParticlesAtKey == null){
                continue;
            }

            for(Particle neighbourParticle :neighbourParticlesAtKey){
                int keyOffset = i - hashKey;        // there are sure ways to make the key offset more efficient

                // assuming there is only one particle per cell
                encodedConfiguration[neighbourParticle.SPECIES].add(keyOffset);
            }
        }
        
        return encodedConfiguration;
    }

}
