package com.mycompany.ciudadanosv.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Protección CSRF: cada sesión tiene un token secreto que va oculto en los formularios.
 * Si un POST no trae el token correcto, se rechaza (evita envíos desde otros sitios).
 *
 * En la JSP:  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
 */
public final class Csrf {

    public static final String ATRIBUTO = "csrf";

    private Csrf() {
    }

    /** Crea el token de la sesión si aún no existe (llamar en el GET que muestra el formulario). */
    public static void asegurarToken(HttpServletRequest request) {
        HttpSession sesion = request.getSession();
        if (sesion.getAttribute(ATRIBUTO) == null) {
            sesion.setAttribute(ATRIBUTO, Seguridad.generarSalt());
        }
    }

    /** true si el POST trae el mismo token que la sesión. */
    public static boolean esValido(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return false;
        }
        Object esperado = sesion.getAttribute(ATRIBUTO);
        String recibido = request.getParameter(ATRIBUTO);
        return esperado != null && recibido != null
                && MessageDigest.isEqual(esperado.toString().getBytes(StandardCharsets.UTF_8),
                        recibido.getBytes(StandardCharsets.UTF_8));
    }
}
