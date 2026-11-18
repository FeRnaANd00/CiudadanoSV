package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.DistritoDAO;
import com.mycompany.ciudadanosv.models.MunicipioDAO;
import com.mycompany.ciudadanosv.models.Perfil;
import com.mycompany.ciudadanosv.models.ReglaNegocioException;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.models.UsuarioDAO;
import com.mycompany.ciudadanosv.util.Csrf;
import com.mycompany.ciudadanosv.util.Fechas;
import com.mycompany.ciudadanosv.util.Parametros;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Mi perfil: ver y editar los datos personales y cambiar la contraseña.
 * URL protegida: /app/perfil   (ciudadanos y funcionarios)
 *   GET                  -> muestra el perfil
 *   POST accion=datos    -> guarda correo, teléfono, dirección, municipio, distrito, estado civil y fecha de nacimiento
 *   POST accion=clave    -> cambia la contraseña (pide la actual)
 */
public class PerfilController extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/ciudadano/perfil.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Csrf.asegurarToken(request);
        Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
        try {
            Perfil perfil = new UsuarioDAO().buscarPerfil(usuario.getIdUsuario());
            if (perfil == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("perfil", perfil);
            request.setAttribute("municipios", new MunicipioDAO().listar());
            request.setAttribute("distritos", new DistritoDAO().listar());
        } catch (SQLException ex) {
            log("Error cargando el perfil", ex);
            request.setAttribute("errorBD", "No se pudo cargar tu perfil. Intenta de nuevo.");
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
        UsuarioDAO dao = new UsuarioDAO();
        String ip = request.getRemoteAddr();
        String accion = Parametros.texto(request, "accion");

        try {
            if ("datos".equals(accion)) {
                Perfil p = new Perfil();
                p.setIdUsuario(usuario.getIdUsuario());
                p.setCorreo(Parametros.texto(request, "correo").toLowerCase(Locale.ROOT));
                p.setTelefono(Parametros.textoONulo(request, "telefono"));
                p.setDireccion(Parametros.textoONulo(request, "direccion"));
                p.setIdMunicipio(Parametros.entero(request, "idMunicipio"));
                p.setIdDistrito(Parametros.entero(request, "idDistrito"));
                p.setEstadoCivil(Parametros.textoONulo(request, "estadoCivil"));
                String fecha = Parametros.texto(request, "fechaNacimiento");
                LocalDate nacimiento = Fechas.leer(fecha);
                if (!fecha.isEmpty() && nacimiento == null) {
                    throw new ReglaNegocioException("La fecha de nacimiento debe tener el formato DD/MM/AAAA y ser una fecha real.");
                }
                p.setFechaNacimiento(nacimiento);
                dao.actualizarPerfil(p, ip);
                usuario.setCorreo(p.getCorreo());
                usuario.setTelefono(p.getTelefono());
                request.getSession().setAttribute("aviso", "Tus datos se guardaron correctamente.");
            } else if ("clave".equals(accion)) {
                dao.cambiarClave(usuario.getIdUsuario(), request.getParameter("claveActual"),
                        request.getParameter("claveNueva"), request.getParameter("claveConfirmacion"), ip);
                request.getSession().setAttribute("aviso", "Tu contraseña se cambió correctamente.");
            }
        } catch (ReglaNegocioException ex) {
            request.getSession().setAttribute("avisoError", ex.getMessage());
        } catch (SQLException ex) {
            log("Error guardando el perfil", ex);
            request.getSession().setAttribute("avisoError", "No se pudieron guardar los cambios. Intenta de nuevo.");
        }
        response.sendRedirect(request.getContextPath() + "/app/perfil" + ("clave".equals(accion) ? "#clave" : ""));
    }

    @Override
    public String getServletInfo() {
        return "Perfil del usuario";
    }
}
