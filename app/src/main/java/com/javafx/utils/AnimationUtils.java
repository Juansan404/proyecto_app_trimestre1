package com.javafx.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Clase de utilidades para animaciones en JavaFX
 * Basada en las recomendaciones del PDF de Animaciones JavaFX
 */
public class AnimationUtils {

    // ========================================
    // ANIMACIONES FADE (Aparecer/Desaparecer)
    // ========================================

    /**
     * Aplica animación de fade in (aparecer gradualmente)
     */
    public static void fadeIn(Node node, double durationMillis) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Aplica animación de fade out (desaparecer gradualmente)
     */
    public static void fadeOut(Node node, double durationMillis) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }

    /**
     * Fade out con acción al finalizar
     */
    public static void fadeOutAndThen(Node node, double durationMillis, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> onFinished.run());
        fade.play();
    }

    // ========================================
    // ANIMACIONES SLIDE (Deslizamiento)
    // ========================================

    /**
     * Desliza un nodo desde la izquierda
     */
    public static void slideInFromLeft(Node node, double durationMillis) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(durationMillis), node);
        translate.setFromX(-300);
        translate.setToX(0);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.play();
    }

    /**
     * Desliza un nodo hacia la izquierda (salida)
     */
    public static void slideOutToLeft(Node node, double durationMillis) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(durationMillis), node);
        translate.setFromX(0);
        translate.setToX(-300);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.play();
    }

    /**
     * Desliza un nodo desde la derecha
     */
    public static void slideInFromRight(Node node, double durationMillis) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(durationMillis), node);
        translate.setFromX(300);
        translate.setToX(0);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.play();
    }

    /**
     * Desliza un nodo desde arriba
     */
    public static void slideInFromTop(Node node, double durationMillis) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(durationMillis), node);
        translate.setFromY(-200);
        translate.setToY(0);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.play();
    }

    // ========================================
    // ANIMACIONES SCALE (Escala/Zoom)
    // ========================================

    /**
     * Aplica animación de zoom in (aparecer con crecimiento)
     */
    public static void scaleIn(Node node, double durationMillis) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMillis), node);
        scale.setFromX(0.0);
        scale.setFromY(0.0);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }

    /**
     * Aplica animación de zoom out (desaparecer con reducción)
     */
    public static void scaleOut(Node node, double durationMillis) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMillis), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.0);
        scale.setToY(0.0);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }

    /**
     * Efecto de pulso (aumenta y reduce el tamaño)
     */
    public static void pulse(Node node, double durationMillis, int cycles) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMillis), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.15);
        scale.setToY(1.15);
        scale.setCycleCount(cycles * 2);
        scale.setAutoReverse(true);
        scale.play();
    }

    // ========================================
    // ANIMACIONES ROTATE (Rotación)
    // ========================================

    /**
     * Rotación completa (360 grados)
     */
    public static void rotate360(Node node, double durationMillis) {
        RotateTransition rotate = new RotateTransition(Duration.millis(durationMillis), node);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.play();
    }

    /**
     * Rotación continua (infinita)
     */
    public static void rotateContinuous(Node node, double durationMillis) {
        RotateTransition rotate = new RotateTransition(Duration.millis(durationMillis), node);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();
    }

    // ========================================
    // ANIMACIONES ESPECIALES
    // ========================================

    /**
     * Efecto shake (temblor) - útil para indicar error
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
     * Efecto bounce (rebote)
     */
    public static void bounce(Node node, double durationMillis) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(durationMillis / 4), node);
        translate.setFromY(0);
        translate.setToY(-30);
        translate.setCycleCount(2);
        translate.setAutoReverse(true);

        TranslateTransition translate2 = new TranslateTransition(Duration.millis(durationMillis / 4), node);
        translate2.setFromY(0);
        translate2.setToY(-15);
        translate2.setCycleCount(2);
        translate2.setAutoReverse(true);

        SequentialTransition sequential = new SequentialTransition(translate, translate2);
        sequential.play();
    }

    /**
     * Efecto de destello (flash) con cambio de opacidad
     */
    public static void flash(Node node, int cycles) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(cycles * 2);
        fade.setAutoReverse(true);
        fade.play();
    }

    // ========================================
    // EFECTOS VISUALES CON ANIMACIÓN
    // ========================================

    /**
     * Añade efecto de sombra animado (glow)
     */
    public static void glowEffect(Node node, Color color, double durationMillis) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(color);
        node.setEffect(dropShadow);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(dropShadow.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(durationMillis / 2), new KeyValue(dropShadow.radiusProperty(), 20)),
            new KeyFrame(Duration.millis(durationMillis), new KeyValue(dropShadow.radiusProperty(), 0))
        );
        timeline.play();
    }

    // ========================================
    // TRANSICIONES COMBINADAS (SECUENCIALES)
    // ========================================

    /**
     * Animación de transición suave entre dos nodos (crossfade)
     */
    public static void crossFade(Node nodeOut, Node nodeIn, double durationMillis) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(durationMillis), nodeOut);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(durationMillis), nodeIn);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(fadeOut, fadeIn);
        parallel.play();
    }

    /**
     * Transición de "cambio de vista" con slide
     */
    public static void slideTransition(Node nodeOut, Node nodeIn, double durationMillis) {
        // Nodo que sale: se desliza hacia la izquierda y desaparece
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(durationMillis), nodeOut);
        slideOut.setFromX(0);
        slideOut.setToX(-300);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(durationMillis), nodeOut);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Nodo que entra: viene desde la derecha y aparece
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(durationMillis), nodeIn);
        slideIn.setFromX(300);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(durationMillis), nodeIn);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition outTransition = new ParallelTransition(slideOut, fadeOut);
        ParallelTransition inTransition = new ParallelTransition(slideIn, fadeIn);

        SequentialTransition sequence = new SequentialTransition(outTransition, inTransition);
        sequence.play();
    }

    // ========================================
    // UTILIDADES
    // ========================================

    /**
     * Detiene todas las animaciones de un nodo
     */
    public static void stopAllAnimations(Node node) {
        node.setTranslateX(0);
        node.setTranslateY(0);
        node.setScaleX(1);
        node.setScaleY(1);
        node.setRotate(0);
        node.setOpacity(1);
    }

    /**
     * Resetea la posición y propiedades de un nodo
     */
    public static void resetNode(Node node) {
        node.setTranslateX(0);
        node.setTranslateY(0);
        node.setScaleX(1.0);
        node.setScaleY(1.0);
        node.setRotate(0);
        node.setOpacity(1.0);
    }
}
