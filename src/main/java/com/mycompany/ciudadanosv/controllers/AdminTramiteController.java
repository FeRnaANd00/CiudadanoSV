package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.InstitucionDAO;
import com.mycompany.ciudadanosv.models.ReglaNegocioException;
import com.mycompany.ciudadanosv.models.Requisito;
import com.mycompany.ciudadanosv.models.RequisitoDAO;
import com.mycompany.ciudadanosv.models.Tramite;
import com.mycompany.ciudadanosv.models.TramiteAdminDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import com.mycompany.ciudadanosv.util.Parametros;
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
 * Alta y edición de un trámite con sus requisitos (solo SUPER_ADMIN).
 * URL protegida: /admin/tramite?id=3   (sin id: trámite nuevo)
 *   POST accion=guardar | requisito (idRequisito=0 para nuevo) | eliminarRequisito | copiar (origen)
 *        | eliminar (solo trámites sin uso y sin formulario programado)
 */
public class AdminTramiteController extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/admin/tramite.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        if (!usuario.isSuperAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Csrf.asegurarToken(request);
        try {
            Integer id = Parametros.entero(request, "id");
            Tramite t;
            if (id == null) {
                t = new Tramite();
                t.setArancel(BigDecimal.ZERO);
                t.setModalidad("DIGITAL");
                t.setEstado("PROXIMAMENTE");
            } else {
                t = new TramiteAdminDAO().buscar(id);
                if (t == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("requisitos", new RequisitoDAO().listarPorTramite(id));
                request.setAttribute("usos", new TramiteAdminDAO().contarUsos(id));
                request.setAttribute("tieneFormulario", TramiteController.tieneFormulario(t.getCodigo()));
            }
            request.setAttribute("tramite", t);
            prepararListas(request);
        } catch (SQLException ex) {
            log("Error cargando el trámite", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Csrf.esValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        if (!usuario.isSuperAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String ip = request.getRemoteAddr();
        TramiteAdminDAO dao = new TramiteAdminDAO();
        String accion = Parametros.texto(request, "accion");
        int id = Parametros.entero(request, "id", 0);

        if ("guardar".equals(accion)) {
            Tramite t = leerTramite(request, id);
            try {
                id = dao.guardar(usuario, t, ip);
                request.getSession().setAttribute("aviso", "Trámite " + t.getCodigo() + " guardado.");
                response.sendRedirect(request.getContextPath() + "/admin/tramite?id=" + id);
            } catch (ReglaNegocioException ex) {
                // Se vuelve a mostrar el formulario con lo que escribió
                request.setAttribute("error", ex.getMessage());
                request.setAttribute("tramite", t);
                try {
                    prepararListas(request);
                    if (id > 0) {
                        request.setAttribute("requisitos", new RequisitoDAO().listarPorTramite(id));
                    }
                } catch (SQLException e) {
                    log("Error cargando listas", e);
                }
                request.getRequestDispatcher(VISTA).forward(request, response);
            } catch (SQLException ex) {
                log("Error guardando trámite", ex);
                request.getSession().setAttribute("avisoError", "No se pudo guardar el trámite. Intenta de nuevo.");
                response.sendRedirect(request.getContextPath() + (id > 0 ? "/admin/tramite?id=" + id : "/admin/tramites"));
            }
            return;
        }

        try {
            switch (accion) {
                case "requisito" -> {
                    Requisito r = new Requisito();
                    r.setIdTramite(id);
                    r.setIdRequisito(Parametros.entero(request, "idRequisito", 0));
                    r.setNombre(Parametros.texto(request, "nombre"));
                    r.setDescripcion(Parametros.textoONulo(request, "descripcion"));
                    r.setObligatorio(Parametros.marcado(request, "obligatorio"));
                    r.setRequiereArchivo(Parametros.marcado(request, "requiereArchivo"));
                    String[] formatos = request.getParameterValues("formatos");
                    r.setFormatos(formatos == null ? "" : String.join(",", formatos));
                    r.setTamanoMaxMb(Parametros.entero(request, "tamanoMaxMb", 5));
                    r.setTipoValidacion(Parametros.texto(request, "tipoValidacion"));
                    dao.guardarRequisito(usuario, r, ip);
                    request.getSession().setAttribute("aviso", "Requisito guardado.");
                }
                case "eliminar" -> {
                    Tramite t = dao.buscar(id);
                    dao.eliminar(usuario, id, t != null && TramiteController.tieneFormulario(t.getCodigo()), ip);
                    request.getSession().setAttribute("aviso", "Trámite eliminado del catálogo.");
                    response.sendRedirect(request.getContextPath() + "/admin/tramites");
                    return;
                }
                case "eliminarRequisito" -> {
                    dao.eliminarRequisito(usuario, id, Parametros.entero(request, "idRequisito", 0), ip);
                    request.getSession().setAttribute("aviso", "Requisito eliminado.");
                }
                case "copiar" -> {
                    int n = dao.copiarRequisitos(usuario, id, Parametros.entero(request, "origen", 0), ip);
                    request.getSession().setAttribute("aviso", n + " requisitos copiados.");
                }
                default -> {
                    // acción desconocida
                }
            }
        } catch (ReglaNegocioException ex) {
            request.getSession().setAttribute("avisoError", ex.getMessage());
        } catch (SQLException ex) {
            log("Error en requisitos", ex);
            request.getSession().setAttribute("avisoError", "No se pudo guardar el cambio. Intenta de nuevo.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/tramite?id=" + id + "#requisitos");
    }

    private Tramite leerTramite(HttpServletRequest request, int id) {
        Tramite t = new Tramite();
        t.setIdTramite(id);
        t.setCodigo(Parametros.texto(request, "codigo").toUpperCase(Locale.ROOT));
        t.setNombre(Parametros.texto(request, "nombre"));
        t.setDescripcion(Parametros.textoONulo(request, "descripcion"));
        try {
            t.setArancel(new BigDecimal(Parametros.texto(request, "arancel").replace(",", ".")));
        } catch (NumberFormatException ex) {
            t.setArancel(null);
        }
        t.setPlazoHoras(Parametros.entero(request, "plazoHoras"));
        t.setModalidad(Parametros.texto(request, "modalidad"));
        t.setEstado(Parametros.texto(request, "estado"));
        t.setIdArea(Parametros.entero(request, "area", 0));
        t.setIdInstitucion(Parametros.entero(request, "institucion", 0));
        t.setRequiereCita(Parametros.marcado(request, "requiereCita"));
        t.setRequiereBiometria(Parametros.marcado(request, "requiereBiometria"));
        return t;
    }

    private void prepararListas(HttpServletRequest request) throws SQLException {
        TramiteAdminDAO dao = new TramiteAdminDAO();
        request.setAttribute("areas", dao.listarAreas());
        request.setAttribute("instituciones", new InstitucionDAO().listar());
        // Trámites con requisitos, para "copiar requisitos de..."
        List<Tramite> conRequisitos = new ArrayList<>();
        for (Tramite t : dao.listar(null, null, null)) {
            if (t.getNumRequisitos() > 0) {
                conRequisitos.add(t);
            }
        }
        request.setAttribute("tramitesConRequisitos", conRequisitos);
    }

    @Override
    public String getServletInfo() {
        return "Edición de un trámite y sus requisitos";
    }
}
