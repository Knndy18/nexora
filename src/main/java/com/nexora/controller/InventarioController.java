package com.nexora.controller;

import com.nexora.model.MovimientoStock;
import com.nexora.model.Producto;
import com.nexora.service.InventarioService;
import com.nexora.service.ProductoService;
import com.nexora.util.ValidationException;
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

public class InventarioController implements Initializable {

    @FXML private TableView<MovimientoStock> tblMovimientos;
    @FXML private TableColumn<MovimientoStock, Number> colId;
    @FXML private TableColumn<MovimientoStock, String> colFecha;
    @FXML private TableColumn<MovimientoStock, String> colProducto;
    @FXML private TableColumn<MovimientoStock, String> colTipo;
    @FXML private TableColumn<MovimientoStock, Number> colCantidad;
    @FXML private TableColumn<MovimientoStock, String> colMotivo;

    // Formulario de Ajuste
    @FXML private ComboBox<Producto> cmbProductoAjuste;
    @FXML private Label lblStockActual;
    @FXML private TextField txtNuevoStock;
    @FXML private TextArea txtMotivoAjuste;

    private final InventarioService inventarioService = new InventarioService();
    private final ProductoService productoService = new ProductoService();
    private final ObservableList<MovimientoStock> listaMovimientos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarProductosAjuste();
        cargarMovimientos();

        cmbProductoAjuste.setOnAction(e -> {
            Producto p = cmbProductoAjuste.getValue();
            if (p != null) {
                lblStockActual.setText(p.getStock() + " unidades");
            } else {
                lblStockActual.setText("-");
            }
        });
    }

    private void configurarTabla() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        colProducto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto() != null ? d.getValue().getProducto().getNombre() : "ID #" + d.getValue().getId()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colCantidad.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidad()));
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));

        tblMovimientos.setItems(listaMovimientos);
    }

    private void cargarProductosAjuste() {
        cmbProductoAjuste.setItems(FXCollections.observableArrayList(productoService.listarActivos()));
    }

    public void cargarMovimientos() {
        listaMovimientos.setAll(inventarioService.listarMovimientosRecientes(150));
    }

    @FXML
    public void aplicarAjuste() {
        try {
            Producto prod = cmbProductoAjuste.getValue();
            if (prod == null) throw new ValidationException("Debe seleccionar un producto.");

            int nuevoStock = Integer.parseInt(txtNuevoStock.getText().trim());
            String motivo = txtMotivoAjuste.getText().trim();

            inventarioService.registrarAjuste(prod.getId(), nuevoStock, motivo);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Ajuste Aplicado",
                    String.format("Stock de '%s' actualizado a %d unidades. Movimiento registrado en el Kardex.",
                            prod.getNombre(), nuevoStock));

            txtNuevoStock.clear();
            txtMotivoAjuste.clear();
            cargarProductosAjuste();
            cargarMovimientos();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Entrada", "El stock debe ser un número entero.");
        } catch (ValidationException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo aplicar el ajuste: " + e.getMessage());
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
