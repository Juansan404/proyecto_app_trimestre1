-- ============================================================
-- SCRIPT SQL PARA TIDB CLOUD - Base de datos TATTOOAGE
-- ============================================================
-- IMPORTANTE: Ejecutar cada bloque por separado en el SQL Editor
-- de TiDB Cloud. NO ejecutar todo de golpe.
-- ============================================================

-- ============================================================
-- PASO 1: Seleccionar la base de datos
-- ============================================================
USE test;

-- ============================================================
-- PASO 2: Eliminar tablas existentes (si las hay)
-- Ejecutar cada DROP por separado
-- ============================================================
DROP TABLE IF EXISTS `CITAS`;

DROP TABLE IF EXISTS `CLIENTES`;

DROP TABLE IF EXISTS `TATUADORES`;

-- ============================================================
-- PASO 3: Crear tabla CLIENTES
-- ============================================================
CREATE TABLE `CLIENTES` (
  `id_cliente` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `notas` text,
  PRIMARY KEY (`id_cliente`),
  KEY `idx_cliente_nombre` (`nombre`,`apellidos`)
);

-- ============================================================
-- PASO 4: Crear tabla TATUADORES
-- ============================================================
CREATE TABLE `TATUADORES` (
  `id_artista` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id_artista`),
  KEY `idx_tatuador_nombre` (`nombre`,`apellidos`)
);

-- ============================================================
-- PASO 5: Crear tabla CITAS (sin foreign keys)
-- ============================================================
CREATE TABLE `CITAS` (
  `id_cita` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente` int(11) NOT NULL,
  `id_artista` int(11) NOT NULL,
  `fecha_cita` date NOT NULL,
  `duracion_aproximada` int(11) DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT NULL,
  `estado` enum('Pendiente','Confirmada','Cancelada','Completada') DEFAULT 'Pendiente',
  `sala` varchar(50) DEFAULT NULL,
  `foto_diseno` longblob,
  `notas` text,
  PRIMARY KEY (`id_cita`),
  KEY `id_cliente` (`id_cliente`),
  KEY `id_artista` (`id_artista`),
  KEY `idx_cita_fecha` (`fecha_cita`),
  KEY `idx_cita_estado` (`estado`)
);

-- ============================================================
-- PASO 6: Añadir foreign keys a CITAS
-- Ejecutar cada ALTER por separado
-- ============================================================
ALTER TABLE `CITAS` ADD CONSTRAINT `CITAS_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `CLIENTES` (`id_cliente`) ON DELETE CASCADE;

ALTER TABLE `CITAS` ADD CONSTRAINT `CITAS_ibfk_2` FOREIGN KEY (`id_artista`) REFERENCES `TATUADORES` (`id_artista`) ON DELETE CASCADE;

-- ============================================================
-- PASO 7: Insertar datos de ejemplo - CLIENTES
-- ============================================================
INSERT INTO `CLIENTES` (`nombre`, `apellidos`, `telefono`, `email`, `fecha_nacimiento`, `notas`) VALUES
('Carlos', 'García López', '612345678', 'carlos.garcia@email.com', '1995-03-15', 'Cliente habitual, prefiere estilo realista'),
('María', 'Fernández Ruiz', '623456789', 'maria.fernandez@email.com', '1998-07-22', 'Primera vez, quiere tatuaje pequeño'),
('David', 'Martínez Sánchez', '634567890', 'david.martinez@email.com', '1990-11-08', 'Varios tatuajes previos, manga en proceso'),
('Laura', 'López Hernández', '645678901', 'laura.lopez@email.com', '1992-05-30', 'Alérgica a tinta roja'),
('Javier', 'Rodríguez Pérez', '656789012', 'javier.rodriguez@email.com', '1988-09-12', 'Quiere cubrir tatuaje antiguo'),
('Ana', 'González Díaz', '667890123', 'ana.gonzalez@email.com', '2000-01-25', 'Estudiante, presupuesto limitado'),
('Pablo', 'Sánchez Torres', '678901234', 'pablo.sanchez@email.com', '1985-12-03', 'Coleccionista, viene cada mes'),
('Elena', 'Díaz Moreno', '689012345', 'elena.diaz@email.com', '1997-04-18', 'Prefiere sesiones cortas');

-- ============================================================
-- PASO 8: Insertar datos de ejemplo - TATUADORES
-- ============================================================
INSERT INTO `TATUADORES` (`nombre`, `apellidos`, `telefono`, `email`, `activo`) VALUES
('Miguel', 'Ángel Pérez', '611111111', 'miguel.perez@tattooage.com', 1),
('Sara', 'Blanco Ruiz', '622222222', 'sara.blanco@tattooage.com', 1),
('Roberto', 'Negro Jiménez', '633333333', 'roberto.negro@tattooage.com', 1),
('Lucía', 'Rojo Martín', '644444444', 'lucia.rojo@tattooage.com', 0);

-- ============================================================
-- PASO 9: Insertar datos de ejemplo - CITAS 
-- ============================================================

--ESTE ULTIMO PASO PUEDE DAR ERRORES. ACONSEJABLE INSERTAR CITAS A MANO
INSERT INTO `CITAS` (`id_cliente`, `id_artista`, `fecha_cita`, `duracion_aproximada`, `precio`, `estado`, `sala`, `notas`) VALUES
(1, 1, '2026-02-10', 120, 180.00, 'Confirmada', 'A', 'Tatuaje realista de león en el brazo'),
(2, 2, '2026-02-12', 60, 80.00, 'Pendiente', 'B', 'Mariposa pequeña en el tobillo'),
(3, 1, '2026-02-15', 240, 350.00, 'Confirmada', 'A', 'Continuación manga japonés'),
(4, 3, '2026-02-18', 90, 120.00, 'Pendiente', 'C', 'Flores en la espalda - sin tinta roja'),
(5, 2, '2026-02-20', 180, 250.00, 'Confirmada', 'B', 'Cover-up tribal antiguo'),
(6, 3, '2026-02-22', 45, 60.00, 'Pendiente', 'C', 'Frase en la muñeca'),
(7, 1, '2026-02-25', 150, 200.00, 'Confirmada', 'A', 'Retrato mascota'),
(1, 2, '2026-03-01', 90, 130.00, 'Pendiente', 'B', 'Segundo tatuaje - geométrico'),
(3, 1, '2026-03-05', 240, 350.00, 'Pendiente', 'A', 'Tercera sesión manga'),
(8, 3, '2026-02-28', 60, 90.00, 'Confirmada', 'C', 'Diseño minimalista');

-- ============================================================
-- FIN DEL SCRIPT
-- Verifica que todo se haya creado correctamente con:
-- SELECT * FROM CLIENTES;
-- SELECT * FROM TATUADORES;
-- SELECT * FROM CITAS;
-- ============================================================
