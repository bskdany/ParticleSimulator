package org.example.particlesimulation;

import javafx.scene.paint.Color;

import java.util.List;

public class Particle {
    private final double CANVAS_WIDTH;
    private final double CANVAS_HEIGTH;
    private double[] RELATIVE_ATTRACTION_MATRIX;
    public double DELTA_TIME;
    public double MASS;
    public int SPECIES;
    private final double WRAP_DIRECTION_LIMIT_WIDTH;
    private final double WRAP_DIRECTION_LIMIT_HEIGHT;
    private final double[] FORCE = {0,0};
    public double[] POSITION = {0, 0};
    public int MAX_ATTRACTION_DISTANCE;
    public final double ATTRACTION_RELATIVE_DISTANCE_CUTOUT = 0.4;
    public double[] VELOCITY = {0,0};
    public double FRICTION = 0.05;
    public final double RADIUS;
    private final double[] DELTA_POSITION = {0,0};
    public Color COLOR;
    double[] directionVector = new double[2];
    public double FORCE_MULTIPLIER;
    Particle(int x, int y, double radius, Color color, double mass, int species, double deltaTime, double[] relativeAttractionMatrix, double forceMultiplier, double paneWidth, double paneHeight){
        POSITION[0] = x;
        POSITION[1] = y;
        RADIUS = radius;
        COLOR = color;
        MASS = mass;
        SPECIES = species;
        DELTA_TIME = deltaTime;
        RELATIVE_ATTRACTION_MATRIX = relativeAttractionMatrix;
        CANVAS_WIDTH = paneWidth;
        CANVAS_HEIGTH = paneHeight;
        MAX_ATTRACTION_DISTANCE = 100;
        FORCE_MULTIPLIER = forceMultiplier;

        WRAP_DIRECTION_LIMIT_WIDTH = CANVAS_WIDTH - MAX_ATTRACTION_DISTANCE - 1; // -1 because you have to consider 0 as a coordinate, so in total you would have CANVAS_WIDTH + 1
        WRAP_DIRECTION_LIMIT_HEIGHT = CANVAS_HEIGTH - MAX_ATTRACTION_DISTANCE -1;
    }

    public void adjustPositionWrapping(){
        if(POSITION[0] < 0){
            POSITION[0] += CANVAS_WIDTH + 1;
        } else if (POSITION[0] > CANVAS_WIDTH) {
            POSITION[0] -= CANVAS_WIDTH - 1;
        }
        if(POSITION[1] < 0){
            POSITION[1] += CANVAS_HEIGTH +1;
        } else if (POSITION[1] > CANVAS_HEIGTH) {
            POSITION[1] -= CANVAS_HEIGTH -1;
        }
    }
    public void simulate(List<Particle> particles){
        FORCE[0] = 0;
        FORCE[1] = 0;
        for(Particle particle : particles){
            if(particle != this){
                directionVector[0] = particle.POSITION[0] - POSITION[0];
                directionVector[1] = particle.POSITION[1] - POSITION[1];

                if(Math.abs(directionVector[0]) > MAX_ATTRACTION_DISTANCE && Math.abs(directionVector[0]) < WRAP_DIRECTION_LIMIT_WIDTH){
                    continue;
                }
                if(Math.abs(directionVector[1]) > MAX_ATTRACTION_DISTANCE && Math.abs(directionVector[1]) < WRAP_DIRECTION_LIMIT_HEIGHT){
                    continue;
                }

                double[] directionVector = calculateVectorWrap();

                // length of the relative distance
                double distance = Math.sqrt(directionVector[0] * directionVector[0] + directionVector[1] * directionVector[1]) / MAX_ATTRACTION_DISTANCE;

                if(distance > 1) {
                    continue;
                }
                double attractionFactor = RELATIVE_ATTRACTION_MATRIX[particle.SPECIES];
                double magnitude =  calculateAttractionForce(distance, attractionFactor);
                double[] normalisedDirectionVector = normalizeVector(directionVector);

                FORCE[0] += normalisedDirectionVector[0] * magnitude * MAX_ATTRACTION_DISTANCE;
                FORCE[1] += normalisedDirectionVector[1] * magnitude * MAX_ATTRACTION_DISTANCE;
            }
        }
        // all particles move towards the center slowly
        double[] vectorTowardsCenter = normalizeVector(new double[] {(( CANVAS_WIDTH / 2) - POSITION[0]), ( CANVAS_HEIGTH / 2) - POSITION[1]});
        FORCE[0] += vectorTowardsCenter[0];
        FORCE[1] += vectorTowardsCenter[1];

        // F = m / a
        double accelerationX = FORCE[0] * FORCE_MULTIPLIER / MASS;
        double accelerationY = FORCE[1] * FORCE_MULTIPLIER / MASS;

        VELOCITY[0] *= FRICTION;
        VELOCITY[1] *= FRICTION;

        VELOCITY[0] += accelerationX * DELTA_TIME;
        VELOCITY[1] += accelerationY * DELTA_TIME;

        DELTA_POSITION[0] = VELOCITY[0] * DELTA_TIME;
        DELTA_POSITION[1] = VELOCITY[1] * DELTA_TIME;

        if(DELTA_POSITION[0] > RADIUS * 10){
            // explode
        }

        POSITION[0] += VELOCITY[0] * DELTA_TIME;
        POSITION[1] += VELOCITY[1] * DELTA_TIME;

    }

    private static double[] normalizeVector(double[] vector) {
        double magnitude = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
        if(magnitude != 0){
            return new double[]{vector[0] / magnitude, vector[1] / magnitude};
        }
        return vector;
    }

    private double calculateAttractionForce(double relativeDistance, double attractionFactor){
        if(relativeDistance < ATTRACTION_RELATIVE_DISTANCE_CUTOUT){
            return relativeDistance / ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 1;
        } else if (relativeDistance < 1.0) {
            return (-Math.abs(relativeDistance - ATTRACTION_RELATIVE_DISTANCE_CUTOUT - 0.5) + 0.5 ) * 2 * attractionFactor;
        }
        return 0;
    }

    private double[] calculateVectorWrap(){
        // vector from source to target
        if(directionVector[0] > WRAP_DIRECTION_LIMIT_WIDTH){  // warp left
            directionVector[0] = directionVector[0] - CANVAS_WIDTH -1;
        }
        else if(directionVector[0] < -WRAP_DIRECTION_LIMIT_WIDTH){ // warp right
            directionVector[0] = directionVector[0] + CANVAS_WIDTH +1;
        }

        if(directionVector[1] > WRAP_DIRECTION_LIMIT_HEIGHT){ // warp top
            directionVector[1] = directionVector[1] - CANVAS_HEIGTH -1;
        }
        else if(directionVector[1] < -WRAP_DIRECTION_LIMIT_HEIGHT){ // warp bottom
            directionVector[1] = directionVector[1] + CANVAS_HEIGTH +1;
        }
        return directionVector;
    }

    public void setRelativeAttractionMatrix(double[] attractionMatrix){
        this.RELATIVE_ATTRACTION_MATRIX = attractionMatrix;
    }

    public void setMaxAttractionDistance(int distance){
        MAX_ATTRACTION_DISTANCE = distance;
    }

}