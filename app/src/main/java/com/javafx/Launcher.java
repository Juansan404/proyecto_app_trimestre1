package com.javafx;

import java.io.File;

public class Launcher {
    public static void main(String[] args) {
        String appData = System.getenv("APPDATA");
        if (appData != null) {
            File tempDir = new File(appData, "TattooAge" + File.separator + "temp");
            tempDir.mkdirs();
            System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
            System.setProperty("net.sf.jasperreports.compiler.temp.dir", tempDir.getAbsolutePath());
        }
        Main.main(args);
    }
}
