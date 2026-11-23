package com.nexora.observer;

import com.nexora.model.Producto;
import com.nexora.model.ProductoUnitario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Patrón Observer: Alertas de Stock Bajo")
public class StockObserverTest {

    @Test
    @DisplayName("Observer: Notifica al observador cuando el stock llega o cae por debajo del mínimo")
    void testNotificacionStockBajo() {
        Producto producto = new ProductoUnitario(1, "OBS-01", "Aceite", 2, 7.0, 9.5, 10, 4, true);

        AtomicBoolean alertaDisparada = new AtomicBoolean(false);
        StockObserver observadorPrueba = p -> alertaDisparada.set(true);

        producto.agregarObservador(observadorPrueba);

        // Descontar a 6 (mayor que el mínimo 4) -> NO debe disparar alerta
        producto.descontarStock(4);
        assertFalse(alertaDisparada.get(), "No debe disparar alerta mientras stock > mínimo");

        // Descontar 2 unidades más -> Stock queda en 4 (igual a mínimo) -> DEBE disparar alerta
        producto.descontarStock(2);
        assertTrue(alertaDisparada.get(), "Debe disparar la alerta al cruzar el umbral del stock mínimo");
    }

    @Test
    @DisplayName("Observer: AlertaStockBajo registra en bitácora la alerta emitida")
    void testAlertaStockBajoRegistro() {
        AlertaStockBajo alertaService = AlertaStockBajo.getInstancia();
        alertaService.limpiarAlertas();

        Producto producto = new ProductoUnitario(2, "OBS-02", "Atún", 2, 3.0, 5.0, 3, 2, true);
        producto.agregarObservador(alertaService);

        // Descontar a 2 unidades (stock mínimo)
        producto.descontarStock(1);

        assertFalse(alertaService.getAlertasActivas().isEmpty(), "La bitácora de alertas activas debe contener el evento");
        assertEquals("OBS-02", alertaService.getAlertasActivas().get(0).getCodigoProducto());
    }
}
