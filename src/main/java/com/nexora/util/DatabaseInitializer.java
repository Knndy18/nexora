package com.nexora.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Inicializador de base de datos SQLite.
 * Ejecuta el script DDL de creación de tablas y si la base está vacía,
 * inserta datos de prueba realistas para demostración y evaluación.
 */
public class DatabaseInitializer {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void initialize() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            executeSchemaScript(conn);
            seedInitialDataIfEmpty(conn);
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeSchemaScript(Connection conn) throws Exception {
        InputStream is = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql");
        if (is == null) {
            throw new IllegalStateException("No se encontró el archivo /db/schema.sql en resources.");
        }

        StringBuilder sql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--")) {
                    continue;
                }
                sql.append(line).append("\n");
            }
        }

        String[] statements = sql.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private static void seedInitialDataIfEmpty(Connection conn) throws SQLException {
        // Verificar si la tabla producto ya tiene registros
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM producto")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Ya contiene datos
            }
        }

        System.out.println("Sembrando datos iniciales en la base de datos NEXORA...");

        conn.setAutoCommit(false);
        try {
            // 1. Categorías
            String insertCat = "INSERT OR IGNORE INTO categoria (id, nombre, descripcion) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertCat)) {
                Object[][] categorias = {
                    {1, "Bebidas", "Gaseosas, aguas, jugos e hidratantes"},
                    {2, "Abarrotes", "Arroz, azúcar, fideos, aceites y conservas"},
                    {3, "Lácteos y Embutidos", "Leche, quesos, yogures, jamones y embutidos"},
                    {4, "Limpieza del Hogar", "Detergentes, lejías, jabones y desinfectantes"},
                    {5, "Snacks y Golosinas", "Galletas, chocolates, piqueos y caramelos"}
                };
                for (Object[] row : categorias) {
                    ps.setInt(1, (int) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.executeUpdate();
                }
            }

            // 2. Proveedores
            String insertProv = "INSERT OR IGNORE INTO proveedor (id, nombre, ruc, telefono, email, direccion) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertProv)) {
                Object[][] proveedores = {
                    {1, "Distribuidora San José S.A.C.", "20548963211", "954123890", "ventas@distribuidorasanjose.pe", "Av. Parra 124, Arequipa"},
                    {2, "Comercializadora del Sur E.I.R.L.", "20489632541", "987456123", "contacto@delsur.com.pe", "Calle Mercaderes 305, Arequipa"},
                    {3, "Distribuidora Gloria del Altiplano", "20100190797", "958741258", "atencion@gloria.com.pe", "Parque Industrial MZ-E Lote 4, Arequipa"},
                    {4, "Inversiones y Alimentos del Perú S.A.", "20601248795", "941258963", "pedidos@alimentosperu.pe", "Av. Jesús 810, Arequipa"}
                };
                for (Object[] row : proveedores) {
                    ps.setInt(1, (int) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setString(4, (String) row[3]);
                    ps.setString(5, (String) row[4]);
                    ps.setString(6, (String) row[5]);
                    ps.executeUpdate();
                }
            }

            // 3. Productos (Unitarios y Perecibles)
            String insertProd = "INSERT OR IGNORE INTO producto (id, codigo, nombre, id_categoria, precio_compra, precio_venta, stock, stock_minimo, tipo, fecha_vencimiento, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertProd)) {
                Object[][] productos = {
                    {1, "BEB-001", "Inca Kola 500ml", 1, 2.00, 3.00, 24, 10, "PERECIBLE", LocalDate.now().plusMonths(6).toString(), 1},
                    {2, "BEB-002", "Coca Cola 1.5L", 1, 4.50, 6.50, 15, 8, "PERECIBLE", LocalDate.now().plusMonths(8).toString(), 1},
                    {3, "BEB-003", "Agua Mineral San Luis 625ml", 1, 1.20, 2.00, 5, 12, "PERECIBLE", LocalDate.now().plusMonths(12).toString(), 1},
                    {4, "ABA-001", "Arroz Costeño Extra 1kg", 2, 3.80, 4.80, 45, 15, "PERECIBLE", LocalDate.now().plusMonths(10).toString(), 1},
                    {5, "ABA-002", "Aceite Primor Premium 900ml", 2, 7.50, 9.80, 18, 10, "PERECIBLE", LocalDate.now().plusMonths(9).toString(), 1},
                    {6, "ABA-003", "Fideos Don Vittorio Spaghetti 500g", 2, 2.10, 3.20, 4, 10, "PERECIBLE", LocalDate.now().plusMonths(14).toString(), 1},
                    {7, "LAC-001", "Leche Evaporada Gloria Azul 400g", 3, 3.40, 4.20, 36, 12, "PERECIBLE", LocalDate.now().plusMonths(5).toString(), 1},
                    {8, "LAC-002", "Yogurt Gloria Fresa 1L", 3, 4.80, 6.50, 8, 6, "PERECIBLE", LocalDate.now().plusDays(20).toString(), 1},
                    {9, "LIM-001", "Detergente Bolívar Floral 800g", 4, 4.50, 6.20, 20, 8, "UNITARIO", null, 1},
                    {10, "LIM-002", "Lejía Clorox Tradicional 1L", 4, 2.60, 4.00, 14, 6, "UNITARIO", null, 1},
                    {11, "LIM-003", "Lavavajillas Sapolio Limón 500g", 4, 2.80, 4.20, 3, 5, "UNITARIO", null, 1},
                    {12, "SNK-001", "Galletas Casino Chocolate Paquete", 5, 0.70, 1.20, 50, 15, "PERECIBLE", LocalDate.now().plusMonths(4).toString(), 1}
                };
                for (Object[] row : productos) {
                    ps.setInt(1, (int) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setInt(4, (int) row[3]);
                    ps.setDouble(5, (double) row[4]);
                    ps.setDouble(6, (double) row[5]);
                    ps.setInt(7, (int) row[6]);
                    ps.setInt(8, (int) row[7]);
                    ps.setString(9, (String) row[8]);
                    if (row[9] != null) {
                        ps.setString(10, (String) row[9]);
                    } else {
                        ps.setNull(10, Types.VARCHAR);
                    }
                    ps.setInt(11, (int) row[10]);
                    ps.executeUpdate();
                }
            }

            // 4. Movimientos iniciales de stock
            String insertMov = "INSERT OR IGNORE INTO movimiento_stock (id_producto, tipo, cantidad, fecha, motivo) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertMov)) {
                String now = LocalDateTime.now().format(ISO_FORMATTER);
                for (int i = 1; i <= 12; i++) {
                    ps.setInt(1, i);
                    ps.setString(2, "ENTRADA");
                    ps.setInt(3, 50);
                    ps.setString(4, now);
                    ps.setString(5, "Inventario inicial de apertura de tienda");
                    ps.executeUpdate();
                }
            }

            // 5. Ventas de prueba previas para el Dashboard y Reportes
            String insertVenta = "INSERT OR IGNORE INTO venta (id, fecha, subtotal, descuento, total) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertVenta)) {
                ps.setInt(1, 1);
                ps.setString(2, LocalDateTime.now().minusDays(2).format(ISO_FORMATTER));
                ps.setDouble(3, 35.40);
                ps.setDouble(4, 3.54);
                ps.setDouble(5, 31.86);
                ps.executeUpdate();

                ps.setInt(1, 2);
                ps.setString(2, LocalDateTime.now().minusDays(1).format(ISO_FORMATTER));
                ps.setDouble(3, 26.00);
                ps.setDouble(4, 0.00);
                ps.setDouble(5, 26.00);
                ps.executeUpdate();

                ps.setInt(1, 3);
                ps.setString(2, LocalDateTime.now().format(ISO_FORMATTER));
                ps.setDouble(3, 19.40);
                ps.setDouble(4, 1.94);
                ps.setDouble(5, 17.46);
                ps.executeUpdate();
            }

            String insertDetVenta = "INSERT OR IGNORE INTO detalle_venta (id, id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertDetVenta)) {
                // Venta 1
                ps.setInt(1, 1); ps.setInt(2, 1); ps.setInt(3, 1); ps.setInt(4, 4); ps.setDouble(5, 3.00); ps.setDouble(6, 12.00); ps.executeUpdate();
                ps.setInt(1, 2); ps.setInt(2, 1); ps.setInt(3, 4); ps.setInt(4, 3); ps.setDouble(5, 4.80); ps.setDouble(6, 14.40); ps.executeUpdate();
                ps.setInt(1, 3); ps.setInt(2, 1); ps.setInt(3, 2); ps.setInt(4, 1); ps.setDouble(5, 6.50); ps.setDouble(6, 6.50); ps.executeUpdate();

                // Venta 2
                ps.setInt(1, 4); ps.setInt(2, 2); ps.setInt(3, 2); ps.setInt(4, 4); ps.setDouble(5, 6.50); ps.setDouble(6, 26.00); ps.executeUpdate();

                // Venta 3
                ps.setInt(1, 5); ps.setInt(2, 3); ps.setInt(3, 7); ps.setInt(4, 3); ps.setDouble(5, 4.20); ps.setDouble(6, 12.60); ps.executeUpdate();
                ps.setInt(1, 6); ps.setInt(2, 3); ps.setInt(3, 8); ps.setInt(4, 1); ps.setDouble(5, 6.50); ps.setDouble(6, 6.50); ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Base de datos inicializada con éxito.");
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
