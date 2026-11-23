package com.nexora.model;

import com.nexora.observer.StockObserver;
import com.nexora.observer.StockSubject;
import com.nexora.util.StockInsuficienteException;

/**
 * Clase base abstracta Producto.
 * Aplica los principios de Encapsulación (atributos privados, métodos de negocio
 * descontarStock/agregarStock con validaciones) y Abstracción (método polimórfico calcularPrecio).
 */
public abstract class Producto {

    private Integer id;
    private String codigo;
    private String nombre;
    private Integer idCategoria;
    private String nombreCategoria; // Para presentación visual rápida
    private double precioCompra;
    private double precioVenta;
    private int stock;
    private int stockMinimo;
    private TipoProducto tipo;
    private boolean activo = true;

    // Gestor de observadores para el patrón Observer
    private final StockSubject stockSubject = new StockSubject();

    public Producto() {}

    public Producto(Integer id, String codigo, String nombre, Integer idCategoria,
                    double precioCompra, double precioVenta, int stock, int stockMinimo,
                    TipoProducto tipo, boolean activo) {
        if (precioCompra < 0 || precioVenta < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }

        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.idCategoria = idCategoria;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.tipo = tipo;
        this.activo = activo;
    }

    /**
     * Método polimórfico fundamental de Tecnología de Objetos.
     * Cada tipo de producto calcula su precio final de venta según sus características.
     */
    public abstract double calcularPrecio();

    /**
     * Regla de negocio encapsulada: descontar stock.
     * Valida que la cantidad sea positiva y que no exceda las existencias actuales.
     * Si el nuevo stock alcanza o es menor al mínimo, notifica a los observadores.
     */
    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a 0");
        }
        if (cantidad > this.stock) {
            throw new StockInsuficienteException(this.codigo != null ? this.codigo : this.nombre, this.stock, cantidad);
        }
        this.stock -= cantidad;

        if (this.stock <= this.stockMinimo) {
            stockSubject.notificarStockBajo(this);
        }
    }

    /**
     * Regla de negocio encapsulada: agregar stock (por compra o reposición).
     */
    public void agregarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor a 0");
        }
        this.stock += cantidad;
    }

    // Métodos para el patrón Observer
    public void agregarObservador(StockObserver observer) {
        stockSubject.agregarObservador(observer);
    }

    public void removerObservador(StockObserver observer) {
        stockSubject.removerObservador(observer);
    }

    public StockSubject getStockSubject() {
        return stockSubject;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) {
        if (precioCompra < 0) throw new IllegalArgumentException("Precio de compra no puede ser negativo");
        this.precioCompra = precioCompra;
    }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) {
        if (precioVenta < 0) throw new IllegalArgumentException("Precio de venta no puede ser negativo");
        this.precioVenta = precioVenta;
    }

    public int getStock() { return stock; }
    protected void setStockDirecto(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) throw new IllegalArgumentException("Stock mínimo no puede ser negativo");
        this.stockMinimo = stockMinimo;
    }

    public TipoProducto getTipo() { return tipo; }
    public void setTipo(TipoProducto tipo) { this.tipo = tipo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean esStockBajo() {
        return this.stock <= this.stockMinimo;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (Stock: %d | S/. %.2f)", codigo, nombre, stock, precioVenta);
    }
}
