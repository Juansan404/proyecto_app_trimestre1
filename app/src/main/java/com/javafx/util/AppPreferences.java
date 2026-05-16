package com.javafx.util;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * Almacena preferencias persistentes de la aplicación usando la API
 * java.util.prefs.Preferences (registro de Windows / fichero en Linux/macOS).
 * Singleton de acceso rápido: AppPreferences.getInstance().
 */
public class AppPreferences {

    private static final String NODE       = "com/javafx/tattooage";
    private static final String KEY_PDF_DIR = "pdfSaveDirectory";

    private static AppPreferences instance;
    private final Preferences prefs;

    private AppPreferences() {
        prefs = Preferences.userRoot().node(NODE);
    }

    public static AppPreferences getInstance() {
        if (instance == null) instance = new AppPreferences();
        return instance;
    }

    // ── PDF ─────────────────────────────────────────────────────────────────

    /** Directorio de guardado de PDFs. Por defecto: carpeta Descargas del usuario. */
    public String getPdfDirectory() {
        String def = System.getProperty("user.home") + File.separator + "Downloads";
        return prefs.get(KEY_PDF_DIR, def);
    }

    public void setPdfDirectory(String path) {
        prefs.put(KEY_PDF_DIR, path);
    }

    public void resetPdfDirectory() {
        prefs.remove(KEY_PDF_DIR);
    }
}
