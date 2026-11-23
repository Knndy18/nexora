package com.nexora.repository;

import com.nexora.factory.ProductoFactory;
import com.nexora.model.Producto;
import com.nexora.model.TipoProducto;
import com.nexora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoRepository implements Repositorio<Producto, Integer> {

    @Override
    public Producto guardar(Producto producto) {
        String sql = "INSERT INTO producto (codigo, nombre, id_categoria, precio_compra, precio_venta, " +
                "stock, stock_minimo, tipo, fecha_vencimiento, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, producto);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setId(rs.getInt(1));
                }
            }
            return producto;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(Integer id) {
        String sql = baseQuery() + " WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        String sql = baseQuery() + " WHERE p.codigo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por código: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = baseQuery() + " ORDER BY p.nombre ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Producto> listarActivos() {
        List<Producto> lista = new ArrayList<>();
        String sql = baseQuery() + " WHERE p.activo = 1 ORDER BY p.nombre ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos activos: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Producto> listarBajoStock() {
        List<Producto> lista = new ArrayList<>();
        String sql = baseQuery() + " WHERE p.activo = 1 AND p.stock <= p.stock_minimo ORDER BY p.stock ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos con stock bajo: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Producto producto) {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, id_categoria = ?, precio_compra = ?, " +
                "precio_venta = ?, stock = ?, stock_minimo = ?, tipo = ?, fecha_vencimiento = ?, activo = ? " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, producto);
            ps.setInt(11, producto.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    public void actualizarStock(int idProducto, int nuevoStock, Connection conn) throws SQLException {
        String sql = "UPDATE producto SET stock = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(Integer id) {
        // En productos aplicamos borrado lógico para preservar el historial de compras/ventas
        String sql = "UPDATE producto SET activo = 0 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar producto: " + e.getMessage(), e);
        }
    }

    private String baseQuery() {
        return "SELECT p.id, p.codigo, p.nombre, p.id_categoria, c.nombre AS categoria_nombre, " +
                "p.precio_compra, p.precio_venta, p.stock, p.stock_minimo, p.tipo, p.fecha_vencimiento, p.activo " +
                "FROM producto p " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id";
    }

    private void setStatementParameters(PreparedStatement ps, Producto producto) throws SQLException {
        ps.setString(1, producto.getCodigo());
        ps.setString(2, producto.getNombre());
        ps.setInt(3, producto.getIdCategoria());
        ps.setDouble(4, producto.getPrecioCompra());
        ps.setDouble(5, producto.getPrecioVenta());
        ps.setInt(6, producto.getStock());
        ps.setInt(7, producto.getStockMinimo());
        ps.setString(8, producto.getTipo().name());

        if (producto.getTipo() == TipoProducto.PERECIBLE && producto instanceof com.nexora.model.ProductoPerecible) {
            LocalDate fv = ((com.nexora.model.ProductoPerecible) producto).getFechaVencimiento();
            if (fv != null) {
                ps.setString(9, fv.toString());
            } else {
                ps.setNull(9, Types.VARCHAR);
            }
        } else {
            ps.setNull(9, Types.VARCHAR);
        }
        ps.setInt(10, producto.isActivo() ? 1 : 0);
    }

    private Producto mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");
        int idCategoria = rs.getInt("id_categoria");
        String categoriaNombre = rs.getString("categoria_nombre");
        double precioCompra = rs.getDouble("precio_compra");
        double precioVenta = rs.getDouble("precio_venta");
        int stock = rs.getInt("stock");
        int stockMinimo = rs.getInt("stock_minimo");
        String tipoStr = rs.getString("tipo");
        String fechaVencStr = rs.getString("fecha_vencimiento");
        boolean activo = rs.getInt("activo") == 1;

        // Uso del patrón Factory para instanciar el tipo correcto
        Producto producto = ProductoFactory.crearProducto(
                tipoStr, id, codigo, nombre, idCategoria,
                precioCompra, precioVenta, stock, stockMinimo,
                fechaVencStr, activo
        );
        producto.setNombreCategoria(categoriaNombre != null ? categoriaNombre : "Sin categoría");
        return producto;
    }
}
