package com.nexora.controller;

import com.nexora.model.Compra;
import com.nexora.model.DetalleCompra;
import com.nexora.model.Producto;
import com.nexora.model.Proveedor;
import com.nexora.service.CompraService;
import com.nexora.service.ProductoService;
import com.nexora.service.ProveedorService;
import com.nexora.util.ValidationException;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CompraController implements Initializable {

    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private TextField txtComprobante;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecioCompra;

    @FXML private TableView<DetalleCompra> tblItemsCompra;
    @FXML private TableColumn<DetalleCompra, String> colItemProducto;
    @FXML private TableColumn<DetalleCompra, Number> colItemCantidad;
    @FXML private TableColumn<DetalleCompra, Number> colItemPrecio;
    @FXML private TableColumn<DetalleCompra, Number> colItemSubtotal;
    @FXML private Label lblTotalCompra;

    // Historial
    @FXML private TableView<Compra> tblHistorialCompras;
    @FXML private TableColumn<Compra, Number> colHistId;
    @FXML private TableColumn<Compra, String> colHistProveedor;
    @FXML private TableColumn<Compra, String> colHistComprobante;
    @FXML private TableColumn<Compra, String> colHistFecha;
    @FXML private TableColumn<Compra, Number> colHistTotal;

    private final ProveedorService proveedorService = new ProveedorService();
    private final ProductoService productoService = new ProductoService();
    private final CompraService compraService = new CompraService();

    private final ObservableList<DetalleCompra> listaItems = FXCollections.observableArrayList();
    private final ObservableList<Compra> listaHistorial = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablas();
        cargarProveedores();
        cargarProductos();
        cargarHistorialCompras();

        cmbProducto.setOnAction(e -> {
            Producto p = cmbProducto.getValue();
            if (p != null) {
                txtPrecioCompra.setText(String.valueOf(p.getPrecioCompra()));
            }
        });
    }

    private void configurarTablas() {
        colItemProducto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombre()));
        colItemCantidad.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidad()));
        colItemPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioUnitario()));
        colItemSubtotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getSubtotal()));
        tblItemsCompra.setItems(listaItems);

        colHistId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colHistProveedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProveedor() != null ? d.getValue().getProveedor().getNombre() : "-"));
        colHistComprobante.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getComprobante()));
        colHistFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        colHistTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotal()));
        tblHistorialCompras.setItems(listaHistorial);
    }

    private void cargarProveedores() {
        cmbProveedor.setItems(FXCollections.observableArrayList(proveedorService.listarTodos()));
    }

    private void cargarProductos() {
        cmbProducto.setItems(FXCollections.observableArrayList(productoService.listarActivos()));
    }

    public void cargarHistorialCompras() {
        com.nexora.repository.CompraRepository repo = new com.nexora.repository.CompraRepository();
        listaHistorial.setAll(repo.listarTodos());
    }

    @FXML
    public void agregarItemCompra() {
        try {
            Producto producto = cmbProducto.getValue();
            if (producto == null) throw new ValidationException("Seleccione un producto.");

            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) throw new ValidationException("La cantidad debe ser mayor a 0.");

            double precio = Double.parseDouble(txtPrecioCompra.getText().trim());
            if (precio < 0) throw new ValidationException("El precio no puede ser negativo.");

            DetalleCompra detalle = new DetalleCompra(producto, cantidad, precio);
            listaItems.add(detalle);

            recalcularTotal();
            txtCantidad.clear();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "Cantidad y precio deben ser valores numéricos.");
        } catch (ValidationException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        }
    }

    @FXML
    public void quitarItemCompra() {
        DetalleCompra sel = tblItemsCompra.getSelectionModel().getSelectedItem();
        if (sel != null) {
            listaItems.remove(sel);
            recalcularTotal();
        }
    }

    private void recalcularTotal() {
        double total = 0.0;
        for (DetalleCompra d : listaItems) {
            total += d.getSubtotal();
        }
        total = Math.round(total * 100.0) / 100.0;
        lblTotalCompra.setText(String.format("S/. %.2f", total));
    }

    @FXML
    public void guardarCompra() {
        try {
            Proveedor prov = cmbProveedor.getValue();
            if (prov == null) throw new ValidationException("Seleccione un proveedor.");
            if (listaItems.isEmpty()) throw new ValidationException("Agregue al menos un producto a la compra.");

            String comprobante = txtComprobante.getText().trim();

            Compra compra = new Compra();
            compra.setProveedor(prov);
            compra.setComprobante(comprobante.isEmpty() ? "S/N" : comprobante);
            compra.getDetalles().addAll(listaItems);

            Compra guardada = compraService.registrarCompra(compra);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Compra Exitosa",
                    String.format("¡Compra #%d registrada!\nTotal invertido: S/. %.2f\nStock de los productos incrementado.",
                            guardada.getId(), guardada.getTotal()));

            listaItems.clear();
            recalcularTotal();
            txtComprobante.clear();
            cmbProveedor.getSelectionModel().clearSelection();
            cmbProducto.getSelectionModel().clearSelection();
            cargarHistorialCompras();
        } catch (ValidationException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación de Compra", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar la compra: " + e.getMessage());
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
