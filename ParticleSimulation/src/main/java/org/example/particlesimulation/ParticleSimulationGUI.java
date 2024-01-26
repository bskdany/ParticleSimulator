package org.example.particlesimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ParticleSimulationGUI extends Application {
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 700;
    private static final int PANE_WIDTH = 1300;
    private static final int PANE_HEIGHT = 700;
    //    private static final double UPDATE_RATE_MS = 16.7; // for 60 fps
    private static final double UPDATE_RATE_MS = 33.3; // for 30 fps
    Pane root = new Pane();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Particle Simulation");
        FXMLLoader loader = new FXMLLoader(ParticleSimulation.class.getResource("Layout.fxml"));
        Pane main = loader.load();

        root = (Pane) loader.getNamespace().get("rightPane");
        Pane sideBar = (Pane) loader.getNamespace().get("leftPane");
        sideBar.setPrefHeight(WINDOW_HEIGHT);
        sideBar.setPrefWidth(WINDOW_WIDTH-PANE_WIDTH);
        root.setPrefWidth(WINDOW_WIDTH);
        root.setPrefHeight(PANE_WIDTH);
        root.setLayoutX(WINDOW_WIDTH-PANE_WIDTH);

        ParticleSimulation simulation = new ParticleSimulation(root, PANE_WIDTH, PANE_HEIGHT, UPDATE_RATE_MS);

        SidebarController controller = loader.getController();
        controller.setMainApp(simulation);

        simulation.initContent();

        Scene scene = new Scene(main, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
//        pressedKeyHandling(scene);
        simulation.update();
        stage.show();

    }



    public static void main(String[] args) {
        launch();
    }
}