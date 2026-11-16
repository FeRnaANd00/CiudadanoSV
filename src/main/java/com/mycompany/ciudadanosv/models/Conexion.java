package com.mycompany.ciudadanosv.models;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión a la base de datos ciudadanosv_bd (MariaDB).
 *
 * El usuario y la contraseña NO van en el código (para no subirlos a GitHub).
 * Se leen, en este orden:
 *   1. Variables de entorno CIUDADANOSV_DB_URL / CIUDADANOSV_DB_USUARIO / CIUDADANOSV_DB_CLAVE
 *   2. El archivo src/main/resources/db.properties (está en .gitignore; copia db.properties.example)
 *   3. Valores por defecto: localhost:3306/ciudadanosv_bd, usuario root, sin contraseña
 */
public class Conexion {

    private static final String DRIVER = "org.mariadb.jdbc.Driver";
    private static final Properties CONFIG = cargarConfiguracion();

    private static final String URL     = valor("db.url", "CIUDADANOSV_DB_URL",
                                                "jdbc:mariadb://localhost:3306/ciudadanosv_bd");
    private static final String USUARIO = valor("db.usuario", "CIUDADANOSV_DB_USUARIO", "root");
    private static final String CLAVE   = valor("db.clave", "CIUDADANOSV_DB_CLAVE", "");

    private static Properties cargarConfiguracion() {
        Properties p = new Properties();
        try (InputStream in = Conexion.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            } else {
                System.err.println("[CiudadanoSV] No se encontró db.properties: copia "
                        + "src/main/resources/db.properties.example como db.properties y pon tu contraseña.");
            }
        } catch (IOException ex) {
            System.err.println("[CiudadanoSV] No se pudo leer db.properties: " + ex.getMessage());
        }
        return p;
    }

    private static String valor(String clave, String variableEntorno, String porDefecto) {
        String env = System.getenv(variableEntorno);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return CONFIG.getProperty(clave, porDefecto).trim();
    }

    /** Abre una conexión nueva. Ciérrala siempre con try-with-resources. */
    public static Connection conectar() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontró el driver de MariaDB", ex);
        }
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
