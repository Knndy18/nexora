package com.nexora.controller;

import com.nexora.model.Categoria;
import com.nexora.service.CategoriaService;
import com.nexora.util.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoriaController implements Initializable {

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Number> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;

    @FXML private Label lblFormTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnEliminar;

    private final CategoriaService categoriaService = new CategoriaService();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private Categoria categoriaSeleccionada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

        tblCategorias.setItems(listaCategorias);
        btnEliminar.setDisable(true);
        cargarCategorias();
    }

    public void cargarCategorias() {
        listaCategorias.setAll(categoriaService.listarTodas());
    }

    @FXML
    public void seleccionarCategoria() {
        categoriaSeleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSeleccionada == null) return;

        lblFormTitulo.setText("Editar Categoría #" + categoriaSeleccionada.getId());
        txtNombre.setText(categoriaSeleccionada.getNombre());
        txtDescripcion.setText(categoriaSeleccionada.getDescripcion());
        btnEliminar.setDisable(false);
    }

    @FXML
    public void limpiarFormulario() {
        categoriaSeleccionada = null;
        lblFormTitulo.setText("Nueva Categoría");
        txtNombre.clear();
        txtDescripcion.clear();
        btnEliminar.setDisable(true);
        tblCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardarCategoria() {
        try {
            String nombre = txtNombre.getText().trim();
            String descripcion = txtDescripcion.getText().trim();

            if (categoriaSeleccionada == null) {
                Categoria nueva = new Categoria(nombre, descripcion);
                categoriaService.registrarCategoria(nueva);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría creada correctamente.");
            } else {
                categoriaSeleccionada.setNombre(nombre);
                categoriaSeleccionada.setDescripcion(descripcion);
                categoriaService.actualizarCategoria(categoriaSeleccionada);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría actualizada correctamente.");
            }

            limpiarFormulario();
            cargarCategorias();
        } catch (ValidationException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar la categoría: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarCategoria() {
        if (categoriaSeleccionada == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro de eliminar la categoría '" + categoriaSeleccionada.getNombre() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                categoriaService.eliminarCategoria(categoriaSeleccionada.getId());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría eliminada.");
                limpiarFormulario();
                cargarCategorias();
            } catch (ValidationException e) {
                // RF-02: Alerta si tiene productos vinculados
                mostrarAlerta(Alert.AlertType.ERROR, "Acción Denegada (RF-02)", e.getMessage());
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
