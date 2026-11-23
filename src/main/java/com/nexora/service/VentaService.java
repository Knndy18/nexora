package com.nexora.service;

import com.nexora.model.*;
import com.nexora.observer.AlertaStockBajo;
import com.nexora.repository.MovimientoStockRepository;
import com.nexora.repository.ProductoRepository;
import com.nexora.repository.VentaRepository;
import com.nexora.strategy.DescuentoStrategy;
import com.nexora.strategy.SinDescuento;
import com.nexora.util.DatabaseConnection;
import com.nexora.util.StockInsuficienteException;
import com.nexora.util.ValidationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de Ventas y Punto de Venta (POS).
 * Gestiona el carrito, la estrategia dinámica de descuentos (patrón Strategy),
 * y la persistencia transaccional atómica (ACID) mediante JDBC.
 */
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;
    private DescuentoStrategy estrategiaDescuento;

    public VentaService() {
        this(new VentaRepository(), new ProductoRepository(), new MovimientoStockRepository());
    }

    public VentaService(VentaRepository ventaRepo, ProductoRepository prodRepo, MovimientoStockRepository movRepo) {
        this.ventaRepository = ventaRepo;
        this.productoRepository = prodRepo;
        this.movimientoRepository = movRepo;
        this.estrategiaDescuento = new SinDescuento(); // Estrategia por defecto
    }

    /**
     * Patrón Strategy: Permite cambiar en caliente la regla de cálculo de descuentos.
     */
    public void setEstrategiaDescuento(DescuentoStrategy estrategia) {
        if (estrategia != null) {
            this.estrategiaDescuento = estrategia;
        }
    }

    public DescuentoStrategy getEstrategiaDescuento() {
        return estrategiaDescuento;
    }

    /**
     * Calcula los importes de una venta aplicando la estrategia de descuento configurada.
     */
    public void calcularTotales(Venta venta) {
        double subtotal = venta.calcularSubtotal();
        double descuento = estrategiaDescuento.calcularDescuento(subtotal, venta.getDetalles());
        double total = Math.max(0.0, Math.round((subtotal - descuento) * 100.0) / 100.0);

        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setTotal(total);
    }

    /**
     * Procesa y registra la venta como una transacción ACID indivisible.
     * Si ocurre cualquier excepción (stock insuficiente, error de BD),
     * se efectúa rollback automático y ningún cambio persiste.
     */
    public Venta procesarVenta(Venta venta) {
        if (venta == null || venta.getDetalles().isEmpty()) {
            throw new ValidationException("No se puede registrar una venta sin productos en el carrito.");
        }

        // Validación previa de stock
        for (DetalleVenta det : venta.getDetalles()) {
            Producto prodActual = productoRepository.buscarPorId(det.getProducto().getId())
                    .orElseThrow(() -> new ValidationException("Producto no encontrado: " + det.getProducto().getNombre()));

            if (!prodActual.isActivo()) {
                throw new ValidationException("El producto '" + prodActual.getNombre() + "' está desactivado y no puede ser vendido.");
            }

            if (det.getCantidad() > prodActual.getStock()) {
                throw new StockInsuficienteException(prodActual.getCodigo(), prodActual.getStock(), det.getCantidad());
            }
        }

        // Cálculo de montos finales
        calcularTotales(venta);
        venta.setFecha(LocalDateTime.now());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Guardar cabecera y detalles de venta
            ventaRepository.guardarVentaConDetalles(venta, conn);

            // 2. Descontar stock y registrar trazabilidad de cada ítem
            for (DetalleVenta det : venta.getDetalles()) {
                Producto prod = productoRepository.buscarPorId(det.getProducto().getId()).orElseThrow();

                // Asociar observador de alerta de stock bajo
                prod.agregarObservador(AlertaStockBajo.getInstancia());

                // Descontar aplicando la regla encapsulada del dominio (notifica observer si <= stockMinimo)
                prod.descontarStock(det.getCantidad());

                // Actualizar stock en BD
                productoRepository.actualizarStock(prod.getId(), prod.getStock(), conn);

                // Registrar movimiento de salida
                MovimientoStock mov = new MovimientoStock(
                        prod,
                        TipoMovimiento.SALIDA,
                        det.getCantidad(),
                        "Venta #" + venta.getId()
                );
                mov.setFecha(venta.getFecha());
                movimientoRepository.guardarMovimiento(mov, conn);
            }

            // Confirmar transacción
            conn.commit();
            return venta;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error al procesar la transacción de venta: " + e.getMessage(), e);
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
