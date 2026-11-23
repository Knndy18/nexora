package com.nexora.controller;

import com.nexora.model.Proveedor;
import com.nexora.service.ProveedorService;
import com.nexora.util.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ProveedorController implements Initializable {

    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, String> colRuc;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TableColumn<Proveedor, String> colDireccion;

    @FXML private Label lblFormTitulo;
    @FXML private TextField txtRuc;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private Button btnEliminar;

    private final ProveedorService proveedorService = new ProveedorService();
    private final ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private Proveedor proveedorSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colRuc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRuc()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colDireccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDireccion()));

        tblProveedores.setItems(listaProveedores);
        btnEliminar.setDisable(true);
        cargarProveedores();
    }

    public void cargarProveedores() {
        listaProveedores.setAll(proveedorService.listarTodos());
    }

    @FXML
    public void seleccionarProveedor() {
        proveedorSeleccionado = tblProveedores.getSelectionModel().getSelectedItem();
        if (proveedorSeleccionado == null) return;

        lblFormTitulo.setText("Editar Proveedor #" + proveedorSeleccionado.getId());
        txtRuc.setText(proveedorSeleccionado.getRuc());
        txtNombre.setText(proveedorSeleccionado.getNombre());
        txtTelefono.setText(proveedorSeleccionado.getTelefono());
        txtEmail.setText(proveedorSeleccionado.getEmail());
        txtDireccion.setText(proveedorSeleccionado.getDireccion());
        btnEliminar.setDisable(false);
    }

    @FXML
    public void limpiarFormulario() {
        proveedorSeleccionado = null;
        lblFormTitulo.setText("Nuevo Proveedor");
        txtRuc.clear();
        txtNombre.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        btnEliminar.setDisable(true);
        tblProveedores.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardarProveedor() {
        try {
            String ruc = txtRuc.getText().trim();
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();
            String direccion = txtDireccion.getText().trim();

            if (proveedorSeleccionado == null) {
                Proveedor nuevo = new Proveedor(nombre, ruc, telefono, email, direccion);
                proveedorService.registrarProveedor(nuevo);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Proveedor registrado correctamente.");
            } else {
                proveedorSeleccionado.setRuc(ruc);
                proveedorSeleccionado.setNombre(nombre);
                proveedorSeleccionado.setTelefono(telefono);
                proveedorSeleccionado.setEmail(email);
                proveedorSeleccionado.setDireccion(direccion);
                proveedorService.actualizarProveedor(proveedorSeleccionado);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Proveedor actualizado correctamente.");
            }

            limpiarFormulario();
            cargarProveedores();
        } catch (ValidationException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el proveedor: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarProveedor() {
        if (proveedorSeleccionado == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el proveedor '" + proveedorSeleccionado.getNombre() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            proveedorService.eliminarProveedor(proveedorSeleccionado.getId());
            limpiarFormulario();
            cargarProveedores();
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
