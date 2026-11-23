package com.nexora.repository;

import com.nexora.model.Proveedor;
import com.nexora.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProveedorRepository implements Repositorio<Proveedor, Integer> {

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedor (nombre, ruc, telefono, email, direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getRuc());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getEmail());
            ps.setString(5, proveedor.getDireccion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    proveedor.setId(rs.getInt(1));
                }
            }
            return proveedor;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar proveedor: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Proveedor> buscarPorId(Integer id) {
        String sql = "SELECT id, nombre, ruc, telefono, email, direccion FROM proveedor WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar proveedor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Proveedor> listarTodos() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, ruc, telefono, email, direccion FROM proveedor ORDER BY nombre ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar proveedores: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedor SET nombre = ?, ruc = ?, telefono = ?, email = ?, direccion = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getRuc());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getEmail());
            ps.setString(5, proveedor.getDireccion());
            ps.setInt(6, proveedor.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar proveedor: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Integer id) {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar proveedor: " + e.getMessage(), e);
        }
    }

    private Proveedor mapResultSet(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("ruc"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("direccion")
        );
    }
}
