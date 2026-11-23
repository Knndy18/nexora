-- ========================================================
-- NEXORA - Esquema de Base de Datos SQLite (DDL)
-- ========================================================

PRAGMA foreign_keys = ON;

-- Tabla: categoria
CREATE TABLE IF NOT EXISTS categoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE,
    descripcion TEXT
);

-- Tabla: proveedor
CREATE TABLE IF NOT EXISTS proveedor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    ruc TEXT NOT NULL UNIQUE,
    telefono TEXT,
    email TEXT,
    direccion TEXT
);

-- Tabla: producto
CREATE TABLE IF NOT EXISTS producto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    id_categoria INTEGER NOT NULL REFERENCES categoria(id),
    precio_compra REAL NOT NULL CHECK (precio_compra >= 0),
    precio_venta REAL NOT NULL CHECK (precio_venta >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo INTEGER NOT NULL DEFAULT 0,
    tipo TEXT NOT NULL CHECK (tipo IN ('UNITARIO', 'PERECIBLE')),
    fecha_vencimiento TEXT,
    activo INTEGER NOT NULL DEFAULT 1
);

-- Tabla: venta
CREATE TABLE IF NOT EXISTS venta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,
    subtotal REAL NOT NULL,
    descuento REAL NOT NULL DEFAULT 0,
    total REAL NOT NULL
);

-- Tabla: detalle_venta
CREATE TABLE IF NOT EXISTS detalle_venta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta INTEGER NOT NULL REFERENCES venta(id) ON DELETE CASCADE,
    id_producto INTEGER NOT NULL REFERENCES producto(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario REAL NOT NULL,
    subtotal REAL NOT NULL
);

-- Tabla: compra
CREATE TABLE IF NOT EXISTS compra (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_proveedor INTEGER NOT NULL REFERENCES proveedor(id),
    fecha TEXT NOT NULL,
    total REAL NOT NULL,
    comprobante TEXT
);

-- Tabla: detalle_compra
CREATE TABLE IF NOT EXISTS detalle_compra (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_compra INTEGER NOT NULL REFERENCES compra(id) ON DELETE CASCADE,
    id_producto INTEGER NOT NULL REFERENCES producto(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario REAL NOT NULL,
    subtotal REAL NOT NULL
);

-- Tabla: movimiento_stock
CREATE TABLE IF NOT EXISTS movimiento_stock (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto INTEGER NOT NULL REFERENCES producto(id),
    tipo TEXT NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    cantidad INTEGER NOT NULL,
    fecha TEXT NOT NULL,
    motivo TEXT
);

-- Índices para optimizar consultas de inventario, POS y reportes
CREATE INDEX IF NOT EXISTS idx_producto_codigo ON producto(codigo);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(id_categoria);
CREATE INDEX IF NOT EXISTS idx_venta_fecha ON venta(fecha);
CREATE INDEX IF NOT EXISTS idx_compra_fecha ON compra(fecha);
CREATE INDEX IF NOT EXISTS idx_movimiento_producto ON movimiento_stock(id_producto);
