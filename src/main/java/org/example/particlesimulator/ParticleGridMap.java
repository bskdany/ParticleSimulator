package org.example.particlesimulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParticleGridMap {
    private ArrayList<Particle>[][] particleGrid; // a 2d array of list of particles
    private int[][] particlePresenceMap;
    private double[][][] cellCumulativeAttractionMatrix;
    private final double LEVEL_OF_DETAIL = 4;  // 1 mean normal, 2 means 4 pixels create 1, 3 means 9 create 1 and so on
    private final int width;
    private final int height;
    ParticleGridMap(double canvasWidth, double canvasHeight){
        // if canvas is 400 * 400, the particle grid will be 200 * 200
        width = (int) Math.floor(canvasWidth / LEVEL_OF_DETAIL) + 1;
        height = (int) Math.floor(canvasHeight / LEVEL_OF_DETAIL) + 1;

        particleGrid = new ArrayList[width][height];
        particlePresenceMap = new int[width][height];
        cellCumulativeAttractionMatrix = new double[width][height][7]; // where 7 is the number of species (yes I know hard-coding is bad)

        // initialize the array lists in each grid cell
        for (int i = 0; i < particleGrid.length; i++) {
            for (int j = 0; j < particleGrid[i].length; j++) {
                particleGrid[i][j] = new ArrayList<>();
            }
        }

    }

    public void update(List<Particle> particles){
        placeParticlesInGrid(particles);
        calculateCumulativeAttractionForEachCell(AttractionMatrix.attractionMatrix);
    }

    private void placeParticlesInGrid(List<Particle> particles){
        // not the most optimized way of doing it but easier to implement
        // maybe it is efficient enough assuming that the level of detail is small
        clearGrid();
        clearParticlePresenceMap();

        particles.forEach(particle -> {
            int indexRow = (int) Math.floor(particle.position[0] / LEVEL_OF_DETAIL); // flooring instead of rounding because I want the precise square
            int indexColumn = (int) Math.floor(particle.position[1] / LEVEL_OF_DETAIL);
            particleGrid[indexRow][indexColumn].addLast(particle);
            particlePresenceMap[indexRow][indexColumn] += 1;
        });
    }
    private void clearGrid(){
        for (ArrayList<Particle>[] arrayLists : particleGrid) {
            for (ArrayList<Particle> arrayList : arrayLists) {
                arrayList.clear();
            }
        }
    }
    private void clearParticlePresenceMap(){
        for (int[] ints : particlePresenceMap) {
            Arrays.fill(ints, 0);
        }
    }
    public void calculateCumulativeAttractionForEachCell(double[][] attractionMatrix){
        // the idea is to pre-compute the attraction matrix
        // so for example if you are a green particle and you want to see the attraction to a specific cell
        // it is already going to be computed

        // going through every cell in the grid
        for (int i = 0; i < particlePresenceMap.length; i++) {
            for (int j = 0; j < particlePresenceMap[i].length; j++) {

                // if there are particles in the cell
                if(particlePresenceMap[i][j] > 0){
                    double[] attraction = new double[7];    // initialize the cumulative attraction array for the cell
                    Arrays.fill(attraction, 0);

                    // for each particle species
                    for (int k = 0; k < 7; k++) {

                        // for each particle in the cell (destination)
                        for (int l = 0; l < particleGrid[i][j].size(); l++) {
                            int targetSpecies = particleGrid[i][j].get(l).SPECIES;      // the species of the particle
                            attraction[k] += attractionMatrix[k][targetSpecies];
                        }
                    }
                    // set the cumulative attraction for the cell
                    cellCumulativeAttractionMatrix[i][j] = attraction;
                }

                // if not particles are found in the cell then just fill it with 0s
                else{
                    Arrays.fill(cellCumulativeAttractionMatrix[i][j], 0);
                }
            }
        }
    }

    public double[] getForcesAroundParticle(Particle particle){
        int indexRow = (int) Math.floor(particle.position[0] / LEVEL_OF_DETAIL); // flooring instead of rounding because I want the precise square
        int indexColumn = (int) Math.floor(particle.position[1] / LEVEL_OF_DETAIL);
        int maxCellsRadius = (int) Math.floor(ParticleSimulation.maxAttractionDistance / LEVEL_OF_DETAIL);

        // I need to get the particles with a max radius away from the row and column index
        // because its kinda hard to create a circle I will approximate it with a square maxCellRadius tall and wide
        int squareIndexStartRow = (indexRow - maxCellsRadius);
        int squareIndexStartColumn = (indexColumn - maxCellsRadius);

        int squareIndexEndRow = (indexRow + maxCellsRadius);
        int squareIndexEndColumn = (indexRow + maxCellsRadius);

        double[] force = new double[]{0,0};

        for (int i = squareIndexStartRow; i < squareIndexEndRow; i++) {
            for (int j = squareIndexStartColumn; j < squareIndexEndColumn; j++) {

                int actualWidth = Math.floorMod(i, width);
                int actualHeight = Math.floorMod(j, height);
                if(particlePresenceMap[actualWidth][actualHeight] > 0){
                    double xDirection = actualWidth - indexRow;
                    double yDirection = actualHeight - indexColumn;


                    double relativeDistance = Math.sqrt(xDirection * xDirection + yDirection * yDirection) / maxCellsRadius;
                    double attractionFactor = cellCumulativeAttractionMatrix[actualWidth][actualHeight][particle.SPECIES];

                    double magnitude = Particle.calculateAttractionForce(relativeDistance, attractionFactor);
                    double[] normalisedDirectionVector = Particle.normalizeVector(new double[]{xDirection,yDirection});
                    force[0] += normalisedDirectionVector[0] * magnitude * maxCellsRadius;
                    force[1] += normalisedDirectionVector[1] * magnitude * maxCellsRadius;
                }

            }
        }
        return force;
    }
}
