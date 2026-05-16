package com.javafx.util;

public class NavigationContext {

    private static Long filtroPublicacion = null;

    public static void setFiltroPublicacion(Long id) {
        filtroPublicacion = id;
    }

    public static Long consumeFiltroPublicacion() {
        Long v = filtroPublicacion;
        filtroPublicacion = null;
        return v;
    }
}
