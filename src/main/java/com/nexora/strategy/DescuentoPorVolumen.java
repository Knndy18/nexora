package com.nexora.strategy;

import com.nexora.model.DetalleVenta;
import java.util.List;

/**
 * Estrategia de descuento por volumen:
 * Si la compra total contiene una cantidad mínima de artículos (e.g. >= 6 unidades)
 * o supera un monto umbral, aplica una tasa de descuento especial (e.g. 10%).
 */
public class DescuentoPorVolumen implements DescuentoStrategy {

    private final int cantidadMinimaArticulos;
    private final double porcentajeDescuento;

    public DescuentoPorVolumen(int cantidadMinimaArticulos, double porcentajeDescuento) {
        if (cantidadMinimaArticulos <= 0) {
            throw new IllegalArgumentException("La cantidad mínima debe ser mayor a 0");
        }
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }
        this.cantidadMinimaArticulos = cantidadMinimaArticulos;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public DescuentoPorVolumen() {
        this(5, 10.0); // Por defecto: 10% si lleva 5 o más unidades
    }

    @Override
    public double calcularDescuento(double subtotal, List<DetalleVenta> items) {
        if (items == null || items.isEmpty() || subtotal <= 0) {
            return 0.0;
        }

        int totalUnidades = 0;
        for (DetalleVenta item : items) {
            totalUnidades += item.getCantidad();
        }

        if (totalUnidades >= cantidadMinimaArticulos) {
            double descuento = subtotal * (porcentajeDescuento / 100.0);
            return Math.round(descuento * 100.0) / 100.0;
        }

        return 0.0;
    }

    public int getCantidadMinimaArticulos() {
        return cantidadMinimaArticulos;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    @Override
    public String getNombre() {
        return String.format("Por Mayor (%.0f%% a partir de %d uds)", porcentajeDescuento, cantidadMinimaArticulos);
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
