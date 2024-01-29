package org.example.particlesimulation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;


public class ParticleSimulationGUI extends Application {
    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 700;

    private static final double SIDEBAR_WIDTH = 200;
    private static final double CANVAS_WIDTH = 1200;
    private static final double CANVAS_HEIGHT = 700;
    //    private static final double UPDATE_RATE_MS = 16.7; // for 60 fps
    private static final double UPDATE_RATE_MS = 33.3; // for 30 fps

    @Override
    public void start(Stage stage) throws IOException {
        Screen primaryScreen = Screen.getPrimary();

        Rectangle2D bounds = primaryScreen.getVisualBounds();

        stage.setTitle("Particle Simulation");
        FXMLLoader loader = new FXMLLoader(ParticleSimulation.class.getResource("Layout.fxml"));
        Pane main = loader.load();

        Pane sideBar = (Pane) loader.getNamespace().get("leftPane");
        sideBar.setPrefHeight(bounds.getHeight());
        sideBar.setPrefWidth(SIDEBAR_WIDTH);

        Canvas canvas = (Canvas) loader.getNamespace().get("canvas");
        canvas.setWidth(bounds.getWidth() - SIDEBAR_WIDTH);
        canvas.setHeight(bounds.getHeight());
        canvas.setLayoutX(SIDEBAR_WIDTH);

        ParticleSimulation simulation = new ParticleSimulation(canvas, UPDATE_RATE_MS);

        SidebarController controller = loader.getController();
        controller.setMainApp(simulation);

        simulation.initContent();

        Scene scene = new Scene(main, bounds.getWidth(), bounds.getHeight());
        stage.setScene(scene);
        simulation.update();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}