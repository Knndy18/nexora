package com.nexora.observer;

import com.nexora.model.Producto;
import java.util.ArrayList;
import java.util.List;

/**
 * Sujeto observable para eventos de stock.
 */
public class StockSubject {
    private final List<StockObserver> observers = new ArrayList<>();

    public void agregarObservador(StockObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removerObservador(StockObserver observer) {
        observers.remove(observer);
    }

    public void notificarStockBajo(Producto producto) {
        for (StockObserver observer : observers) {
            observer.onStockBajo(producto);
        }
    }

    public List<StockObserver> getObservadores() {
        return new ArrayList<>(observers);
    }
}
