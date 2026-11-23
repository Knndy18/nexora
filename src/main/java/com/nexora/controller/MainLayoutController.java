package com.nexora.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnPos;
    @FXML private Button btnProductos;
    @FXML private Button btnCategorias;
    @FXML private Button btnCompras;
    @FXML private Button btnProveedores;
    @FXML private Button btnInventario;
    @FXML private Button btnReportes;

    private Button botonActivo;
    private final Map<String, Node> vistasCacheadas = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mostrarDashboard();
    }

    @FXML
    public void mostrarDashboard() {
        cargarVista("/views/dashboard.fxml", btnDashboard);
    }

    @FXML
    public void mostrarPOS() {
        // En POS recargamos para reflejar cambios frescos de inventario
        cargarVistaRecargando("/views/pos.fxml", btnPos);
    }

    @FXML
    public void mostrarProductos() {
        cargarVistaRecargando("/views/productos.fxml", btnProductos);
    }

    @FXML
    public void mostrarCategorias() {
        cargarVistaRecargando("/views/categorias.fxml", btnCategorias);
    }

    @FXML
    public void mostrarCompras() {
        cargarVistaRecargando("/views/compras.fxml", btnCompras);
    }

    @FXML
    public void mostrarProveedores() {
        cargarVistaRecargando("/views/proveedores.fxml", btnProveedores);
    }

    @FXML
    public void mostrarInventario() {
        cargarVistaRecargando("/views/inventario.fxml", btnInventario);
    }

    @FXML
    public void mostrarReportes() {
        cargarVistaRecargando("/views/reportes.fxml", btnReportes);
    }

    private void cargarVista(String fxmlPath, Button navBtn) {
        try {
            Node vista = vistasCacheadas.get(fxmlPath);
            if (vista == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                vista = loader.load();
                vistasCacheadas.put(fxmlPath, vista);
            }
            contentArea.getChildren().setAll(vista);
            actualizarEstiloBoton(navBtn);
        } catch (IOException e) {
            System.err.println("Error al cargar vista: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void cargarVistaRecargando(String fxmlPath, Button navBtn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vista = loader.load();
            vistasCacheadas.put(fxmlPath, vista);
            contentArea.getChildren().setAll(vista);
            actualizarEstiloBoton(navBtn);
        } catch (IOException e) {
            System.err.println("Error al recargar vista: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void actualizarEstiloBoton(Button navBtn) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("nav-button-active");
        }
        if (navBtn != null) {
            navBtn.getStyleClass().add("nav-button-active");
            botonActivo = navBtn;
        }
    }
}
