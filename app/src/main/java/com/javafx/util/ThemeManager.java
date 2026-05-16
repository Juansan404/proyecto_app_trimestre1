package com.javafx.util;

import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.scene.Scene;

/**
 * Gestiona el tema visual de la aplicación: Oscuro, Claro o Sistema.
 * En modo Sistema escucha Platform.getPreferences() (JavaFX 21+) para
 * reaccionar automáticamente a cambios del tema del SO en tiempo real.
 */
public class ThemeManager {

    public enum ThemeMode { DARK, SYSTEM, LIGHT }

    private static final ThemeManager INSTANCE = new ThemeManager();

    private ThemeMode mode = ThemeMode.SYSTEM;
    private Scene activeScene;
    private boolean listenerRegistered = false;

    private static final String CSS_DARK  = "/styles.css";
    private static final String CSS_LIGHT = "/styles-light.css";

    private ThemeManager() {}

    public static ThemeManager getInstance() { return INSTANCE; }

    // ── Registro de escena ──────────────────────────────────────────────

    /** Registra la escena activa, instala el listener del sistema y aplica el tema. */
    public void setScene(Scene scene) {
        this.activeScene = scene;
        if (!listenerRegistered) {
            registrarListenerSistema();
            listenerRegistered = true;
        }
        applyTo(scene);
    }

    // ── Cambio de modo ──────────────────────────────────────────────────

    public void setMode(ThemeMode mode) {
        this.mode = mode;
        if (activeScene != null) applyTo(activeScene);
    }

    /** Compatibilidad con código legado (ConfiguracionController antiguo). */
    public void setDarkMode(boolean dark) {
        setMode(dark ? ThemeMode.DARK : ThemeMode.LIGHT);
    }

    // ── Consultas ───────────────────────────────────────────────────────

    public ThemeMode getMode() { return mode; }

    /** Devuelve si el tema efectivo (resolviendo SYSTEM) es oscuro. */
    public boolean isDarkMode() { return isEffectivelyDark(); }

    // ── Aplicación del tema ─────────────────────────────────────────────

    /**
     * Aplica el tema activo a la escena dada.
     *
     * Regla: styles.css (oscuro) siempre está en la lista como base.
     * Para cambiar a claro se AÑADE styles-light.css al final.
     * Para cambiar a oscuro se ELIMINA styles-light.css.
     *
     * Nunca se limpia toda la lista: JavaFX cachea los stylesheets por URL
     * y no re-procesa los nodos si se elimina y vuelve a añadir la misma URL.
     */
    public void applyTo(Scene scene) {
        String darkUrl  = ThemeManager.class.getResource(CSS_DARK).toExternalForm();
        String lightUrl = ThemeManager.class.getResource(CSS_LIGHT).toExternalForm();

        // styles.css siempre debe estar como primera hoja de estilo
        if (!scene.getStylesheets().contains(darkUrl)) {
            scene.getStylesheets().add(0, darkUrl);
        }

        // Añadir o quitar styles-light.css según el modo efectivo
        boolean wantsLight = !isEffectivelyDark();
        boolean hasLight   = scene.getStylesheets().contains(lightUrl);

        if (wantsLight && !hasLight) {
            scene.getStylesheets().add(lightUrl);
        } else if (!wantsLight && hasLight) {
            scene.getStylesheets().remove(lightUrl);
        }
    }

    // ── Internos ────────────────────────────────────────────────────────

    private boolean isEffectivelyDark() {
        return switch (mode) {
            case DARK   -> true;
            case LIGHT  -> false;
            case SYSTEM -> detectarTemaDelSistema();
        };
    }

    /**
     * Lee el tema del sistema usando Platform.getPreferences() (JavaFX 21+).
     * Devuelve true (oscuro) si no está disponible o no puede determinarse.
     */
    private boolean detectarTemaDelSistema() {
        try {
            ColorScheme scheme = Platform.getPreferences().getColorScheme();
            return scheme == null || scheme == ColorScheme.DARK;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Registra un listener en Platform.getPreferences() para reaccionar
     * automáticamente cuando el usuario cambia el tema del SO.
     */
    private void registrarListenerSistema() {
        try {
            Platform.getPreferences().colorSchemeProperty().addListener((obs, old, scheme) -> {
                if (mode == ThemeMode.SYSTEM && activeScene != null) {
                    Platform.runLater(() -> applyTo(activeScene));
                }
            });
        } catch (Exception ignored) {
            // La API no está disponible en este entorno; se continúa sin listener
        }
    }
}
