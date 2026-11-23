package com.nexora.model;

import java.time.LocalDateTime;

public class MovimientoStock {
    private Integer id;
    private Producto producto;
    private TipoMovimiento tipo;
    private int cantidad;
    private LocalDateTime fecha;
    private String motivo;

    public MovimientoStock() {
        this.fecha = LocalDateTime.now();
    }

    public MovimientoStock(Integer id, Producto producto, TipoMovimiento tipo, int cantidad, LocalDateTime fecha, String motivo) {
        this.id = id;
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    public MovimientoStock(Producto producto, TipoMovimiento tipo, int cantidad, String motivo) {
        this(null, producto, tipo, cantidad, LocalDateTime.now(), motivo);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
