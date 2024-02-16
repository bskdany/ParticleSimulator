package org.example.particlesimulator;

import java.util.*;

public class ParticleGridMap {

    private final HashMap<Integer, LinkedList<Particle>> particleHashMap;
    private final LinkedList<Particle>[] particleGrid;                                // a 2d array of list of particles
    private final HashMap<Integer, List<Particle>> cellsCoordinatesToParticles;         // mapping coordinates to cell
    private final HashMap<Integer, int[]> neighbourLookupHashMap = new HashMap<>();

    private final HashMap<Integer, Integer> mathFloorModCacheWidth = new HashMap<>();   // I can't believe I have to cache Math.floorMod
    private final HashMap<Integer, Integer> mathFloorModCacheHeight = new HashMap<>();
    private final int CELL_SIZE;
    private final int CELL_LOOKUP_RADIUS;
    private final int width;
    private final int height;
    ParticleGridMap(double canvasWidth, double canvasHeight){
        CELL_SIZE = 25;
        CELL_LOOKUP_RADIUS = (int) ParticleSimulation.maxAttractionDistance / CELL_SIZE;
//        CELL_LOOKUP_RADIUS = 3;

        // if canvas is 400 * 400, the particle grid will be 200 * 200
        width = (int) canvasWidth / CELL_SIZE + 1;
        height = (int) canvasHeight / CELL_SIZE + 1;

        particleHashMap = new HashMap<>();

        particleGrid = new LinkedList[width*height];
        cellsCoordinatesToParticles = new HashMap<>(width*height);

        // initialize the array lists in each grid cell
        for (int i = 0; i < particleGrid.length; i++) {
            particleGrid[i] = new LinkedList<>();
        }

        seedMathFloorModCache();        // seed the math.floorMod cache for later use
        preComputeCellLookupIndices();
    }

    public void update(List<Particle> particles){
        clearGrid();
        placeParticlesInGrid(particles);
    }

    public LinkedList<LinkedList<Particle>> getParticleAround(Particle particle){
        int indexRow = (int) particle.position[0] / CELL_SIZE;
        int indexColumn = (int) particle.position[1] / CELL_SIZE;
        int key = indexRow * height + indexColumn;

        LinkedList<LinkedList<Particle>> result = new LinkedList<LinkedList<Particle>>();

        for(int targetCellKey : neighbourLookupHashMap.get(key)){
            LinkedList<Particle> particlesInCell = particleHashMap.get(targetCellKey);
            if(particlesInCell!=null){
                result.add(particlesInCell);
            }
        }



        // I return a list of lists of particles, why not just a single list? because
        // addAll is very slow, I can't make streams work and haven't found any better method yet
        return result;
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
    private void seedMathFloorModCache(){
        int offset = CELL_LOOKUP_RADIUS;

        for (int i = -offset; i < 0; i++) {
            mathFloorModCacheWidth.put(i, Math.floorMod(i, width));
            mathFloorModCacheHeight.put(i, Math.floorMod(i, height));
        }

        for (int i = width; i < width+offset; i++) {
            mathFloorModCacheWidth.put(i, Math.floorMod(i, width));
        }

        for (int i = height; i < height+offset; i++) {
            mathFloorModCacheHeight.put(i, Math.floorMod(i, height));
        }
    }
    private void preComputeCellLookupIndices(){
        for (int row = 0; row < width; row++) {
            for (int column = 0; column < height; column++) {
                int[] targetCellKeys = new int[(int) Math.pow(CELL_LOOKUP_RADIUS*2+1,2)]; // 2x the radius from the center + 1 to account for the center, squared
                int squareIndexStartRow = (row - CELL_LOOKUP_RADIUS);
                int squareIndexEndRow = (row + CELL_LOOKUP_RADIUS);
                int squareIndexStartColumn = (column - CELL_LOOKUP_RADIUS);
                int squareIndexEndColumn = (column + CELL_LOOKUP_RADIUS);

                int targetCellKeysCounter = 0;
                for (int i = squareIndexStartRow; i < squareIndexEndRow; i++) {
                    int mathFloorWidth = mathFloorModCacheWidth.getOrDefault(i, i);
                    for (int j = squareIndexStartColumn; j < squareIndexEndColumn; j++) {
                        int mathFloorHeight = mathFloorModCacheHeight.getOrDefault(j,j);
                        int keyToCell = mathFloorWidth * height + mathFloorHeight;
                        targetCellKeys[targetCellKeysCounter] = keyToCell;
                        targetCellKeysCounter++;
                    }
                }
                int sourceKey = row * height + column;
                neighbourLookupHashMap.put(sourceKey,targetCellKeys);
            }
        }

    }
}
