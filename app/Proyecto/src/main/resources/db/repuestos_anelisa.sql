
-- 1. Base de datos ---------------------------------------------------
CREATE DATABASE IF NOT EXISTS repuestos_anelisa
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE repuestos_anelisa;

-- 2. Tablas ----------------------------------------------------------

-- RF04 / RF06: usuarios y roles
CREATE TABLE IF NOT EXISTS usuarios (
                                        id              INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                        nombre          VARCHAR(100) NOT NULL,
    usuario         VARCHAR(50)  NOT NULL,
    contrasena_hash CHAR(64)     NOT NULL COMMENT 'SHA-256 en hexadecimal',
    rol             ENUM('ADMINISTRADOR','EMPLEADO') NOT NULL,
    activo          TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_usuario (usuario)
    ) ENGINE=InnoDB;

-- Proveedores (RF02: una compra exige proveedor registrado)
CREATE TABLE IF NOT EXISTS proveedores (
                                           id       INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                           nombre   VARCHAR(100) NOT NULL,
    contacto VARCHAR(150) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    UNIQUE KEY uk_proveedores_nombre (nombre)
    ) ENGINE=InnoDB;

-- RF01 / RF05: inventario
CREATE TABLE IF NOT EXISTS productos (
                                         id           INT UNSIGNED  NOT NULL AUTO_INCREMENT,
                                         codigo       VARCHAR(30)   NOT NULL,
    nombre       VARCHAR(150)  NOT NULL,
    categoria    VARCHAR(60)   NOT NULL,
    cantidad     INT           NOT NULL DEFAULT 0,
    precio       DECIMAL(12,2) NOT NULL DEFAULT 0,
    proveedor_id INT UNSIGNED  NULL,
    descripcion  TEXT          NULL,
    foto         VARCHAR(255)  NOT NULL DEFAULT '' COMMENT 'nombre del archivo en app-data/fotos',
    PRIMARY KEY (id),
    UNIQUE KEY uk_productos_codigo (codigo),
    KEY ix_productos_nombre (nombre),
    KEY ix_productos_categoria (categoria),
    CONSTRAINT fk_productos_proveedor FOREIGN KEY (proveedor_id)
    REFERENCES proveedores (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT ck_productos_cantidad CHECK (cantidad >= 0),
    CONSTRAINT ck_productos_precio   CHECK (precio >= 0)
    ) ENGINE=InnoDB;

-- RF02: ventas (salidas)
CREATE TABLE IF NOT EXISTS ventas (
                                      id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                      fecha      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      usuario_id INT UNSIGNED NOT NULL,
                                      cliente    VARCHAR(120) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    KEY ix_ventas_fecha (fecha),
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON UPDATE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS venta_detalle (
                                             id              INT UNSIGNED  NOT NULL AUTO_INCREMENT,
                                             venta_id        INT UNSIGNED  NOT NULL,
                                             producto_id     INT UNSIGNED  NULL,
                                             codigo          VARCHAR(30)   NOT NULL COMMENT 'copia del codigo al momento de vender',
    nombre          VARCHAR(150)  NOT NULL COMMENT 'copia del nombre al momento de vender',
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_vdet_venta (venta_id),
    CONSTRAINT fk_vdet_venta FOREIGN KEY (venta_id)
    REFERENCES ventas (id) ON DELETE CASCADE,
    CONSTRAINT fk_vdet_producto FOREIGN KEY (producto_id)
    REFERENCES productos (id) ON DELETE SET NULL,
    CONSTRAINT ck_vdet_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_vdet_precio   CHECK (precio_unitario >= 0)
    ) ENGINE=InnoDB;

-- RF02: compras (entradas)
CREATE TABLE IF NOT EXISTS compras (
                                       id           INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                       fecha        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       proveedor_id INT UNSIGNED NOT NULL,
                                       usuario_id   INT UNSIGNED NOT NULL,
                                       PRIMARY KEY (id),
    KEY ix_compras_fecha (fecha),
    CONSTRAINT fk_compras_proveedor FOREIGN KEY (proveedor_id)
    REFERENCES proveedores (id) ON UPDATE CASCADE,
    CONSTRAINT fk_compras_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON UPDATE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS compra_detalle (
                                              id              INT UNSIGNED  NOT NULL AUTO_INCREMENT,
                                              compra_id       INT UNSIGNED  NOT NULL,
                                              producto_id     INT UNSIGNED  NULL,
                                              codigo          VARCHAR(30)   NOT NULL,
    nombre          VARCHAR(150)  NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL COMMENT 'costo unitario de la compra',
    PRIMARY KEY (id),
    KEY ix_cdet_compra (compra_id),
    CONSTRAINT fk_cdet_compra FOREIGN KEY (compra_id)
    REFERENCES compras (id) ON DELETE CASCADE,
    CONSTRAINT fk_cdet_producto FOREIGN KEY (producto_id)
    REFERENCES productos (id) ON DELETE SET NULL,
    CONSTRAINT ck_cdet_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_cdet_precio   CHECK (precio_unitario >= 0)
    ) ENGINE=InnoDB;

-- 3. Vistas utiles para el panel de inicio y los reportes -----------

-- Productos con poco stock (la app usa umbral 5: Config.UMBRAL_POCO_STOCK)
CREATE OR REPLACE VIEW v_poco_stock AS
SELECT codigo, nombre, categoria, cantidad
FROM productos
WHERE cantidad <= 5
ORDER BY cantidad, nombre;

-- Total de cada venta
CREATE OR REPLACE VIEW v_ventas_total AS
SELECT v.id, v.fecha, u.usuario, v.cliente,
       SUM(d.cantidad * d.precio_unitario) AS total
FROM ventas v
         JOIN usuarios u       ON u.id = v.usuario_id
         JOIN venta_detalle d  ON d.venta_id = v.id
GROUP BY v.id, v.fecha, u.usuario, v.cliente;

-- Total de cada compra
CREATE OR REPLACE VIEW v_compras_total AS
SELECT c.id, c.fecha, p.nombre AS proveedor, u.usuario,
       SUM(d.cantidad * d.precio_unitario) AS total
FROM compras c
         JOIN proveedores p    ON p.id = c.proveedor_id
         JOIN usuarios u       ON u.id = c.usuario_id
         JOIN compra_detalle d ON d.compra_id = c.id
GROUP BY c.id, c.fecha, p.nombre, u.usuario;

-- 4. Datos iniciales (los mismos de demostracion de la app) ---------

INSERT IGNORE INTO usuarios (nombre, usuario, contrasena_hash, rol, activo) VALUES
 ('Anelisa',    'anelisa',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR', 1),
 ('Empleado 1', 'empleado1', 'e03d3ec8d5035f8721f5dc64546e59ed790dbcb3b7b598fe57057ccd7b683b00', 'EMPLEADO',      1);

INSERT IGNORE INTO proveedores (nombre, contacto) VALUES
 ('Proveedor 1', ''), ('Proveedor 2', ''), ('Proveedor 3', ''), ('Proveedor 4', '');

INSERT IGNORE INTO productos (codigo, nombre, categoria, cantidad, precio, proveedor_id, descripcion, foto)
SELECT t.codigo, t.nombre, t.categoria, t.cantidad, t.precio, p.id, '', ''
FROM (
         SELECT 'A-001' AS codigo, 'Pastillas de freno delanteras' AS nombre, 'Frenos' AS categoria, 12 AS cantidad, 45000 AS precio, 'Proveedor 1' AS prov UNION ALL
         SELECT 'A-002', 'Disco de freno',          'Frenos',      8, 120000, 'Proveedor 2' UNION ALL
         SELECT 'B-010', 'Batería 12V',             'Eléctrico',  25, 280000, 'Proveedor 1' UNION ALL
         SELECT 'B-021', 'Amortiguador trasero',    'Suspensión',  3, 190000, 'Proveedor 3' UNION ALL
         SELECT 'C-005', 'Filtro de aceite',        'Filtros',    40,  18000, 'Proveedor 2' UNION ALL
         SELECT 'C-007', 'Filtro de aire',          'Filtros',     0,  25000, 'Proveedor 2' UNION ALL
         SELECT 'C-033', 'Espejo retrovisor',       'Accesorios',  6,  60000, 'Proveedor 4' UNION ALL
         SELECT 'D-001', 'Bujía',                   'Motor',      50,  12000, 'Proveedor 1' UNION ALL
         SELECT 'D-014', 'Correa de distribución',  'Motor',       2,  95000, 'Proveedor 3' UNION ALL
         SELECT 'E-002', 'Bombillo H4',             'Eléctrico',  30,   9000, 'Proveedor 4' UNION ALL
         SELECT 'E-010', 'Cable de bujía',          'Eléctrico',  15,  22000, 'Proveedor 1' UNION ALL
         SELECT 'F-003', 'Aceite 20W-50 (litro)',   'Lubricantes',35,  28000, 'Proveedor 2'
     ) t
         JOIN proveedores p ON p.nombre = t.prov;

-- 5. Usuario de la aplicacion (solo lo que necesita, NO es root) ----

CREATE USER IF NOT EXISTS 'anelisa_app'@'localhost' IDENTIFIED BY 'Anelisa#2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON repuestos_anelisa.* TO 'anelisa_app'@'localhost';
FLUSH PRIVILEGES;

