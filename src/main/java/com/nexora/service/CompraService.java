package com.nexora.service;

import com.nexora.model.*;
import com.nexora.repository.CompraRepository;
import com.nexora.repository.MovimientoStockRepository;
import com.nexora.repository.ProductoRepository;
import com.nexora.util.DatabaseConnection;
import com.nexora.util.ValidationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Servicio de Compras a Proveedores.
 * Registra compras, incrementa existencias y almacena movimientos de entrada
 * de forma atómica bajo transacciones JDBC.
 */
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;

    public CompraService() {
        this(new CompraRepository(), new ProductoRepository(), new MovimientoStockRepository());
    }

    public CompraService(CompraRepository compraRepo, ProductoRepository prodRepo, MovimientoStockRepository movRepo) {
        this.compraRepository = compraRepo;
        this.productoRepository = prodRepo;
        this.movimientoRepository = movRepo;
    }

    public Compra registrarCompra(Compra compra) {
        if (compra == null || compra.getProveedor() == null) {
            throw new ValidationException("Debe seleccionar un proveedor válido para la compra.");
        }
        if (compra.getDetalles().isEmpty()) {
            throw new ValidationException("Debe agregar al menos un producto a la compra.");
        }

        compra.calcularTotal();
        compra.setFecha(LocalDateTime.now());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Guardar cabecera y detalles de compra
            compraRepository.guardarCompraConDetalles(compra, conn);

            // 2. Incrementar stock de cada producto y registrar movimiento
            for (DetalleCompra det : compra.getDetalles()) {
                Producto prod = productoRepository.buscarPorId(det.getProducto().getId())
                        .orElseThrow(() -> new ValidationException("Producto no encontrado: " + det.getProducto().getNombre()));

                // Agregar stock con validación de cantidad positiva
                prod.agregarStock(det.getCantidad());

                // Actualizar en base de datos
                productoRepository.actualizarStock(prod.getId(), prod.getStock(), conn);

                // Registrar movimiento de inventario (ENTRADA)
                String motivo = String.format("Compra #%d (Comprobante: %s) - Proveedor: %s",
                        compra.getId(),
                        compra.getComprobante() != null ? compra.getComprobante() : "S/N",
                        compra.getProveedor().getNombre());

                MovimientoStock mov = new MovimientoStock(prod, TipoMovimiento.ENTRADA, det.getCantidad(), motivo);
                mov.setFecha(compra.getFecha());
                movimientoRepository.guardarMovimiento(mov, conn);
            }

            conn.commit();
            return compra;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback de compra: " + ex.getMessage());
                }
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error al registrar la transacción de compra: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error al restaurar autocommit: " + e.getMessage());
                }
            }
        }
    }
}
