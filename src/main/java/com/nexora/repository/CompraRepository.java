package com.nexora.repository;

import com.nexora.model.Compra;
import com.nexora.model.DetalleCompra;
import com.nexora.model.Producto;
import com.nexora.model.Proveedor;
import com.nexora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompraRepository implements Repositorio<Compra, Integer> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ProveedorRepository proveedorRepository = new ProveedorRepository();
    private final ProductoRepository productoRepository = new ProductoRepository();

    @Override
    public Compra guardar(Compra compra) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return guardarCompraConDetalles(compra, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar compra: " + e.getMessage(), e);
        }
    }

    public Compra guardarCompraConDetalles(Compra compra, Connection conn) throws SQLException {
        String sqlCompra = "INSERT INTO compra (id_proveedor, fecha, total, comprobante) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, compra.getProveedor().getId());
            String fechaStr = compra.getFecha() != null ? compra.getFecha().format(FORMATTER) : LocalDateTime.now().format(FORMATTER);
            ps.setString(2, fechaStr);
            ps.setDouble(3, compra.getTotal());
            ps.setString(4, compra.getComprobante());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    compra.setId(rs.getInt(1));
                }
            }
        }

        String sqlDetalle = "INSERT INTO detalle_compra (id_compra, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS)) {
            for (DetalleCompra d : compra.getDetalles()) {
                ps.setInt(1, compra.getId());
                ps.setInt(2, d.getProducto().getId());
                ps.setInt(3, d.getCantidad());
                ps.setDouble(4, d.getPrecioUnitario());
                ps.setDouble(5, d.getSubtotal());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        d.setId(rs.getInt(1));
                        d.setIdCompra(compra.getId());
                    }
                }
            }
        }

        return compra;
    }

    @Override
    public Optional<Compra> buscarPorId(Integer id) {
        String sql = "SELECT id, id_proveedor, fecha, total, comprobante FROM compra WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Compra compra = mapCompra(rs);
                    return Optional.of(compra);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar compra: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Compra> listarTodos() {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT id, id_proveedor, fecha, total, comprobante FROM compra ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapCompra(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar compras: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Compra entidad) {
        throw new UnsupportedOperationException("Las compras registradas no se editan directamente.");
    }

    @Override
    public void eliminar(Integer id) {
        String sql = "DELETE FROM compra WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar compra: " + e.getMessage(), e);
        }
    }

    private Compra mapCompra(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int idProveedor = rs.getInt("id_proveedor");
        Proveedor proveedor = proveedorRepository.buscarPorId(idProveedor).orElse(null);
        String fechaStr = rs.getString("fecha");
        LocalDateTime fecha = LocalDateTime.parse(fechaStr, FORMATTER);
        double total = rs.getDouble("total");
        String comprobante = rs.getString("comprobante");
        return new Compra(id, proveedor, fecha, total, comprobante);
    }
}
