USE defaultdb;

-- =====================================================================
-- 1. CATÁLOGOS Y CONFIGURACIONES (Lookup Tables)
-- =====================================================================

INSERT INTO permisos (id, nombre_permiso, descripcion) VALUES
(1, 'crear_usuario', 'Permite crear nuevos usuarios'),
(2, 'editar_usuario', 'Permite modificar usuarios existentes'),
(3, 'eliminar_usuario', 'Permite desactivar usuarios'),
(4, 'ver_usuarios', 'Permite listar usuarios'),
(5, 'crear_empresa', 'Permite registrar nuevas empresas'),
(6, 'editar_empresa', 'Permite modificar empresas'),
(7, 'ver_empresas', 'Permite listar empresas del complejo'),
(8, 'crear_persona', 'Permite registrar trabajadores o invitados'),
(9, 'editar_persona', 'Permite modificar datos de personas'),
(10, 'bloquear_persona', 'Cambia el estado a prohibicion de ingreso'),
(11, 'registrar_visita_manual', 'Permite asentar ingresos manualmente'),
(12, 'aprobar_visita', 'Aprueba ingresos de invitados no anunciados'),
(13, 'rechazar_visita', 'Rechaza ingresos de invitados'),
(14, 'cerrar_visita_forzada', 'Cierra visitas por salidas olvidadas'),
(15, 'reportar_incidente', 'Permite crear un registro de incidente'),
(16, 'ver_incidentes', 'Permite listar incidentes de seguridad'),
(17, 'generar_reporte_visitas', 'Exporta historial de visitas'),
(18, 'generar_reporte_auditoria', 'Exporta la bitacora del sistema'),
(19, 'gestionar_roles', 'Permite asignar roles a usuarios'),
(20, 'gestionar_biometria', 'Permite actualizar vectores faciales');

INSERT INTO roles (id, nombre_rol) VALUES
(1, 'Superusuario'), (2, 'Supervisor de Seguridad'), (3, 'Guarda de Entrada Principal'),
(4, 'Guarda de Parqueadero'), (5, 'Funcionario RRHH'), (6, 'Gerente de Empresa'),
(7, 'Auditor Externo'), (8, 'Administrador del Complejo');

INSERT INTO rol_permisos (rol_id, permiso_id) VALUES
(1,1),(1,2),(1,3),(1,5),(1,8),(1,10),(1,15),(1,18),(1,19),(1,20),
(2,4),(2,7),(2,10),(2,15),(2,16),(2,17),
(3,4),(3,11),(3,15),
(5,8),(5,9),(5,12);

INSERT INTO tipos_persona (id, nombre_tipo, descripcion) VALUES 
(1, 'Trabajador', 'Empleado directo de una empresa del complejo'),
(2, 'Invitado', 'Visitante ocasional o cliente'),
(3, 'Contratista', 'Personal externo con labores temporales'),
(4, 'Proveedor', 'Personal de entregas o suministros'),
(5, 'Auditor', 'Personal de revision externa');

INSERT INTO persona_estados_acceso (id, nombre_estado) VALUES 
(1, 'Activo'), (2, 'Inactivo'), (3, 'Suspendido Temporalmente'), (4, 'Prohibicion Definitiva');

INSERT INTO visita_estados (id, nombre_estado) VALUES 
(1, 'Aprobado'), (2, 'Pendiente Aprobacion'), (3, 'Rechazado'), (4, 'Dentro'), (5, 'Fuera'), (6, 'Cerrada por Sistema');

INSERT INTO tipos_telefono (id, nombre_tipo) VALUES 
(1, 'Movil Personal'), (2, 'Movil Corporativo'), (3, 'Fijo Oficina'), (4, 'Extension'), (5, 'WhatsApp');

INSERT INTO tipos_correo (id, nombre_tipo) VALUES 
(1, 'Corporativo Principal'), (2, 'Personal'), (3, 'Facturacion'), (4, 'Soporte IT');

INSERT INTO metodos_acceso (id, nombre_metodo) VALUES 
(1, 'Manual_Documento'), (2, 'Biometrico_Camara'), (3, 'Tarjeta_RFID'), (4, 'Escaneo_QR'), (5, 'PIN_Digitado');

INSERT INTO tipos_token (id, nombre_tipo) VALUES
(1, 'PIN_Numerico'), (2, 'Codigo_QR'), (3, 'Codigo_Barras');

INSERT INTO token_estados (id, nombre_estado) VALUES
(1, 'Emitido'), (2, 'Usado'), (3, 'Expirado'), (4, 'Revocado');

-- =====================================================================
-- 2. USUARIOS Y ROLES (20 Registros)
-- =====================================================================

INSERT INTO usuarios (id, nombre, email, password, esta_activo) VALUES
(1, 'Johan Sarmiento', 'admin@zonaacme.com', '$2a$10$hash...', 1),
(2, 'Carlos Ruiz', 'cruiz@seguridad.com', '$2a$10$hash...', 1),
(3, 'Ana Gomez', 'agomez@empresa1.com', '$2a$10$hash...', 1),
(4, 'Luis Perez', 'lperez@logistica.com', '$2a$10$hash...', 1),
(5, 'Maria Lopez', 'mlopez@tech.com', '$2a$10$hash...', 1),
(6, 'Jorge Diaz', 'jdiaz@seguridad.com', '$2a$10$hash...', 1),
(7, 'Elena Velez', 'evelez@agro.com', '$2a$10$hash...', 1),
(8, 'Pedro Ramirez', 'pramirez@finanzas.com', '$2a$10$hash...', 0),
(9, 'Laura Torres', 'ltorres@empresa2.com', '$2a$10$hash...', 1),
(10, 'Diego Castro', 'dcastro@seguridad.com', '$2a$10$hash...', 1),
(11, 'Sofia Rojas', 'srojas@ventas.com', '$2a$10$hash...', 1),
(12, 'Andres Ortiz', 'aortiz@sistemas.com', '$2a$10$hash...', 1),
(13, 'Camila Silva', 'csilva@rrhh.com', '$2a$10$hash...', 1),
(14, 'Felipe Morales', 'fmorales@auditoria.com', '$2a$10$hash...', 1),
(15, 'Diana Herrera', 'dherrera@legal.com', '$2a$10$hash...', 1),
(16, 'Oscar Jimenez', 'ojimenez@seguridad.com', '$2a$10$hash...', 1),
(17, 'Natalia Rios', 'nrios@gerencia.com', '$2a$10$hash...', 1),
(18, 'Ivan Acosta', 'iacosta@operaciones.com', '$2a$10$hash...', 1),
(19, 'Paula Navarro', 'pnavarro@marketing.com', '$2a$10$hash...', 1),
(20, 'Roberto Blanco', 'rblanco@mantenimiento.com', '$2a$10$hash...', 1);

INSERT INTO usuario_roles (usuario_id, rol_id) VALUES
(1,1), (2,2), (3,5), (4,6), (5,5), (6,3), (7,6), (8,5), (9,5), (10,4),
(11,5), (12,1), (13,5), (14,7), (15,6), (16,3), (17,8), (18,6), (19,5), (20,4);

-- =====================================================================
-- 3. EMPRESAS Y CONTACTOS (20 Registros cada uno)
-- =====================================================================

INSERT INTO empresas (id, nombre) VALUES
(1, 'Campus Lands S.A.S.'), (2, 'Avicola ERP Solutions'), (3, 'Tech Santander'), (4, 'Logistica Acme'),
(5, 'Desarrollos Hexagonales'), (6, 'AgroData Colombia'), (7, 'Finanzas del Oriente'), (8, 'Inversiones Bucaramanga'),
(9, 'Cloud Systems S.A.'), (10, 'Seguridad Inteligente'), (11, 'Redes y Fibras'), (12, 'Constructora Bellomonte'),
(13, 'Mantenimiento Industrial'), (14, 'Consultoria Legal Acme'), (15, 'Importaciones Globales'), (16, 'DataCenters Andinos'),
(17, 'Agencia de Marketing Digital'), (18, 'Distribuidora de Alimentos'), (19, 'Software a la Medida'), (20, 'Clinica Oftalmologica Acme');

INSERT INTO contactos_empresa (id, empresa_id, nombre_contacto, cargo) VALUES
(1, 1, 'Juan Director', 'Director Academico'), (2, 2, 'Pedro Avicola', 'Gerente de Produccion'),
(3, 3, 'Maria Tech', 'CTO'), (4, 4, 'Luis Logistica', 'Jefe de Despachos'),
(5, 5, 'Ana Dev', 'Arquitecta de Software'), (6, 6, 'Carlos Agro', 'Ingeniero Agronomo'),
(7, 7, 'Elena Finanzas', 'CFO'), (8, 8, 'Jorge Inversor', 'CEO'),
(9, 9, 'Laura Nube', 'SysAdmin'), (10, 10, 'Diego Security', 'Jefe de Seguridad'),
(11, 11, 'Sofia Redes', 'Gerente de Infraestructura'), (12, 12, 'Andres Constructor', 'Ingeniero Residente'),
(13, 13, 'Camila Mantenimiento', 'Coordinadora SST'), (14, 14, 'Felipe Abogado', 'Asesor Juridico'),
(15, 15, 'Diana Imports', 'Gerente de Aduanas'), (16, 16, 'Oscar Data', 'DBA Principal'),
(17, 17, 'Natalia Marketing', 'CMO'), (18, 18, 'Ivan Alimentos', 'Jefe de Bodega'),
(19, 19, 'Paula Code', 'Scrum Master'), (20, 20, 'Roberto Doctor', 'Director Medico');

INSERT INTO telefonos_contacto (id, contacto_id, numero_telefono, tipo_telefono_id) VALUES
(1, 1, '3101112233', 1), (2, 2, '3112223344', 2), (3, 3, '3123334455', 5), (4, 4, '6076431111', 3), (5, 5, '3134445566', 1),
(6, 6, '3145556677', 2), (7, 7, '3156667788', 1), (8, 8, '6076432222', 3), (9, 9, '3167778899', 5), (10, 10, '3178889900', 1),
(11, 11, '3189990011', 2), (12, 12, '3190001122', 1), (13, 13, '6076433333', 3), (14, 14, '3201112222', 1), (15, 15, '3212223333', 5),
(16, 16, '3223334444', 2), (17, 17, '3234445555', 1), (18, 18, '6076434444', 3), (19, 19, '3245556666', 5), (20, 20, '3256667777', 1);

INSERT INTO correos_contacto (id, contacto_id, email, tipo_correo_id) VALUES
(1, 1, 'juan@campus.com', 1), (2, 2, 'pedro@avicola.com', 1), (3, 3, 'maria@tech.com', 1), (4, 4, 'luis@logistica.com', 4), (5, 5, 'ana@hexagonal.com', 1),
(6, 6, 'carlos@agro.com', 1), (7, 7, 'elena@finanzas.com', 3), (8, 8, 'jorge@inversiones.com', 1), (9, 9, 'laura@cloud.com', 4), (10, 10, 'diego@seguridad.com', 1),
(11, 11, 'sofia@redes.com', 1), (12, 12, 'andres@constructora.com', 1), (13, 13, 'camila@mantenimiento.com', 1), (14, 14, 'felipe@legal.com', 1), (15, 15, 'diana@import.com', 3),
(16, 16, 'oscar@data.com', 4), (17, 17, 'natalia@marketing.com', 1), (18, 18, 'ivan@alimentos.com', 1), (19, 19, 'paula@software.com', 1), (20, 20, 'roberto@clinica.com', 1);

-- =====================================================================
-- 4. PERSONAS, PERFILES Y VEHÍCULOS (20 Registros)
-- =====================================================================

INSERT INTO personas (id, nombre, documento_identidad, url_foto, vector_biometrico) VALUES
(1, 'Sebastian Chaves', '1098000111', 'url/foto1.jpg', '[-0.1, 0.5, 0.9, 0.2]'),
(2, 'Alberto Ruiz', '1098000222', 'url/foto2.jpg', '[0.2, -0.1, 0.8, 0.3]'),
(3, 'Camila Londono', '1098000333', 'url/foto3.jpg', '[0.3, 0.2, -0.4, 0.5]'),
(4, 'Daniela Vargas', '1098000444', 'url/foto4.jpg', '[-0.5, 0.3, 0.7, -0.2]'),
(5, 'Eduardo Mejia', '1098000555', 'url/foto5.jpg', '[0.6, -0.4, 0.2, 0.1]'),
(6, 'Fabian Nunez', '1098000666', 'url/foto6.jpg', '[-0.2, 0.8, -0.1, 0.4]'),
(7, 'Gabriela Pinto', '1098000777', 'url/foto7.jpg', '[0.1, 0.1, 0.1, 0.9]'),
(8, 'Hector Reyes', '1098000888', 'url/foto8.jpg', '[0.9, -0.9, 0.5, 0.5]'),
(9, 'Isabel Castro', '1098000999', 'url/foto9.jpg', '[-0.3, -0.3, 0.6, 0.6]'),
(10, 'Julio Cesar', '1098001000', 'url/foto10.jpg', '[0.4, 0.5, 0.6, 0.7]'),
(11, 'Karen Pardo', '1098001111', 'url/foto11.jpg', '[-0.7, 0.2, 0.2, -0.4]'),
(12, 'Leonardo Gil', '1098001222', 'url/foto12.jpg', '[0.8, 0.4, -0.5, 0.2]'),
(13, 'Monica Salas', '1098001333', 'url/foto13.jpg', '[0.5, 0.5, 0.5, -0.5]'),
(14, 'Nestor Cruz', '1098001444', 'url/foto14.jpg', '[-0.6, -0.6, -0.6, 0.1]'),
(15, 'Olga Marina', '1098001555', 'url/foto15.jpg', '[0.1, 0.9, 0.2, 0.8]'),
(16, 'Pablo Emilio', '1098001666', 'url/foto16.jpg', '[0.7, -0.2, 0.3, -0.9]'),
(17, 'Quintin Banderas', '1098001777', 'url/foto17.jpg', '[-0.8, 0.7, -0.4, 0.3]'),
(18, 'Rosa Mendoza', '1098001888', 'url/foto18.jpg', '[0.2, 0.2, 0.9, -0.1]'),
(19, 'Santiago Mora', '1098001999', 'url/foto19.jpg', '[0.3, -0.8, 0.1, 0.4]'),
(20, 'Tatiana Roa', '1098002000', 'url/foto20.jpg', '[-0.9, 0.1, -0.7, 0.5]');

INSERT INTO perfiles_persona (id, persona_id, empresa_id, tipo_persona_id, estado_acceso_id) VALUES
(1, 1, 1, 1, 1), (2, 2, 2, 1, 1), (3, 3, 3, 1, 1), (4, 4, 4, 2, 1), (5, 5, 5, 3, 1),
(6, 6, 6, 1, 1), (7, 7, 7, 1, 1), (8, 8, 8, 4, 1), (9, 9, 9, 1, 1), (10, 10, 10, 1, 1),
(11, 11, 11, 1, 2), (12, 12, 12, 2, 1), (13, 13, 13, 1, 1), (14, 14, 14, 5, 1), (15, 15, 15, 1, 1),
(16, 16, 16, 1, 4), (17, 17, 17, 2, 1), (18, 18, 18, 1, 1), (19, 19, 19, 1, 1), (20, 20, 20, 1, 1);

INSERT INTO vehiculos (placa, descripcion) VALUES
('AAA-111', 'Renault Logan Plata'), ('BBB-222', 'Chevrolet Tracker Blanca'), ('CCC-333', 'Kia Picanto Rojo'), ('DDD-444', 'Moto Yamaha BWS'), ('EEE-555', 'Toyota Hilux Gris'),
('FFF-666', 'Mazda 3 Azul'), ('GGG-777', 'Ford Fiesta Negro'), ('HHH-888', 'Moto Honda CB160'), ('III-999', 'Nissan March Plata'), ('JJJ-000', 'Hyundai Tucson Blanca'),
('KKK-111', 'Volkswagen Jetta Gris'), ('LLL-222', 'Suzuki Swift Rojo'), ('MMM-333', 'Moto AKT NKD 125'), ('NNN-444', 'BMW Serie 3 Negro'), ('OOO-555', 'Audi Q3 Blanco'),
('PPP-666', 'Chevrolet Onix Gris'), ('QQQ-777', 'Renault Duster Verde'), ('RRR-888', 'Moto Pulsar NS200'), ('SSS-999', 'Kia Sportage Plata'), ('TTT-000', 'Toyota Prado TXL Blanca');

INSERT INTO persona_vehiculo (persona_id, vehiculo_placa) VALUES
(1, 'AAA-111'), (2, 'BBB-222'), (3, 'CCC-333'), (4, 'DDD-444'), (5, 'EEE-555'), (6, 'FFF-666'), (7, 'GGG-777'), (8, 'HHH-888'), (9, 'III-999'), (10, 'JJJ-000'),
(11, 'KKK-111'), (12, 'LLL-222'), (13, 'MMM-333'), (14, 'NNN-444'), (15, 'OOO-555'), (16, 'PPP-666'), (17, 'QQQ-777'), (18, 'RRR-888'), (19, 'SSS-999'), (20, 'TTT-000');

-- =====================================================================
-- 5. VISITAS, TOKENS E INCIDENTES (20 Registros cada uno)
-- =====================================================================

INSERT INTO visitas (id, persona_id, fecha_entrada, fecha_salida, estado_visita_id, visita_aprobada_por, metodo_ingreso_id, metodo_salida_id) VALUES
(1, 1, '2026-11-19 07:00:00', '2026-11-19 12:00:00', 5, NULL, 2, 2),
(2, 2, '2026-11-19 07:15:00', NULL, 4, NULL, 2, NULL),
(3, 3, '2026-11-19 07:30:00', '2026-11-19 13:00:00', 5, NULL, 1, 1),
(4, 4, '2026-11-19 08:00:00', NULL, 4, 3, 5, NULL),
(5, 5, '2026-11-19 08:15:00', '2026-11-19 10:15:00', 5, NULL, 2, 2),
(6, 6, '2026-11-19 08:20:00', NULL, 4, NULL, 2, NULL),
(7, 7, '2026-11-19 08:30:00', '2026-11-19 11:30:00', 5, NULL, 4, 2),
(8, 8, '2026-11-19 09:00:00', NULL, 4, 7, 1, NULL),
(9, 9, '2026-11-19 09:15:00', NULL, 4, NULL, 2, NULL),
(10, 10, '2026-11-19 09:30:00', NULL, 2, NULL, 1, NULL),
(11, 11, '2026-11-19 09:45:00', '2026-11-19 10:00:00', 5, NULL, 2, 2),
(12, 12, '2026-11-19 10:00:00', NULL, 4, 15, 4, NULL),
(13, 13, '2026-11-19 10:15:00', NULL, 4, NULL, 2, NULL),
(14, 14, '2026-11-19 10:30:00', '2026-11-19 11:00:00', 5, NULL, 1, 1),
(15, 15, '2026-11-19 10:45:00', NULL, 4, NULL, 2, NULL),
(16, 16, '2026-11-19 11:00:00', '2026-11-19 11:05:00', 3, NULL, 1, NULL),
(17, 17, '2026-11-19 11:15:00', NULL, 4, 17, 4, NULL),
(18, 18, '2026-11-19 11:30:00', NULL, 4, NULL, 2, NULL),
(19, 19, '2026-11-19 11:45:00', NULL, 4, NULL, 2, NULL),
(20, 20, '2026-11-19 12:00:00', NULL, 4, NULL, 2, NULL);

INSERT INTO visita_vehiculo (visita_id, vehiculo_placa) VALUES
(1, 'AAA-111'), (2, 'BBB-222'), (3, 'CCC-333'), (4, 'DDD-444'), (5, 'EEE-555'), (6, 'FFF-666'), (7, 'GGG-777'), (8, 'HHH-888'), (9, 'III-999'), (10, 'JJJ-000'),
(11, 'KKK-111'), (12, 'LLL-222'), (13, 'MMM-333'), (14, 'NNN-444'), (15, 'OOO-555'), (16, 'PPP-666'), (17, 'QQQ-777'), (18, 'RRR-888'), (19, 'SSS-999'), (20, 'TTT-000');

INSERT INTO tokens_acceso (id, visita_id, codigo_token, tipo_token_id, estado_token_id, fecha_emision, fecha_expiracion) VALUES
(1, 1, '834921', 1, 2, '2026-11-18 14:00:00', '2026-12-31 23:59:59'),
(2, 2, 'QR-A1B2C3D4E5', 2, 2, '2026-11-18 15:30:00', '2026-12-31 23:59:59'),
(3, 3, 'CB-9988776655', 3, 2, '2026-11-19 06:00:00', '2026-12-31 18:00:00'),
(4, 4, '159263', 1, 1, '2026-11-19 07:45:00', '2026-12-31 23:59:59'),
(5, 5, 'QR-X9Y8Z7W6V5', 2, 2, '2026-11-17 10:00:00', '2026-12-31 23:59:59'),
(6, 6, '482019', 1, 2, '2026-11-19 08:00:00', '2026-12-31 23:59:59'),
(7, 7, 'QR-F6G7H8I9J0', 2, 2, '2026-11-19 08:10:00', '2026-12-31 23:59:59'),
(8, 8, 'CB-1122334455', 3, 1, '2026-11-19 08:30:00', '2026-12-31 23:59:59'),
(9, 9, '739104', 1, 2, '2026-11-18 09:00:00', '2026-12-31 12:00:00'),
(10, 10, 'QR-M4N5O6P7Q8', 2, 4, '2026-11-19 09:00:00', '2026-12-31 18:00:00'),
(11, 11, '610293', 1, 2, '2026-11-19 09:15:00', '2026-12-31 23:59:59'),
(12, 12, 'QR-R1S2T3U4V5', 2, 1, '2026-11-19 09:30:00', '2026-12-31 23:59:59'),
(13, 13, 'CB-5544332211', 3, 2, '2026-11-18 10:00:00', '2026-12-31 23:59:59'),
(14, 14, '902145', 1, 2, '2026-11-19 10:15:00', '2026-12-31 23:59:59'),
(15, 15, 'QR-W9X8Y7Z6A5', 2, 2, '2026-11-19 10:30:00', '2026-12-31 23:59:59'),
(16, 16, '384756', 1, 3, '2026-11-15 08:00:00', '2026-11-15 23:59:59'),
(17, 17, 'QR-B1C2D3E4F5', 2, 1, '2026-11-19 11:00:00', '2026-12-31 23:59:59'),
(18, 18, 'CB-6677889900', 3, 2, '2026-11-19 11:15:00', '2026-12-31 23:59:59'),
(19, 19, '571920', 1, 2, '2026-11-19 11:30:00', '2026-12-31 23:59:59'),
(20, 20, 'QR-G6H7I8J9K0', 2, 1, '2026-11-19 11:45:00', '2026-12-31 23:59:59');

INSERT INTO incidentes (id, visita_id, reportado_por_id, fecha, descripcion) VALUES
(1, 4, 2, '2026-11-19 08:05:00', 'Invitado ingreso sin casco a zona de bodegas'),
(2, 6, 6, '2026-11-19 08:25:00', 'Trabajador olvido el carnet, validado por camara'),
(3, 8, 2, '2026-11-19 09:10:00', 'Proveedor estaciono en zona de gerencia'),
(4, 10, 6, '2026-11-19 09:35:00', 'Visitante alterado por tiempo de espera en porteria'),
(5, 16, 2, '2026-11-19 11:02:00', 'Intento de ingreso de persona con prohibicion activa. Se retiro pacificamente'),
(6, 1, 6, '2026-11-19 12:05:00', 'Salida de vehiculo con reporte de luces averiadas'),
(7, 3, 2, '2026-11-19 07:35:00', 'Reporte de perdida de gafete temporal'),
(8, 7, 6, '2026-11-19 08:40:00', 'Ingreso de material sin guia de despacho'),
(9, 12, 2, '2026-11-19 10:10:00', 'Visitante se desvio de la ruta asignada'),
(10, 14, 6, '2026-11-19 10:40:00', 'Auditor solicito escolta de seguridad'),
(11, 2, 2, '2026-11-19 07:20:00', 'Activacion accidental de alarma en puerta 2'),
(12, 5, 6, '2026-11-19 08:20:00', 'Contratista ingreso herramientas no declaradas'),
(13, 9, 2, '2026-11-19 09:20:00', 'Falla temporal en talanquera durante ingreso'),
(14, 11, 6, '2026-11-19 09:50:00', 'Trabajador reporto perdida de llaves de su vehiculo'),
(15, 13, 2, '2026-11-19 10:20:00', 'Derrame de liquido en zona de parqueo asignada'),
(16, 15, 6, '2026-11-19 10:50:00', 'Conato de discusion en zona de fumadores'),
(17, 17, 2, '2026-11-19 11:20:00', 'Invitado requirio asistencia medica leve'),
(18, 18, 6, '2026-11-19 11:35:00', 'Trabajador ingreso mascota no autorizada'),
(19, 19, 2, '2026-11-19 11:50:00', 'Camara biometrica requirio limpieza de lente'),
(20, 20, 6, '2026-11-19 12:05:00', 'Vehiculo goteando aceite en parqueadero principal');

-- =====================================================================
-- 6. BITÁCORA DE AUDITORÍA (20 Registros)
-- =====================================================================

INSERT INTO bitacora_auditoria (id, usuario_id, fecha_hora, accion_realizada, tabla_afectada, registro_id_afectado, detalles) VALUES
(1, 1, '2026-11-19 06:00:00', 'LOGIN_EXITOSO', 'usuarios', 1, '{"ip": "192.168.1.5"}'),
(2, 2, '2026-11-19 06:50:00', 'INICIO_TURNO_GUARDA', 'usuarios', 2, '{"puesto": "Entrada Principal"}'),
(3, 2, '2026-11-19 07:00:00', 'CHECK_IN_BIOMETRICO', 'visitas', 1, '{"coincidencia": "99.1%"}'),
(4, 2, '2026-11-19 07:15:00', 'CHECK_IN_BIOMETRICO', 'visitas', 2, '{"coincidencia": "98.5%"}'),
(5, 6, '2026-11-19 07:30:00', 'CHECK_IN_MANUAL', 'visitas', 3, '{"metodo": "Cedula Fisica"}'),
(6, 3, '2026-11-19 07:55:00', 'APROBACION_VISITA', 'visitas', 4, '{"funcionario": "Ana Gomez"}'),
(7, 2, '2026-11-19 08:05:00', 'CREACION_INCIDENTE', 'incidentes', 1, '{"gravedad": "Baja"}'),
(8, 2, '2026-11-19 08:15:00', 'CHECK_IN_BIOMETRICO', 'visitas', 5, '{"coincidencia": "97.8%"}'),
(9, 6, '2026-11-19 08:25:00', 'CREACION_INCIDENTE', 'incidentes', 2, '{"gravedad": "Informativa"}'),
(10, 7, '2026-11-19 08:50:00', 'APROBACION_VISITA', 'visitas', 8, '{"funcionario": "Elena Velez"}'),
(11, 2, '2026-11-19 09:10:00', 'CREACION_INCIDENTE', 'incidentes', 3, '{"gravedad": "Media"}'),
(12, 6, '2026-11-19 09:30:00', 'REGISTRO_VISITANTE_NUEVO', 'visitas', 10, '{"estado": "Pendiente"}'),
(13, 6, '2026-11-19 09:35:00', 'CREACION_INCIDENTE', 'incidentes', 4, '{"gravedad": "Media"}'),
(14, 2, '2026-11-19 10:00:00', 'CHECK_OUT_BIOMETRICO', 'visitas', 11, '{"coincidencia": "99.0%"}'),
(15, 15, '2026-11-19 10:10:00', 'APROBACION_VISITA', 'visitas', 12, '{"funcionario": "Diana Herrera"}'),
(16, 2, '2026-11-19 11:00:00', 'ACCESO_DENEGADO', 'visitas', 16, '{"motivo": "Persona con prohibicion activa"}'),
(17, 2, '2026-11-19 11:02:00', 'CREACION_INCIDENTE', 'incidentes', 5, '{"gravedad": "Alta"}'),
(18, 17, '2026-11-19 11:10:00', 'APROBACION_VISITA', 'visitas', 17, '{"funcionario": "Natalia Rios"}'),
(19, 2, '2026-11-19 12:00:00', 'CHECK_OUT_BIOMETRICO', 'visitas', 1, '{"coincidencia": "99.5%"}'),
(20, 6, '2026-11-19 12:05:00', 'CREACION_INCIDENTE', 'incidentes', 20, '{"gravedad": "Baja"}');