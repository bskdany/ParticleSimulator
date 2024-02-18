package org.example.particlesimulator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParticleGridMap {
    private final HashMap<Integer, LinkedList<Particle>> particleHashMap;
    private final LinkedList<Particle>[] particleGrid;                                // a 2d array of list of particles
    private final HashMap<Integer, LinkedList<Integer>> neighbourLookupHashMap = new HashMap<>();
    private final HashMap<Integer, ParticleApproximation> cellAveragedParticleHashMap = new HashMap<>();
    private final HashMap<Integer, double[]> cellToPositionHashMap = new HashMap<>();
    private final int CELL_SIZE;
    private final int CELL_LOOKUP_RADIUS;
    private final int width;
    private final int height;
    private final boolean COMPUTE_LOD;
    private final int LOD_THRESHOLD;
    ParticleGridMap(double canvasWidth, double canvasHeight){
        CELL_SIZE = 10;
        CELL_LOOKUP_RADIUS = (int) ParticleSimulation.maxAttractionDistance / CELL_SIZE;
        COMPUTE_LOD = true;
        LOD_THRESHOLD = 1; // the most outside layer

        width = (int) canvasWidth / CELL_SIZE + 1;
        height = (int) canvasHeight / CELL_SIZE + 1;

        particleHashMap = new HashMap<>();

        particleGrid = new LinkedList[width*height];

        // initialize the array lists in each grid cell
        for (int i = 0; i < particleGrid.length; i++) {
            particleGrid[i] = new LinkedList<>();
        }

        preComputeCellLookupIndices();
        preComputeCellToPositionHashMap();
    }
    public void update(List<Particle> particles){
        clearGrid();
        placeParticlesInGrid(particles);
        averageParticles();
    }
    public Stream<Particle> getParticleAround(Particle particle){
        int indexRow = (int) particle.position[0] / CELL_SIZE;
        int indexColumn = (int) particle.position[1] / CELL_SIZE;
        int key = indexRow * height + indexColumn;

        return neighbourLookupHashMap.get(key).stream()
                .map(particleHashMap::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream);
        // for further optimizations I need to precalculate the distance and the attraction matrix
        // I can do this with the help of the level of detail in which cells that are further out enough
        // get approximated to a single point in the middle of the cell, which holds mass of the total of
        // the particles and has a sum of their attraction constants (one sum for each source particle)
    }

    public Stream<ParticleApproximation> getApproximatedParticlesAround(Particle particle){
        int indexRow = (int) particle.position[0] / CELL_SIZE;
        int indexColumn = (int) particle.position[1] / CELL_SIZE;
        int key = indexRow * height + indexColumn;

        return neighbourLookupHashMap.get(key).stream()
                .map(cellAveragedParticleHashMap::get)
                .filter(Objects::nonNull);
    }


    private void placeParticlesInGrid(List<Particle> particles){
        particleHashMap.clear();
        particles.forEach(particle -> {
            int indexRow = (int) particle.position[0] / CELL_SIZE; // flooring instead of rounding because I want the precise square
            int indexColumn = (int) particle.position[1] / CELL_SIZE;
            int key = indexRow * height + indexColumn;
            particleHashMap.putIfAbsent(key, new LinkedList<>());
            particleHashMap.get(key).add(particle);
//            particleGrid[key].add(particle);
        });
    }
    private void clearGrid(){
        for (LinkedList<Particle> list : particleGrid) {
            list.clear();
        }
    }
    private void preComputeCellLookupIndices(){
        for (int row = 0; row < width; row++) {
            for (int column = 0; column < height; column++) {
//                int[] targetCellKeys = new int[(int) Math.pow(CELL_LOOKUP_RADIUS*2+1,2)]; // 2x the radius from the center + 1 to account for the center, squared
                LinkedList<Integer> targetCellKeys = new LinkedList<>();
                int squareIndexStartRow = (row - CELL_LOOKUP_RADIUS);
                int squareIndexEndRow = (row + CELL_LOOKUP_RADIUS);
                int squareIndexStartColumn = (column - CELL_LOOKUP_RADIUS);
                int squareIndexEndColumn = (column + CELL_LOOKUP_RADIUS);

//                int targetCellKeysCounter = 0;
                for (int i = squareIndexStartRow; i < squareIndexEndRow; i++) {
                    int mathFloorWidth = Math.floorMod(i, width);
                    for (int j = squareIndexStartColumn; j < squareIndexEndColumn; j++) {
                        int mathFloorHeight = Math.floorMod(j, height);
                        int keyToCell = mathFloorWidth * height + mathFloorHeight;
//                        targetCellKeys[targetCellKeysCounter] = keyToCell;
                        targetCellKeys.add(keyToCell);
//                        targetCellKeysCounter++;
                    }
                }
                int sourceKey = row * height + column;
                neighbourLookupHashMap.put(sourceKey,targetCellKeys);
            }
        }
    }

    private void preComputeCellToPositionHashMap(){
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cellToPositionHashMap.put(i * height + j, new double[]{(i+0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE});
            }
        }
    }

    private void averageParticles() {
        // going over each (non-empty) cell and getting the entry
        for (Map.Entry<Integer, LinkedList<Particle>> particleHashMapEntry : particleHashMap.entrySet()) {
            int key = particleHashMapEntry.getKey();
            double[] position = cellToPositionHashMap.get(key);

            int[] speciesPresent = new int[7];
            Arrays.fill(speciesPresent, 0);

            for (Particle particle : particleHashMapEntry.getValue()){
                speciesPresent[particle.SPECIES] ++;
            }
            ParticleApproximation particle = new ParticleApproximation(position, speciesPresent);
            cellAveragedParticleHashMap.put(key, particle);
        }

    }

}
