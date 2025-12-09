package com.javafx.utils;

import javafx.scene.Scene;

public class CSSUtils {

    /*
        Aplica el archivo de estilos principal a una escena
    */
    public static void aplicarEstilos(Scene scene) {
        String css = CSSUtils.class.getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
    }
}
