package com.nexora.controller;

import com.nexora.factory.ProductoFactory;
import com.nexora.model.Categoria;
import com.nexora.model.Producto;
import com.nexora.model.ProductoPerecible;
import com.nexora.model.TipoProducto;
import com.nexora.service.CategoriaService;
import com.nexora.service.ProductoService;
import com.nexora.util.ValidationException;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProductoController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbFiltroCat;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colTipo;
    @FXML private TableColumn<Producto, Number> colStock;
    @FXML private TableColumn<Producto, Number> colStockMin;
    @FXML private TableColumn<Producto, Number> colPCompra;
    @FXML private TableColumn<Producto, Number> colPVenta;
    @FXML private TableColumn<Producto, String> colVencimiento;
    @FXML private TableColumn<Producto, String> colEstado;

    // Formulario
    @FXML private Label lblFormularioTitulo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cmbCategoriaForm;
    @FXML private ComboBox<TipoProducto> cmbTipoForm;
    @FXML private VBox vboxFechaVencimiento;
    @FXML private DatePicker dpFechaVencimiento;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMinimo;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnDesactivar;

    private final ProductoService productoService = new ProductoService();
    private final CategoriaService categoriaService = new CategoriaService();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private Producto productoSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarFormulario();
        cargarCategorias();
        cargarProductos();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCategoria()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colStock.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStock()));
        colStockMin.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStockMinimo()));
        colPCompra.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioCompra()));
        colPVenta.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioVenta()));

        colVencimiento.setCellValueFactory(d -> {
            if (d.getValue() instanceof ProductoPerecible perecible && perecible.getFechaVencimiento() != null) {
                return new SimpleStringProperty(perecible.getFechaVencimiento().toString());
            }
            return new SimpleStringProperty("-");
        });

        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));

        tblProductos.setItems(listaProductos);
    }

    private void configurarFormulario() {
        cmbTipoForm.setItems(FXCollections.observableArrayList(TipoProducto.values()));
        cmbTipoForm.getSelectionModel().select(TipoProducto.UNITARIO);
        cambiarTipoProductoForm();
        btnDesactivar.setDisable(true);
    }

    private void cargarCategorias() {
        List<Categoria> cats = categoriaService.listarTodas();
        cmbCategoriaForm.setItems(FXCollections.observableArrayList(cats));

        List<Categoria> filtroCats = new ArrayList<>();
        filtroCats.add(new Categoria(null, "Todas las categorías", ""));
        filtroCats.addAll(cats);
        cmbFiltroCat.setItems(FXCollections.observableArrayList(filtroCats));
        cmbFiltroCat.getSelectionModel().selectFirst();
    }

    public void cargarProductos() {
        listaProductos.setAll(productoService.listarTodos());
        tblProductos.setItems(listaProductos);
    }

    @FXML
    public void cambiarTipoProductoForm() {
        TipoProducto tipo = cmbTipoForm.getValue();
        vboxFechaVencimiento.setVisible(tipo == TipoProducto.PERECIBLE);
        vboxFechaVencimiento.setManaged(tipo == TipoProducto.PERECIBLE);
    }

    @FXML
    public void seleccionarProductoTabla() {
        productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) return;

        lblFormularioTitulo.setText("Editar Producto #" + productoSeleccionado.getId());
        txtCodigo.setText(productoSeleccionado.getCodigo());
        txtNombre.setText(productoSeleccionado.getNombre());

        for (Categoria c : cmbCategoriaForm.getItems()) {
            if (c.getId().equals(productoSeleccionado.getIdCategoria())) {
                cmbCategoriaForm.setValue(c);
                break;
            }
        }

        cmbTipoForm.setValue(productoSeleccionado.getTipo());
        cambiarTipoProductoForm();

        if (productoSeleccionado instanceof ProductoPerecible perecible) {
            dpFechaVencimiento.setValue(perecible.getFechaVencimiento());
        } else {
            dpFechaVencimiento.setValue(null);
        }

        txtPrecioCompra.setText(String.valueOf(productoSeleccionado.getPrecioCompra()));
        txtPrecioVenta.setText(String.valueOf(productoSeleccionado.getPrecioVenta()));
        txtStock.setText(String.valueOf(productoSeleccionado.getStock()));
        txtStockMinimo.setText(String.valueOf(productoSeleccionado.getStockMinimo()));
        chkActivo.setSelected(productoSeleccionado.isActivo());

        btnDesactivar.setDisable(false);
    }

    @FXML
    public void limpiarFormulario() {
        productoSeleccionado = null;
        lblFormularioTitulo.setText("Nuevo Producto");
        txtCodigo.clear();
        txtNombre.clear();
        cmbCategoriaForm.getSelectionModel().clearSelection();
        cmbTipoForm.setValue(TipoProducto.UNITARIO);
        cambiarTipoProductoForm();
        dpFechaVencimiento.setValue(null);
        txtPrecioCompra.clear();
        txtPrecioVenta.clear();
        txtStock.clear();
        txtStockMinimo.setText("5");
        chkActivo.setSelected(true);
        btnDesactivar.setDisable(true);
        tblProductos.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardarProducto() {
        try {
            String codigo = txtCodigo.getText().trim();
            String nombre = txtNombre.getText().trim();
            Categoria cat = cmbCategoriaForm.getValue();
            TipoProducto tipo = cmbTipoForm.getValue();

            if (cat == null) throw new ValidationException("Seleccione una categoría.");
            if (tipo == null) throw new ValidationException("Seleccione un tipo de producto.");

            double precioCompra = Double.parseDouble(txtPrecioCompra.getText().trim());
            double precioVenta = Double.parseDouble(txtPrecioVenta.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
            LocalDate fechaVenc = (tipo == TipoProducto.PERECIBLE) ? dpFechaVencimiento.getValue() : null;
            boolean activo = chkActivo.isSelected();

            // Uso del patrón ProductoFactory
            Integer id = (productoSeleccionado != null) ? productoSeleccionado.getId() : null;
            Producto producto = ProductoFactory.crearProducto(
                    tipo, id, codigo, nombre, cat.getId(), precioCompra, precioVenta,
                    stock, stockMinimo, fechaVenc, activo
            );

            if (productoSeleccionado == null) {
                productoService.registrarProducto(producto);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto registrado correctamente.");
            } else {
                productoService.actualizarProducto(producto);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            }

            limpiarFormulario();
            cargarProductos();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Dato Inválido", "Precios y existencias deben ser numéricos válidos.");
        } catch (ValidationException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el producto: " + e.getMessage());
        }
    }

    @FXML
    public void desactivarProducto() {
        if (productoSeleccionado == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro de desactivar el producto '" + productoSeleccionado.getNombre() + "'?\nNo aparecerá en el POS pero conservará su historial.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            productoService.desactivarProducto(productoSeleccionado.getId());
            limpiarFormulario();
            cargarProductos();
        }
    }

    @FXML
    public void filtrar() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
        Categoria cat = cmbFiltroCat.getValue();

        List<Producto> filtrados = productoService.listarTodos().stream()
                .filter(p -> {
                    boolean coincideTexto = texto.isEmpty()
                            || p.getNombre().toLowerCase().contains(texto)
                            || p.getCodigo().toLowerCase().contains(texto);

                    boolean coincideCat = cat == null || cat.getId() == null
                            || p.getIdCategoria().equals(cat.getId());

                    return coincideTexto && coincideCat;
                })
                .toList();

        tblProductos.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
