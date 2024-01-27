package org.example.particlesimulation;

public class ParticleSpeciesData {
    private int quantity;
    private int radius;

    public ParticleSpeciesData(int quantity, int radius) {
        this.quantity = quantity;
        this.radius = radius;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}

