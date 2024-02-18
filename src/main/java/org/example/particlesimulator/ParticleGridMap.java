package org.example.particlesimulator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParticleGridMap {
    private final HashMap<Integer, LinkedList<Particle>> particleHashMap;
    private final LinkedList<Particle>[] particleGrid;                                // a 2d array of list of particles
    private final HashMap<Integer, LinkedList<Integer>> neighbourLookupHashMap = new HashMap<>();           // returns the keys in the grid to cell within CELL_LOOKUP_RADIUS

    // returns the keys in the grid to cell within LOD_THRESHOLD layers outside CELL_LOOKUP_RADIUS
    // It's used to retrieve averaged particles around the central particle, like it's a level of detail
    private final HashMap<Integer, LinkedList<Integer>> neighbourLookupHashMapLOD = new HashMap<>();
    private final HashMap<Integer, Particle> cellAveragedParticleHashMap = new HashMap<>();
    private final HashMap<Integer, double[]> cellToPositionHashMap = new HashMap<>();
    private final int CELL_SIZE;
    private final int CELL_LOOKUP_RADIUS;
    private final int width;
    private final int height;
    private final int LOD_THRESHOLD;
    private final boolean USE_LOD;
    ParticleGridMap(double canvasWidth, double canvasHeight){
        CELL_SIZE = 10;
        USE_LOD = true;
        CELL_LOOKUP_RADIUS = (int) ParticleSimulation.maxAttractionDistance / CELL_SIZE;
        LOD_THRESHOLD = 1; // the most outside layer

        width = (int) canvasWidth / CELL_SIZE + 1;
        height = (int) canvasHeight / CELL_SIZE + 1;

        particleHashMap = new HashMap<>();

        particleGrid = new LinkedList[width*height];

        // initialize the array lists in each grid cell
        for (int i = 0; i < particleGrid.length; i++) {
            particleGrid[i] = new LinkedList<>();
        }

        preComputeNeighbourLookupHashmap();
        preComputeCellToPositionHashMap();
        preComputeNeighbourLookupHashMapLOD();
    }
    public void update(List<Particle> particles){
        clearGrid();
        placeParticlesInGrid(particles);
        if(USE_LOD){
            averageParticles();
        }
    }
    public Stream<Particle> getParticleAround(Particle particle){
        int indexRow = (int) particle.position[0] / CELL_SIZE;
        int indexColumn = (int) particle.position[1] / CELL_SIZE;
        int key = indexRow * height + indexColumn;

        if(USE_LOD){
            return  Stream.concat(
                    neighbourLookupHashMap.get(key).stream()
                            .map(particleHashMap::get)
                            .filter(Objects::nonNull)
                            .flatMap(List::stream),
                    neighbourLookupHashMapLOD.get(key).stream()
                            .map(cellAveragedParticleHashMap::get)
                            .filter(Objects::nonNull)
            );
        }

        else{
            return neighbourLookupHashMap.get(key).stream()
                    .map(particleHashMap::get)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream);
        }
    }

    private void placeParticlesInGrid(List<Particle> particles){
        particleHashMap.clear();
        particles.forEach(particle -> {
            int indexRow = (int) particle.position[0] / CELL_SIZE; // flooring instead of rounding because I want the precise square
            int indexColumn = (int) particle.position[1] / CELL_SIZE;
            int key = indexRow * height + indexColumn;
            particleHashMap.putIfAbsent(key, new LinkedList<>());
            particleHashMap.get(key).add(particle);
        });
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
            Particle particle = new Particle(position, speciesPresent);
            cellAveragedParticleHashMap.put(key, particle);
        }
    }
    private void clearGrid(){
        for (LinkedList<Particle> list : particleGrid) {
            list.clear();
        }
    }
    private void preComputeNeighbourLookupHashmap(){
        for (int row = 0; row < width; row++) {
            for (int column = 0; column < height; column++) {
                LinkedList<Integer> targetCellKeys = new LinkedList<>();
                int squareIndexStartRow = (row - CELL_LOOKUP_RADIUS) + LOD_THRESHOLD;
                int squareIndexEndRow = (row + CELL_LOOKUP_RADIUS) - LOD_THRESHOLD;
                int squareIndexStartColumn = (column - CELL_LOOKUP_RADIUS) + LOD_THRESHOLD;
                int squareIndexEndColumn = (column + CELL_LOOKUP_RADIUS) - LOD_THRESHOLD;

                for (int i = squareIndexStartRow; i < squareIndexEndRow; i++) {
                    int mathFloorWidth = Math.floorMod(i, width);
                    for (int j = squareIndexStartColumn; j < squareIndexEndColumn; j++) {
                        int mathFloorHeight = Math.floorMod(j, height);
                        int keyToCell = mathFloorWidth * height + mathFloorHeight;
                        targetCellKeys.add(keyToCell);
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
    private void preComputeNeighbourLookupHashMapLOD(){
        for (int row = 0; row < width; row++) {
            for (int column = 0; column < height; column++) {
                LinkedList<Integer> targetCellKeys = new LinkedList<>();
                int squareIndexStartRow = (row - CELL_LOOKUP_RADIUS);
                int squareIndexEndRow = (row + CELL_LOOKUP_RADIUS);
                int squareIndexStartColumn = (column - CELL_LOOKUP_RADIUS);
                int squareIndexEndColumn = (column + CELL_LOOKUP_RADIUS);

                int LODThresholdStartRow = squareIndexStartRow + LOD_THRESHOLD;
                int LODThresholdEndRow = squareIndexEndRow - LOD_THRESHOLD;
                int LODThresholdStartColumn = squareIndexStartColumn + LOD_THRESHOLD;
                int LODThresholdEndColumn = squareIndexEndColumn - LOD_THRESHOLD;

                for (int i = squareIndexStartRow; i < squareIndexEndRow; i++) {
                    if(i > LODThresholdStartRow && i < LODThresholdEndRow){
                        continue;
                    }

                    int mathFloorWidth = Math.floorMod(i, width);
                    for (int j = squareIndexStartColumn; j < squareIndexEndColumn; j++) {
                        if(j > LODThresholdStartColumn && j < LODThresholdEndColumn){
                            continue;
                        }

                        int mathFloorHeight = Math.floorMod(j, height);
                        int keyToCell = mathFloorWidth * height + mathFloorHeight;
                        targetCellKeys.add(keyToCell);
                    }
                }
                int sourceKey = row * height + column;
                neighbourLookupHashMapLOD.put(sourceKey,targetCellKeys);
            }
        }
    }
}
