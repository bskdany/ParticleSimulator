package org.example.particlesimulator;

public class Configs {
    public static boolean CAP_PARTICLE_SPEED = true;
    public static boolean USE_IMMOBILE_OPTIMIZATION = true;
    public static boolean REJECT_RANDOM_PARTICLES_OPTIMIZATION = false;

    public static boolean CLUSTER_CLOSE_PARTICLES_OPTIMIZATION = false;
    public static boolean APPROXIMATE_CIRCLE_OPTIMIZATION = false;

    public static int GRID_MAP_LOOKUP_RADIUS = 3;
    public static int CELL_SIZE_FINE = 3;


    public static boolean CAP_FPS = true;
    public static int TARGET_FPS = 30;

    public static int DEFAULT_PARTICLE_COUNT = 1500;
    public static int DEFAULT_MAX_ATTRACTION_DISTANCE = 30;
    public static double DEFAULT_MIN_ATTRACTION_DISTANCE_RELATIVE = 0.4;
    public static int DEFAULT_FORCE_MULTIPLIER = 5;

    public static int CENTRAL_ATTRACTION_MULTIPLIER = 5;
    public static double PARTICLE_FRICTION = 0.04;
    public static double PARTICLE_RADIUS = 0.5;
    public static double REPULSION_MULTIPLIER = 1.5;

}
