package com.nexora.util;

/**
 * Excepción lanzada cuando se intenta descontar una cantidad mayor
 * al stock disponible en el inventario. Demuestra encapsulación en el modelo.
 */
public class StockInsuficienteException extends RuntimeException {
    private final String codigoProducto;
    private final int stockDisponible;
    private final int cantidadSolicitada;

    public StockInsuficienteException(String codigoProducto, int stockDisponible, int cantidadSolicitada) {
        super(String.format("Stock insuficiente para el producto '%s'. Disponible: %d, Solicitado: %d",
                codigoProducto, stockDisponible, cantidadSolicitada));
        this.codigoProducto = codigoProducto;
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public int getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}
