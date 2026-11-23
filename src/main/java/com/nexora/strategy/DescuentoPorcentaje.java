package com.nexora.strategy;

import com.nexora.model.DetalleVenta;
import java.util.List;

/**
 * Estrategia de descuento por porcentaje configurable (e.g. 5%, 10%, 15%).
 */
public class DescuentoPorcentaje implements DescuentoStrategy {

    private final double porcentaje;

    public DescuentoPorcentaje(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcularDescuento(double subtotal, List<DetalleVenta> items) {
        if (subtotal <= 0) return 0.0;
        double descuento = subtotal * (porcentaje / 100.0);
        return Math.round(descuento * 100.0) / 100.0;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    @Override
    public String getNombre() {
        return String.format("Descuento Porcentual (%.0f%%)", porcentaje);
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
