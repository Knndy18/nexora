package com.nexora.strategy;

import com.nexora.model.DetalleVenta;
import com.nexora.model.ProductoUnitario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Patrón Strategy: Algoritmos de Descuento")
public class DescuentoStrategyTest {

    @Test
    @DisplayName("Strategy: SinDescuento siempre devuelve 0.0")
    void testSinDescuento() {
        DescuentoStrategy estrategia = new SinDescuento();
        List<DetalleVenta> items = new ArrayList<>();
        double subtotal = 100.0;

        assertEquals(0.0, estrategia.calcularDescuento(subtotal, items), 0.001);
    }

    @Test
    @DisplayName("Strategy: DescuentoPorcentaje aplica porcentaje exacto")
    void testDescuentoPorcentaje() {
        DescuentoStrategy estrategia10 = new DescuentoPorcentaje(10.0);
        DescuentoStrategy estrategia15 = new DescuentoPorcentaje(15.0);

        List<DetalleVenta> items = new ArrayList<>();
        double subtotal = 200.0;

        assertEquals(20.0, estrategia10.calcularDescuento(subtotal, items), 0.001);
        assertEquals(30.0, estrategia15.calcularDescuento(subtotal, items), 0.001);
    }

    @Test
    @DisplayName("Strategy: DescuentoPorVolumen solo aplica si supera el umbral de unidades")
    void testDescuentoPorVolumen() {
        // Aplica 10% si lleva 5 o más unidades
        DescuentoStrategy estrategia = new DescuentoPorVolumen(5, 10.0);

        ProductoUnitario p = new ProductoUnitario(1, "COD-1", "Gaseosa", 1, 2.0, 3.0, 100, 10, true);

        // Caso 1: 3 unidades (Menor al umbral) -> Descuento 0.0
        List<DetalleVenta> itemsMenor = List.of(new DetalleVenta(p, 3));
        double subtotalMenor = 9.0;
        assertEquals(0.0, estrategia.calcularDescuento(subtotalMenor, itemsMenor), 0.001);

        // Caso 2: 6 unidades (Supera umbral de 5) -> Descuento 10%
        List<DetalleVenta> itemsMayor = List.of(new DetalleVenta(p, 6));
        double subtotalMayor = 18.0;
        assertEquals(1.80, estrategia.calcularDescuento(subtotalMayor, itemsMayor), 0.001);
    }
}
