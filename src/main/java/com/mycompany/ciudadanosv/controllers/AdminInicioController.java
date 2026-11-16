package com.mycompany.ciudadanosv.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Inicio del portal administrativo (solo funcionarios; lo protege AutenticacionFilter).
 * URL: /admin/inicio
 */
public class AdminInicioController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/admin/inicio.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Inicio del portal administrativo";
    }
}
