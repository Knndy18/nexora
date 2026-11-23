package com.nexora.service;

import com.nexora.model.Proveedor;
import com.nexora.repository.ProveedorRepository;
import com.nexora.util.ValidationException;

import java.util.List;
import java.util.Optional;

public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService() {
        this(new ProveedorRepository());
    }

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public Proveedor registrarProveedor(Proveedor proveedor) {
        validarProveedor(proveedor);
        return proveedorRepository.guardar(proveedor);
    }

    public void actualizarProveedor(Proveedor proveedor) {
        if (proveedor.getId() == null) {
            throw new ValidationException("ID de proveedor requerido para actualizar.");
        }
        validarProveedor(proveedor);
        proveedorRepository.actualizar(proveedor);
    }

    public void eliminarProveedor(int id) {
        proveedorRepository.eliminar(id);
    }

    public Optional<Proveedor> buscarPorId(int id) {
        return proveedorRepository.buscarPorId(id);
    }

    public List<Proveedor> listarTodos() {
        return proveedorRepository.listarTodos();
    }

    private void validarProveedor(Proveedor p) {
        if (p == null) {
            throw new ValidationException("El proveedor no puede ser nulo.");
        }
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre de la empresa proveedora es obligatorio.");
        }
        if (p.getRuc() == null || p.getRuc().trim().length() < 8) {
            throw new ValidationException("El RUC del proveedor debe ser válido (mínimo 8 dígitos).");
        }
    }
}
