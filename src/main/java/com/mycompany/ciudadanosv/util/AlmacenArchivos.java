package com.mycompany.ciudadanosv.util;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Guarda y lee los archivos que suben los ciudadanos.
 *
 * Los archivos van FUERA de la aplicación (si van dentro del .war se borran al redesplegar):
 *   por defecto  C:\Users\<usuario>\CiudadanoSV-archivos\EXP-2026-00005\<uuid>.pdf
 *   se puede cambiar con el context-param "ciudadanosv.rutaArchivos" del web.xml.
 *
 * En la base de datos solo se guarda la ruta relativa: EXP-2026-00005/<uuid>.pdf
 */
public final class AlmacenArchivos {

    private AlmacenArchivos() {
    }

    /** Carpeta raíz donde se guardan los archivos. */
    public static Path raiz(ServletContext contexto) {
        String configurada = contexto.getInitParameter("ciudadanosv.rutaArchivos");
        Path ruta = (configurada == null || configurada.isBlank())
                ? Paths.get(System.getProperty("user.home"), "CiudadanoSV-archivos")
                : Paths.get(configurada);
        return ruta.toAbsolutePath().normalize();
    }

    /**
     * Tipo real del archivo según sus primeros bytes ("firma"), sin confiar en la extensión.
     * Devuelve application/pdf, image/jpeg, image/png o null si no es ninguno.
     */
    public static String detectarTipo(byte[] b) {
        if (b == null || b.length < 4) {
            return null;
        }
        if (b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') {
            return "application/pdf";
        }
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        return null;
    }

    /** "application/pdf" -> "PDF" (como se escribe en requisito.formatos). */
    public static String formatoCorto(String mime) {
        return switch (mime) {
            case "application/pdf" -> "PDF";
            case "image/jpeg" -> "JPG";
            case "image/png" -> "PNG";
            default -> "";
        };
    }

    /** Guarda el archivo con nombre aleatorio en la carpeta del folio. Devuelve la ruta relativa. */
    public static String guardar(Path raiz, String folio, String mime, byte[] contenido) throws IOException {
        String extension = switch (mime) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            default -> ".png";
        };
        Path carpeta = raiz.resolve(folio);
        Files.createDirectories(carpeta);
        String nombre = UUID.randomUUID() + extension;
        Files.write(carpeta.resolve(nombre), contenido);
        return folio + "/" + nombre;
    }

    /** Ruta absoluta segura de un archivo guardado (null si intenta salirse de la raíz). */
    public static Path resolver(Path raiz, String rutaRelativa) {
        Path ruta = raiz.resolve(rutaRelativa).normalize();
        return ruta.startsWith(raiz) ? ruta : null;
    }

    /** Borra un archivo guardado (se usa si la base de datos falla después de escribirlo). */
    public static void borrarSilencioso(Path raiz, String rutaRelativa) {
        try {
            Path ruta = resolver(raiz, rutaRelativa);
            if (ruta != null) {
                Files.deleteIfExists(ruta);
            }
        } catch (IOException ignorado) {
            // no interrumpe el flujo principal
        }
    }

    /** SHA-256 en hexadecimal: permite comprobar después que el archivo no fue alterado. */
    public static String sha256(byte[] contenido) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Deja solo caracteres seguros en el nombre original (para mostrarlo y descargarlo). */
    public static String limpiarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "archivo";
        }
        String base = Paths.get(nombre).getFileName().toString();      // quita rutas tipo C:\...
        base = base.replaceAll("[^A-Za-z0-9._ -]", "_");
        byte[] bytes = base.getBytes(StandardCharsets.UTF_8);
        return bytes.length > 150 ? base.substring(0, 150) : base;
    }
}
