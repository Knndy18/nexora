package com.nexora.model;

public class DetalleVenta {
    private Integer id;
    private Integer idVenta;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleVenta() {}

    public DetalleVenta(Integer id, Integer idVenta, Producto producto, int cantidad, double precioUnitario, double subtotal) {
        this.id = id;
        this.idVenta = idVenta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = (producto != null) ? producto.calcularPrecio() : 0.0;
        this.subtotal = Math.round((this.precioUnitario * cantidad) * 100.0) / 100.0;
    }

    public void recalcularSubtotal() {
        this.subtotal = Math.round((this.precioUnitario * this.cantidad) * 100.0) / 100.0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
