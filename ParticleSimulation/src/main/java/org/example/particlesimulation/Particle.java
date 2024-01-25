package org.example.particlesimulation;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class Particle extends Circle {
    private final double PANE_WIDTH;
    private final double PANE_HEIGHT;
    private final double[] RELATIVE_ATTRACTION_MATRIX;
    public double DELTA_TIME;
    public double MASS;
    public int SPECIES;
    private final double WRAP_DIRECTION_LIMIT_WIDTH;
    private final double WRAP_DIRECTION_LIMIT_HEIGHT;
    private final double[] FORCE = {0,0};
    public double[] POSITION = {0, 0};
    public final double MAX_ATTRACTION_DISTANCE;
    public final double ATTRACTION_RELATIVE_DISTANCE_CUTOUT = 0.3;
    public double[] VELOCITY = {0,0};
    public double FRICTION = 0.04;
    private final double RADIUS;
    private double[] DELTA_POSITION = {0,0};

    double[] directionVector = new double[2];
    public double FORCE_MULTIPLIER = 7;
    Particle(int x, int y, double radius, Color color, double mass, int species, double deltaTime, double[] relativeAttractionMatrix, double paneWidth, double paneHeight){
        super(x,y,radius,color);
        POSITION[0] = x;
        POSITION[1] = y;
        MASS = mass;
        SPECIES = species;
        DELTA_TIME = deltaTime;
        RELATIVE_ATTRACTION_MATRIX = relativeAttractionMatrix;
        PANE_WIDTH = paneWidth;
        PANE_HEIGHT = paneHeight;
        MAX_ATTRACTION_DISTANCE = radius * 100;
        RADIUS = radius;
        WRAP_DIRECTION_LIMIT_WIDTH = PANE_WIDTH - MAX_ATTRACTION_DISTANCE - 1; // -1 because you have to consider 0 as a coordinate, so in total you would have PANE_WIDTH + 1
        WRAP_DIRECTION_LIMIT_HEIGHT = PANE_HEIGHT - MAX_ATTRACTION_DISTANCE -1;
    }
    public void move(){
        adjustPositionWrapping();
        setCenterX(POSITION[0]);
        setCenterY(POSITION[1]);
    }

    public void adjustPositionWrapping(){
        if(POSITION[0] < 0){
            POSITION[0] += PANE_WIDTH + 1;
        } else if (POSITION[0] > PANE_WIDTH) {
            POSITION[0] -= PANE_WIDTH - 1;
        }
        if(POSITION[1] < 0){
            POSITION[1] += PANE_HEIGHT +1;
        } else if (POSITION[1] > PANE_HEIGHT) {
            POSITION[1] -= PANE_HEIGHT -1;
        }
    }
    public void simulate(List<Particle> particles){
        FORCE[0] = 0;
        FORCE[1] = 0;
        for(Particle particle : particles){
            if(particle != this){
                directionVector[0] = particle.POSITION[0] - POSITION[0];
                if(Math.abs(directionVector[0]) > MAX_ATTRACTION_DISTANCE && Math.abs(directionVector[0]) < WRAP_DIRECTION_LIMIT_WIDTH){
                    continue;
                }
                directionVector[1] = particle.POSITION[1] - POSITION[1];
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
//        double[] vectorTowardsCenter = normalizeVector(new double[] {(( PANE_WIDTH / 2) - POSITION[0]), ( PANE_HEIGHT / 2) - POSITION[1]});
//        FORCE[0] += vectorTowardsCenter[0];
//        FORCE[1] += vectorTowardsCenter[1];

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
    //        private static double[] calculateReflectionVector(double[] directionVector, double[] normalVector) {
//            // Normalize the direction vector
//            directionVector = normalizeVector(directionVector);
//
//            // Calculate the dot product
//            double dotProduct = directionVector[0] * normalVector[0] + directionVector[1] * normalVector[1];
//
//            // Calculate the reflection vector
//            double[] reflectionVector = {
//                    directionVector[0] - 2 * dotProduct * normalVector[0],
//                    directionVector[1] - 2 * dotProduct * normalVector[1]
//            };
//
//            // Normalize the reflection vector
//            reflectionVector = normalizeVector(reflectionVector);
//
//            return reflectionVector;
//        }
//        private void handleCollisionWalls(){
//
//            for (Wall wall : walls) {
//                if (getBoundsInParent().intersects(wall.getBoundsInParent())) {
//
//                    double particleX = getCenterX();
//                    double particleY = getCenterY();
//
//                    double rectangleX = wall.getX();
//                    double rectangleY = wall.getY();
//                    double rectangleWidth = wall.getWidth();
//                    double rectangleHeight = wall.getHeight();
//                    double rectangleOriginX = rectangleX + rectangleWidth / 2;
//                    double rectangleOriginY = rectangleY + rectangleHeight / 2;
//
//                    double deltaXRectangleParticle = rectangleOriginX - particleX;
//                    double deltaYRectangleParticle = rectangleOriginY - particleY;
//
//                    double collisionOriginX = particleX;
//                    double collisionOriginY = particleY;
//                    double[] normalVector = {0, 0};
//
//                    if (deltaXRectangleParticle > rectangleWidth / 2) {
//                        collisionOriginX = particleX + (deltaXRectangleParticle - rectangleWidth / 2);
//                        normalVector[0] = -1;
//                    } else if (deltaXRectangleParticle < -(rectangleWidth / 2)) {
//                        collisionOriginX = particleX + (deltaXRectangleParticle + rectangleWidth / 2);
//                        normalVector[0] = 1;
//                    }
//
//                    if (deltaYRectangleParticle > rectangleHeight / 2) {
//                        collisionOriginY = particleY + (deltaYRectangleParticle - rectangleHeight / 2);
//                        normalVector[1] = -1;
//                    } else if (deltaYRectangleParticle < -(rectangleHeight / 2)) {
//                        collisionOriginY = particleY + (deltaYRectangleParticle + rectangleHeight / 2);
//                        normalVector[1] = 1;
//                    }
//
//                    Circle collisionPlaceholder = new Circle(collisionOriginX, collisionOriginY, 3, Color.RED);
//                    root.getChildren().add(collisionPlaceholder);
//
//                    FORCE = calculateReflectionVector(FORCE, normalVector);
//                }
//            }
//        }
//        private double[] calculateAttractionForce(Particle source, Particle target){
//            // vector from source to target
//            double[] directionVector = {target.getCenterX() - source.getCenterX(), target.getCenterY() - source.getCenterY()};
//            // length of the distance
//            double distance = Math.sqrt(Math.pow(directionVector[0],2) + Math.pow(directionVector[1], 2));
//            // F = G * (m1 * m2) / r^2
//            double magnitude =  ATTRACTION * (source.MASS * target.MASS) / (distance * distance);
//
//            return normalizeVector(new double[]{directionVector[0] * magnitude, directionVector[1] * magnitude});
//        }
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
            directionVector[0] = directionVector[0] - PANE_WIDTH -1;
        }
        else if(directionVector[0] < -WRAP_DIRECTION_LIMIT_WIDTH){ // warp right
            directionVector[0] = directionVector[0] + PANE_WIDTH +1;
        }

        if(directionVector[1] > WRAP_DIRECTION_LIMIT_HEIGHT){ // warp top
            directionVector[1] = directionVector[1] - PANE_HEIGHT -1;
        }
        else if(directionVector[1] < -WRAP_DIRECTION_LIMIT_HEIGHT){ // warp bottom
            directionVector[1] = directionVector[1] + PANE_HEIGHT +1;
        }
        return directionVector;
    }
}