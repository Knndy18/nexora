package com.nexora.service;

import com.nexora.model.*;
import com.nexora.repository.ProductoRepository;
import com.nexora.util.DatabaseConnection;
import com.nexora.util.DatabaseInitializer;
import com.nexora.util.StockInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Integridad Transaccional (ACID): Transacción de Ventas")
public class TransaccionVentaTest {

    private VentaService ventaService;
    private ProductoRepository productoRepository;

    @BeforeEach
    void setUp() {
        File testDir = new File("target/test_dbs");
        if (!testDir.exists()) testDir.mkdirs();

        String dbName = "target/test_dbs/tx_" + UUID.randomUUID() + ".db";
        DatabaseConnection.setDatabaseUrl("jdbc:sqlite:" + dbName);
        DatabaseInitializer.initialize();

        ventaService = new VentaService();
        productoRepository = new ProductoRepository();
    }

    @Test
    @DisplayName("Transacción Venta Exitosa: Confirma venta, descuenta stock y registra detalles")
    void testTransaccionVentaExitosa() {
        Producto p1 = productoRepository.buscarPorId(1).orElseThrow();
        int stockInicial = p1.getStock();

        Venta venta = new Venta();
        venta.agregarDetalle(new DetalleVenta(p1, 2));

        Venta confirmada = ventaService.procesarVenta(venta);

        assertNotNull(confirmada.getId(), "La venta debe haber recibido un ID persistido");

        Producto p1Actualizado = productoRepository.buscarPorId(1).orElseThrow();
        assertEquals(stockInicial - 2, p1Actualizado.getStock(), "El stock debe haberse descontado en 2 unidades");
    }

    @Test
    @DisplayName("Transacción Rollback: Si un ítem excede stock, revierte toda la operación sin alterar inventario")
    void testTransaccionRollbackPorStockInsuficiente() {
        Producto p1 = productoRepository.buscarPorId(1).orElseThrow(); // Stock 24
        Producto p3 = productoRepository.buscarPorId(3).orElseThrow(); // Stock 5 (stock bajo)

        int stockP1Inicial = p1.getStock();
        int stockP3Inicial = p3.getStock();

        Venta ventaFallida = new Venta();
        ventaFallida.agregarDetalle(new DetalleVenta(p1, 3)); // Válido
        ventaFallida.agregarDetalle(new DetalleVenta(p3, 20)); // Excede stock disponible (5)

        assertThrows(StockInsuficienteException.class, () -> {
            ventaService.procesarVenta(ventaFallida);
        });

        // Verificar integridad referencial: P1 no debe haberse descontado porque la transacción fue abortada
        Producto p1TrasFallo = productoRepository.buscarPorId(1).orElseThrow();
        Producto p3TrasFallo = productoRepository.buscarPorId(3).orElseThrow();

        assertEquals(stockP1Inicial, p1TrasFallo.getStock(), "Rollback: El producto 1 no debe haber sufrido descuento de stock");
        assertEquals(stockP3Inicial, p3TrasFallo.getStock(), "Rollback: El producto 3 debe permanecer intacto");
    }
}
