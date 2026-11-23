package com.nexora.model;

import com.nexora.util.StockInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Tecnología de Objetos: Encapsulación, Herencia y Polimorfismo")
public class EncapsulacionYPolimorfismoTest {

    @Test
    @DisplayName("Encapsulación: Lanza StockInsuficienteException al intentar descontar más del disponible")
    void testEncapsulacionDescontarStockExcesivo() {
        Producto producto = new ProductoUnitario(1, "TEST-01", "Jabón", 1, 2.0, 3.5, 10, 2, true);

        // Descuento válido dentro del límite
        producto.descontarStock(4);
        assertEquals(6, producto.getStock(), "El stock debe descontarse correctamente a 6");

        // Intento de descontar más del stock existente -> Debe lanzar excepción
        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class, () -> {
            producto.descontarStock(7);
        });

        assertEquals(6, ex.getStockDisponible());
        assertEquals(7, ex.getCantidadSolicitada());
        assertEquals(6, producto.getStock(), "El stock debe permanecer inalterado tras el fallo");
    }

    @Test
    @DisplayName("Encapsulación: Lanza IllegalArgumentException ante cantidades menores o iguales a cero")
    void testEncapsulacionCantidadesInvalidas() {
        Producto producto = new ProductoUnitario(1, "TEST-02", "Lejía", 1, 1.0, 2.5, 10, 2, true);

        assertThrows(IllegalArgumentException.class, () -> producto.descontarStock(0));
        assertThrows(IllegalArgumentException.class, () -> producto.descontarStock(-5));
        assertThrows(IllegalArgumentException.class, () -> producto.agregarStock(0));
        assertThrows(IllegalArgumentException.class, () -> producto.agregarStock(-3));
    }

    @Test
    @DisplayName("Polimorfismo: ProductoUnitario y ProductoPerecible calculan precio con comportamiento polimórfico")
    void testPolimorfismoCalcularPrecio() {
        double precioVentaBase = 10.0;

        // 1. Producto Unitario estándar: mantiene precio fijo
        Producto unitario = new ProductoUnitario(1, "UNI-01", "Detergente", 1, 6.0, precioVentaBase, 20, 5, true);
        assertEquals(precioVentaBase, unitario.calcularPrecio(), 0.001, "ProductoUnitario debe devolver precio regular");

        // 2. Producto Perecible lejano a vencer (ej. 60 días): mantiene precio base
        Producto perecibleLejano = new ProductoPerecible(2, "PER-01", "Yogurt Fresco", 2, 6.0, precioVentaBase,
                15, 5, LocalDate.now().plusDays(60), true);
        assertEquals(precioVentaBase, perecibleLejano.calcularPrecio(), 0.001, "Perecible lejano debe cobrar precio regular");

        // 3. Producto Perecible próximo a vencer (<= 7 días): descuenta 20% automáticamente por liquidación
        Producto pereciblePorVencer = new ProductoPerecible(3, "PER-02", "Yogurt Próximo a Vencer", 2, 6.0, precioVentaBase,
                15, 5, LocalDate.now().plusDays(3), true);
        assertEquals(8.0, pereciblePorVencer.calcularPrecio(), 0.001, "Perecible próximo debe aplicar 20% de descuento polimórfico");
    }
}
