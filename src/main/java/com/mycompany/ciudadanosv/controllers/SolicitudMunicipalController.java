package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Municipio;
import com.mycompany.ciudadanosv.models.MunicipioDAO;
import com.mycompany.ciudadanosv.models.Sede;
import com.mycompany.ciudadanosv.models.SedeDAO;
import com.mycompany.ciudadanosv.models.SolicitudDAO;
import com.mycompany.ciudadanosv.models.SolicitudMunicipal;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Trámites municipales: solvencia municipal y constancia de residencia (mismo formulario).
 * URL protegida: /app/solicitud/municipal?codigo=MUNI-01 | MUNI-02
 */
public class SolicitudMunicipalController extends HttpServlet {

    /** Trámites que atiende este formulario. */
    public static final List<String> CODIGOS = List.of("MUNI-01", "MUNI-02");
    private static final String VISTA = "/WEB-INF/vistas/ciudadano/solicitud_municipal.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Csrf.asegurarToken(request);
        try {
            if (cargarDatos(request, request.getParameter("codigo"))) {
                request.getRequestDispatcher(VISTA).forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            log("Error cargando formulario municipal", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        String codigo = request.getParameter("codigo");

        try {
            if (!cargarDatos(request, codigo)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Tramite tramite = (Tramite) request.getAttribute("tramite");
            @SuppressWarnings("unchecked")
            List<Municipio> municipios = (List<Municipio>) request.getAttribute("municipios");

            int idMunicipio = entero(request.getParameter("idMunicipio"));
            String domicilio = limpiar(request.getParameter("domicilio"));

            String error = null;
            if (municipios.stream().noneMatch(m -> m.getIdMunicipio() == idMunicipio)) {
                error = "Selecciona el municipio.";
            } else if (domicilio.length() < 10 || domicilio.length() > 200) {
                error = "Escribe tu dirección completa (colonia, calle, número).";
            } else if (request.getParameter("declaracion") == null) {
                error = "Debes confirmar la declaración de veracidad de los datos.";
            }
            if (error != null) {
                request.setAttribute("error", error);
                request.getRequestDispatcher(VISTA).forward(request, response);
                return;
            }

            SolicitudMunicipal datos = new SolicitudMunicipal();
            datos.setIdMunicipio(idMunicipio);
            datos.setDomicilio(domicilio);
            Sede alcaldia = new SedeDAO().buscarPorInstitucionYMunicipio("ALCALDIA", idMunicipio);

            String folio = new SolicitudDAO().crearMunicipal(usuario.getIdUsuario(), tramite,
                    alcaldia == null ? null : alcaldia.getIdSede(), datos, request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/app/solicitud/enviada?folio="
                    + URLEncoder.encode(folio, StandardCharsets.UTF_8));

        } catch (SQLException ex) {
            log("Error guardando solicitud municipal", ex);
            request.setAttribute("error", "No se pudo guardar la solicitud. Intenta de nuevo en unos minutos.");
            request.getRequestDispatcher(VISTA).forward(request, response);
        }
    }

    /**
     * Pone en la petición el trámite elegido, las dos opciones y los municipios.
     * Devuelve false si el código no es un trámite municipal disponible.
     */
    private boolean cargarDatos(HttpServletRequest request, String codigo) throws SQLException {
        if (codigo == null || !CODIGOS.contains(codigo)) {
            codigo = CODIGOS.get(0);
        }
        TramiteDAO tdao = new TramiteDAO();
        List<Tramite> opciones = new ArrayList<>();
        Tramite elegido = null;
        for (String c : CODIGOS) {
            Tramite t = tdao.buscarPorCodigo(c);
            if (t != null && t.isDisponible()) {
                opciones.add(t);
                if (c.equals(codigo)) {
                    elegido = t;
                }
            }
        }
        if (elegido == null) {
            return false;
        }
        request.setAttribute("tramite", elegido);
        request.setAttribute("opciones", opciones);
        request.setAttribute("municipios", new MunicipioDAO().listar());
        return true;
    }

    private static String limpiar(String v) {
        return v == null ? "" : v.trim();
    }

    private static int entero(String v) {
        try {
            return v == null ? 0 : Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public String getServletInfo() {
        return "Solicitud de trámites municipales";
    }
}
