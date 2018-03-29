package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.MenuBar;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game();
        game.setTableBackground(new Image("/table/green.png"));
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem restart = new MenuItem("Restart");
        MenuItem fullScreen = new MenuItem("FullScreen");
        restart.setOnAction(e -> {
            game.restartGame();
            game.getChildren().add(menuBar);
        });
        fullScreen.setOnAction(e -> primaryStage.setFullScreen(true));
        fileMenu.getItems().addAll(restart, fullScreen);
        menuBar.getMenus().add(fileMenu);
        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        game.getChildren().add(menuBar);
        primaryStage.show();
    }

}
