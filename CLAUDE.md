# CiudadanoSV — guía para Claude

Portal académico de servicios digitales del ciudadano (El Salvador). Lo desarrollamos por **fases**;
el avance está en `README.md` → "Avance del proyecto". Antes de empezar: `git pull` y revisa ese avance.

## Forma de trabajar (acordada con el usuario)
- **Responder siempre en español**, breve y directo.
- **El usuario edita archivos en NetBeans entre sesiones.** Antes de modificar un archivo existente, léelo del
  disco y usa ediciones puntuales; no lo reescribas completo (ya se perdió un cambio suyo así).
- **No agregar avisos de "Prototipo académico / datos simulados"** ni etiquetas tipo "Entorno académico
  demostrativo" o "Gobierno Digital • El Salvador": el usuario los quitó a propósito.
- Trabajar **una fase completa** (todos sus pasos), probar cada paso por dentro y al final entregar al usuario
  un resumen + lista de lo que debe probar en NetBeans.
- Probar con un Tomcat temporal (CATALINA_BASE propio, puerto 8095, sin `conf/Catalina`) y `curl`; detenerlo al terminar.
  Avisar qué datos de prueba quedaron en la base.
- **Nunca subir credenciales.** La contraseña de MariaDB está en `src/main/resources/db.properties`
  (en `.gitignore`). Antes de cada push: `git grep --cached` para verificar que no se cuela.
- Subir a GitHub solo cuando el usuario lo pida. Commits en español.

## Entorno
- Java 21 · Jakarta EE 11 Web (Servlet 6.1, JSP, JSTL 3) · Tomcat 11.0.27 · MariaDB 11.4 (puerto 3306) · HeidiSQL · NetBeans 31.
- Maven: el que trae NetBeans (`C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd`); `mvn -q clean package`.
- Base de datos: `ciudadanosv_bd`, script completo en `database/ciudadanosv_bd.sql` (24 tablas + datos de prueba).
- Diseño original de pantallas (HTML con Tailwind): repo aparte `PANTALLASCiudadanoSV`.

## Arquitectura (MVC con Servlets, igual al proyecto de referencia WebProductos)
- `models/`: POJO + DAO con JDBC (`PreparedStatement`, try-with-resources). Escrituras de varias tablas en
  **una transacción** (`setAutoCommit(false)` + rollback). Reglas de negocio → `ReglaNegocioException` (mensaje para el usuario).
- `controllers/`: Servlets mapeados en `WEB-INF/web.xml` (no anotaciones). Patrón Post/Redirect/Get.
- `filters/AutenticacionFilter`: `/app/*` requiere sesión; `/admin/*` requiere funcionario (REVISOR, OPERADOR, SUPER_ADMIN).
- `util/`: `Seguridad` (SHA-256 + salt: `clave_hash = SHA2(CONCAT(clave_salt, clave),256)`), `Csrf` (token en sesión,
  campo oculto `csrf` en todo POST), `Fechas` (DD/MM/AAAA), `AlmacenArchivos` (archivos subidos).
- Vistas JSP en `WEB-INF/vistas/` (solo accesibles vía controlador). Includes: `head.jspf`, `header.jspf`, `footer.jspf`,
  `badge_estado.jspf`. Tema de colores en `resources/js/tailwind-config.js` (primary `#002869`, dorado `#C68A2E`) + `resources/css/ciudadanosv.css`.
- `TramiteController.FORMULARIOS`: mapa código de trámite → formulario; agregar una entrada por cada formulario nuevo.
- Archivos subidos: fuera del proyecto en `C:\Users\<usuario>\CiudadanoSV-archivos\<folio>\` (configurable con
  `ciudadanosv.rutaArchivos` en web.xml); en BD solo ruta + metadatos + SHA-256. Tipo validado por contenido.
- Folios: `EXP-AAAA-NNNNN` (solicitudes), `CIT-AAAA-NNNNN` (citas), generados a partir del id.

## Decisiones tomadas
- Pantallas eliminadas o fusionadas: `index`, `catalogo_Servicios` y `panel_principal` → landing (`/inicio`);
  los menús intermedios de `Servicios/*` ya no existen (la landing lista los trámites); `panelControl` +
  `panel_estadistica` se fusionan en el paso 14.
- **Administración:** un solo SUPER_ADMIN, que crea funcionarios y asigna a cada uno su institución
  (`usuario.id_institucion`). Revisores y operadores **solo ven trámites de su institución**.
- Multas "por licencia" solo consultan el DUI propio (privacidad); "por placa", cualquier placa.
- Citas: una cita activa por servicio, sin choques de horario, cancelación hasta 2 h antes; si un día no tiene
  cupos se crean 4 bloques × 10 turnos (el admin los gestionará en el paso 11).
- Fecha de nacimiento con texto + máscara DD/MM/AAAA (el calendario nativo del teléfono del usuario sale en era Reiwa).

## Lo que sigue
- **Fase 4:** paso 8 notificaciones (bandeja, filtros Trámites/Citas/Seguridad, marcar leídas, bitácora) ·
  paso 9 perfil (datos, cambiar contraseña, 2FA/notificaciones, sesiones y dispositivos). Diseños: `notificaciones.html`, `perfil.html`.
  En el menú de `header.jspf`, "Notificaciones" y "Mi perfil" están desactivados esperando estos pasos.
- **Fase 5 (admin):** 10 bandeja de solicitudes + detalle/resolución (cambiar estado → historial + notificación,
  observaciones internas, cotejo de documentos, programar cita biométrica de DUI) · 11 cupos y agenda ·
  12 catálogo de trámites y requisitos · 13 gestión de usuarios · 14 panel estadístico.
- **Fase 6:** páginas de error finales, revisión general, script de BD actualizado.
- Pendiente de datos: `RNPN-DUI-02` no tiene requisitos en el script (se puede copiar los de `RNPN-DUI-01`).

## Usuarios de prueba
Ciudadano `04829104-5` / `Ciudadano123*` · Revisor `douglas.morales@sv.gob.ficticio` / `Revisor123*` ·
Super admin `admin@ciudadano.sv` / `Admin123*`.
