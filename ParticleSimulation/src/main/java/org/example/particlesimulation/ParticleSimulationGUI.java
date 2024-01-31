package org.example.particlesimulation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;


public class ParticleSimulationGUI extends Application {
    private static final double SIDEBAR_WIDTH = 200;
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

        VBox canvasContainer = (VBox) loader.getNamespace().get("canvasContainer");
        canvasContainer.setLayoutX(SIDEBAR_WIDTH);

        Canvas canvas = (Canvas) loader.getNamespace().get("canvas");
        canvas.setWidth(bounds.getWidth() - SIDEBAR_WIDTH);
        // HARDCODE THE FUCKING  HEIGHT OFFSET FROM THE BOTTOM BECAUSE THERE IS NO ABSOLUTE RELIABLE WAY
        // TO DO IT NORMALLY. SCENE HEIGHT DOES NOT INCLUDE THE TOP BAR, THERE IS NO ABSOLUTE WAY OF GETTING THE TOP
        // BAR HEIGHT. YOU CAN SET A LISTENER OF THE SCENE HEIGHT PROPERTY BUT IT DOES NOT FUCKING TRIGGER ON WINDOW
        // MAXIMISE SO I GIVE UP FUCK YOU
        double heightVooDooOffset = 40;
        canvas.setHeight(bounds.getHeight() - heightVooDooOffset);

        ParticleSimulation simulation = new ParticleSimulation(canvas, UPDATE_RATE_MS);
        simulation.initContent();
        SidebarController controller = loader.getController();
        controller.setMainApp(simulation);

        Scene scene = new Scene(main, bounds.getWidth(), bounds.getHeight());
        stage.setScene(scene);
        simulation.update();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}