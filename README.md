# CiudadanoSV

Portal de servicios digitales del ciudadano (proyecto académico).
Java 21 · Jakarta EE 11 (Servlets + JSP) · MariaDB 11.4 · Apache Tomcat 11 · NetBeans.

## Arquitectura (MVC)
```
src/main/java/com/mycompany/ciudadanosv/
  models/       Modelos + DAO (JDBC) — Conexion lee db.properties
  controllers/  Servlets (mapeados en WEB-INF/web.xml)
  filters/      AutenticacionFilter: protege /app/* y /admin/*
  util/         Seguridad (hash de contraseñas), Csrf, Fechas, AlmacenArchivos
src/main/webapp/WEB-INF/vistas/   Vistas JSP (no accesibles directamente)
database/ciudadanosv_bd.sql       Script de la base de datos con datos de prueba
```

## Cómo ejecutarlo
1. **Base de datos:** abre `database/ciudadanosv_bd.sql` en HeidiSQL y ejecútalo completo (F9).
2. **Credenciales:** copia `src/main/resources/db.properties.example` como
   `src/main/resources/db.properties` y escribe tu usuario y contraseña de MariaDB.
   *Este archivo no se sube a GitHub.*
3. **NetBeans:** abre el proyecto, elige Tomcat 11 como servidor y pulsa **Run**.
4. Entra a `http://localhost:8080/CiudadanoSV/`.

## Usuarios de prueba
| Rol | Usuario | Contraseña |
|---|---|---|
| Ciudadano | `04829104-5` o `carlos.hernandez@sv.gob.ficticio` | `Ciudadano123*` |
| Revisor | `douglas.morales@sv.gob.ficticio` | `Revisor123*` |
| Super admin | `admin@ciudadano.sv` | `Admin123*` |

Los archivos que suben los ciudadanos se guardan en `C:\Users\<usuario>\CiudadanoSV-archivos`
(se puede cambiar con el parámetro `ciudadanosv.rutaArchivos` del `web.xml`).
