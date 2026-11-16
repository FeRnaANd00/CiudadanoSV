package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Multa;
import com.mycompany.ciudadanosv.models.MultaDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Consulta de multas e infracciones del VMT (solo lectura).
 * URL protegida: /app/multas?tipo=dui            -> multas de MI licencia (DUI del usuario en sesión)
 *                /app/multas?tipo=placa&placa=P123-456 -> multas de un vehículo
 *                &estado=PENDIENTE|PAGADA|EN_APELACION  -> filtro opcional
 *
 * Por privacidad, "por licencia" solo consulta el DUI propio (no se puede escribir otro DUI).
 */
public class MultasController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        String tipo = "placa".equals(request.getParameter("tipo")) ? "placa" : "dui";
        String placaTexto = request.getParameter("placa") == null ? "" : request.getParameter("placa").trim();
        String estado = request.getParameter("estado") == null ? "" : request.getParameter("estado");

        request.setAttribute("tipo", tipo);
        request.setAttribute("placa", placaTexto);

        List<Multa> multas = null;
        try {
            if ("dui".equals(tipo)) {
                multas = new MultaDAO().listarPorDui(usuario.getDui());
            } else if (!placaTexto.isEmpty()) {
                String normalizada = placaTexto.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
                if (!normalizada.matches("^[A-Z]{1,3}[0-9]{3,6}$")) {
                    request.setAttribute("error", "La placa no es válida. Ejemplo: P123-456.");
                } else {
                    multas = new MultaDAO().listarPorPlaca(normalizada);
                }
            }
        } catch (SQLException ex) {
            log("Error consultando multas", ex);
            request.setAttribute("error", "No se pudo consultar el sistema del VMT. Intenta de nuevo.");
        }

        if (multas != null) {
            int pendientes = 0, pagadas = 0, apelacion = 0;
            BigDecimal montoPendiente = BigDecimal.ZERO;
            List<Multa> filtradas = new ArrayList<>();
            for (Multa m : multas) {
                switch (m.getEstado()) {
                    case "PENDIENTE" -> {
                        pendientes++;
                        montoPendiente = montoPendiente.add(m.getMonto());
                    }
                    case "PAGADA" -> pagadas++;
                    case "EN_APELACION" -> apelacion++;
                    default -> { }
                }
                if (estado.isEmpty() || estado.equals(m.getEstado())) {
                    filtradas.add(m);
                }
            }
            request.setAttribute("consultado", true);
            request.setAttribute("multas", filtradas);
            request.setAttribute("total", multas.size());
            request.setAttribute("pendientes", pendientes);
            request.setAttribute("pagadas", pagadas);
            request.setAttribute("apelacion", apelacion);
            request.setAttribute("montoPendiente", montoPendiente);
            request.setAttribute("solvente", pendientes == 0);
        }
        request.getRequestDispatcher("/WEB-INF/vistas/ciudadano/multas.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Consulta de multas del VMT";
    }
}
