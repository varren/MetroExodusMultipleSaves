package ru.varren;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class Main extends Application {
    private static final int CHECK_SAVES_TIME = 30000; // 30 secs


    @Override
    public void start(Stage primaryStage) throws Exception {

        //save
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Service.instance().save();
            }
        }, 0, CHECK_SAVES_TIME);
        URL fxml = getClass().getResource("/sample.fxml");
        Parent root = (Parent) FXMLLoader.load(fxml);
        primaryStage.setTitle("Varren's Metro Saver");
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        String css = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);

    }


}
