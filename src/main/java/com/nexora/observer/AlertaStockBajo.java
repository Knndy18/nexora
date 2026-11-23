package com.nexora.observer;

import com.nexora.model.Producto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación concreta del observador de stock bajo.
 * Registra alertas y mantiene una lista en memoria para consulta en la UI y Dashboard.
 */
public class AlertaStockBajo implements StockObserver {

    public static class RegistroAlerta {
        private final String codigoProducto;
        private final String nombreProducto;
        private final int stockActual;
        private final int stockMinimo;
        private final LocalDateTime timestamp;

        public RegistroAlerta(String codigoProducto, String nombreProducto, int stockActual, int stockMinimo) {
            this.codigoProducto = codigoProducto;
            this.nombreProducto = nombreProducto;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.timestamp = LocalDateTime.now();
        }

        public String getCodigoProducto() { return codigoProducto; }
        public String getNombreProducto() { return nombreProducto; }
        public int getStockActual() { return stockActual; }
        public int getStockMinimo() { return stockMinimo; }
        public LocalDateTime getTimestamp() { return timestamp; }

        public String getMensaje() {
            return String.format("[%s] ¡Alerta de reposición! Producto: '%s' (%s) tiene stock %d (mínimo: %d)",
                    timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    nombreProducto, codigoProducto, stockActual, stockMinimo);
        }

        @Override
        public String toString() {
            return getMensaje();
        }
    }

    private final List<RegistroAlerta> alertasActivas = Collections.synchronizedList(new ArrayList<>());
    private static final AlertaStockBajo INSTANCIA = new AlertaStockBajo();

    public static AlertaStockBajo getInstancia() {
        return INSTANCIA;
    }

    @Override
    public void onStockBajo(Producto producto) {
        RegistroAlerta alerta = new RegistroAlerta(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getStock(),
                producto.getStockMinimo()
        );
        alertasActivas.add(0, alerta);
        System.out.println(alerta.getMensaje());
    }

    public List<RegistroAlerta> getAlertasActivas() {
        return new ArrayList<>(alertasActivas);
    }

    public void limpiarAlertas() {
        alertasActivas.clear();
    }
}
