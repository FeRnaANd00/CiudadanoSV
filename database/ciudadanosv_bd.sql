/* =====================================================================
   BASE DE DATOS: ciudadanosv_bd
   Proyecto     : CiudadanoSV - Portal de Servicios Digitales (académico)
   Motor        : MariaDB 11.4  |  Gestor: HeidiSQL 12.10
   Charset      : utf8mb4 (acentos, ñ y emojis sin problema)

   CÓMO EJECUTARLO EN HEIDISQL
   1. Conéctate a tu servidor MariaDB (localhost, puerto 3306).
   2. Abre una pestaña de "Consulta" (Ctrl+T).
   3. Pega TODO este archivo y ejecútalo completo con F9.
   4. Pulsa F5 en el árbol de la izquierda: aparecerá "ciudadanosv_bd".

   DATOS PARA LA CONEXIÓN DESDE JAVA (src/main/resources/db.properties)
     driver : org.mariadb.jdbc.Driver
     url    : jdbc:mariadb://localhost:3306/ciudadanosv_bd
     user   : root   (o el usuario de la sección 0, si lo creas)

   CONTRASEÑAS DE USUARIOS
     No se guardan en texto plano. Se guarda:
       clave_hash = SHA2( CONCAT(clave_salt, clave_escrita), 256 )
     En Java se replica con MessageDigest("SHA-256"), sin librerías extra.

   USUARIOS DE PRUEBA (datos ficticios)
     Super Admin : admin@ciudadano.sv               / Admin123*
     Revisor     : douglas.morales@sv.gob.ficticio   / Revisor123*   (RNPN)
     Operadora   : silvia.menjivar@sv.gob.ficticio   / Operador123*  (MINSAL)
     Ciudadano   : carlos.hernandez@sv.gob.ficticio  / Ciudadano123*
     (El ciudadano también puede entrar con su DUI: 04829104-5)

   ADVERTENCIA: este script BORRA y vuelve a crear la base de datos.
   ===================================================================== */

DROP DATABASE IF EXISTS ciudadanosv_bd;
CREATE DATABASE ciudadanosv_bd
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE ciudadanosv_bd;

/* ---------------------------------------------------------------------
   0. (OPCIONAL) Usuario propio para la aplicación, en lugar de root.
      Quita los comentarios y cambia la contraseña si lo quieres usar.
   --------------------------------------------------------------------- */
-- CREATE USER IF NOT EXISTS 'ciudadanosv_app'@'localhost' IDENTIFIED BY 'CambiaEstaClave2026!';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ciudadanosv_bd.* TO 'ciudadanosv_app'@'localhost';
-- FLUSH PRIVILEGES;


/* =====================================================================
   1. SEGURIDAD: ROLES Y PERMISOS (RBAC)
      Pantalla: admin/gestionUsuarios.html (Matriz de Roles y Privilegios)
   ===================================================================== */
CREATE TABLE rol (
  id_rol        INT AUTO_INCREMENT PRIMARY KEY,
  nombre        VARCHAR(40)  NOT NULL UNIQUE,      -- CIUDADANO, REVISOR...
  descripcion   VARCHAR(150)
) ENGINE=InnoDB;

CREATE TABLE permiso (
  id_permiso    INT AUTO_INCREMENT PRIMARY KEY,
  codigo        VARCHAR(50)  NOT NULL UNIQUE,      -- SOLICITUD_RESOLVER...
  descripcion   VARCHAR(150)
) ENGINE=InnoDB;

CREATE TABLE rol_permiso (
  id_rol        INT NOT NULL,
  id_permiso    INT NOT NULL,
  PRIMARY KEY (id_rol, id_permiso),
  FOREIGN KEY (id_rol)     REFERENCES rol(id_rol)         ON DELETE CASCADE,
  FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON DELETE CASCADE
) ENGINE=InnoDB;


/* =====================================================================
   2. CATÁLOGOS GEOGRÁFICOS E INSTITUCIONALES
      Pantallas: solicitud_DUI, citas, solicitudMunicipal,
                 solicitudPartidaNacimiento, bandejaSolicitudes, citas-Cupos
   ===================================================================== */
CREATE TABLE departamento (
  id_departamento INT AUTO_INCREMENT PRIMARY KEY,
  nombre          VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE municipio (
  id_municipio    INT AUTO_INCREMENT PRIMARY KEY,
  id_departamento INT NOT NULL,
  nombre          VARCHAR(80) NOT NULL,
  UNIQUE (id_departamento, nombre),
  FOREIGN KEY (id_departamento) REFERENCES departamento(id_departamento)
) ENGINE=InnoDB;

/* Distritos (antiguos municipios) de cada municipio. Pantalla: perfil.html (lugar de residencia) */
CREATE TABLE distrito (
  id_distrito     INT AUTO_INCREMENT PRIMARY KEY,
  id_municipio    INT NOT NULL,
  nombre          VARCHAR(80) NOT NULL,
  UNIQUE (id_municipio, nombre),
  FOREIGN KEY (id_municipio) REFERENCES municipio(id_municipio)
) ENGINE=InnoDB;

CREATE TABLE institucion (
  id_institucion  INT AUTO_INCREMENT PRIMARY KEY,
  siglas          VARCHAR(20)  NOT NULL UNIQUE,    -- RNPN, MINSAL, VMT...
  nombre          VARCHAR(150) NOT NULL,
  activo          BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

/* Sede = Duicentro, Unidad de Salud, SERTRACEN, Alcaldía, etc. */
CREATE TABLE sede (
  id_sede         INT AUTO_INCREMENT PRIMARY KEY,
  id_institucion  INT NOT NULL,
  id_municipio    INT NOT NULL,
  nombre          VARCHAR(120) NOT NULL,
  direccion       VARCHAR(200),
  horario         VARCHAR(200),
  num_ventanillas INT NOT NULL DEFAULT 1,
  activo          BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (id_institucion) REFERENCES institucion(id_institucion),
  FOREIGN KEY (id_municipio)   REFERENCES municipio(id_municipio)
) ENGINE=InnoDB;


/* =====================================================================
   3. USUARIOS (ciudadanos y funcionarios en una sola tabla)
      Pantallas: login.html (login por DUI o correo + registro),
                 perfil.html, admin/gestionUsuarios.html
   ===================================================================== */
CREATE TABLE usuario (
  id_usuario          INT AUTO_INCREMENT PRIMARY KEY,
  id_rol              INT NOT NULL,
  dui                 CHAR(10)     NOT NULL UNIQUE,   -- formato 00000000-0
  nombre_completo     VARCHAR(120) NOT NULL,
  correo              VARCHAR(120) NOT NULL UNIQUE,
  telefono            VARCHAR(15),                    -- 7000-0000
  clave_hash          CHAR(64)     NOT NULL,          -- SHA-256 en hexadecimal
  clave_salt          CHAR(32)     NOT NULL,
  fecha_nacimiento    DATE,
  estado_civil        ENUM('SOLTERO','CASADO','DIVORCIADO','VIUDO','ACOMPANADO'),
  direccion           VARCHAR(200),
  id_municipio        INT,
  id_distrito         INT,                            -- distrito de residencia (dentro del municipio)
  -- Solo funcionarios:
  id_institucion      INT,
  id_sede             INT,
  cargo               VARCHAR(100),                   -- "Oficial Registral RNPN"
  -- Estado y seguridad:
  estado              ENUM('ACTIVO','SUSPENDIDO','EN_REVISION') NOT NULL DEFAULT 'ACTIVO',
  nivel_verificacion  TINYINT NOT NULL DEFAULT 1,     -- "Biometría Nivel 2"
  dos_factores        BOOLEAN NOT NULL DEFAULT FALSE, -- perfil: 2FA
  notif_correo        BOOLEAN NOT NULL DEFAULT TRUE,  -- perfil: alertas por correo
  acceso_biometrico   BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_registro      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_acceso       DATETIME,
  ultimo_cambio_clave DATETIME,
  FOREIGN KEY (id_rol)         REFERENCES rol(id_rol),
  FOREIGN KEY (id_municipio)   REFERENCES municipio(id_municipio),
  FOREIGN KEY (id_distrito)    REFERENCES distrito(id_distrito),
  FOREIGN KEY (id_institucion) REFERENCES institucion(id_institucion),
  FOREIGN KEY (id_sede)        REFERENCES sede(id_sede)
) ENGINE=InnoDB;

/* perfil.html -> "Sesiones y Dispositivos" (botón Revocar) */
CREATE TABLE sesion_dispositivo (
  id_sesion         INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario        INT NOT NULL,
  dispositivo       VARCHAR(100) NOT NULL,          -- "Portal Web Escritorio"
  ubicacion         VARCHAR(100),
  ip                VARCHAR(45),
  fecha_inicio      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultima_actividad  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activa            BOOLEAN  NOT NULL DEFAULT TRUE,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;


/* =====================================================================
   4. CATÁLOGO DE SERVICIOS Y TRÁMITES
      Pantallas: catalogo_Servicios, index, Servicios/*.html,
                 admin/servicios-tramites.html
   ===================================================================== */
CREATE TABLE area_servicio (
  id_area      INT AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(60) NOT NULL UNIQUE,   -- Salud Pública, Transporte VMT...
  icono        VARCHAR(40),                   -- nombre del icono Material
  orden        INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE tramite (
  id_tramite          INT AUTO_INCREMENT PRIMARY KEY,
  id_area             INT NOT NULL,
  id_institucion      INT NOT NULL,
  codigo              VARCHAR(20)  NOT NULL UNIQUE,   -- RNPN-DUI-01
  nombre              VARCHAR(120) NOT NULL,
  descripcion         VARCHAR(300),
  arancel             DECIMAL(8,2) NOT NULL DEFAULT 0.00,   -- USD
  plazo_horas         INT,                                  -- plazo legal (hábiles)
  modalidad           ENUM('PRESENCIAL','DIGITAL','MIXTA') NOT NULL DEFAULT 'MIXTA',
  requiere_cita       BOOLEAN NOT NULL DEFAULT FALSE,
  requiere_biometria  BOOLEAN NOT NULL DEFAULT FALSE,
  estado              ENUM('ACTIVO','EN_REVISION','PROXIMAMENTE','INACTIVO')
                      NOT NULL DEFAULT 'ACTIVO',
  FOREIGN KEY (id_area)        REFERENCES area_servicio(id_area),
  FOREIGN KEY (id_institucion) REFERENCES institucion(id_institucion)
) ENGINE=InnoDB;

/* admin/servicios-tramites.html -> "Matriz de Requisitos" / "Parámetro de Requisito" */
CREATE TABLE requisito (
  id_requisito      INT AUTO_INCREMENT PRIMARY KEY,
  id_tramite        INT NOT NULL,
  nombre            VARCHAR(150) NOT NULL,
  descripcion       VARCHAR(250),
  obligatorio       BOOLEAN NOT NULL DEFAULT TRUE,
  requiere_archivo  BOOLEAN NOT NULL DEFAULT FALSE,
  formatos          VARCHAR(50)  DEFAULT 'PDF,JPG,PNG',
  tamano_max_mb     INT          DEFAULT 5,
  tipo_validacion   ENUM('MANUAL','AUTOMATICA','PRESENCIAL') NOT NULL DEFAULT 'MANUAL',
  FOREIGN KEY (id_tramite) REFERENCES tramite(id_tramite) ON DELETE CASCADE
) ENGINE=InnoDB;


/* =====================================================================
   5. SOLICITUDES (EXPEDIENTES)
      Pantallas: solicitudes, detalles_solicitudes, CitasDUI/*,
                 admin/bandejaSolicitudes, solicitudMunicipal,
                 solicitudPartidaNacimiento
   ===================================================================== */
CREATE TABLE solicitud (
  id_solicitud          INT AUTO_INCREMENT PRIMARY KEY,
  folio                 VARCHAR(20) NOT NULL UNIQUE,     -- EXP-2024-00891
  id_usuario            INT NOT NULL,                    -- ciudadano solicitante
  id_tramite            INT NOT NULL,
  id_sede               INT,                             -- sede de atención / retiro
  id_revisor            INT,                             -- funcionario asignado
  estado                ENUM('BORRADOR','RECIBIDA','EN_REVISION','CITA_PROGRAMADA',
                             'APROBADA','RECHAZADA','FINALIZADA')
                        NOT NULL DEFAULT 'RECIBIDA',
  observacion_ciudadano VARCHAR(500),                    -- visible para el ciudadano
  observacion_interna   VARCHAR(500),                    -- solo funcionarios
  declaracion_jurada    BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_creacion        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
  fecha_resolucion      DATETIME,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_tramite) REFERENCES tramite(id_tramite),
  FOREIGN KEY (id_sede)    REFERENCES sede(id_sede),
  FOREIGN KEY (id_revisor) REFERENCES usuario(id_usuario),
  INDEX idx_solicitud_estado (estado),
  INDEX idx_solicitud_fecha  (fecha_creacion)
) ENGINE=InnoDB;

/* Datos propios de cada tipo de solicitud (relación 1 a 1 con solicitud) */

-- Servicios/CitasDUI/solicitud_DUI.html
CREATE TABLE solicitud_dui (
  id_solicitud      INT PRIMARY KEY,
  motivo            ENUM('VENCIMIENTO','DETERIORO','EXTRAVIO_ROBO','MODIFICACION_DATOS') NOT NULL,
  id_municipio_res  INT NOT NULL,                   -- departamento / municipio de residencia
  FOREIGN KEY (id_solicitud)     REFERENCES solicitud(id_solicitud) ON DELETE CASCADE,
  FOREIGN KEY (id_municipio_res) REFERENCES municipio(id_municipio)
) ENGINE=InnoDB;

-- Servicios/solicitudPartidaNacimiento.html
CREATE TABLE solicitud_partida (
  id_solicitud            INT PRIMARY KEY,
  nombre_inscrito         VARCHAR(120) NOT NULL,
  relacion                ENUM('PERSONA_INSCRITA','PADRE_MADRE_REPRESENTANTE','OTRA_AUTORIZADA') NOT NULL,
  fecha_nacimiento        DATE NOT NULL,
  id_municipio_inscripcion INT NOT NULL,
  uso                     ENUM('TRAMITE_PERSONAL','ESTUDIO','OTRO') NOT NULL,
  FOREIGN KEY (id_solicitud)             REFERENCES solicitud(id_solicitud) ON DELETE CASCADE,
  FOREIGN KEY (id_municipio_inscripcion) REFERENCES municipio(id_municipio)
) ENGINE=InnoDB;

-- Servicios/solicitudMunicipal.html (solvencia / constancia de residencia)
CREATE TABLE solicitud_municipal (
  id_solicitud      INT PRIMARY KEY,
  id_municipio      INT NOT NULL,
  domicilio         VARCHAR(200) NOT NULL,
  FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud) ON DELETE CASCADE,
  FOREIGN KEY (id_municipio) REFERENCES municipio(id_municipio)
) ENGINE=InnoDB;

/* "Línea de Tiempo del Trámite" y "Resolución y Cambio de Estado" */
CREATE TABLE solicitud_historial (
  id_historial      INT AUTO_INCREMENT PRIMARY KEY,
  id_solicitud      INT NOT NULL,
  estado_anterior   VARCHAR(20),
  estado_nuevo      VARCHAR(20) NOT NULL,
  comentario        VARCHAR(500),
  es_interno        BOOLEAN NOT NULL DEFAULT FALSE,   -- "Observación interna (sin notificar)"
  id_usuario        INT,                              -- quién hizo el cambio
  fecha             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud) ON DELETE CASCADE,
  FOREIGN KEY (id_usuario)   REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

/* "Documentos del Expediente" / "Documentos para Cotejo" */
CREATE TABLE documento (
  id_documento      INT AUTO_INCREMENT PRIMARY KEY,
  id_solicitud      INT NOT NULL,
  id_requisito      INT,
  nombre_archivo    VARCHAR(200) NOT NULL,
  ruta_archivo      VARCHAR(300) NOT NULL,            -- ruta en disco del servidor
  tipo_mime         VARCHAR(60),
  tamano_kb         INT,
  hash_sha256       CHAR(64),
  estado_cotejo     ENUM('PENDIENTE','VALIDO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
  fecha_carga       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud) ON DELETE CASCADE,
  FOREIGN KEY (id_requisito) REFERENCES requisito(id_requisito)
) ENGINE=InnoDB;


/* =====================================================================
   6. CITAS Y CUPOS
      Pantallas: CitasSalud/citas.html, admin/citas-Cupos.html,
                 detalles_solicitudes (cita de biometría)
   ===================================================================== */
/* Bloque horario con capacidad: "08:00 AM - 10:00 AM | Cupos: 25/25" */
CREATE TABLE cupo (
  id_cupo        INT AUTO_INCREMENT PRIMARY KEY,
  id_sede        INT NOT NULL,
  id_tramite     INT NOT NULL,
  fecha          DATE NOT NULL,
  hora_inicio    TIME NOT NULL,
  hora_fin       TIME NOT NULL,
  capacidad      INT  NOT NULL,
  ocupados       INT  NOT NULL DEFAULT 0,
  habilitado     BOOLEAN NOT NULL DEFAULT TRUE,     -- "Habilitar turnos extra"
  UNIQUE (id_sede, id_tramite, fecha, hora_inicio),
  CHECK (ocupados >= 0 AND ocupados <= capacidad),
  FOREIGN KEY (id_sede)    REFERENCES sede(id_sede),
  FOREIGN KEY (id_tramite) REFERENCES tramite(id_tramite)
) ENGINE=InnoDB;

CREATE TABLE cita (
  id_cita        INT AUTO_INCREMENT PRIMARY KEY,
  codigo         VARCHAR(20) NOT NULL UNIQUE,       -- CIT-2024-00024
  id_usuario     INT NOT NULL,
  id_cupo        INT NOT NULL,
  id_solicitud   INT,                               -- si nace de un trámite (ej. biometría DUI)
  fecha_hora     DATETIME NOT NULL,
  numero_turno   INT,                               -- "turno #24"
  ventanilla     VARCHAR(20),                       -- "Ventanilla 04"
  estado         ENUM('PROGRAMADA','EN_ATENCION','ATENDIDA','CANCELADA','NO_ASISTIO')
                 NOT NULL DEFAULT 'PROGRAMADA',
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario)   REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_cupo)      REFERENCES cupo(id_cupo),
  FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud),
  INDEX idx_cita_fecha (fecha_hora)
) ENGINE=InnoDB;


/* =====================================================================
   7. TRANSPORTE VMT: MULTAS / ESQUELAS
      Pantallas: transporteVMT.html, CitasTransporte/consultasMultas.html
   ===================================================================== */
CREATE TABLE tipo_infraccion (
  id_tipo_infraccion INT AUTO_INCREMENT PRIMARY KEY,
  codigo             VARCHAR(10)  NOT NULL UNIQUE,   -- TO-01, LE-05...
  descripcion        VARCHAR(250) NOT NULL,
  gravedad           ENUM('LEVE','GRAVE','MUY_GRAVE') NOT NULL,
  monto              DECIMAL(8,2) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE multa (
  id_multa           INT AUTO_INCREMENT PRIMARY KEY,
  folio              VARCHAR(20) NOT NULL UNIQUE,    -- INFR-2024-88412
  id_tipo_infraccion INT NOT NULL,
  dui_infractor      CHAR(10)    NOT NULL,           -- búsqueda "Por Licencia" (DUI vinculado)
  placa              VARCHAR(10),                    -- búsqueda "Por Placa"
  fecha_hora         DATETIME    NOT NULL,
  lugar              VARCHAR(200),
  agente             VARCHAR(100),                   -- "Inspector VMT - Patrulla #14"
  monto              DECIMAL(8,2) NOT NULL,
  fecha_vencimiento  DATE,
  estado             ENUM('PENDIENTE','PAGADA','EN_APELACION','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
  recibo_pago        VARCHAR(30),                    -- REC-992182-SV
  fecha_pago         DATETIME,
  FOREIGN KEY (id_tipo_infraccion) REFERENCES tipo_infraccion(id_tipo_infraccion),
  INDEX idx_multa_dui   (dui_infractor),
  INDEX idx_multa_placa (placa)
) ENGINE=InnoDB;


/* =====================================================================
   8. NOTIFICACIONES Y BITÁCORA
      Pantallas: notificaciones.html ("Bandeja Oficial" y "Bitácora Ciudadana")
   ===================================================================== */
CREATE TABLE notificacion (
  id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario      INT NOT NULL,
  tipo            ENUM('TRAMITE','CITA','SEGURIDAD','SISTEMA') NOT NULL,
  titulo          VARCHAR(120) NOT NULL,
  mensaje         VARCHAR(500) NOT NULL,
  id_solicitud    INT,
  id_cita         INT,
  leida           BOOLEAN NOT NULL DEFAULT FALSE,
  archivada       BOOLEAN NOT NULL DEFAULT FALSE,
  fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario)   REFERENCES usuario(id_usuario) ON DELETE CASCADE,
  FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud) ON DELETE SET NULL,
  FOREIGN KEY (id_cita)      REFERENCES cita(id_cita)           ON DELETE SET NULL
) ENGINE=InnoDB;

/* Registro de auditoría ("Registro inmutable"): solo INSERT, nunca UPDATE */
CREATE TABLE bitacora (
  id_bitacora     BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_usuario      INT,
  accion          VARCHAR(50)  NOT NULL,     -- LOGIN, ENVIO_SOLICITUD, CARGA_ARCHIVO...
  descripcion     VARCHAR(300),
  ip              VARCHAR(45),
  fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL,
  INDEX idx_bitacora_usuario (id_usuario, fecha)
) ENGINE=InnoDB;


/* =====================================================================
   9. DATOS INICIALES (catálogos tomados de las pantallas)
   ===================================================================== */

-- Roles
INSERT INTO rol (id_rol, nombre, descripcion) VALUES
 (1, 'CIUDADANO',   'Ciudadano titular que realiza trámites'),
 (2, 'REVISOR',     'Revisor técnico / oficial registral de una institución'),
 (3, 'OPERADOR',    'Operador de ventanilla y agenda de citas'),
 (4, 'SUPER_ADMIN', 'Administrador total del sistema');

-- Permisos
INSERT INTO permiso (id_permiso, codigo, descripcion) VALUES
 (1, 'TRAMITE_SOLICITAR',   'Crear y enviar solicitudes'),
 (2, 'CITA_AGENDAR',        'Agendar y cancelar citas propias'),
 (3, 'SOLICITUD_VER_TODAS', 'Ver la bandeja de solicitudes de la institución'),
 (4, 'SOLICITUD_RESOLVER',  'Cambiar estado y emitir resolución'),
 (5, 'DOCUMENTO_VALIDAR',   'Cotejar y validar documentos'),
 (6, 'CUPO_GESTIONAR',      'Administrar cupos y capacidad de sedes'),
 (7, 'SERVICIO_GESTIONAR',  'Administrar el catálogo de trámites y requisitos'),
 (8, 'USUARIO_GESTIONAR',   'Crear, editar y suspender usuarios'),
 (9, 'ESTADISTICA_VER',     'Ver panel de control y analítica');

INSERT INTO rol_permiso (id_rol, id_permiso) VALUES
 (1,1),(1,2),
 (2,2),(2,3),(2,4),(2,5),
 (3,2),(3,3),(3,6),
 (4,1),(4,2),(4,3),(4,4),(4,5),(4,6),(4,7),(4,8),(4,9);

-- Departamentos y municipios (ids 1-11: los usados en las pantallas; el resto, a continuación)
INSERT INTO departamento (id_departamento, nombre) VALUES
 (1,'San Salvador'),(2,'La Libertad'),(3,'Santa Ana'),(4,'San Miguel'),(5,'Sonsonate');

INSERT INTO municipio (id_municipio, id_departamento, nombre) VALUES
 (1,1,'San Salvador Centro'),(2,1,'San Salvador Este'),(3,1,'San Salvador Oeste'),
 (4,1,'San Salvador Sur'),(5,1,'San Salvador Norte'),
 (6,2,'La Libertad Sur'),(7,2,'La Libertad Este'),(8,2,'La Libertad Oeste'),
 (9,3,'Santa Ana Centro'),(10,4,'San Miguel Centro'),(11,5,'Sonsonate Centro');

-- Resto del país (división territorial vigente desde 2024: 14 departamentos, 44 municipios)
INSERT INTO departamento (id_departamento, nombre) VALUES
 (6,'Ahuachapán'),(7,'Cabañas'),(8,'Chalatenango'),(9,'Cuscatlán'),(10,'La Paz'),
 (11,'La Unión'),(12,'Morazán'),(13,'San Vicente'),(14,'Usulután');

INSERT INTO municipio (id_municipio, id_departamento, nombre) VALUES
 (12,6,'Ahuachapán Norte'),(13,6,'Ahuachapán Centro'),(14,6,'Ahuachapán Sur'),
 (15,7,'Cabañas Este'),(16,7,'Cabañas Oeste'),
 (17,8,'Chalatenango Norte'),(18,8,'Chalatenango Centro'),(19,8,'Chalatenango Sur'),
 (20,9,'Cuscatlán Norte'),(21,9,'Cuscatlán Sur'),
 (22,2,'La Libertad Norte'),(23,2,'La Libertad Centro'),(24,2,'La Libertad Costa'),
 (25,10,'La Paz Oeste'),(26,10,'La Paz Centro'),(27,10,'La Paz Este'),
 (28,11,'La Unión Norte'),(29,11,'La Unión Sur'),
 (30,12,'Morazán Norte'),(31,12,'Morazán Sur'),
 (32,4,'San Miguel Norte'),(33,4,'San Miguel Oeste'),
 (34,13,'San Vicente Norte'),(35,13,'San Vicente Sur'),
 (36,3,'Santa Ana Norte'),(37,3,'Santa Ana Este'),(38,3,'Santa Ana Oeste'),
 (39,5,'Sonsonate Norte'),(40,5,'Sonsonate Este'),(41,5,'Sonsonate Oeste'),
 (42,14,'Usulután Norte'),(43,14,'Usulután Este'),(44,14,'Usulután Oeste');

-- Distritos de cada municipio
-- 262 distritos
INSERT INTO distrito (id_municipio, nombre) VALUES
 (1,'Ayutuxtepeque'),(1,'Mejicanos'),(1,'Cuscatancingo'),(1,'Delgado'),(1,'San Salvador'),(2,'Ilopango'),
 (2,'San Martín'),(2,'Soyapango'),(2,'Tonacatepeque'),(3,'Apopa'),(3,'Nejapa'),(4,'San Marcos'),
 (4,'Santo Tomás'),(4,'Santiago Texacuangos'),(4,'Panchimalco'),(4,'Rosario de Mora'),(5,'Aguilares'),(5,'El Paisnal'),
 (5,'Guazapa'),(6,'Santa Tecla'),(6,'Comasagua'),(7,'Antiguo Cuscatlán'),(7,'Huizúcar'),(7,'Nuevo Cuscatlán'),
 (7,'San José Villanueva'),(7,'Zaragoza'),(8,'Colón'),(8,'Jayaque'),(8,'Sacacoyo'),(8,'Tepecoyo'),
 (8,'Talnique'),(9,'Santa Ana'),(10,'San Miguel'),(10,'Comacarán'),(10,'Uluazapa'),(10,'Moncagua'),
 (10,'Quelepa'),(10,'Chirilagua'),(11,'Sonsonate'),(11,'Sonzacate'),(11,'Nahulingo'),(11,'San Antonio del Monte'),
 (11,'Santo Domingo de Guzmán'),(12,'Atiquizaya'),(12,'El Refugio'),(12,'San Lorenzo'),(12,'Turín'),(13,'Ahuachapán'),
 (13,'Apaneca'),(13,'Concepción de Ataco'),(13,'Tacuba'),(14,'Guaymango'),(14,'Jujutla'),(14,'San Francisco Menéndez'),
 (14,'San Pedro Puxtla'),(15,'Guacotecti'),(15,'San Isidro'),(15,'Sensuntepeque'),(15,'Victoria'),(15,'Dolores'),
 (16,'Cinquera'),(16,'Ilobasco'),(16,'Jutiapa'),(16,'Tejutepeque'),(17,'Citalá'),(17,'La Palma'),
 (17,'San Ignacio'),(18,'Agua Caliente'),(18,'Dulce Nombre de María'),(18,'El Paraíso'),(18,'La Reina'),(18,'Nueva Concepción'),
 (18,'San Fernando'),(18,'San Francisco Morazán'),(18,'San Rafael'),(18,'Santa Rita'),(18,'Tejutla'),(19,'Arcatao'),
 (19,'Azacualpa'),(19,'Cancasque'),(19,'Chalatenango'),(19,'Comalapa'),(19,'Concepción Quezaltepeque'),(19,'El Carrizal'),
 (19,'La Laguna'),(19,'Las Vueltas'),(19,'Las Flores'),(19,'Nombre de Jesús'),(19,'Nueva Trinidad'),(19,'Ojos de Agua'),
 (19,'Potonico'),(19,'San Antonio de la Cruz'),(19,'San Antonio Los Ranchos'),(19,'San Francisco Lempa'),(19,'San Isidro Labrador'),(19,'San Luis del Carmen'),
 (19,'San Miguel de Mercedes'),(20,'Suchitoto'),(20,'San José Guayabal'),(20,'Oratorio de Concepción'),(20,'San Bartolomé Perulapía'),(20,'San Pedro Perulapán'),
 (21,'Cojutepeque'),(21,'Candelaria'),(21,'El Carmen'),(21,'El Rosario'),(21,'Monte San Juan'),(21,'San Cristóbal'),
 (21,'San Rafael Cedros'),(21,'San Ramón'),(21,'Santa Cruz Analquito'),(21,'Santa Cruz Michapa'),(21,'Tenancingo'),(22,'Quezaltepeque'),
 (22,'San Matías'),(22,'San Pablo Tacachico'),(23,'San Juan Opico'),(23,'Ciudad Arce'),(24,'Chiltiupán'),(24,'Jicalapa'),
 (24,'La Libertad'),(24,'Tamanique'),(24,'Teotepeque'),(25,'Cuyultitán'),(25,'Olocuilta'),(25,'San Juan Talpa'),
 (25,'San Luis Talpa'),(25,'San Pedro Masahuat'),(25,'Tapalhuaca'),(25,'San Francisco Chinameca'),(26,'El Rosario'),(26,'Jerusalén'),
 (26,'Mercedes La Ceiba'),(26,'Paraíso de Osorio'),(26,'San Antonio Masahuat'),(26,'San Emigdio'),(26,'San Juan Tepezontes'),(26,'San Luis La Herradura'),
 (26,'San Miguel Tepezontes'),(26,'San Pedro Nonualco'),(26,'Santa María Ostuma'),(26,'Santiago Nonualco'),(27,'San Juan Nonualco'),(27,'San Rafael Obrajuelo'),
 (27,'Zacatecoluca'),(28,'Anamorós'),(28,'Bolívar'),(28,'Concepción de Oriente'),(28,'El Sauce'),(28,'Lislique'),
 (28,'Nueva Esparta'),(28,'Pasaquina'),(28,'Polorós'),(28,'San José'),(28,'Santa Rosa de Lima'),(29,'Conchagua'),
 (29,'El Carmen'),(29,'Intipucá'),(29,'La Unión'),(29,'Meanguera del Golfo'),(29,'San Alejo'),(29,'Yayantique'),
 (29,'Yucuaiquín'),(30,'Arambala'),(30,'Cacaopera'),(30,'Corinto'),(30,'El Rosario'),(30,'Joateca'),
 (30,'Jocoaitique'),(30,'Meanguera'),(30,'Perquín'),(30,'San Fernando'),(30,'San Isidro'),(30,'Torola'),
 (31,'Chilanga'),(31,'Delicias de Concepción'),(31,'El Divisadero'),(31,'Gualococti'),(31,'Guatajiagua'),(31,'Jocoro'),
 (31,'Lolotiquillo'),(31,'Osicala'),(31,'San Carlos'),(31,'San Francisco Gotera'),(31,'San Simón'),(31,'Sensembra'),
 (31,'Sociedad'),(31,'Yamabal'),(31,'Yoloaiquín'),(32,'Ciudad Barrios'),(32,'Sesori'),(32,'Nuevo Edén de San Juan'),
 (32,'San Gerardo'),(32,'San Luis de la Reina'),(32,'Carolina'),(32,'San Antonio'),(32,'Chapeltique'),(33,'Chinameca'),
 (33,'El Tránsito'),(33,'Lolotique'),(33,'Nueva Guadalupe'),(33,'San Jorge'),(33,'San Rafael Oriente'),(34,'Apastepeque'),
 (34,'Santa Clara'),(34,'San Ildefonso'),(34,'San Esteban Catarina'),(34,'San Sebastián'),(34,'San Lorenzo'),(34,'Santo Domingo'),
 (35,'San Vicente'),(35,'Guadalupe'),(35,'San Cayetano Istepeque'),(35,'Tecoluca'),(35,'Tepetitán'),(35,'Verapaz'),
 (36,'Masahuat'),(36,'Metapán'),(36,'Santa Rosa Guachipilín'),(36,'Texistepeque'),(37,'Coatepeque'),(37,'El Congo'),
 (38,'Candelaria de la Frontera'),(38,'Chalchuapa'),(38,'El Porvenir'),(38,'San Antonio Pajonal'),(38,'San Sebastián Salitrillo'),(38,'Santiago de la Frontera'),
 (39,'Juayúa'),(39,'Nahuizalco'),(39,'Salcoatitán'),(39,'Santa Catarina Masahuat'),(40,'Armenia'),(40,'Caluco'),
 (40,'Cuisnahuat'),(40,'Izalco'),(40,'San Julián'),(40,'Santa Isabel Ishuatán'),(41,'Acajutla'),(42,'Alegría'),
 (42,'Berlín'),(42,'El Triunfo'),(42,'Estanzuelas'),(42,'Jucuapa'),(42,'Mercedes Umaña'),(42,'Nueva Granada'),
 (42,'San Buenaventura'),(42,'Santiago de María'),(43,'California'),(43,'Concepción Batres'),(43,'Ereguayquín'),(43,'Jucuarán'),
 (43,'Ozatlán'),(43,'Santa Elena'),(43,'San Dionisio'),(43,'Santa María'),(43,'Tecapán'),(43,'Usulután'),
 (44,'Jiquilisco'),(44,'Puerto El Triunfo'),(44,'San Agustín'),(44,'San Francisco Javier');

-- Instituciones
INSERT INTO institucion (id_institucion, siglas, nombre) VALUES
 (1,'RNPN',     'Registro Nacional de las Personas Naturales'),
 (2,'MINSAL',   'Ministerio de Salud'),
 (3,'VMT',      'Viceministerio de Transporte'),
 (4,'MINED',    'Ministerio de Educación'),
 (5,'ALCALDIA', 'Alcaldías Municipales'),
 (6,'HACIENDA', 'Ministerio de Hacienda'),
 (7,'DGME',     'Dirección General de Migración y Extranjería');

-- Sedes
INSERT INTO sede (id_sede, id_institucion, id_municipio, nombre, horario, num_ventanillas) VALUES
 (1, 1, 1, 'Duicentro Galerías Escalón',          'Lun-Vie 8:00-16:00, Sáb 8:00-12:00', 6),
 (2, 1, 2, 'Duicentro Plaza Mundo - Soyapango',    'Lun-Vie 8:00-16:00, Sáb 8:00-12:00', 5),
 (3, 1, 6, 'Duicentro Santa Tecla',                'Lun-Vie 8:00-16:00, Sáb 8:00-12:00', 4),
 (4, 1, 8, 'Duicentro Pasaje Comercial - Lourdes Colón', 'Lun-Vie 8:00-16:00, Sáb 8:00-12:00', 3),
 (5, 1, 7, 'Duicentro Las Cascadas - Antiguo Cuscatlán', 'Lun-Vie 8:00-16:00, Sáb 8:00-12:00', 4),
 (6, 1, 1, 'RNPN Central',                         'Lun-Vie 8:00-16:00', 8),
 (7, 2, 1, 'Unidad de Salud Arce',                 'Lun-Vie 7:00-15:00', 4),
 (8, 2, 1, 'Clínica Barrios',                      'Lun-Vie 7:00-15:00', 3),
 (9, 2, 2, 'Unidad de Salud Soyapango',            'Lun-Vie 7:00-15:00', 3),
 (10,2, 6, 'Unidad de Salud Santa Tecla',          'Lun-Vie 7:00-15:00', 3),
 (11,3, 1, 'SERTRACEN San Salvador',               'Lun-Vie 8:00-16:00', 6),
 (12,5, 1, 'Alcaldía San Salvador Centro',         'Lun-Vie 8:00-16:00', 4),
 (13,7, 7, 'Dirección de Migración Las Cascadas',  'Lun-Vie 8:00-16:00', 5);

-- Áreas de servicio
INSERT INTO area_servicio (id_area, nombre, icono, orden) VALUES
 (1,'Documentos e Identidad','badge',1),
 (2,'Salud Pública','local_hospital',2),
 (3,'Transporte VMT','commute',3),
 (4,'Educación','school',4),
 (5,'Servicios Municipales','location_city',5);

-- Trámites
INSERT INTO tramite (id_tramite, id_area, id_institucion, codigo, nombre, arancel, plazo_horas,
                     modalidad, requiere_cita, requiere_biometria, estado) VALUES
 (1, 1,1,'RNPN-DUI-01','Renovación de DUI Ordinario',          10.31, 48,'PRESENCIAL',TRUE, TRUE, 'ACTIVO'),
 (2, 1,1,'RNPN-DUI-02','Reposición de DUI por Deterioro',      10.31, 48,'PRESENCIAL',TRUE, TRUE, 'ACTIVO'),
 (3, 1,1,'RNPN-PN-01', 'Partida de Nacimiento Certificada',     5.00, 24,'DIGITAL',   FALSE,FALSE,'ACTIVO'),
 (4, 2,2,'MINSAL-01',  'Cita odontológica o medicina general',  0.00,NULL,'PRESENCIAL',TRUE, FALSE,'ACTIVO'),
 (5, 2,2,'MINSAL-02',  'Atención pediátrica preventiva',        0.00,NULL,'PRESENCIAL',TRUE, FALSE,'ACTIVO'),
 (6, 2,2,'MINSAL-03',  'Toma de muestras clínicas',             0.00,NULL,'PRESENCIAL',TRUE, FALSE,'ACTIVO'),
 (7, 2,2,'MINSAL-04',  'Esquema nacional de vacunación',        0.00,NULL,'PRESENCIAL',TRUE, FALSE,'ACTIVO'),
 (8, 3,3,'VMT-01',     'Consulta de esquelas por licencia',     0.00,NULL,'DIGITAL',   FALSE,FALSE,'ACTIVO'),
 (9, 3,3,'VMT-02',     'Consulta de esquelas por placa',        0.00,NULL,'DIGITAL',   FALSE,FALSE,'ACTIVO'),
 (10,3,3,'VMT-03',     'Renovación de Licencia de Conducir',   25.00, 72,'PRESENCIAL',TRUE, TRUE, 'ACTIVO'),
 (11,4,4,'MINED-01',   'Certificación de notas o estudios',     0.00, 72,'DIGITAL',   FALSE,FALSE,'PROXIMAMENTE'),
 (12,4,4,'MINED-02',   'Consulta de becas',                     0.00,NULL,'DIGITAL',   FALSE,FALSE,'PROXIMAMENTE'),
 (13,4,4,'MINED-03',   'Constancia de Estudios',                0.00, 48,'DIGITAL',   FALSE,FALSE,'ACTIVO'),
 (14,5,5,'MUNI-01',    'Solvencia Municipal',                   3.00, 24,'DIGITAL',   FALSE,FALSE,'ACTIVO'),
 (15,5,5,'MUNI-02',    'Constancia de Residencia',              2.00, 24,'DIGITAL',   FALSE,FALSE,'ACTIVO');

-- Requisitos (Renovación de DUI)
INSERT INTO requisito (id_tramite, nombre, descripcion, obligatorio, requiere_archivo, tipo_validacion) VALUES
 (1,'DUI anterior o denuncia de extravío','Presentar en físico al momento de la entrega',TRUE, FALSE,'PRESENCIAL'),
 (1,'Comprobante de domicilio reciente',  'Recibo de energía eléctrica o agua no mayor a 3 meses',TRUE, TRUE,'MANUAL'),
 (1,'Fotografía digital en ventanilla',   'Se captura en el Duicentro sin costo adicional',FALSE,FALSE,'PRESENCIAL');

-- Requisitos (Reposición de DUI por deterioro: los mismos de la renovación)
INSERT INTO requisito (id_tramite, nombre, descripcion, obligatorio, requiere_archivo, tipo_validacion)
SELECT 2, nombre, descripcion, obligatorio, requiere_archivo, tipo_validacion FROM requisito WHERE id_tramite = 1;

-- Catálogo de infracciones
INSERT INTO tipo_infraccion (id_tipo_infraccion, codigo, descripcion, gravedad, monto) VALUES
 (1,'TO-01','Estacionar en zona señalizada como eje preferencial o parada de transporte colectivo.','MUY_GRAVE',57.14),
 (2,'LE-05','No portar en lugar visible el comprobante de emisión de gases o extintor vigente.',   'LEVE',     28.57),
 (3,'ME-02','Infracción grave al reglamento de tránsito.',                                         'GRAVE',    34.29);


/* =====================================================================
   10. DATOS DE PRUEBA (ficticios, tomados de las pantallas)
   ===================================================================== */
INSERT INTO usuario (id_usuario, id_rol, dui, nombre_completo, correo, telefono,
                     clave_salt, clave_hash, id_institucion, id_sede, cargo,
                     nivel_verificacion, estado) VALUES
 (1, 4, '00000001-1', 'Administrador del Sistema', 'admin@ciudadano.sv', NULL,
  'a1b2c3d4e5f60718293a4b5c6d7e8f90',
  SHA2(CONCAT('a1b2c3d4e5f60718293a4b5c6d7e8f90','Admin123*'),256),
  NULL, NULL, 'Super Administrador', 2, 'ACTIVO'),
 (2, 2, '01884291-3', 'Douglas Morales', 'douglas.morales@sv.gob.ficticio', '7100-2233',
  'f0e1d2c3b4a5968778695a4b3c2d1e0f',
  SHA2(CONCAT('f0e1d2c3b4a5968778695a4b3c2d1e0f','Revisor123*'),256),
  1, 1, 'Oficial Registral RNPN', 2, 'ACTIVO');

INSERT INTO usuario (id_usuario, id_rol, dui, nombre_completo, correo, telefono,
                     clave_salt, clave_hash, fecha_nacimiento, estado_civil, direccion,
                     id_municipio, nivel_verificacion, dos_factores, estado) VALUES
 (3, 1, '04829104-5', 'Carlos Alberto Hernández Meza', 'carlos.hernandez@sv.gob.ficticio', '7289-4012',
  '0123456789abcdef0123456789abcdef',
  SHA2(CONCAT('0123456789abcdef0123456789abcdef','Ciudadano123*'),256),
  '1991-03-18', 'SOLTERO', 'Colonia Escalón, Pasaje Las Palmeras #124', 1, 2, TRUE, 'ACTIVO');
UPDATE usuario SET id_distrito = (SELECT id_distrito FROM distrito WHERE id_municipio = 1 AND nombre = 'San Salvador')
WHERE id_usuario = 3;

-- Operadora de agenda (MINSAL, Unidad de Salud Arce)
INSERT INTO usuario (id_usuario, id_rol, dui, nombre_completo, correo, telefono,
                     clave_salt, clave_hash, id_institucion, id_sede, cargo,
                     nivel_verificacion, estado) VALUES
 (4, 3, '09999999-9', 'Silvia Margarita Menjívar', 'silvia.menjivar@sv.gob.ficticio', '7111-2222',
  '5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d',
  SHA2(CONCAT('5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d','Operador123*'),256),
  2, 7, 'Operadora de agenda MINSAL', 2, 'ACTIVO');

-- Solicitud de renovación de DUI (EXP-2024-00891)
INSERT INTO solicitud (id_solicitud, folio, id_usuario, id_tramite, id_sede, id_revisor, estado,
                       declaracion_jurada, fecha_creacion) VALUES
 (1,'EXP-2024-00891',3,1,1,2,'EN_REVISION',TRUE,'2024-11-14 09:15:00'),
 (2,'EXP-2024-00630',3,14,12,NULL,'APROBADA', TRUE,'2024-11-10 10:00:00');

INSERT INTO solicitud_dui (id_solicitud, motivo, id_municipio_res) VALUES (1,'VENCIMIENTO',1);
INSERT INTO solicitud_municipal (id_solicitud, id_municipio, domicilio)
 VALUES (2,1,'Colonia Escalón, Pasaje Las Palmeras #124');

INSERT INTO solicitud_historial (id_solicitud, estado_anterior, estado_nuevo, comentario, id_usuario, fecha) VALUES
 (1, NULL,       'RECIBIDA',    'Solicitud enviada por el ciudadano', 3, '2024-11-14 09:15:00'),
 (1, 'RECIBIDA', 'EN_REVISION', 'Asignada a revisor técnico',         2, '2024-11-15 08:30:00'),
 (2, NULL,       'RECIBIDA',    'Solicitud enviada por el ciudadano', 3, '2024-11-10 10:00:00'),
 (2, 'RECIBIDA', 'APROBADA',    'Solvencia emitida con firma electrónica', NULL, '2024-11-13 16:45:00');

INSERT INTO documento (id_solicitud, id_requisito, nombre_archivo, ruta_archivo, tipo_mime, tamano_kb, estado_cotejo) VALUES
 (1, 2,    'recibo_energia_octubre.pdf',        'uploads/EXP-2024-00891/recibo_energia_octubre.pdf',        'application/pdf', 1229, 'VALIDO'),
 (1, NULL, 'formulario_solicitud_firmado.pdf',  'uploads/EXP-2024-00891/formulario_solicitud_firmado.pdf',  'application/pdf',  940, 'PENDIENTE');

-- Cupos y cita médica (Odontología, 18 Nov 10:30)
INSERT INTO cupo (id_cupo, id_sede, id_tramite, fecha, hora_inicio, hora_fin, capacidad, ocupados) VALUES
 (1, 7, 4, '2024-11-18', '10:00:00', '12:00:00', 20, 1),
 (2, 1, 1, '2024-11-18', '08:00:00', '10:00:00', 25, 25),
 (3, 1, 1, '2024-11-22', '10:00:00', '12:00:00', 25, 0);

INSERT INTO cita (id_cita, codigo, id_usuario, id_cupo, id_solicitud, fecha_hora, numero_turno, estado) VALUES
 (1,'CIT-2024-00024',3,1,NULL,'2024-11-18 10:30:00',24,'PROGRAMADA');

-- Multas del ciudadano
INSERT INTO multa (folio, id_tipo_infraccion, dui_infractor, placa, fecha_hora, lugar, agente,
                   monto, fecha_vencimiento, estado, recibo_pago, fecha_pago) VALUES
 ('INFR-2024-88412',1,'04829104-5','P123-456','2024-11-08 14:25:00',
  'Alameda Franklin Delano Roosevelt y 45 Av. Sur, San Salvador','Inspector VMT - Patrulla #14',
  57.14,'2024-11-22','PENDIENTE',NULL,NULL),
 ('INFR-2024-76201',2,'04829104-5','P123-456','2024-10-24 09:10:00',
  'Boulevard de Los Héroes, frente a Centro Judicial','Agente Tránsito PNC - Placa #4418',
  28.57,'2024-11-23','PENDIENTE',NULL,NULL),
 ('INFR-2023-11094',3,'04829104-5','P123-456','2023-05-15 11:00:00',
  'San Salvador','Agente Tránsito PNC',
  34.29,'2023-06-14','PAGADA','REC-992182-SV','2023-05-20 10:00:00');

-- Notificaciones
INSERT INTO notificacion (id_usuario, tipo, titulo, mensaje, id_solicitud, id_cita, leida, fecha) VALUES
 (3,'TRAMITE','Cambio de estado: Renovación de DUI',
  'Su trámite EXP-2024-00891 ha pasado a estado "En revisión" por el oficial asignado.',1,NULL,FALSE,'2024-11-15 08:30:00'),
 (3,'CITA','Recordatorio de Cita Médica',
  'Tiene una cita programada para el 18 de noviembre a las 10:30 AM en Unidad de Salud Arce.',NULL,1,FALSE,'2024-11-18 08:00:00'),
 (3,'TRAMITE','Solvencia Municipal Aprobada',
  'Su solvencia municipal EXP-2024-00630 ya está disponible para descarga.',2,NULL,TRUE,'2024-11-13 16:45:00'),
 (3,'SEGURIDAD','Inicio de sesión seguro',
  'Nuevo acceso detectado desde dispositivo móvil registrado en San Salvador.',NULL,NULL,TRUE,'2024-11-14 07:50:00');

-- Bitácora
INSERT INTO bitacora (id_usuario, accion, descripcion, fecha) VALUES
 (3,'ACTUALIZAR_PERFIL','Teléfono y dirección actualizados con 2FA',             '2024-11-10 11:20:00'),
 (3,'CARGA_ARCHIVO',    'Documento recibo_energia_octubre.pdf adjuntado',        '2024-11-14 09:10:00'),
 (3,'ENVIO_SOLICITUD',  'Solicitud de renovación de DUI EXP-2024-00891 enviada', '2024-11-14 09:15:00'),
 (3,'AGENDAR_CITA',     'Cita de Odontología agendada con turno #24',            '2024-11-15 16:40:00');

-- Sesiones activas
INSERT INTO sesion_dispositivo (id_usuario, dispositivo, ubicacion, activa) VALUES
 (3,'iPhone 15 Pro - App Móvil CiudadanoSV','San Salvador',TRUE),
 (3,'Portal Web Escritorio','San Salvador',TRUE);


/* =====================================================================
   11. VERIFICACIÓN RÁPIDA (debe listar 25 tablas y 4 usuarios)
   ===================================================================== */
SHOW TABLES;
SELECT u.id_usuario, u.dui, u.nombre_completo, u.correo, r.nombre AS rol
FROM usuario u JOIN rol r ON r.id_rol = u.id_rol;

/* Prueba de login (debe devolver 1 fila):
SELECT * FROM usuario
WHERE (correo = 'carlos.hernandez@sv.gob.ficticio' OR dui = 'carlos.hernandez@sv.gob.ficticio')
  AND clave_hash = SHA2(CONCAT(clave_salt, 'Ciudadano123*'), 256);
*/
