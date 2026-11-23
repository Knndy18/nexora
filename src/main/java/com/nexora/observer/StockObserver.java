package com.nexora.observer;

import com.nexora.model.Producto;

/**
 * Interfaz de observador para el patrón Observer.
 * Se notifica cuando un producto alcanza o baja de su umbral mínimo de stock.
 */
public interface StockObserver {
    void onStockBajo(Producto producto);
}
