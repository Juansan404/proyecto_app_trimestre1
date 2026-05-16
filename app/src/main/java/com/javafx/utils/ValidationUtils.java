package com.javafx.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Validaciones de formulario alineadas con las reglas del backend TattooAge.
 * Uso fluido: new ValidationUtils().required(...).maxLength(...).hasErrors()
 */
public class ValidationUtils {

    private final List<String> errors = new ArrayList<>();

    // ── Checks de campo ────────────────────────────────────────────────────

    /** El campo no puede ser nulo ni estar vacío. */
    public ValidationUtils required(String value, String fieldName) {
        if (value == null || value.isBlank())
            errors.add("• " + fieldName + " es obligatorio.");
        return this;
    }

    /** El objeto ComboBox no puede ser nulo. */
    public ValidationUtils requiredObject(Object value, String fieldName) {
        if (value == null)
            errors.add("• " + fieldName + " es obligatorio.");
        return this;
    }

    /** Longitud máxima permitida. */
    public ValidationUtils maxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max)
            errors.add("• " + fieldName + " no puede superar " + max + " caracteres (actual: " + value.length() + ").");
        return this;
    }

    /** Longitud mínima (solo si el campo tiene contenido). */
    public ValidationUtils minLength(String value, int min, String fieldName) {
        if (value != null && !value.isBlank() && value.length() < min)
            errors.add("• " + fieldName + " debe tener al menos " + min + " caracteres.");
        return this;
    }

    /** Formato de email básico. Solo valida si hay contenido. */
    public ValidationUtils email(String value, String fieldName) {
        if (value != null && !value.isBlank()
                && !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$"))
            errors.add("• " + fieldName + " no es un email válido.");
        return this;
    }

    /** Número decimal positivo (opcional: solo valida si hay contenido). */
    public ValidationUtils positiveDecimal(String value, String fieldName) {
        if (value != null && !value.isBlank()) {
            try {
                double d = Double.parseDouble(value.replace(",", "."));
                if (d < 0)
                    errors.add("• " + fieldName + " debe ser un valor positivo.");
            } catch (NumberFormatException e) {
                errors.add("• " + fieldName + " debe ser un número válido (ej. 50.00).");
            }
        }
        return this;
    }

    /** Entero no negativo (opcional: solo valida si hay contenido). */
    public ValidationUtils positiveInt(String value, String fieldName) {
        if (value != null && !value.isBlank()) {
            try {
                int i = Integer.parseInt(value.trim());
                if (i < 0)
                    errors.add("• " + fieldName + " debe ser un número positivo.");
            } catch (NumberFormatException e) {
                errors.add("• " + fieldName + " debe ser un número entero (ej. 60).");
            }
        }
        return this;
    }

    /** Hora con formato HH:mm (opcional: solo valida si hay contenido). */
    public ValidationUtils horaFormato(String value, String fieldName) {
        if (value != null && !value.isBlank()
                && !value.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
            errors.add("• " + fieldName + " debe tener formato HH:mm (ej. 10:30).");
        return this;
    }

    /** Teléfono: solo dígitos, espacios, guiones y el prefijo '+'. */
    public ValidationUtils telefono(String value, String fieldName) {
        if (value != null && !value.isBlank()
                && !value.matches("^[+]?[0-9 \\-]{6,15}$"))
            errors.add("• " + fieldName + " solo puede contener dígitos, espacios y guiones.");
        return this;
    }

    // ── Resultado ──────────────────────────────────────────────────────────

    public boolean hasErrors() { return !errors.isEmpty(); }

    /** Muestra los errores en un diálogo de aviso y devuelve true si hay errores. */
    public boolean showIfErrors() {
        if (!errors.isEmpty()) {
            DialogUtils.mostrarAviso("Por favor, corrige los siguientes errores:\n\n"
                    + String.join("\n", errors));
            return true;
        }
        return false;
    }
}
