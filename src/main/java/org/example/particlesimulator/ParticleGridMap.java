package org.example.particlesimulator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ParticleGridMap {
    // encoding the positions of the particles for categorization and quick access via spartial hashing
    private final ConcurrentHashMap<Integer, ArrayList<Particle>> particlesPositionHashMap;
    private final ConcurrentHashMap<Integer, ArrayList<Particle>> particlesPositionHashMapAveraged;

    private final ConcurrentHashMap<Integer, ArrayList<Particle>> particlesPositionHashMapFine;

    // helper hashmap that based on a particle position hash returns all the keys to the particle position hashmap
    // in which it should check for neighbours
    private final ConcurrentHashMap<Integer, ArrayList<Integer>> neighbourLookupHashMap = new ConcurrentHashMap<>();

    // returns the keys in the grid to cell within LOD_OFFSET layers outside CELL_LOOKUP_RADIUS
    // It's used to retrieve averaged particles around the central particle, like it's a level of detail
    private final ConcurrentHashMap<Integer, ArrayList<Integer>> neighbourLookupHashMapLOD = new ConcurrentHashMap<>();

    // the size of the cell in which particles are stored
    // if it's big the particles are easier to get, less linked lists to concatenate
    // with the cost of having to discard a lot of particles in the force calculation
    // if it's small the amount of cells with particles becomes immense, it's going to be
    // harder to get all the particles in the first place, but the force function is going to discard
    // a smaller amount of particles
    public static  int CELL_SIZE;

    private final int CELL_SIZE_FINE;

    // with the default values the  cell_lookup_radius should amount to 40 / 5 = 8
    // it's the amount of cells in each direction from the source cell that the
    // program should look for particles
    private final int CELL_LOOKUP_RADIUS;

    // if two or more particles fit in the same CELL_SIZE_FINE
    // they will get approximated to one
    private final boolean CLUSTER_CLOSE_PARTICLES;
    private final boolean APPROXIMATE_CIRCLE;

    // the maximum distance between two particles that would make them a cluster

    // I use this number to calculate the range of cells from the source particle
    // in which particles should be checked, because it's hard to simulate a circle, thus
    // I opted to approximate it by implementing an offset from the borders
    // of the square. The reason behind is that I have a lot of discarded particles
    // due to them being too distant from the source particle, without this variable
    // the discarded particles would amount to 20%, with it only 6%
    // https://en.wikipedia.org/wiki/Squaring_the_circle
    private final int CIRCLE_APPROXIMATION_OFFSET;

    public static int WIDTH;
    public static int HEIGHT;

    private final int WIDTH_FINE;
    private final int HEIGHT_FINE;

    ParticleGridMap(double canvasWidth, double canvasHeight){
        CELL_LOOKUP_RADIUS = 3;
        CIRCLE_APPROXIMATION_OFFSET = 1;
        CELL_SIZE = (int) ParticleSimulation.maxAttractionDistance / CELL_LOOKUP_RADIUS ;
        CELL_SIZE_FINE = 3;

        WIDTH = (int) canvasWidth / CELL_SIZE + 1;
        HEIGHT = (int) canvasHeight / CELL_SIZE + 1;

        WIDTH_FINE = (int) canvasWidth / CELL_SIZE_FINE + 1;
        HEIGHT_FINE = (int) canvasHeight / CELL_SIZE_FINE + 1;

        CLUSTER_CLOSE_PARTICLES = false;
        APPROXIMATE_CIRCLE = true;


        particlesPositionHashMap = new ConcurrentHashMap<>();
        particlesPositionHashMapFine = new ConcurrentHashMap<>();
        particlesPositionHashMapAveraged = new ConcurrentHashMap<>();

        preComputeNeighbourLookupHashmap();
    }
    public void update(ArrayList<Particle> particles){
        hashParticlePositions(particles);
    }

    public Stream<Particle> getParticleAround(Particle particle){
        int key = particleToHashKey(particle);
        int fineKey = particleToHashKeyFine(particle);

        if(CLUSTER_CLOSE_PARTICLES){
            return Stream.concat(
                    neighbourLookupHashMap.get(key).stream()
                        .map(particlesPositionHashMapAveraged::get)
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                    ,
                    particlesPositionHashMapFine.get(fineKey).stream().filter(Objects::nonNull)
            );
        }
        else{
            return neighbourLookupHashMap.get(key).stream()
                    .map(particlesPositionHashMap::get)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream);
        }
    }
    private void hashParticlePositions(ArrayList<Particle> particles){
        particlesPositionHashMap.clear();

        OptimizationTracking.getInstance().increaseUpdate();

        if(CLUSTER_CLOSE_PARTICLES){
            particlesPositionHashMapFine.clear();
            particlesPositionHashMapAveraged.clear();

            particles.forEach(particle -> {
                int fineKey = particleToHashKeyFine(particle);
                particlesPositionHashMapFine.putIfAbsent(fineKey, new ArrayList<>());  // add new linked list if space not initialized
                particlesPositionHashMapFine.get(fineKey).add(particle);
            });

            List<Particle> mergedParticleList = new LinkedList<>();
            for(ArrayList<Particle> list : particlesPositionHashMapFine.values()){
                if(list.size() == 1){
                    mergedParticleList.add(list.getFirst());
                }
                else{
                    double averageX = 0;
                    double averageY = 0;

                    for (Particle particle : list){
                        averageX += particle.position[0];
                        averageY += particle.position[1];
                    }

                    averageX /= list.size();
                    averageY /= list.size();

                    int[] speciesPresent = new int[7];
                    Arrays.fill(speciesPresent, 0);

                    for (Particle particle : list){
                        speciesPresent[particle.SPECIES] ++;
                    }

                    mergedParticleList.add(list.getFirst());
                    OptimizationTracking.getInstance().increaseParticlesAveraged();
                }

            }

            mergedParticleList.forEach(particle -> {
                int key = particleToHashKey(particle);
                particlesPositionHashMapAveraged.putIfAbsent(key, new ArrayList<>());  // add new linked list if space not initialized
                particlesPositionHashMapAveraged.get(key).add(particle);
            });
        }
        else{
            particles.forEach(particle -> {
                int key = particleToHashKey(particle);
                particlesPositionHashMap.putIfAbsent(key, new ArrayList<>());  // add new linked list if space not initialized
                particlesPositionHashMap.get(key).add(particle);
            });
        }
    }

    private void preComputeNeighbourLookupHashmap(){
        for (int row = 0; row < WIDTH; row++) {
            for (int column = 0; column < HEIGHT; column++) {
                ArrayList<Integer> targetCellKeys = new ArrayList<>();
                int squareIndexStartRow = row - CELL_LOOKUP_RADIUS;
                int squareIndexEndRow = row + CELL_LOOKUP_RADIUS;
                int squareIndexStartColumn = column - CELL_LOOKUP_RADIUS;
                int squareIndexEndColumn = column + CELL_LOOKUP_RADIUS;

                for (int i = squareIndexStartRow; i <= squareIndexEndRow; i++) {
                    int mathFloorWidth = Math.floorMod(i, WIDTH);

                    for (int j = squareIndexStartColumn; j <= squareIndexEndColumn; j++) {

                        if(APPROXIMATE_CIRCLE){
                            int offsetFromOriginRow = Math.abs(i - row);
                            int offsetFromOriginColumn = Math.abs(j - column);

                            if(Math.round(Math.sqrt(Math.pow(offsetFromOriginRow, 2) + Math.pow(offsetFromOriginColumn, 2))) > CELL_LOOKUP_RADIUS){
                                continue;
                            }

                        }

                        int mathFloorHeight = Math.floorMod(j, HEIGHT);
                        int keyToCell = mathFloorWidth * HEIGHT + mathFloorHeight;
                        targetCellKeys.add(keyToCell);
                    }
                }
                int sourceKey = row * HEIGHT + column;
                neighbourLookupHashMap.put(sourceKey,targetCellKeys);
            }
        }
    }
    public static int particleToHashKey(Particle particle){
        int indexRow = (int) particle.position[0] / ParticleGridMap.CELL_SIZE;
        int indexColumn = (int) particle.position[1] / CELL_SIZE;
        return indexRow * HEIGHT + indexColumn;
    }
    private int particleToHashKeyFine(Particle particle){
        int indexRow = (int) particle.position[0] / CELL_SIZE_FINE;
        int indexColumn = (int) particle.position[1] / CELL_SIZE_FINE;
        return indexRow * HEIGHT_FINE + indexColumn;
    }
    public ArrayList<Integer> getKeysToNeighbours(int key){
        return neighbourLookupHashMap.get(key);
    }
    public ArrayList<Particle> getParticlesAtKey(int key){
        return particlesPositionHashMap.get(key);
    }
    public Collection<ArrayList<Particle>> getParticlesPositionHashMap() {
        if(CLUSTER_CLOSE_PARTICLES){
            return particlesPositionHashMapAveraged.values();
        }
        return particlesPositionHashMap.values();
    }
}
