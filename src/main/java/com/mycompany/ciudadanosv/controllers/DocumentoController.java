package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Documento;
import com.mycompany.ciudadanosv.models.SolicitudDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.AlmacenArchivos;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Muestra / descarga un documento adjunto.
 * URL protegida: /app/documento?id=5
 * Solo lo puede abrir el ciudadano dueño de la solicitud, un funcionario de la institución
 * del trámite o el super administrador.
 */
public class DocumentoController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Documento doc;
        try {
            doc = new SolicitudDAO().buscarDocumento(id);
        } catch (SQLException ex) {
            log("Error consultando documento " + id, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        // Mismo 404 si no existe o no es suyo: no revelamos qué documentos existen
        if (doc == null || !puedeVer(usuario, doc)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path archivo = AlmacenArchivos.resolver(AlmacenArchivos.raiz(getServletContext()), doc.getRutaArchivo());
        if (archivo == null || !Files.isRegularFile(archivo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String tipo = doc.getTipoMime() == null ? "application/octet-stream" : doc.getTipoMime();
        String nombre = URLEncoder.encode(doc.getNombreArchivo(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(tipo);
        response.setContentLengthLong(Files.size(archivo));
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + nombre);
        response.setHeader("X-Content-Type-Options", "nosniff");
        Files.copy(archivo, response.getOutputStream());
    }

    private static boolean puedeVer(Usuario usuario, Documento doc) {
        if (doc.getIdUsuarioDueno() == usuario.getIdUsuario() || usuario.isSuperAdmin()) {
            return true;
        }
        return usuario.isFuncionario() && usuario.getIdInstitucion() != null
                && usuario.getIdInstitucion() == doc.getIdInstitucion();
    }

    @Override
    public String getServletInfo() {
        return "Visor de documentos adjuntos";
    }
}
