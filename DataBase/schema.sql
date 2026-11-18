-- =====================================================================
-- 1. TABLAS DE AUTENTICACIÓN Y AUTORIZACIÓN (RBAC NORMALIZADO)
-- =====================================================================

CREATE TABLE permisos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
);

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE rol_permisos (
    rol_id INT,
    permiso_id INT,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
);

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    esta_activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE usuario_roles (
    usuario_id INT,
    rol_id INT,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- =====================================================================
-- 2. TABLAS DE CONSULTA (CATÁLOGOS PURISTAS - SIN ENUMS)
-- =====================================================================

CREATE TABLE tipos_persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT
);

CREATE TABLE persona_estados_acceso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE visita_estados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE tipos_telefono (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE tipos_correo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE metodos_acceso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_metodo VARCHAR(50) UNIQUE NOT NULL
);

-- =====================================================================
-- 3. GESTIÓN EMPRESARIAL (4FN)
-- =====================================================================

CREATE TABLE empresas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE contactos_empresa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id INT NOT NULL,
    nombre_contacto VARCHAR(100) NOT NULL,
    cargo VARCHAR(50),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

CREATE TABLE telefonos_contacto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contacto_id INT NOT NULL,
    numero_telefono VARCHAR(20) NOT NULL,
    tipo_telefono_id INT NOT NULL,
    FOREIGN KEY (contacto_id) REFERENCES contactos_empresa(id) ON DELETE CASCADE,
    FOREIGN KEY (tipo_telefono_id) REFERENCES tipos_telefono(id)
);

CREATE TABLE correos_contacto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contacto_id INT NOT NULL,
    email VARCHAR(100) NOT NULL,
    tipo_correo_id INT NOT NULL,
    FOREIGN KEY (contacto_id) REFERENCES contactos_empresa(id) ON DELETE CASCADE,
    FOREIGN KEY (tipo_correo_id) REFERENCES tipos_correo(id)
);

-- =====================================================================
-- 4. GESTIÓN DE PERSONAS Y BIOMETRÍA
-- =====================================================================

CREATE TABLE personas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL,
    url_foto VARCHAR(255),
    vector_biometrico JSON
);

CREATE TABLE perfiles_persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL,
    empresa_id INT NOT NULL,
    tipo_persona_id INT NOT NULL,
    estado_acceso_id INT NOT NULL,
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    FOREIGN KEY (tipo_persona_id) REFERENCES tipos_persona(id),
    FOREIGN KEY (estado_acceso_id) REFERENCES persona_estados_acceso(id)
);

CREATE TABLE vehiculos (
    placa VARCHAR(10) PRIMARY KEY,
    descripcion VARCHAR(100)
);

CREATE TABLE persona_vehiculo (
    persona_id INT NOT NULL,
    vehiculo_placa VARCHAR(10) NOT NULL,
    PRIMARY KEY (persona_id, vehiculo_placa),
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    FOREIGN KEY (vehiculo_placa) REFERENCES vehiculos(placa) ON DELETE CASCADE
);

-- =====================================================================
-- 5. TRANSACCIONES (VISITAS E INCIDENTES)
-- =====================================================================

CREATE TABLE visitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL,
    fecha_entrada DATETIME,
    fecha_salida DATETIME,
    estado_visita_id INT NOT NULL,
    visita_aprobada_por INT,
    metodo_ingreso_id INT NOT NULL,
    metodo_salida_id INT,
    FOREIGN KEY (persona_id) REFERENCES personas(id),
    FOREIGN KEY (estado_visita_id) REFERENCES visita_estados(id),
    FOREIGN KEY (visita_aprobada_por) REFERENCES usuarios(id),
    FOREIGN KEY (metodo_ingreso_id) REFERENCES metodos_acceso(id),
    FOREIGN KEY (metodo_salida_id) REFERENCES metodos_acceso(id)
);

CREATE TABLE visita_vehiculo (
    visita_id INT,
    vehiculo_placa VARCHAR(10),
    PRIMARY KEY (visita_id, vehiculo_placa),
    FOREIGN KEY (visita_id) REFERENCES visitas(id) ON DELETE CASCADE,
    FOREIGN KEY (vehiculo_placa) REFERENCES vehiculos(placa) ON DELETE CASCADE
);

CREATE TABLE incidentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    visita_id INT,
    reportado_por_id INT NOT NULL,
    fecha DATETIME NOT NULL,
    descripcion TEXT NOT NULL,
    FOREIGN KEY (visita_id) REFERENCES visitas(id),
    FOREIGN KEY (reportado_por_id) REFERENCES usuarios(id)
);

-- =====================================================================
-- 6. BITÁCORA DE AUDITORÍA
-- =====================================================================

CREATE TABLE bitacora_auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion_realizada VARCHAR(255) NOT NULL,
    tabla_afectada VARCHAR(100),
    registro_id_afectado INT,
    detalles JSON,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);