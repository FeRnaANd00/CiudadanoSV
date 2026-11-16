package com.mycompany.ciudadanosv.controllers;

import com.mycompany.ciudadanosv.models.Tramite;
import com.mycompany.ciudadanosv.models.TramiteDAO;
import com.mycompany.ciudadanosv.models.Usuario;
import com.mycompany.ciudadanosv.models.UsuarioDAO;
import com.mycompany.ciudadanosv.util.Seguridad;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Inicio de sesión, registro de ciudadanos y cierre de sesión.
 * URL: /login
 *   GET  /login                            -> muestra el formulario
 *   GET  /login?servicio=RNPN-DUI-01       -> formulario indicando el servicio elegido
 *   GET  /login?destino=/app/...           -> lo envía el filtro; tras el login vuelve ahí
 *   GET  /login?accion=salir               -> cierra la sesión
 *   POST /login  accion=ingresar           -> valida credenciales
 *   POST /login  accion=registrar          -> crea la cuenta de ciudadano
 *
 * A dónde va después de iniciar sesión (en este orden):
 *   1. Si eligió un servicio en la landing -> /app/tramite?codigo=...
 *   2. Si venía de una página protegida    -> esa página (destino)
 *   3. Funcionario -> /admin/inicio   |   Ciudadano -> /inicio
 */
public class LoginController extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/login.jsp";
    private static final String ERROR_BD =
            "No se pudo conectar con la base de datos. Intenta de nuevo en unos minutos.";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("salir".equals(request.getParameter("accion"))) {
            HttpSession sesion = request.getSession(false);
            if (sesion != null) {
                sesion.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/inicio");
            return;
        }

        // Si ya inició sesión, lo mandamos directo a donde quería ir
        Usuario usuario = usuarioEnSesion(request);
        if (usuario != null) {
            response.sendRedirect(request.getContextPath() + rutaDespuesDeLogin(request, usuario));
            return;
        }

        prepararVista(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("registrar".equals(request.getParameter("accion"))) {
            registrar(request, response);
        } else {
            ingresar(request, response);
        }
    }

    // ------------------------------------------------------------------
    private void ingresar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String identificador = limpiar(request.getParameter("identificador"));
        String clave = request.getParameter("clave");

        if (identificador.isEmpty() || clave == null || clave.isEmpty()) {
            mostrarError(request, response, "Ingresa tu DUI o correo y tu contraseña.", "login");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.autenticar(identificador, clave);
            if (usuario == null) {
                mostrarError(request, response, "DUI, correo o contraseña incorrectos.", "login");
                return;
            }

            dao.registrarAcceso(usuario.getIdUsuario(), request.getRemoteAddr());

            request.getSession(true);
            request.changeSessionId();              // evita fijación de sesión
            HttpSession sesion = request.getSession();
            sesion.setAttribute("usuario", usuario);
            sesion.setAttribute("aviso", "Bienvenido(a), " + usuario.getPrimerNombre() + ". Sesión iniciada correctamente.");

            response.sendRedirect(request.getContextPath() + rutaDespuesDeLogin(request, usuario));

        } catch (SQLException ex) {
            log("Error al iniciar sesión", ex);
            mostrarError(request, response, ERROR_BD, "login");
        }
    }

    private void registrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = limpiar(request.getParameter("nombre"));
        String dui = limpiar(request.getParameter("dui"));
        String telefono = limpiar(request.getParameter("telefono"));
        String correo = limpiar(request.getParameter("correo")).toLowerCase();
        String clave = request.getParameter("claveNueva");

        String error = null;
        if (nombre.length() < 5) {
            error = "Escribe tu nombre completo.";
        } else if (!dui.matches("\\d{8}-\\d")) {
            error = "El DUI debe tener el formato 00000000-0.";
        } else if (!telefono.matches("\\d{4}-\\d{4}")) {
            error = "El teléfono debe tener el formato 7000-0000.";
        } else if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            error = "Escribe un correo electrónico válido.";
        } else if (!Seguridad.claveEsSegura(clave)) {
            error = "La contraseña debe tener mínimo 8 caracteres, un número, una mayúscula y un símbolo.";
        } else if (request.getParameter("acepto") == null) {
            error = "Debes aceptar las condiciones del entorno académico.";
        }
        if (error != null) {
            mostrarError(request, response, error, "registro");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            if (dao.existeDuiOCorreo(dui, correo)) {
                mostrarError(request, response, "Ya existe una cuenta con ese DUI o correo.", "registro");
                return;
            }
            Usuario nuevo = new Usuario();
            nuevo.setNombreCompleto(nombre);
            nuevo.setDui(dui);
            nuevo.setTelefono(telefono);
            nuevo.setCorreo(correo);
            dao.registrarCiudadano(nuevo, clave);

            request.setAttribute("exito", "Cuenta creada. Ya puedes iniciar sesión con tu DUI o correo.");
            request.setAttribute("tab", "login");
            prepararVista(request);
            request.getRequestDispatcher(VISTA).forward(request, response);

        } catch (SQLException ex) {
            log("Error al registrar ciudadano", ex);
            mostrarError(request, response, ERROR_BD, "registro");
        }
    }

    // ------------------------------------------------------------------
    /** Decide la ruta (sin el contextPath) a la que va el usuario tras iniciar sesión. */
    private String rutaDespuesDeLogin(HttpServletRequest request, Usuario usuario) {
        Tramite servicio = buscarServicio(request.getParameter("servicio"));
        if (servicio != null) {
            return "/app/tramite?codigo=" + URLEncoder.encode(servicio.getCodigo(), StandardCharsets.UTF_8);
        }
        String destino = request.getParameter("destino");
        if (Seguridad.destinoSeguro(destino)) {
            if (destino.startsWith("/admin") && !usuario.isFuncionario()) {
                return "/inicio";
            }
            return destino;
        }
        return usuario.isFuncionario() ? "/admin/inicio" : "/inicio";
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response,
            String mensaje, String tab) throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.setAttribute("tab", tab);
        prepararVista(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /** Pasa a la vista el servicio elegido y el destino (validado) para no perderlos en el formulario. */
    private void prepararVista(HttpServletRequest request) {
        Tramite t = buscarServicio(request.getParameter("servicio"));
        if (t != null) {
            request.setAttribute("servicio", t);
        }
        String destino = request.getParameter("destino");
        if (Seguridad.destinoSeguro(destino)) {
            request.setAttribute("destino", destino);
        }
    }

    private Tramite buscarServicio(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        try {
            return new TramiteDAO().buscarPorCodigo(codigo.trim());
        } catch (SQLException ex) {
            log("No se pudo consultar el trámite " + codigo, ex);
            return null;
        }
    }

    private Usuario usuarioEnSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    @Override
    public String getServletInfo() {
        return "Inicio de sesión y registro de ciudadanos";
    }
}
