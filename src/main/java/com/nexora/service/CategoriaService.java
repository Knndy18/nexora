package com.nexora.service;

import com.nexora.model.Categoria;
import com.nexora.repository.CategoriaRepository;
import com.nexora.util.ValidationException;

import java.util.List;
import java.util.Optional;

public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService() {
        this(new CategoriaRepository());
    }

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria registrarCategoria(Categoria categoria) {
        validarCategoria(categoria);
        return categoriaRepository.guardar(categoria);
    }

    public void actualizarCategoria(Categoria categoria) {
        if (categoria.getId() == null) {
            throw new ValidationException("ID de categoría requerido para actualizar.");
        }
        validarCategoria(categoria);
        categoriaRepository.actualizar(categoria);
    }

    /**
     * Regla de negocio RF-02: Impedir eliminar una categoría con productos asociados.
     */
    public void eliminarCategoria(int id) {
        if (categoriaRepository.tieneProductosAsociados(id)) {
            throw new ValidationException("No se puede eliminar la categoría porque tiene productos asociados en el catálogo.");
        }
        categoriaRepository.eliminar(id);
    }

    public Optional<Categoria> buscarPorId(int id) {
        return categoriaRepository.buscarPorId(id);
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.listarTodos();
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new ValidationException("La categoría no puede ser nula.");
        }
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre de la categoría es obligatorio.");
        }
    }
}
