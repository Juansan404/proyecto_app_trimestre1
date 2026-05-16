package com.javafx.utils;

import com.javafx.util.ThemeManager;
import javafx.scene.Scene;

public class CSSUtils {

    /**
     * Aplica el tema activo (oscuro o claro) a la escena
     * y la registra en ThemeManager para cambios futuros.
     */
    public static void aplicarEstilos(Scene scene) {
        ThemeManager.getInstance().setScene(scene);
    }
}
