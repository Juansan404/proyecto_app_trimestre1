package com.javafx.utils;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Clase de utilidades para animaciones en JavaFX
 */
public class AnimationUtils {

    /**
     * Efecto shake (temblor) - para indicar error en validación
     */
    public static void shake(Node node) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), 10)),
            new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 10)),
            new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), 0))
        );
        timeline.play();
    }

    /**
     * Aplica una animación fade-in al nodo especificado
     */
    public static void fadeIn(Node node) {
        fadeIn(node, 400);
    }

    /**
     * Aplica una animación fade-in al nodo especificado con duración personalizada
     */
    public static void fadeIn(Node node, int durationMillis) {
        node.setOpacity(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(durationMillis), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeIn.play();
    }
}
