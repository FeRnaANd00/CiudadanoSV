package com.mycompany.ciudadanosv.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Fechas en el formato que usa el portal: DD/MM/AAAA. */
public final class Fechas {

    /** STRICT + "uuuu": rechaza fechas imposibles como 31/02/2020. */
    private static final DateTimeFormatter DD_MM_AAAA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Fechas() {
    }

    /** Convierte "18/03/1991" en LocalDate. Devuelve null si el texto no es una fecha válida. */
    public static LocalDate leer(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), DD_MM_AAAA);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /** LocalDate -> "18/03/1991". */
    public static String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(DD_MM_AAAA);
    }

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu • hh:mm a", java.util.Locale.of("es", "SV"));

    /** LocalDateTime -> "14/11/2026 • 09:15 a. m." */
    public static String formatearFechaHora(java.time.LocalDateTime fechaHora) {
        return fechaHora == null ? "" : fechaHora.format(FECHA_HORA);
    }
}
