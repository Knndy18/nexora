package com.nexora.factory;

import com.nexora.model.Producto;
import com.nexora.model.ProductoPerecible;
import com.nexora.model.ProductoUnitario;
import com.nexora.model.TipoProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Patrón Factory Method: ProductoFactory")
public class ProductoFactoryTest {

    @Test
    @DisplayName("Factory: Instancia ProductoUnitario cuando el tipo es UNITARIO")
    void testCrearProductoUnitario() {
        Producto p = ProductoFactory.crearProducto(TipoProducto.UNITARIO, 1, "U-100", "Escoba",
                4, 5.0, 9.0, 10, 2, null, true);

        assertNotNull(p);
        assertInstanceOf(ProductoUnitario.class, p);
        assertEquals(TipoProducto.UNITARIO, p.getTipo());
        assertEquals("Escoba", p.getNombre());
    }

    @Test
    @DisplayName("Factory: Instancia ProductoPerecible con fecha cuando el tipo es PERECIBLE")
    void testCrearProductoPerecible() {
        LocalDate fechaVenc = LocalDate.now().plusMonths(3);
        Producto p = ProductoFactory.crearProducto("PERECIBLE", 2, "P-200", "Leche 1L",
                3, 3.2, 4.5, 25, 5, fechaVenc.toString(), true);

        assertNotNull(p);
        assertInstanceOf(ProductoPerecible.class, p);
        assertEquals(TipoProducto.PERECIBLE, p.getTipo());
        assertEquals(fechaVenc, ((ProductoPerecible) p).getFechaVencimiento());
    }
}
