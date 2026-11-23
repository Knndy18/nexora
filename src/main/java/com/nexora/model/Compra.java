package com.nexora.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    private Integer id;
    private Proveedor proveedor;
    private LocalDateTime fecha;
    private double total;
    private String comprobante;
    private final List<DetalleCompra> detalles = new ArrayList<>();

    public Compra() {
        this.fecha = LocalDateTime.now();
    }

    public Compra(Integer id, Proveedor proveedor, LocalDateTime fecha, double total, String comprobante) {
        this.id = id;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.total = total;
        this.comprobante = comprobante;
    }

    public void agregarDetalle(DetalleCompra detalle) {
        if (detalle != null) {
            detalles.add(detalle);
        }
    }

    public double calcularTotal() {
        double suma = 0.0;
        for (DetalleCompra d : detalles) {
            suma += d.getSubtotal();
        }
        this.total = Math.round(suma * 100.0) / 100.0;
        return this.total;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getComprobante() { return comprobante; }
    public void setComprobante(String comprobante) { this.comprobante = comprobante; }

    public List<DetalleCompra> getDetalles() { return detalles; }
}
