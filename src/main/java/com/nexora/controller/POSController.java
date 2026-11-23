package com.nexora.controller;

import com.nexora.model.Categoria;
import com.nexora.model.DetalleVenta;
import com.nexora.model.Producto;
import com.nexora.model.Venta;
import com.nexora.service.CategoriaService;
import com.nexora.service.ProductoService;
import com.nexora.service.VentaService;
import com.nexora.strategy.*;
import com.nexora.util.StockInsuficienteException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class POSController implements Initializable {

    // Catálogo
    @FXML private TextField txtBuscarProducto;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private TableView<Producto> tblCatalogoPOS;
    @FXML private TableColumn<Producto, String> colCatCodigo;
    @FXML private TableColumn<Producto, String> colCatNombre;
    @FXML private TableColumn<Producto, String> colCatTipo;
    @FXML private TableColumn<Producto, Number> colCatStock;
    @FXML private TableColumn<Producto, Number> colCatPrecio;
    @FXML private Spinner<Integer> spnCantidadAgregar;

    // Carrito
    @FXML private TableView<DetalleVenta> tblCarrito;
    @FXML private TableColumn<DetalleVenta, String> colCarNombre;
    @FXML private TableColumn<DetalleVenta, Number> colCarPrecio;
    @FXML private TableColumn<DetalleVenta, Number> colCarCantidad;
    @FXML private TableColumn<DetalleVenta, Number> colCarSubtotal;

    // Descuentos y Totales
    @FXML private ComboBox<DescuentoStrategy> cmbEstrategiaDescuento;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;

    private final ProductoService productoService = new ProductoService();
    private final CategoriaService categoriaService = new CategoriaService();
    private final VentaService ventaService = new VentaService();

    private final ObservableList<Producto> listaProductosCatalogo = FXCollections.observableArrayList();
    private final ObservableList<DetalleVenta> listaCarrito = FXCollections.observableArrayList();
    private final Venta ventaActual = new Venta();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablas();
        configurarEstrategiasDescuento();
        configurarSpinner();
        cargarCategorias();
        cargarProductos();
    }

    private void configurarTablas() {
        // Catálogo
        colCatCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        colCatNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colCatTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colCatStock.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStock()));
        colCatPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().calcularPrecio()));

        // Carrito
        colCarNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombre()));
        colCarPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioUnitario()));
        colCarCantidad.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidad()));
        colCarSubtotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getSubtotal()));

        tblCarrito.setItems(listaCarrito);
    }

    private void configurarEstrategiasDescuento() {
        ObservableList<DescuentoStrategy> estrategias = FXCollections.observableArrayList(
                new SinDescuento(),
                new DescuentoPorcentaje(5.0),
                new DescuentoPorcentaje(10.0),
                new DescuentoPorVolumen(5, 10.0) // 10% si lleva 5 o más unidades
        );
        cmbEstrategiaDescuento.setItems(estrategias);
        cmbEstrategiaDescuento.getSelectionModel().selectFirst();
    }

    private void configurarSpinner() {
        spnCantidadAgregar.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
    }

    private void cargarCategorias() {
        List<Categoria> cats = new ArrayList<>();
        cats.add(new Categoria(null, "Todas las Categorías", ""));
        cats.addAll(categoriaService.listarTodas());
        cmbFiltroCategoria.setItems(FXCollections.observableArrayList(cats));
        cmbFiltroCategoria.getSelectionModel().selectFirst();
    }

    public void cargarProductos() {
        listaProductosCatalogo.setAll(productoService.listarActivos());
        tblCatalogoPOS.setItems(listaProductosCatalogo);
    }

    @FXML
    public void filtrarProductos() {
        String texto = txtBuscarProducto.getText() != null ? txtBuscarProducto.getText().trim().toLowerCase() : "";
        Categoria catSeleccionada = cmbFiltroCategoria.getValue();

        List<Producto> filtrados = productoService.listarActivos().stream()
                .filter(p -> {
                    boolean coincideTexto = texto.isEmpty()
                            || p.getNombre().toLowerCase().contains(texto)
                            || p.getCodigo().toLowerCase().contains(texto);

                    boolean coincideCat = catSeleccionada == null || catSeleccionada.getId() == null
                            || p.getIdCategoria().equals(catSeleccionada.getId());

                    return coincideTexto && coincideCat;
                })
                .toList();

        tblCatalogoPOS.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    public void agregarAlCarrito() {
        Producto seleccionado = tblCatalogoPOS.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Por favor seleccione un producto del catálogo para agregar.");
            return;
        }

        int cantidad = spnCantidadAgregar.getValue();

        // Validar si el producto ya está en el carrito
        DetalleVenta existente = null;
        for (DetalleVenta d : listaCarrito) {
            if (d.getProducto().getId().equals(seleccionado.getId())) {
                existente = d;
                break;
            }
        }

        int cantidadTotal = cantidad + (existente != null ? existente.getCantidad() : 0);
        if (cantidadTotal > seleccionado.getStock()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Stock insuficiente",
                    String.format("No hay suficiente stock para '%s'. Stock actual: %d, Solicitado: %d",
                            seleccionado.getNombre(), seleccionado.getStock(), cantidadTotal));
            return;
        }

        if (existente != null) {
            existente.setCantidad(cantidadTotal);
            tblCarrito.refresh();
        } else {
            DetalleVenta nuevoDetalle = new DetalleVenta(seleccionado, cantidad);
            listaCarrito.add(nuevoDetalle);
        }

        spnCantidadAgregar.getValueFactory().setValue(1);
        recalcularTotales();
    }

    @FXML
    public void quitarDelCarrito() {
        DetalleVenta seleccionado = tblCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            listaCarrito.remove(seleccionado);
            recalcularTotales();
        }
    }

    @FXML
    public void limpiarCarrito() {
        listaCarrito.clear();
        recalcularTotales();
    }

    @FXML
    public void cambiarEstrategiaDescuento() {
        DescuentoStrategy seleccionada = cmbEstrategiaDescuento.getValue();
        if (seleccionada != null) {
            ventaService.setEstrategiaDescuento(seleccionada);
            recalcularTotales();
        }
    }

    private void recalcularTotales() {
        ventaActual.getDetalles().clear();
        ventaActual.getDetalles().addAll(listaCarrito);

        ventaService.calcularTotales(ventaActual);

        lblSubtotal.setText(String.format("S/. %.2f", ventaActual.getSubtotal()));
        lblDescuento.setText(String.format("- S/. %.2f", ventaActual.getDescuento()));
        lblTotal.setText(String.format("S/. %.2f", ventaActual.getTotal()));
    }

    @FXML
    public void procesarCobro() {
        if (listaCarrito.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Carrito Vacío", "Agregue productos al carrito antes de cobrar.");
            return;
        }

        try {
            Venta ventaParaRegistrar = new Venta();
            ventaParaRegistrar.getDetalles().addAll(listaCarrito);

            Venta confirmada = ventaService.procesarVenta(ventaParaRegistrar);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Venta Exitosa",
                    String.format("¡Venta #%d registrada con éxito!\nTotal cobrado: S/. %.2f\nStock actualizado correctamente.",
                            confirmada.getId(), confirmada.getTotal()));

            limpiarCarrito();
            cargarProductos();
        } catch (StockInsuficienteException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Stock Insuficiente", e.getMessage());
            cargarProductos();
        } catch (ValidationException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación de Venta", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error en Transacción", "No se pudo registrar la venta: " + e.getMessage());
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
