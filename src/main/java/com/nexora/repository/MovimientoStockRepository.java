package com.nexora.repository;

import com.nexora.model.MovimientoStock;
import com.nexora.model.Producto;
import com.nexora.model.TipoMovimiento;
import com.nexora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovimientoStockRepository implements Repositorio<MovimientoStock, Integer> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ProductoRepository productoRepository = new ProductoRepository();

    @Override
    public MovimientoStock guardar(MovimientoStock movimiento) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return guardarMovimiento(movimiento, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar movimiento de stock: " + e.getMessage(), e);
        }
    }

    public MovimientoStock guardarMovimiento(MovimientoStock mov, Connection conn) throws SQLException {
        String sql = "INSERT INTO movimiento_stock (id_producto, tipo, cantidad, fecha, motivo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, mov.getProducto().getId());
            ps.setString(2, mov.getTipo().name());
            ps.setInt(3, mov.getCantidad());
            String fechaStr = mov.getFecha() != null ? mov.getFecha().format(FORMATTER) : LocalDateTime.now().format(FORMATTER);
            ps.setString(4, fechaStr);
            ps.setString(5, mov.getMotivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    mov.setId(rs.getInt(1));
                }
            }
        }
        return mov;
    }

    @Override
    public Optional<MovimientoStock> buscarPorId(Integer id) {
        String sql = "SELECT id, id_producto, tipo, cantidad, fecha, motivo FROM movimiento_stock WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar movimiento: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<MovimientoStock> listarTodos() {
        return listarRecientes(100);
    }

    public List<MovimientoStock> listarRecientes(int limite) {
        List<MovimientoStock> lista = new ArrayList<>();
        String sql = "SELECT id, id_producto, tipo, cantidad, fecha, motivo FROM movimiento_stock ORDER BY id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar movimientos recientes: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<MovimientoStock> listarPorProducto(int idProducto) {
        List<MovimientoStock> lista = new ArrayList<>();
        String sql = "SELECT id, id_producto, tipo, cantidad, fecha, motivo FROM movimiento_stock WHERE id_producto = ? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar movimientos de producto: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(MovimientoStock entidad) {
        throw new UnsupportedOperationException("Los movimientos de inventario son inmutables por trazabilidad.");
    }

    @Override
    public void eliminar(Integer id) {
        throw new UnsupportedOperationException("Los movimientos de inventario no se eliminan por auditoría.");
    }

    private MovimientoStock mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int idProducto = rs.getInt("id_producto");
        Producto producto = productoRepository.buscarPorId(idProducto).orElse(null);
        TipoMovimiento tipo = TipoMovimiento.valueOf(rs.getString("tipo"));
        int cantidad = rs.getInt("cantidad");
        String fechaStr = rs.getString("fecha");
        LocalDateTime fecha = LocalDateTime.parse(fechaStr, FORMATTER);
        String motivo = rs.getString("motivo");
        return new MovimientoStock(id, producto, tipo, cantidad, fecha, motivo);
    }
}
