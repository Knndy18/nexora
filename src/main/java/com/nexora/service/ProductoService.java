package com.nexora.service;

import com.nexora.model.Producto;
import com.nexora.repository.ProductoRepository;
import com.nexora.util.ValidationException;

import java.util.List;
import java.util.Optional;

public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService() {
        this(new ProductoRepository());
    }

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto registrarProducto(Producto producto) {
        validarProducto(producto);

        // Verificar código único
        if (productoRepository.buscarPorCodigo(producto.getCodigo()).isPresent()) {
            throw new ValidationException("Ya existe un producto con el código: " + producto.getCodigo());
        }

        return productoRepository.guardar(producto);
    }

    public void actualizarProducto(Producto producto) {
        if (producto.getId() == null) {
            throw new ValidationException("El ID del producto es requerido para actualizar.");
        }
        validarProducto(producto);

        Optional<Producto> existente = productoRepository.buscarPorCodigo(producto.getCodigo());
        if (existente.isPresent() && !existente.get().getId().equals(producto.getId())) {
            throw new ValidationException("El código '" + producto.getCodigo() + "' ya está en uso por otro producto.");
        }

        productoRepository.actualizar(producto);
    }

    public void desactivarProducto(int id) {
        productoRepository.eliminar(id);
    }

    public Optional<Producto> buscarPorId(int id) {
        return productoRepository.buscarPorId(id);
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productoRepository.buscarPorCodigo(codigo);
    }

    public List<Producto> listarActivos() {
        return productoRepository.listarActivos();
    }

    public List<Producto> listarTodos() {
        return productoRepository.listarTodos();
    }

    public List<Producto> listarBajoStock() {
        return productoRepository.listarBajoStock();
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new ValidationException("El producto no puede ser nulo.");
        }
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            throw new ValidationException("El código del producto es obligatorio.");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del producto es obligatorio.");
        }
        if (producto.getIdCategoria() == null || producto.getIdCategoria() <= 0) {
            throw new ValidationException("Debe seleccionar una categoría válida.");
        }
        if (producto.getPrecioCompra() < 0) {
            throw new ValidationException("El precio de compra no puede ser negativo.");
        }
        if (producto.getPrecioVenta() < 0) {
            throw new ValidationException("El precio de venta no puede ser negativo.");
        }
        if (producto.getStock() < 0) {
            throw new ValidationException("El stock no puede ser negativo.");
        }
        if (producto.getStockMinimo() < 0) {
            throw new ValidationException("El stock mínimo no puede ser negativo.");
        }
    }
}
