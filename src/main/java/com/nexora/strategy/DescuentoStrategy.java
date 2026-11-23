package com.nexora.strategy;

import com.nexora.model.DetalleVenta;
import java.util.List;

/**
 * Patrón de diseño Strategy para algoritmos de cálculo de descuentos.
 * Permite intercambiar en tiempo de ejecución las políticas de descuento del POS
 * sin modificar la lógica del servicio de ventas (Open/Closed Principle).
 */
public interface DescuentoStrategy {
    /**
     * Calcula el monto a descontar sobre la venta actual.
     * @param subtotal Suma de importes de los detalles de la venta
     * @param items Lista de líneas de venta (para validar volumen o cantidades)
     * @return Importe en dinero del descuento a aplicar
     */
    double calcularDescuento(double subtotal, List<DetalleVenta> items);

    /**
     * Nombre legible de la estrategia para mostrar en la interfaz.
     */
    String getNombre();
}
