package com.nexora.service;

import com.nexora.model.MovimientoStock;
import com.nexora.model.Producto;
import com.nexora.model.TipoMovimiento;
import com.nexora.repository.MovimientoStockRepository;
import com.nexora.repository.ProductoRepository;
import com.nexora.util.DatabaseConnection;
import com.nexora.util.ValidationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de Inventario y Movimientos de Stock (Kardex).
 */
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;

    public InventarioService() {
        this(new ProductoRepository(), new MovimientoStockRepository());
    }

    public InventarioService(ProductoRepository prodRepo, MovimientoStockRepository movRepo) {
        this.productoRepository = prodRepo;
        this.movimientoRepository = movRepo;
    }

    /**
     * Realiza un ajuste de stock manual (por conteo físico, merma, rotura o devolución).
     */
    public void registrarAjuste(int idProducto, int nuevoStock, String motivo) {
        if (nuevoStock < 0) {
            throw new ValidationException("El stock de un producto no puede ser negativo.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ValidationException("Debe especificar un motivo para el ajuste de inventario.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Producto producto = productoRepository.buscarPorId(idProducto)
                    .orElseThrow(() -> new ValidationException("Producto no encontrado."));

            int stockAnterior = producto.getStock();
            int diferencia = nuevoStock - stockAnterior;

            if (diferencia == 0) {
                conn.setAutoCommit(true);
                return; // Sin cambios
            }

            // Actualizar stock en BD
            productoRepository.actualizarStock(idProducto, nuevoStock, conn);

            // Registrar movimiento de trazabilidad
            TipoMovimiento tipo = TipoMovimiento.AJUSTE;
            MovimientoStock mov = new MovimientoStock(
                    producto,
                    tipo,
                    Math.abs(diferencia),
                    String.format("Ajuste de inventario (%s%d). Motivo: %s",
                            (diferencia > 0 ? "+" : "-"), Math.abs(diferencia), motivo)
            );
            mov.setFecha(LocalDateTime.now());
            movimientoRepository.guardarMovimiento(mov, conn);

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Error al registrar ajuste de inventario: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public List<MovimientoStock> listarMovimientosRecientes(int limite) {
        return movimientoRepository.listarRecientes(limite);
    }

    public List<MovimientoStock> listarMovimientosPorProducto(int idProducto) {
        return movimientoRepository.listarPorProducto(idProducto);
    }
}
