package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.ArchivoSubido;
import com.mycompany.ciudadanosv.models.Municipio;
import com.mycompany.ciudadanosv.models.MunicipioDAO;
import com.mycompany.ciudadanosv.models.Requisito;
import com.mycompany.ciudadanosv.models.RequisitoDAO;
import com.mycompany.ciudadanosv.models.Sede;
import com.mycompany.ciudadanosv.models.SedeDAO;
import com.mycompany.ciudadanosv.models.SolicitudDAO;
import com.mycompany.ciudadanosv.models.SolicitudDui;
import com.mycompany.ciudadanosv.models.SolicitudPartida;
import com.mycompany.ciudadanosv.models.Tramite;
import com.mycompany.ciudadanosv.models.TramiteDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.AlmacenArchivos;
import com.mycompany.ciudadanosv.util.Csrf;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Renovación (RNPN-DUI-01) y reposición por deterioro (RNPN-DUI-02) de DUI, con carga de archivos.
 * URL protegida: /app/solicitud/dui?codigo=RNPN-DUI-01
 *
 * El formulario es multipart/form-data (límites en <multipart-config> del web.xml: 5 MB por archivo).
 * Por cada requisito con requiere_archivo = TRUE aparece un campo "archivo_<idRequisito>".
 */
public class SolicitudDuiController extends HttpServlet {

    public static final List<String> CODIGOS = List.of("RNPN-DUI-01", "RNPN-DUI-02");
    private static final String VISTA = "/WEB-INF/vistas/ciudadano/solicitud_dui.jsp";

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
            log("Error cargando formulario de DUI", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1) Leer el multipart. Si un archivo supera el límite del web.xml, Tomcat lanza IllegalStateException.
        String errorCarga = null;
        try {
            request.getParts();
        } catch (IllegalStateException ex) {
            errorCarga = "El archivo es demasiado grande. El máximo es 5 MB.";
        } catch (ServletException ex) {
            errorCarga = "No se pudo leer el formulario. Intenta de nuevo.";
        }
        if (errorCarga != null) {
            try {
                Csrf.asegurarToken(request);
                cargarDatos(request, null);
            } catch (SQLException ex) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            request.setAttribute("error", errorCarga);
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");

        try {
            if (!cargarDatos(request, request.getParameter("codigo"))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Tramite tramite = (Tramite) request.getAttribute("tramite");
            @SuppressWarnings("unchecked")
            List<Municipio> municipios = (List<Municipio>) request.getAttribute("municipios");
            @SuppressWarnings("unchecked")
            List<Sede> sedes = (List<Sede>) request.getAttribute("sedes");
            @SuppressWarnings("unchecked")
            List<Requisito> requisitos = (List<Requisito>) request.getAttribute("requisitosArchivo");

            // 2) Validar campos
            String motivo = limpiar(request.getParameter("motivo"));
            int idMunicipio = entero(request.getParameter("idMunicipio"));
            int idSede = entero(request.getParameter("idSede"));

            String error = null;
            if (!SolicitudPartida.esValorValido(SolicitudDui.MOTIVOS, motivo)) {
                error = "Selecciona el motivo del trámite.";
            } else if (municipios.stream().noneMatch(m -> m.getIdMunicipio() == idMunicipio)) {
                error = "Selecciona tu departamento y municipio de residencia.";
            } else if (sedes.stream().noneMatch(s -> s.getIdSede() == idSede)) {
                error = "Selecciona la sede donde quieres ser atendido.";
            }

            // 3) Validar archivos (tipo real por contenido, formato permitido y tamaño)
            List<ArchivoSubido> archivos = new ArrayList<>();
            if (error == null) {
                for (Requisito r : requisitos) {
                    Part parte = request.getPart("archivo_" + r.getIdRequisito());
                    if (parte == null || parte.getSize() == 0) {
                        if (r.isObligatorio()) {
                            error = "Adjunta el documento: " + r.getNombre() + ".";
                            break;
                        }
                        continue;
                    }
                    long maximo = (long) r.getTamanoMaxMb() * 1024 * 1024;
                    if (parte.getSize() > maximo) {
                        error = r.getNombre() + ": el archivo supera " + r.getTamanoMaxMb() + " MB.";
                        break;
                    }
                    byte[] contenido;
                    try (InputStream in = parte.getInputStream()) {
                        contenido = in.readAllBytes();
                    }
                    String mime = AlmacenArchivos.detectarTipo(contenido);
                    Set<String> permitidos = formatos(r.getFormatos());
                    if (mime == null || !permitidos.contains(AlmacenArchivos.formatoCorto(mime))) {
                        error = r.getNombre() + ": solo se aceptan archivos " + String.join(", ", permitidos) + ".";
                        break;
                    }
                    archivos.add(new ArchivoSubido(r.getIdRequisito(),
                            AlmacenArchivos.limpiarNombre(parte.getSubmittedFileName()), mime, contenido));
                }
            }
            if (error == null && request.getParameter("declaracion") == null) {
                error = "Debes aceptar la declaración jurada.";
            }
            if (error != null) {
                request.setAttribute("error", error);
                request.getRequestDispatcher(VISTA).forward(request, response);
                return;
            }

            // 4) Guardar
            SolicitudDui datos = new SolicitudDui();
            datos.setMotivo(motivo);
            datos.setIdMunicipioRes(idMunicipio);
            String folio = new SolicitudDAO().crearDui(usuario.getIdUsuario(), tramite, idSede, datos,
                    archivos, AlmacenArchivos.raiz(getServletContext()), request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/app/solicitud/enviada?folio="
                    + URLEncoder.encode(folio, StandardCharsets.UTF_8));

        } catch (SQLException | IOException ex) {
            log("Error guardando solicitud de DUI", ex);
            request.setAttribute("error", "No se pudo guardar la solicitud. Intenta de nuevo en unos minutos.");
            request.getRequestDispatcher(VISTA).forward(request, response);
        }
    }

    /** Trámite, municipios, Duicentros y requisitos. False si el código no es un trámite de DUI disponible. */
    private boolean cargarDatos(HttpServletRequest request, String codigo) throws SQLException {
        if (codigo == null || !CODIGOS.contains(codigo)) {
            codigo = CODIGOS.get(0);
        }
        Tramite tramite = new TramiteDAO().buscarPorCodigo(codigo);
        if (tramite == null || !tramite.isDisponible()) {
            return false;
        }
        List<Requisito> requisitos = new RequisitoDAO().listarPorTramite(tramite.getIdTramite());
        List<Requisito> conArchivo = new ArrayList<>();
        for (Requisito r : requisitos) {
            if (r.isRequiereArchivo()) {
                conArchivo.add(r);
            }
        }
        request.setAttribute("tramite", tramite);
        request.setAttribute("requisitos", requisitos);
        request.setAttribute("requisitosArchivo", conArchivo);
        request.setAttribute("municipios", new MunicipioDAO().listar());
        request.setAttribute("sedes", new SedeDAO().listarPorInstitucion("RNPN"));
        request.setAttribute("motivos", SolicitudDui.MOTIVOS);
        request.setAttribute("motivoSugerido", "RNPN-DUI-02".equals(codigo) ? "DETERIORO" : "VENCIMIENTO");
        return true;
    }

    /** "PDF,JPG,PNG" -> {PDF, JPG, PNG} */
    private static Set<String> formatos(String texto) {
        Set<String> set = new LinkedHashSet<>();
        if (texto == null || texto.isBlank()) {
            set.addAll(Arrays.asList("PDF", "JPG", "PNG"));
        } else {
            for (String f : texto.split(",")) {
                String limpio = f.trim().toUpperCase();
                set.add("JPEG".equals(limpio) ? "JPG" : limpio);
            }
        }
        return set;
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
        return "Solicitud de renovación o reposición de DUI";
    }
}
