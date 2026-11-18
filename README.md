# CiudadanoSV

Portal de servicios digitales del ciudadano (proyecto académico).
Java 21 · Jakarta EE 11 (Servlets + JSP) · MariaDB 11.4 · Apache Tomcat 11 · NetBeans.

## Arquitectura (MVC)
```
src/main/java/com/mycompany/ciudadanosv/
  models/       Modelos + DAO (JDBC) — Conexion lee db.properties
  controllers/  Servlets (mapeados en WEB-INF/web.xml)
  filters/      AutenticacionFilter (protege /app/* y /admin/*), ContadorNotificacionesFilter
  util/         Seguridad (hash de contraseñas), Csrf, Fechas, AlmacenArchivos
src/main/webapp/WEB-INF/vistas/   Vistas JSP (no accesibles directamente)
database/ciudadanosv_bd.sql       Script de la base de datos (25 tablas) con datos de prueba
```

## Cómo ejecutar el sistema

### 1. Requisitos
| Programa | Versión | Para qué |
|---|---|---|
| JDK | 21 | Compilar y ejecutar Java |
| Apache NetBeans | 21 o superior (probado en 31) | IDE; trae Maven incluido |
| Apache Tomcat | 11.0.x | Servidor web (Jakarta EE 11) |
| MariaDB | 11.4 (puerto 3306) | Base de datos |
| HeidiSQL | cualquiera | Ejecutar el script de la base (opcional, sirve cualquier cliente) |
| Git | cualquiera | Descargar el proyecto |

### 2. Descargar el proyecto
```bash
git clone https://github.com/FeRnaANd00/CiudadanoSV.git
```
Si ya lo tienes, actualízalo con `git pull`.

### 3. Crear la base de datos
1. Abre HeidiSQL y conéctate a tu MariaDB (`localhost`, puerto `3306`, usuario `root`).
2. Abre una pestaña de consulta (**Ctrl+T**), pega **todo** el contenido de `database/ciudadanosv_bd.sql`
   y ejecútalo con **F9**.
3. Pulsa **F5** en el árbol: debe aparecer `ciudadanosv_bd` con 25 tablas.

> ⚠️ El script **borra y vuelve a crear** la base `ciudadanosv_bd`. Si ya tenías datos, se pierden.

Desde la consola también funciona:
```bash
mysql -u root -p --default-character-set=utf8mb4 < database/ciudadanosv_bd.sql
```

### 4. Configurar la conexión a la base
1. En `src/main/resources/` copia `db.properties.example` y nombra la copia **`db.properties`**.
2. Escribe tus datos:
   ```properties
   db.url=jdbc:mariadb://localhost:3306/ciudadanosv_bd
   db.usuario=root
   db.clave=TU_CLAVE_DE_MARIADB
   ```
   `db.properties` está en `.gitignore`: **nunca se sube a GitHub**. Cada computadora tiene el suyo.
   (También se pueden usar las variables de entorno `CIUDADANOSV_DB_URL`, `CIUDADANOSV_DB_USUARIO`
   y `CIUDADANOSV_DB_CLAVE`.)

### 5. Registrar Tomcat en NetBeans (solo la primera vez)
1. Descarga Tomcat 11 (zip) desde https://tomcat.apache.org y descomprímelo, por ejemplo en `C:pache-tomcat-11.0.27`.
2. En NetBeans: **Tools → Servers → Add Server… → Apache Tomcat or TomEE**, elige esa carpeta,
   crea un usuario/contraseña de administrador y termina.

### 6. Abrir y ejecutar
1. **File → Open Project…** y elige la carpeta `CiudadanoSV` (NetBeans la reconoce como proyecto Maven).
2. Clic derecho en el proyecto → **Properties → Run** → Server: **Apache Tomcat 11**.
3. Clic derecho → **Clean and Build** (la primera vez descarga las dependencias; necesita internet).
4. Clic derecho → **Run**. Se abre `http://localhost:8080/CiudadanoSV/`.

Después de cambiar código, vuelve a hacer **Clean and Build** y **Run**.

### 7. Probar
Entra con los [usuarios de prueba](#usuarios-de-prueba):
- **Ciudadano:** trámites, citas, multas, notificaciones (campanita) y Mi perfil.
- **Revisor RNPN:** menú **Administración** → bandeja de solicitudes de RNPN.
- **Operadora MINSAL:** Administración → Citas y cupos.
- **Super admin:** todos los módulos, incluidos Trámites y Usuarios.

### Problemas frecuentes
| Síntoma | Solución |
|---|---|
| "No se pudo conectar con la base de datos" | Revisa que MariaDB esté encendido y la clave en `db.properties`; luego Clean and Build. |
| Error 404 en `/CiudadanoSV/` | El proyecto no se desplegó: revisa que el servidor sea Tomcat 11 y vuelve a hacer Run. |
| Se ven cambios viejos | Clean and Build y Run; si persiste, cierra sesión y vuelve a entrar. |
| Puerto 8080 ocupado | Cierra el otro programa o cambia el puerto de Tomcat en Tools → Servers. |
| Acentos raros en los datos | Ejecuta el script con codificación UTF-8 (`--default-character-set=utf8mb4`). |

## Avance del proyecto
| Fase | Pasos | Estado |
|---|---|---|
| 1. Base y seguridad | 0. Landing, login, registro · 1. Filtro /app y /admin, roles, errores | ✅ |
| 2. Trámites del ciudadano | 2. Partida de nacimiento · 3. Mis solicitudes + detalle · 4. Municipales · 5. DUI con archivos | ✅ |
| 3. Citas y consultas | 6. Citas de salud con cupos · 7. Multas VMT | ✅ |
| 4. Cuenta del ciudadano | 8. Notificaciones y bitácora · 9. Perfil (edición de datos y cambio de contraseña) | ✅ |
| 5. Administración | 10. Bandeja y resolución · 11. Cupos · 12. Catálogo · 13. Usuarios · 14. Estadísticas | ✅ |
| 6. Cierre | 15. Páginas de error, revisión de seguridad, script de BD actualizado | ✅ |

## Módulos
- **Ciudadano:** landing con catálogo, registro e inicio de sesión, partida de nacimiento, solvencia y constancia
  municipal, DUI con archivos, mis solicitudes con línea de tiempo, citas de salud, multas VMT, notificaciones con bitácora
  y Mi perfil (editar datos y cambiar contraseña).
- **Administración** (`/admin`): panel estadístico, bandeja de expedientes y resolución (cotejo, observaciones
  internas, cita biométrica), citas y cupos, catálogo de trámites y requisitos, gestión de usuarios.
  Revisores y operadores solo ven su institución; el super admin, todo.

## Seguridad
Contraseñas con SHA-256 + salt · token CSRF en todo formulario · consultas con `PreparedStatement` ·
salidas escapadas con `<c:out>` · cookie de sesión `HttpOnly` y sin id en la URL · cabeceras
`X-Frame-Options: DENY` y `nosniff` · página de error que nunca muestra detalles técnicos ·
archivos validados por contenido y guardados fuera del proyecto.

## Usuarios de prueba
| Rol | Usuario | Contraseña |
|---|---|---|
| Ciudadano | `04829104-5` o `carlos.hernandez@sv.gob.ficticio` | `Ciudadano123*` |
| Revisor (RNPN) | `douglas.morales@sv.gob.ficticio` | `Revisor123*` |
| Operadora (MINSAL) | `silvia.menjivar@sv.gob.ficticio` | `Operador123*` |
| Super admin | `admin@ciudadano.sv` | `Admin123*` |

Los archivos que suben los ciudadanos se guardan en `C:\Users\<usuario>\CiudadanoSV-archivos`
(se puede cambiar con el parámetro `ciudadanosv.rutaArchivos` del `web.xml`).
