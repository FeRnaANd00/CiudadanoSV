package com.mycompany.ciudadanosv.models;

/**
 * Error de una regla del sistema (ej. "el cupo ya está lleno").
 * El mensaje se muestra tal cual al usuario, a diferencia de SQLException.
 */
public class ReglaNegocioException extends Exception {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
