-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 18-11-2025 a las 11:07:34
-- Versión del servidor: 5.7.35-0ubuntu0.18.04.2
-- Versión de PHP: 8.0.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `tattooage`
--
CREATE DATABASE IF NOT EXISTS `tattooage` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `tattooage`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `CITAS`
--

DROP TABLE IF EXISTS `CITAS`;
CREATE TABLE `CITAS` (
  `id_cita` int(11) NOT NULL,
  `id_cliente` int(11) NOT NULL,
  `id_artista` int(11) NOT NULL,
  `fecha_cita` date NOT NULL,
  `duracion_aproximada` int(11) DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT NULL,
  `estado` enum('Pendiente','Confirmada','Cancelada','Completada') COLLATE utf8mb4_unicode_ci DEFAULT 'Pendiente',
  `sala` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `foto_diseno` longblob,
  `notas` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `CITAS`
--

INSERT INTO `CITAS` (`id_cita`, `id_cliente`, `id_artista`, `fecha_cita`, `duracion_aproximada`, `precio`, `estado`, `sala`, `foto_diseno`, `notas`) VALUES
(1, 25, 23, '2024-12-20', 120, '150.00', 'Confirmada', 'Sala A', NULL, 'Tatuaje de dragón en el brazo'),
(2, 26, 24, '2024-12-21', 90, '120.00', 'Pendiente', 'Sala B', NULL, 'Diseño minimalista en la muñeca'),
(3, 27, 25, '2024-12-22', 180, '250.00', 'Confirmada', 'Sala A', NULL, 'Primera sesión de manga completa'),
(4, 28, 26, '2024-12-23', 60, '80.00', 'Completada', 'Sala C', NULL, 'Tatuaje pequeño de gato'),
(5, 29, 23, '2024-12-24', 240, '400.00', 'Confirmada', 'Sala A', NULL, 'Espalda completa - sesión 2'),
(6, 30, 28, '2024-12-26', 120, '150.00', 'Pendiente', 'Sala B', NULL, 'Flores en el antebrazo'),
(7, 31, 29, '2024-12-27', 150, '200.00', 'Confirmada', 'Sala A', NULL, 'Tribal en pierna'),
(8, 32, 30, '2024-12-28', 90, '130.00', 'Cancelada', 'Sala C', NULL, 'Cliente canceló por motivos personales'),
(9, 33, 26, '2024-12-29', 180, '220.00', 'Confirmada', 'Sala B', NULL, 'Diseño geométrico en costillas'),
(10, 34, 32, '2024-12-30', 120, '180.00', 'Pendiente', 'Sala A', NULL, 'Cover-up de tatuaje antiguo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `CLIENTES`
--

DROP TABLE IF EXISTS `CLIENTES`;
CREATE TABLE `CLIENTES` (
  `id_cliente` int(11) NOT NULL,
  `nombre` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellidos` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `notas` text COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `CLIENTES`
--

INSERT INTO `CLIENTES` (`id_cliente`, `nombre`, `apellidos`, `telefono`, `email`, `fecha_nacimiento`, `notas`) VALUES
(25, 'Carlos', 'García López', '612345678', 'carlos.garcia@email.com', '1995-03-15', 'Cliente regular, prefiere estilos tradicionales'),
(26, 'María', 'Rodríguez Pérez', '623456789', 'maria.rodriguez@email.com', '1990-07-22', 'Interesada en diseños minimalistas'),
(27, 'Juan', 'Martínez Sánchez', '634567890', 'juan.martinez@email.com', '1988-11-10', 'Primera vez haciéndose un tatuaje'),
(28, 'Ana', 'López Fernández', '645678901', 'ana.lopez@email.com', '1992-05-18', 'Colecciona tatuajes de animales'),
(29, 'Pedro', 'González Ruiz', '656789012', 'pedro.gonzalez@email.com', '1985-09-03', 'Cliente VIP'),
(30, 'Laura', 'Hernández Díaz', '667890123', 'laura.hernandez@email.com', '1998-12-25', 'Alérgica a ciertos pigmentos'),
(31, 'Diego', 'Jiménez Castro', '678901234', 'diego.jimenez@email.com', '1993-04-08', 'Prefiere sesiones cortas'),
(32, 'Sofía', 'Moreno Ramos', '689012345', 'sofia.moreno@email.com', '1996-08-14', 'Interesada en tatuajes geométricos'),
(33, 'Miguel', 'Álvarez Torres', '690123456', 'miguel.alvarez@email.com', '1987-01-30', 'Cliente desde 2020'),
(34, 'Carmen', 'Romero Navarro', '601234567', 'carmen.romero@email.com', '1991-06-05', 'Busca cover-up de tatuaje antiguo');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `TATUADORES`
--

DROP TABLE IF EXISTS `TATUADORES`;
CREATE TABLE `TATUADORES` (
  `id_artista` int(11) NOT NULL,
  `nombre` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellidos` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `TATUADORES`
--

INSERT INTO `TATUADORES` (`id_artista`, `nombre`, `apellidos`, `telefono`, `email`, `activo`) VALUES
(23, 'Roberto', 'Ink Master', '611111111', 'roberto.ink@tattooage.com', 1),
(24, 'Elena', 'Tattoo Pro', '622222222', 'elena.pro@tattooage.com', 1),
(25, 'Javier', 'Dark Artist', '633333333', 'javier.dark@tattooage.com', 1),
(26, 'Natalia', 'Color Queen', '644444444', 'natalia.color@tattooage.com', 1),
(27, 'Alberto', 'Skin Wizard', '655555555', 'alberto.wizard@tattooage.com', 0),
(28, 'Patricia', 'Fine Line', '666666666', 'patricia.line@tattooage.com', 1),
(29, 'Fernando', 'Tribal King', '677777777', 'fernando.tribal@tattooage.com', 1),
(30, 'Raquel', 'Neo Traditional', '688888888', 'raquel.neo@tattooage.com', 1),
(31, 'Sergio', 'Blackwork Master', '699999999', 'sergio.black@tattooage.com', 0),
(32, 'Cristina', 'Watercolor Art', '600000000', 'cristina.water@tattooage.com', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `CITAS`
--
ALTER TABLE `CITAS`
  ADD PRIMARY KEY (`id_cita`),
  ADD KEY `id_cliente` (`id_cliente`),
  ADD KEY `id_artista` (`id_artista`),
  ADD KEY `idx_cita_fecha` (`fecha_cita`),
  ADD KEY `idx_cita_estado` (`estado`);

--
-- Indices de la tabla `CLIENTES`
--
ALTER TABLE `CLIENTES`
  ADD PRIMARY KEY (`id_cliente`),
  ADD KEY `idx_cliente_nombre` (`nombre`,`apellidos`);

--
-- Indices de la tabla `TATUADORES`
--
ALTER TABLE `TATUADORES`
  ADD PRIMARY KEY (`id_artista`),
  ADD KEY `idx_tatuador_nombre` (`nombre`,`apellidos`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `CITAS`
--
ALTER TABLE `CITAS`
  MODIFY `id_cita` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `CLIENTES`
--
ALTER TABLE `CLIENTES`
  MODIFY `id_cliente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT de la tabla `TATUADORES`
--
ALTER TABLE `TATUADORES`
  MODIFY `id_artista` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `CITAS`
--
ALTER TABLE `CITAS`
  ADD CONSTRAINT `CITAS_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `CLIENTES` (`id_cliente`) ON DELETE CASCADE,
  ADD CONSTRAINT `CITAS_ibfk_2` FOREIGN KEY (`id_artista`) REFERENCES `TATUADORES` (`id_artista`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
