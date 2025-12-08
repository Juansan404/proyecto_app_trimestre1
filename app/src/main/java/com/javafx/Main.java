package com.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Juan
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primeraEscena) throws Exception { //puede lanzar excep'
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventanaPrincipal.fxml"));
        loader.setCharset(java.nio.charset.StandardCharsets.UTF_8);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primeraEscena.setScene(scene);
        primeraEscena.setTitle("--TATTOOAGE--");
        primeraEscena.show();
    }
}
