package com.nexora.model;

/**
 * Especialización de Producto para bienes unitarios no perecibles
 * (detergentes, utensilios, herramientas, artículos de limpieza general).
 */
public class ProductoUnitario extends Producto {

    public ProductoUnitario() {
        setTipo(TipoProducto.UNITARIO);
    }

    public ProductoUnitario(Integer id, String codigo, String nombre, Integer idCategoria,
                            double precioCompra, double precioVenta, int stock, int stockMinimo,
                            boolean activo) {
        super(id, codigo, nombre, idCategoria, precioCompra, precioVenta, stock, stockMinimo, TipoProducto.UNITARIO, activo);
    }

    @Override
    public double calcularPrecio() {
        // En productos unitarios estándar se respeta el precio de venta regular establecido
        return getPrecioVenta();
    }
}
