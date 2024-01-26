package org.example.particlesimulation;

import javafx.fxml.FXML;

public class SidebarController {
    private ParticleSimulation simulation;

    public void setMainApp(ParticleSimulation simulation){
        this.simulation = simulation;
    }
    @FXML
    protected void handleResetButton() {
        simulation.reset();
    }
}