package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.CuentaUsuario;
import com.mycompany.ciudadanosv.models.InstitucionDAO;
import com.mycompany.ciudadanosv.models.ReglaNegocioException;
import com.mycompany.ciudadanosv.models.UsuarioAdminDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.util.Csrf;
import com.mycompany.ciudadanosv.util.Parametros;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Ficha de una cuenta y alta de funcionarios (solo SUPER_ADMIN).
 * URL protegida: /admin/usuario?id=2   (sin id: nuevo funcionario)
 *   POST accion=guardar (crea o actualiza un funcionario)
 *   POST accion=suspender | reactivar
 *   POST accion=clave (claveTemporal)
 *   POST accion=eliminar (solo cuentas sin uso)
 */
public class AdminUsuarioController extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/admin/usuario.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario admin = (Usuario) request.getSession().getAttribute("usuario");
        if (!admin.isSuperAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Csrf.asegurarToken(request);
        try {
            Integer id = Parametros.entero(request, "id");
            CuentaUsuario c;
            if (id == null) {
                c = new CuentaUsuario();
                c.setRol("REVISOR");
            } else {
                c = new UsuarioAdminDAO().buscar(id);
                if (c == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("usos", new UsuarioAdminDAO().contarUsos(id));
            }
            request.setAttribute("cuenta", c);
            prepararListas(request);
        } catch (SQLException ex) {
            log("Error cargando la cuenta", ex);
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
        Usuario admin = (Usuario) request.getSession().getAttribute("usuario");
        if (!admin.isSuperAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String ip = request.getRemoteAddr();
        UsuarioAdminDAO dao = new UsuarioAdminDAO();
        int id = Parametros.entero(request, "id", 0);
        String accion = Parametros.texto(request, "accion");

        if ("guardar".equals(accion)) {
            CuentaUsuario c = leerCuenta(request, id);
            try {
                if (id == 0) {
                    id = dao.crearFuncionario(admin, c, request.getParameter("clave"), ip);
                    request.getSession().setAttribute("aviso", "Funcionario creado. Comparte con él su contraseña inicial.");
                } else {
                    dao.actualizarFuncionario(admin, c, ip);
                    request.getSession().setAttribute("aviso", "Datos del funcionario actualizados. Los cambios aplican en su siguiente página.");
                }
                response.sendRedirect(request.getContextPath() + "/admin/usuario?id=" + id);
            } catch (ReglaNegocioException ex) {
                request.setAttribute("error", ex.getMessage());
                request.setAttribute("cuenta", c);
                try {
                    prepararListas(request);
                } catch (SQLException e) {
                    log("Error cargando listas", e);
                }
                request.getRequestDispatcher(VISTA).forward(request, response);
            } catch (SQLException ex) {
                log("Error guardando funcionario", ex);
                request.getSession().setAttribute("avisoError", "No se pudo guardar la cuenta. Intenta de nuevo.");
                response.sendRedirect(request.getContextPath() + (id > 0 ? "/admin/usuario?id=" + id : "/admin/usuarios"));
            }
            return;
        }

        try {
            switch (accion) {
                case "suspender", "reactivar" -> {
                    boolean reactivar = "reactivar".equals(accion);
                    dao.cambiarEstado(admin, id, reactivar ? "ACTIVO" : "SUSPENDIDO", ip);
                    request.getSession().setAttribute("aviso", reactivar
                            ? "Cuenta reactivada." : "Cuenta suspendida: ya no puede iniciar sesión y su sesión abierta se cierra.");
                }
                case "eliminar" -> {
                    dao.eliminar(admin, id, ip);
                    request.getSession().setAttribute("aviso", "Cuenta eliminada.");
                    response.sendRedirect(request.getContextPath() + "/admin/usuarios");
                    return;
                }
                case "clave" -> {
                    dao.restablecerClave(admin, id, request.getParameter("claveTemporal"), ip);
                    request.getSession().setAttribute("aviso", "Contraseña temporal asignada. Compártela con el usuario por un medio seguro.");
                }
                default -> {
                    // acción desconocida
                }
            }
        } catch (ReglaNegocioException ex) {
            request.getSession().setAttribute("avisoError", ex.getMessage());
        } catch (SQLException ex) {
            log("Error actualizando cuenta", ex);
            request.getSession().setAttribute("avisoError", "No se pudo guardar el cambio. Intenta de nuevo.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/usuario?id=" + id);
    }

    private CuentaUsuario leerCuenta(HttpServletRequest request, int id) {
        CuentaUsuario c = new CuentaUsuario();
        c.setIdUsuario(id);
        c.setRol(Parametros.texto(request, "rol"));
        c.setDui(Parametros.texto(request, "dui"));
        c.setNombreCompleto(Parametros.texto(request, "nombre"));
        c.setCorreo(Parametros.texto(request, "correo").toLowerCase(Locale.ROOT));
        c.setTelefono(Parametros.textoONulo(request, "telefono"));
        c.setIdInstitucion(Parametros.entero(request, "institucion"));
        c.setIdSede(Parametros.entero(request, "sede"));
        c.setCargo(Parametros.textoONulo(request, "cargo"));
        c.setEstado("ACTIVO");
        return c;
    }

    private void prepararListas(HttpServletRequest request) throws SQLException {
        request.setAttribute("instituciones", new InstitucionDAO().listar());
        request.setAttribute("sedes", new UsuarioAdminDAO().listarSedes());
    }

    @Override
    public String getServletInfo() {
        return "Ficha de cuenta y alta de funcionarios";
    }
}
