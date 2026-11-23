package com.nexora.repository;

import com.nexora.model.Categoria;
import com.nexora.model.Proveedor;
import com.nexora.util.DatabaseConnection;
import com.nexora.util.DatabaseInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Programación Genérica: Repositorio<T, ID>")
public class RepositorioGenericoTest {

    @BeforeAll
    static void setUp() {
        File testDir = new File("target/test_dbs");
        if (!testDir.exists()) testDir.mkdirs();

        String dbPath = "target/test_dbs/gen_" + UUID.randomUUID() + ".db";
        DatabaseConnection.setDatabaseUrl("jdbc:sqlite:" + dbPath);
        DatabaseInitializer.initialize();
    }

    @Test
    @DisplayName("Programación Genérica: Prueba de CRUD con Entidad 1 (Categoria)")
    void testRepositorioConCategoria() {
        Repositorio<Categoria, Integer> repo = new CategoriaRepository();

        Categoria nueva = new Categoria("Mascotas", "Alimentos y accesorios para mascotas");
        Categoria guardada = repo.guardar(nueva);

        assertNotNull(guardada.getId(), "La entidad Categoria debe recibir un ID generado");

        Optional<Categoria> encontrada = repo.buscarPorId(guardada.getId());
        assertTrue(encontrada.isPresent());
        assertEquals("Mascotas", encontrada.get().getNombre());

        // Actualizar
        guardada.setDescripcion("Alimentos premium y accesorios");
        repo.actualizar(guardada);
        assertEquals("Alimentos premium y accesorios", repo.buscarPorId(guardada.getId()).get().getDescripcion());

        // Eliminar
        repo.eliminar(guardada.getId());
        assertTrue(repo.buscarPorId(guardada.getId()).isEmpty());
    }

    @Test
    @DisplayName("Programación Genérica: Prueba de CRUD con Entidad 2 (Proveedor)")
    void testRepositorioConProveedor() {
        Repositorio<Proveedor, Integer> repo = new ProveedorRepository();

        Proveedor nuevo = new Proveedor("Distribuidora Arequipa", "20998877665", "955112233", "contacto@dist.pe", "Av. Cayma 100");
        Proveedor guardado = repo.guardar(nuevo);

        assertNotNull(guardado.getId(), "La entidad Proveedor debe recibir un ID generado");

        Optional<Proveedor> encontrado = repo.buscarPorId(guardado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("20998877665", encontrado.get().getRuc());

        // Eliminar
        repo.eliminar(guardado.getId());
        assertTrue(repo.buscarPorId(guardado.getId()).isEmpty());
    }
}
