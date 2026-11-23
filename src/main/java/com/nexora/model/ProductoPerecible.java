package com.nexora.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Especialización de Producto para bienes perecibles con fecha de vencimiento
 * (lácteos, bebidas, carnes, embutidos, panadería).
 * Aplica polimorfismo en calcularPrecio() aplicando descuento por cercanía a vencimiento.
 */
public class ProductoPerecible extends Producto {

    private LocalDate fechaVencimiento;

    public ProductoPerecible() {
        setTipo(TipoProducto.PERECIBLE);
    }

    public ProductoPerecible(Integer id, String codigo, String nombre, Integer idCategoria,
                             double precioCompra, double precioVenta, int stock, int stockMinimo,
                             LocalDate fechaVencimiento, boolean activo) {
        super(id, codigo, nombre, idCategoria, precioCompra, precioVenta, stock, stockMinimo, TipoProducto.PERECIBLE, activo);
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * Implementación polimórfica:
     * Si el producto está próximo a vencer (dentro de 7 días o menos),
     * aplica automáticamente un 20% de descuento para liquidación rápida.
     * Si ya está vencido, se liquida a precio de costo o se marca en cero.
     */
    @Override
    public double calcularPrecio() {
        double precioBase = getPrecioVenta();
        if (fechaVencimiento == null) {
            return precioBase;
        }

        LocalDate hoy = LocalDate.now();
        long diasParaVencer = ChronoUnit.DAYS.between(hoy, fechaVencimiento);

        if (diasParaVencer <= 0) {
            // Producto vencido: liquidación al 50%
            return Math.round((precioBase * 0.50) * 100.0) / 100.0;
        } else if (diasParaVencer <= 7) {
            // Próximo a vencer (1 a 7 días): descuento especial del 20%
            return Math.round((precioBase * 0.80) * 100.0) / 100.0;
        }

        return precioBase;
    }
}
