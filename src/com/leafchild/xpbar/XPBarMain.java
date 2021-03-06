package com.leafchild.xpbar;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

/**
 * Created by: vmalyshev
 * Project: XPbar
 * Date: 08-May-14
 * Time: 18:40
 * Main app class
 */
public class XPBarMain extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("xpBar.fxml"));
        primaryStage.setTitle("XP bar v.0.3, Leafchild");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
