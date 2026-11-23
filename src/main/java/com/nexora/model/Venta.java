package com.nexora.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private Integer id;
    private LocalDateTime fecha;
    private double subtotal;
    private double descuento;
    private double total;
    private final List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
        this.fecha = LocalDateTime.now();
    }

    public Venta(Integer id, LocalDateTime fecha, double subtotal, double descuento, double total) {
        this.id = id;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
    }

    public void agregarDetalle(DetalleVenta detalle) {
        if (detalle != null) {
            detalles.add(detalle);
        }
    }

    public void removerDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
    }

    public double calcularSubtotal() {
        double suma = 0.0;
        for (DetalleVenta d : detalles) {
            suma += d.getSubtotal();
        }
        this.subtotal = Math.round(suma * 100.0) / 100.0;
        return this.subtotal;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public List<DetalleVenta> getDetalles() { return detalles; }
}
