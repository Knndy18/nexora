package com.nexora.factory;

import com.nexora.model.Producto;
import com.nexora.model.ProductoPerecible;
import com.nexora.model.ProductoUnitario;
import com.nexora.model.TipoProducto;

import java.time.LocalDate;

/**
 * Patrón de diseño Factory Method.
 * Encapsula la creación de instancias de Producto (ProductoUnitario o ProductoPerecible)
 * según el tipo especificado, aislando al cliente de la instanciación directa.
 */
public class ProductoFactory {

    public static Producto crearProducto(TipoProducto tipo, Integer id, String codigo, String nombre,
                                         Integer idCategoria, double precioCompra, double precioVenta,
                                         int stock, int stockMinimo, LocalDate fechaVencimiento, boolean activo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de producto no puede ser nulo");
        }

        switch (tipo) {
            case PERECIBLE:
                return new ProductoPerecible(id, codigo, nombre, idCategoria, precioCompra, precioVenta,
                        stock, stockMinimo, fechaVencimiento, activo);
            case UNITARIO:
                return new ProductoUnitario(id, codigo, nombre, idCategoria, precioCompra, precioVenta,
                        stock, stockMinimo, activo);
            default:
                throw new IllegalArgumentException("Tipo de producto no soportado: " + tipo);
        }
    }

    public static Producto crearProducto(String tipoString, Integer id, String codigo, String nombre,
                                         Integer idCategoria, double precioCompra, double precioVenta,
                                         int stock, int stockMinimo, String fechaVencimientoStr, boolean activo) {
        TipoProducto tipo = TipoProducto.valueOf(tipoString.toUpperCase());
        LocalDate fechaVenc = (fechaVencimientoStr != null && !fechaVencimientoStr.trim().isEmpty())
                ? LocalDate.parse(fechaVencimientoStr)
                : null;
        return crearProducto(tipo, id, codigo, nombre, idCategoria, precioCompra, precioVenta,
                stock, stockMinimo, fechaVenc, activo);
    }
}
