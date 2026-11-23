package com.nexora.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para la capa de acceso a datos.
 * Aplica Programación Genérica (Generic Programming en Java) parametrizando
 * el tipo de entidad (T) y el identificador (ID), promoviendo la reutilización de código.
 *
 * @param <T> Tipo de entidad de dominio
 * @param <ID> Tipo del identificador primario
 */
public interface Repositorio<T, ID> {

    /**
     * Inserta una nueva entidad en la base de datos y le asigna su ID generado.
     */
    T guardar(T entidad);

    /**
     * Busca una entidad por su clave primaria.
     */
    Optional<T> buscarPorId(ID id);

    /**
     * Obtiene todas las entidades almacenadas.
     */
    List<T> listarTodos();

    /**
     * Actualiza el estado de la entidad existente.
     */
    void actualizar(T entidad);

    /**
     * Elimina físicamente la entidad por su clave primaria.
     */
    void eliminar(ID id);
}
