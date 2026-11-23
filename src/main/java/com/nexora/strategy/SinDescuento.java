package com.nexora.strategy;

import com.nexora.model.DetalleVenta;
import java.util.List;

/**
 * Estrategia por defecto: Sin descuento (0.0).
 */
public class SinDescuento implements DescuentoStrategy {

    @Override
    public double calcularDescuento(double subtotal, List<DetalleVenta> items) {
        return 0.0;
    }

    @Override
    public String getNombre() {
        return "Sin Descuento (0%)";
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
