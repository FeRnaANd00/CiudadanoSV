package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Cita;
import com.mycompany.ciudadanosv.models.CitaDAO;
import com.mycompany.ciudadanosv.models.ReglaNegocioException;
import com.mycompany.ciudadanosv.models.Solicitud;
import com.mycompany.ciudadanosv.models.SolicitudAdminDAO;
import com.mycompany.ciudadanosv.models.SolicitudDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import com.mycompany.ciudadanosv.util.Fechas;
import com.mycompany.ciudadanosv.util.Parametros;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Ficha de una solicitud para el funcionario: datos del ciudadano, documentos (cotejo),
 * historial completo, cambio de estado, observaciones internas y cita biométrica.
 * URL protegida: /admin/solicitud?folio=EXP-2026-00012[&fecha=DD/MM/AAAA]
 *   POST accion=estado      estado=..&comentario=..
 *   POST accion=interna     texto=..
 *   POST accion=asignarme
 *   POST accion=cotejo      idDocumento=..&cotejo=VALIDO|RECHAZADO|PENDIENTE
 *   POST accion=cita        idCupo=..
 */
public class AdminSolicitudController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Csrf.asegurarToken(request);
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        String folio = Parametros.texto(request, "folio");
        if (folio.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/solicitudes");
            return;
        }
        try {
            SolicitudAdminDAO dao = new SolicitudAdminDAO();
            Solicitud s = dao.buscar(usuario, folio);
            if (s == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            SolicitudDAO datos = new SolicitudDAO();
            request.setAttribute("solicitud", s);
            request.setAttribute("historial", dao.listarHistorialCompleto(s.getIdSolicitud()));
            request.setAttribute("documentos", datos.listarDocumentos(s.getIdSolicitud()));
            request.setAttribute("partida", datos.buscarPartida(s.getIdSolicitud()));
            request.setAttribute("municipal", datos.buscarMunicipal(s.getIdSolicitud()));
            request.setAttribute("dui", datos.buscarDui(s.getIdSolicitud()));
            request.setAttribute("citas", dao.listarCitas(s.getIdSolicitud()));

            // Horarios para la cita biométrica (día elegido o el siguiente día hábil)
            if (s.isPuedeProgramarCita()) {
                LocalDate fecha = Fechas.leer(request.getParameter("fecha"));
                LocalDate manana = LocalDate.now().plusDays(1);
                if (fecha == null || fecha.isBefore(manana)) {
                    fecha = manana;
                }
                while (!CitaDAO.esDiaHabil(fecha)) {
                    fecha = fecha.plusDays(1);
                }
                request.setAttribute("fechaCita", Fechas.formatear(fecha));
                request.setAttribute("fechaCitaIso", fecha.toString());
                request.setAttribute("fechaMinIso", manana.toString());
                request.setAttribute("cupos", new CitaDAO().listarCupos(s.getIdSede(), s.getIdTramite(), fecha));
            }
        } catch (SQLException ex) {
            log("Error cargando la solicitud " + folio, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.getRequestDispatcher("/WEB-INF/vistas/admin/solicitud.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        String folio = Parametros.texto(request, "folio");
        String ip = request.getRemoteAddr();
        SolicitudAdminDAO dao = new SolicitudAdminDAO();
        String aviso = null;
        try {
            switch (Parametros.texto(request, "accion")) {
                case "estado" -> {
                    String estado = Parametros.texto(request, "estado");
                    dao.cambiarEstado(usuario, folio, estado, Parametros.textoONulo(request, "comentario"), ip);
                    aviso = "La solicitud pasó a \"" + Solicitud.textoEstado(estado) + "\" y se notificó al ciudadano.";
                }
                case "interna" -> {
                    dao.agregarObservacionInterna(usuario, folio, Parametros.textoONulo(request, "texto"), ip);
                    aviso = "Observación interna guardada (el ciudadano no la ve).";
                }
                case "asignarme" -> {
                    dao.asignarme(usuario, folio, ip);
                    aviso = "Ahora eres el revisor asignado.";
                }
                case "cotejo" -> {
                    dao.cotejarDocumento(usuario, folio, Parametros.entero(request, "idDocumento", 0),
                            Parametros.texto(request, "cotejo"), ip);
                    aviso = "Cotejo del documento actualizado.";
                }
                case "cita" -> {
                    Cita c = dao.programarCita(usuario, folio, Parametros.entero(request, "idCupo", 0), ip);
                    aviso = "Cita " + c.getCodigo() + " programada para el " + c.getFechaTexto() + " a las "
                            + c.getHoraTexto() + " (turno #" + c.getNumeroTurno() + "). Se notificó al ciudadano.";
                }
                default -> {
                    // acción desconocida: solo vuelve a la ficha
                }
            }
            if (aviso != null) {
                request.getSession().setAttribute("aviso", aviso);
            }
        } catch (ReglaNegocioException ex) {
            request.getSession().setAttribute("avisoError", ex.getMessage());
        } catch (SQLException ex) {
            log("Error actualizando la solicitud " + folio, ex);
            request.getSession().setAttribute("avisoError", "No se pudo guardar el cambio. Intenta de nuevo.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/solicitud?folio="
                + URLEncoder.encode(folio, StandardCharsets.UTF_8));
    }

    @Override
    public String getServletInfo() {
        return "Detalle y resolución de una solicitud (administración)";
    }
}
