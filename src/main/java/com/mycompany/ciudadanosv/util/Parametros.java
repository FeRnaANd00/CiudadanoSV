package com.mycompany.ciudadanosv.util;

import jakarta.servlet.http.HttpServletRequest;

/** Lectura segura de parámetros de la petición. */
public final class Parametros {

    private Parametros() {
    }

    /** Texto sin espacios a los lados ("" si no viene). */
    public static String texto(HttpServletRequest request, String nombre) {
        String v = request.getParameter(nombre);
        return v == null ? "" : v.trim();
    }

    /** Texto o null si viene vacío. */
    public static String textoONulo(HttpServletRequest request, String nombre) {
        String v = texto(request, nombre);
        return v.isEmpty() ? null : v;
    }

    /** Entero o null si no viene o no es un número. */
    public static Integer entero(HttpServletRequest request, String nombre) {
        try {
            return Integer.valueOf(texto(request, nombre));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Entero con valor por defecto. */
    public static int entero(HttpServletRequest request, String nombre, int porDefecto) {
        Integer v = entero(request, nombre);
        return v == null ? porDefecto : v;
    }

    /** Casilla marcada (checkbox). */
    public static boolean marcado(HttpServletRequest request, String nombre) {
        return request.getParameter(nombre) != null;
    }
}
