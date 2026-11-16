package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Municipio;
import com.mycompany.ciudadanosv.models.MunicipioDAO;
import com.mycompany.ciudadanosv.models.SolicitudDAO;
import com.mycompany.ciudadanosv.models.SolicitudPartida;
import com.mycompany.ciudadanosv.models.Tramite;
import com.mycompany.ciudadanosv.models.TramiteDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import com.mycompany.ciudadanosv.util.Fechas;
import java.util.List;

/**
 * Solicitud de certificación de partida de nacimiento.
 * URL protegida: /app/solicitud/partida
 *   GET  -> muestra el formulario
 *   POST -> valida, guarda y redirige a /app/solicitud/enviada?folio=...
 */
public class SolicitudPartidaController extends HttpServlet {

    public static final String CODIGO_TRAMITE = "RNPN-PN-01";
    private static final String VISTA = "/WEB-INF/vistas/ciudadano/solicitud_partida.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Csrf.asegurarToken(request);
        if (cargarDatosFormulario(request, response)) {
            request.getRequestDispatcher(VISTA).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");

        Tramite tramite;
        List<Municipio> municipios;
        try {
            tramite = new TramiteDAO().buscarPorCodigo(CODIGO_TRAMITE);
            municipios = new MunicipioDAO().listar();
        } catch (SQLException ex) {
            log("Error cargando datos del formulario de partida", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (tramite == null || !tramite.isDisponible()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // ----- Validación -----
        String nombre = limpiar(request.getParameter("nombreInscrito"));
        String relacion = limpiar(request.getParameter("relacion"));
        String fechaTexto = limpiar(request.getParameter("fechaNacimiento"));
        String municipioTexto = limpiar(request.getParameter("idMunicipio"));
        String uso = limpiar(request.getParameter("uso"));

        String error = null;
        LocalDate fecha = null;
        int idMunicipio = 0;

        if (!SolicitudPartida.esValorValido(SolicitudPartida.RELACIONES, relacion)) {
            error = "Selecciona tu relación con la persona inscrita.";
        } else if (nombre.length() < 5 || nombre.length() > 120) {
            error = "Escribe el nombre completo.";
        } else {
            fecha = Fechas.leer(fechaTexto);
            if (fecha == null) {
                error = "DD/MM/AAAA";
            } else if (fecha.isAfter(LocalDate.now()) || fecha.isBefore(LocalDate.of(1900, 1, 1))) {
                error = "Formato o fecha invalida";
            }
        }
        if (error == null) {
            try {
                idMunicipio = Integer.parseInt(municipioTexto);
            } catch (NumberFormatException ex) {
                idMunicipio = 0;
            }
            final int buscado = idMunicipio;
            if (municipios.stream().noneMatch(m -> m.getIdMunicipio() == buscado)) {
                error = "Selecciona el municipio de inscripción.";
            } else if (!SolicitudPartida.esValorValido(SolicitudPartida.USOS, uso)) {
                error = "Selecciona el uso de la certificación.";
            } else if (request.getParameter("declaracion") == null) {
                error = "Debes confirmar la declaración de veracidad de los datos.";
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("tramite", tramite);
            request.setAttribute("municipios", municipios);
            request.setAttribute("relaciones", SolicitudPartida.RELACIONES);
            request.setAttribute("usos", SolicitudPartida.USOS);
            request.setAttribute("hoy", LocalDate.now().toString());
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        // ----- Guardar -----
        SolicitudPartida datos = new SolicitudPartida();
        datos.setNombreInscrito(nombre);
        datos.setRelacion(relacion);
        datos.setFechaNacimiento(fecha);
        datos.setIdMunicipioInscripcion(idMunicipio);
        datos.setUso(uso);
        try {
            String folio = new SolicitudDAO().crearPartida(usuario.getIdUsuario(), tramite.getIdTramite(),
                    datos, request.getRemoteAddr());
            // Post/Redirect/Get: al recargar la página no se envía otra solicitud
            response.sendRedirect(request.getContextPath() + "/app/solicitud/enviada?folio="
                    + URLEncoder.encode(folio, StandardCharsets.UTF_8));
        } catch (SQLException ex) {
            log("Error guardando solicitud de partida", ex);
            request.setAttribute("error", "No se pudo guardar la solicitud. Intenta de nuevo en unos minutos.");
            request.setAttribute("tramite", tramite);
            request.setAttribute("municipios", municipios);
            request.setAttribute("relaciones", SolicitudPartida.RELACIONES);
            request.setAttribute("usos", SolicitudPartida.USOS);
            request.setAttribute("hoy", LocalDate.now().toString());
            request.getRequestDispatcher(VISTA).forward(request, response);
        }
    }

    /** Carga trámite, municipios y opciones. Devuelve false si ya respondió con un error. */
    private boolean cargarDatosFormulario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Tramite tramite = new TramiteDAO().buscarPorCodigo(CODIGO_TRAMITE);
            if (tramite == null || !tramite.isDisponible()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
            request.setAttribute("tramite", tramite);
            request.setAttribute("municipios", new MunicipioDAO().listar());
        } catch (SQLException ex) {
            log("Error cargando formulario de partida", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
        request.setAttribute("relaciones", SolicitudPartida.RELACIONES);
        request.setAttribute("usos", SolicitudPartida.USOS);
        request.setAttribute("hoy", LocalDate.now().toString());
        return true;
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    @Override
    public String getServletInfo() {
        return "Solicitud de partida de nacimiento";
    }
}
