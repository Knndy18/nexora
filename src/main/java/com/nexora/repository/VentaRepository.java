package com.nexora.repository;

import com.nexora.model.DetalleVenta;
import com.nexora.model.Producto;
import com.nexora.model.Venta;
import com.nexora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaRepository implements Repositorio<Venta, Integer> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ProductoRepository productoRepository = new ProductoRepository();

    @Override
    public Venta guardar(Venta venta) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return guardarVentaConDetalles(venta, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar venta: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda la venta y sus líneas de detalle utilizando la conexión de la transacción activa.
     */
    public Venta guardarVentaConDetalles(Venta venta, Connection conn) throws SQLException {
        String sqlVenta = "INSERT INTO venta (fecha, subtotal, descuento, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
            String fechaStr = venta.getFecha() != null ? venta.getFecha().format(FORMATTER) : LocalDateTime.now().format(FORMATTER);
            ps.setString(1, fechaStr);
            ps.setDouble(2, venta.getSubtotal());
            ps.setDouble(3, venta.getDescuento());
            ps.setDouble(4, venta.getTotal());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    venta.setId(rs.getInt(1));
                }
            }
        }

        String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS)) {
            for (DetalleVenta d : venta.getDetalles()) {
                ps.setInt(1, venta.getId());
                ps.setInt(2, d.getProducto().getId());
                ps.setInt(3, d.getCantidad());
                ps.setDouble(4, d.getPrecioUnitario());
                ps.setDouble(5, d.getSubtotal());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        d.setId(rs.getInt(1));
                        d.setIdVenta(venta.getId());
                    }
                }
            }
        }

        return venta;
    }

    @Override
    public Optional<Venta> buscarPorId(Integer id) {
        String sql = "SELECT id, fecha, subtotal, descuento, total FROM venta WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Venta venta = mapVenta(rs);
                    cargarDetalles(venta, conn);
                    return Optional.of(venta);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar venta: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Venta> listarTodos() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT id, fecha, subtotal, descuento, total FROM venta ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapVenta(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ventas: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Venta> listarPorRangoFechas(String fechaInicio, String fechaFin) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT id, fecha, subtotal, descuento, total FROM venta WHERE fecha >= ? AND fecha <= ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapVenta(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al filtrar ventas por fechas: " + e.getMessage(), e);
        }
        return lista;
    }

    public void cargarDetalles(Venta venta, Connection conn) throws SQLException {
        String sql = "SELECT dv.id, dv.id_venta, dv.id_producto, dv.cantidad, dv.precio_unitario, dv.subtotal " +
                "FROM detalle_venta dv WHERE dv.id_venta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venta.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("id_producto");
                    Producto prod = productoRepository.buscarPorId(idProducto).orElse(null);
                    DetalleVenta det = new DetalleVenta(
                            rs.getInt("id"),
                            rs.getInt("id_venta"),
                            prod,
                            rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("subtotal")
                    );
                    venta.agregarDetalle(det);
                }
            }
        }
    }

    @Override
    public void actualizar(Venta entidad) {
        throw new UnsupportedOperationException("Las ventas confirmadas no deben ser modificadas directamente.");
    }

    @Override
    public void eliminar(Integer id) {
        String sql = "DELETE FROM venta WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar venta: " + e.getMessage(), e);
        }
    }

    private Venta mapVenta(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String fechaStr = rs.getString("fecha");
        LocalDateTime fecha = LocalDateTime.parse(fechaStr, FORMATTER);
        double subtotal = rs.getDouble("subtotal");
        double descuento = rs.getDouble("descuento");
        double total = rs.getDouble("total");
        return new Venta(id, fecha, subtotal, descuento, total);
    }
}
