package com.mycompany.ciudadanosv.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Hash de contraseñas compatible con la base de datos:
 *   clave_hash = SHA2(CONCAT(clave_salt, clave), 256)
 */
public final class Seguridad {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Seguridad() {
    }

    /** Salt aleatorio de 16 bytes en hexadecimal (32 caracteres). */
    public static String generarSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** SHA-256 de (salt + clave) en hexadecimal minúscula (64 caracteres). */
    public static String hashClave(String salt, String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((salt + clave).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    /** Compara la clave escrita con el hash guardado (en tiempo constante). */
    public static boolean verificarClave(String clave, String salt, String hashGuardado) {
        if (clave == null || salt == null || hashGuardado == null) {
            return false;
        }
        byte[] calculado = hashClave(salt, clave).getBytes(StandardCharsets.UTF_8);
        byte[] guardado = hashGuardado.toLowerCase().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculado, guardado);
    }

    /**
     * true si "destino" es una ruta interna protegida a la que se puede volver
     * después del login (ej. /app/tramite?codigo=RNPN-DUI-01).
     * Evita redirigir a sitios externos (open redirect).
     */
    public static boolean destinoSeguro(String destino) {
        return destino != null
                && destino.length() <= 300
                && destino.matches("^/(app|admin)/[A-Za-z0-9/_-]*(\\?[A-Za-z0-9=&%._-]*)?$");
    }

    /** Reglas del formulario de registro: 8+ caracteres, número, mayúscula y símbolo. */
    public static boolean claveEsSegura(String clave) {
        return clave != null
                && clave.length() >= 8
                && clave.matches(".*\\d.*")
                && clave.matches(".*[A-Z].*")
                && clave.matches(".*[^A-Za-z0-9].*");
    }
}
