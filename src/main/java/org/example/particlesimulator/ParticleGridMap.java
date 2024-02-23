package org.example.particlesimulator;

import java.util.*;
import java.util.stream.Stream;

public class ParticleGridMap {
    // encoding the positions of the particles for categorization and quick access via spartial hashing
    private final HashMap<Integer, LinkedList<Particle>> particlesPositionHashMap;

    private final HashMap<Integer, LinkedList<Particle>> particlesPositionHashMapFineGrained;

    // helper hashmap that based on a particle position hash returns all the keys to the particle position hashmap
    // in which it should check for neighbours
    private final HashMap<Integer, LinkedList<Integer>> neighbourLookupHashMap = new HashMap<>();

    // returns the keys in the grid to cell within LOD_THRESHOLD layers outside CELL_LOOKUP_RADIUS
    // It's used to retrieve averaged particles around the central particle, like it's a level of detail
    private final HashMap<Integer, LinkedList<Integer>> neighbourLookupHashMapLOD = new HashMap<>();

    private final HashMap<Integer, Particle> cellAveragedParticleHashMap = new HashMap<>();

    private final HashMap<Integer, double[]> cellToPositionHashMap = new HashMap<>();

    // the size of the cell in which particles are stored
    // if it's big the particles are easier to get, less linked lists to concatenate
    // with the cost of having to discard a lot of particles in the force calculation
    // if it's small the amount of cells with particles becomes immense, it's going to be
    // harder to get all the particles in the first place, but the force function is going to discard
    // a smaller amount of particles
    public static  int CELL_SIZE;

    private final int FINE_GRAINED_CELL_SIZE;

    // with the default values the  cell_lookup_radius should amount to 40 / 5 = 8
    // it's the amount of cells in each direction from the source cell that the
    // program should look for particles
    private final int CELL_LOOKUP_RADIUS;

    // LOD = level of detail, used mostly in games to render high quality textures
    // when player is close up to target and low quality if it's far away
    // in my case I use it to approximate particles that are in the far side of the
    // cell check radius
    private final boolean USE_LOD;

    // amount of layers from the outside that should be approximated per cell basis
    private final int LOD_THRESHOLD;

    // if two or more particles fit in the same FINE_GRAINED_CELL_SIZE
    // they will get approximated to one
    private final boolean CLUSTER_CLOSE_PARTICLES;

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

    private final int fineWidth;
    private final int fineHeight;

    ParticleGridMap(double canvasWidth, double canvasHeight){
        CELL_SIZE = 4;
        CELL_LOOKUP_RADIUS = (int) ParticleSimulation.maxAttractionDistance / CELL_SIZE;
        FINE_GRAINED_CELL_SIZE = 1;         //  double the particle radius
        CIRCLE_APPROXIMATION_OFFSET = 1;
        USE_LOD = false;
        // this is necessary because there are methods that use the lod threshold for calculations
        int LOD_VALUE = 4;
        LOD_THRESHOLD = USE_LOD ? LOD_VALUE : 0;

        CLUSTER_CLOSE_PARTICLES = false;

        WIDTH = (int) canvasWidth / CELL_SIZE + 1;
        HEIGHT = (int) canvasHeight / CELL_SIZE + 1;

        fineWidth = (int) canvasWidth / FINE_GRAINED_CELL_SIZE + 1;
        fineHeight = (int) canvasHeight / FINE_GRAINED_CELL_SIZE + 1;

        particlesPositionHashMap = new HashMap<>();
        particlesPositionHashMapFineGrained = new HashMap<>();

        preComputeNeighbourLookupHashmap();
        preComputeCellToPositionHashMap();
        preComputeNeighbourLookupHashMapLOD();
    }
    public void update(List<Particle> particles){
        hashParticlePositions(particles);
        if(USE_LOD){
            averageParticles();
        }
    }
    public Stream<Particle> getParticleAround(Particle particle){
        int key = particleToHashKey(particle);

        if(USE_LOD){
            return  Stream.concat(
                    neighbourLookupHashMap.get(key).stream()
                            .map(particlesPositionHashMap::get)
                            .filter(Objects::nonNull)
                            .flatMap(List::stream),
                    neighbourLookupHashMapLOD.get(key).stream()
                            .map(cellAveragedParticleHashMap::get)
                            .filter(Objects::nonNull)
            );
        }

        else{
            return neighbourLookupHashMap.get(key).stream()
                    .map(particlesPositionHashMap::get)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream);
        }
    }

    public Stream<Particle> getParticlesAtKeysOfSpecie(ArrayList<Integer> keysToCells, int targetSpecies){
        return keysToCells.stream().map(particlesPositionHashMap::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream);
//                .filter(p -> p.SPECIES == targetSpecies);
    }

    private void hashParticlePositions(List<Particle> particles){
        particlesPositionHashMap.clear();


        if(CLUSTER_CLOSE_PARTICLES){
            particlesPositionHashMapFineGrained.clear();

            particles.forEach(particle -> {
                int fineKey = hashParticlePositionFine(particle);
                particlesPositionHashMapFineGrained.putIfAbsent(fineKey, new LinkedList<>());  // add new linked list if space not initialized
                particlesPositionHashMapFineGrained.get(fineKey).add(particle);
            });

            List<Particle> mergedParticleList = new LinkedList<>();
            for(LinkedList<Particle> list : particlesPositionHashMapFineGrained.values()){
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
                    mergedParticleList.add(new Particle(new double[]{averageX, averageY}, speciesPresent));
                }

            }

            mergedParticleList.forEach(particle -> {
                int key = particleToHashKey(particle);
                particlesPositionHashMap.putIfAbsent(key, new LinkedList<>());  // add new linked list if space not initialized
                particlesPositionHashMap.get(key).add(particle);

            });
        }

        particles.forEach(particle -> {
            int key = particleToHashKey(particle);
            particlesPositionHashMap.putIfAbsent(key, new LinkedList<>());  // add new linked list if space not initialized
            particlesPositionHashMap.get(key).add(particle);
        });
    }
    private void averageParticles() {
        cellAveragedParticleHashMap.clear();

        // going over each (non-empty) cell and getting the entry
        for (Map.Entry<Integer, LinkedList<Particle>> particleHashMapEntry : particlesPositionHashMap.entrySet()) {
            int key = particleHashMapEntry.getKey();

            if(particleHashMapEntry.getValue().size() == 1){
                cellAveragedParticleHashMap.put(key, particleHashMapEntry.getValue().getFirst());
                continue;
            }

            double[] position = cellToPositionHashMap.get(key);

            int[] speciesPresent = new int[7];
            Arrays.fill(speciesPresent, 0);

            for (Particle particle : particleHashMapEntry.getValue()){
                speciesPresent[particle.SPECIES] ++;
            }
            Particle particle = new Particle(position, speciesPresent);
            cellAveragedParticleHashMap.put(key, particle);
        }
    }
    private void preComputeNeighbourLookupHashmap(){
        for (int row = 0; row < WIDTH; row++) {
            for (int column = 0; column < HEIGHT; column++) {
                LinkedList<Integer> targetCellKeys = new LinkedList<>();
                int squareIndexStartRow = (row - CELL_LOOKUP_RADIUS) + LOD_THRESHOLD + 1;
                int squareIndexEndRow = (row + CELL_LOOKUP_RADIUS) - LOD_THRESHOLD - 1;
                int squareIndexStartColumn = (column - CELL_LOOKUP_RADIUS) + LOD_THRESHOLD + 1;
                int squareIndexEndColumn = (column + CELL_LOOKUP_RADIUS) - LOD_THRESHOLD - 1;

                for (int i = squareIndexStartRow; i < squareIndexEndRow; i++) {
                    int mathFloorWidth = Math.floorMod(i, WIDTH);
                    for (int j = squareIndexStartColumn; j < squareIndexEndColumn; j++) {
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
    private void preComputeCellToPositionHashMap(){
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                cellToPositionHashMap.put(i * HEIGHT + j, new double[]{(i+0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE});
            }
        }
    }
    private void preComputeNeighbourLookupHashMapLOD(){
        for (int row = 0; row < WIDTH; row++) {
            for (int column = 0; column < HEIGHT; column++) {
                LinkedList<Integer> targetCellKeys = new LinkedList<>();
                int squareIndexStartRow = (row - CELL_LOOKUP_RADIUS) + CIRCLE_APPROXIMATION_OFFSET;
                int squareIndexEndRow = (row + CELL_LOOKUP_RADIUS) - CIRCLE_APPROXIMATION_OFFSET;
                int squareIndexStartColumn = (column - CELL_LOOKUP_RADIUS) + CIRCLE_APPROXIMATION_OFFSET;
                int squareIndexEndColumn = (column + CELL_LOOKUP_RADIUS) - CIRCLE_APPROXIMATION_OFFSET;

                int LODThresholdStartRow = squareIndexStartRow + LOD_THRESHOLD;
                int LODThresholdEndRow = squareIndexEndRow - LOD_THRESHOLD;
                int LODThresholdStartColumn = squareIndexStartColumn + LOD_THRESHOLD;
                int LODThresholdEndColumn = squareIndexEndColumn - LOD_THRESHOLD;

                for (int i = squareIndexStartRow; i < squareIndexEndRow; i++) {

                    for (int j = squareIndexStartColumn; j < squareIndexEndColumn; j++) {
                        if(j > LODThresholdStartColumn && j < LODThresholdEndColumn && i > LODThresholdStartRow && i < LODThresholdEndRow){
                            continue;
                        }
                        int mathFloorWidth = Math.floorMod(i, WIDTH);
                        int mathFloorHeight = Math.floorMod(j, HEIGHT);
                        int keyToCell = mathFloorWidth * HEIGHT + mathFloorHeight;
                        targetCellKeys.add(keyToCell);
                    }
                }
                int sourceKey = row * HEIGHT + column;
                neighbourLookupHashMapLOD.put(sourceKey,targetCellKeys);
            }
        }
    }
    public static int particleToHashKey(Particle particle){
        int indexRow = (int) particle.position[0] / ParticleGridMap.CELL_SIZE;
        int indexColumn = (int) particle.position[1] / CELL_SIZE;
        return indexRow * HEIGHT + indexColumn;
    }
    private int hashParticlePositionFine(Particle particle){
        int indexRow = (int) particle.position[0] / FINE_GRAINED_CELL_SIZE;
        int indexColumn = (int) particle.position[1] / FINE_GRAINED_CELL_SIZE;
        return indexRow * fineHeight + indexColumn;
    }
    public LinkedList<Integer> getKeysToNeighbours(int key){
        return neighbourLookupHashMap.get(key);
    }
    public LinkedList<Particle> getParticlesAtKey(int key){
        return particlesPositionHashMap.get(key);
    }
}
