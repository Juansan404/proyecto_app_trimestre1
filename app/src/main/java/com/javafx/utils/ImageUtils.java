package com.javafx.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Base64;

/**
 * Utilidad para manejar conversión de imágenes a/desde Base64
 */
public class ImageUtils {

    /**
     * Abre un selector de archivos y convierte la imagen seleccionada a byte[]
     */
    public static byte[] seleccionarYConvertirImagen(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            try {
                return convertirArchivoABytes(archivo);
            } catch (IOException e) {
                System.err.println("Error al leer la imagen: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Convierte un archivo a byte[]
     */
    public static byte[] convertirArchivoABytes(File archivo) throws IOException {
        try (FileInputStream fis = new FileInputStream(archivo);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Convierte byte[] a Image de JavaFX
     */
    public static Image convertirBytesAImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            return new Image(bais);
        } catch (Exception e) {
            System.err.println("Error al convertir bytes a imagen: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convierte byte[] a String Base64
     */
    public static String convertirBytesABase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Convierte String Base64 a byte[]
     */
    public static byte[] convertirBase64ABytes(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Muestra una imagen en un ImageView desde byte[]
     */
    public static void mostrarImagenEnImageView(ImageView imageView, byte[] bytes, double ancho, double alto) {
        Image imagen = convertirBytesAImage(bytes);
        if (imagen != null) {
            imageView.setImage(imagen);
            imageView.setFitWidth(ancho);
            imageView.setFitHeight(alto);
            imageView.setPreserveRatio(true);
        } else {
            imageView.setImage(null);
        }
    }
}
