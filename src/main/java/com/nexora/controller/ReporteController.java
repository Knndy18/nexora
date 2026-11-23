package com.nexora.controller;

import com.nexora.model.Venta;
import com.nexora.service.ReporteService;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controlador de Reportes.
 * Aplica Multithreading con Task de JavaFX y ExecutorService,
 * ejecutando el cálculo analítico en un hilo secundario y
 * sincronizando la UI mediante Platform.runLater().
 */
public class ReporteController implements Initializable {

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private Button btnGenerar;
    @FXML private ProgressIndicator progresoCarga;
    @FXML private Label lblEstado;

    @FXML private Label lblIngresos;
    @FXML private Label lblCostos;
    @FXML private Label lblUtilidad;
    @FXML private Label lblMargen;

    @FXML private TableView<ReporteService.ProductoRankingDTO> tblTopProductos;
    @FXML private TableColumn<ReporteService.ProductoRankingDTO, String> colTopCodigo;
    @FXML private TableColumn<ReporteService.ProductoRankingDTO, String> colTopNombre;
    @FXML private TableColumn<ReporteService.ProductoRankingDTO, Number> colTopCantidad;
    @FXML private TableColumn<ReporteService.ProductoRankingDTO, Number> colTopTotal;

    @FXML private TableView<Venta> tblVentasPeriodo;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaFecha;
    @FXML private TableColumn<Venta, Number> colVentaSubtotal;
    @FXML private TableColumn<Venta, Number> colVentaDescuento;
    @FXML private TableColumn<Venta, Number> colVentaTotal;

    private final ReporteService reporteService = new ReporteService();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ReporteThread");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablas();

        // Rango por defecto: del primer día del mes actual al día de hoy
        LocalDate hoy = LocalDate.now();
        dpInicio.setValue(hoy.withDayOfMonth(1));
        dpFin.setValue(hoy);

        generarReporteAsincrono();
    }

    private void configurarTablas() {
        colTopCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        colTopNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colTopCantidad.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidadVendida()));
        colTopTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalRecaudado()));

        colVentaId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colVentaFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        colVentaSubtotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getSubtotal()));
        colVentaDescuento.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getDescuento()));
        colVentaTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotal()));
    }

    @FXML
    public void generarReporteAsincrono() {
        LocalDate inicio = dpInicio.getValue() != null ? dpInicio.getValue() : LocalDate.now().minusDays(30);
        LocalDate fin = dpFin.getValue() != null ? dpFin.getValue() : LocalDate.now();

        if (inicio.isAfter(fin)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "La fecha de inicio no puede ser posterior a la fecha final.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        btnGenerar.setDisable(true);
        progresoCarga.setVisible(true);
        lblEstado.setText("Calculando reporte en segundo plano...");

        // Creación del Task de JavaFX para concurrencia y multithreading
        Task<ReporteService.ReporteFinancieroDTO> reporteTask = new Task<>() {
            @Override
            protected ReporteService.ReporteFinancieroDTO call() throws Exception {
                long tInicio = System.currentTimeMillis();
                // Simulación de carga analítica intensiva
                Thread.sleep(250);
                ReporteService.ReporteFinancieroDTO resultado = reporteService.generarReporteFinanciero(inicio, fin);
                long duracion = System.currentTimeMillis() - tInicio;
                updateMessage("Reporte generado en " + duracion + " ms sin congelar la interfaz.");
                return resultado;
            }
        };

        reporteTask.setOnSucceeded(e -> {
            ReporteService.ReporteFinancieroDTO dto = reporteTask.getValue();

            // Sincronización explícita con el JavaFX Application Thread
            Platform.runLater(() -> {
                lblIngresos.setText(String.format("S/. %.2f", dto.getIngresosTotales()));
                lblCostos.setText(String.format("S/. %.2f", dto.getCostoTotalEstimado()));
                lblUtilidad.setText(String.format("S/. %.2f", dto.getUtilidadBruta()));
                lblMargen.setText(String.format("%.1f %%", dto.getMargenPorcentaje()));

                tblTopProductos.setItems(FXCollections.observableArrayList(dto.getTopProductos()));
                tblVentasPeriodo.setItems(FXCollections.observableArrayList(dto.getListaVentas()));

                lblEstado.setText(reporteTask.getMessage());
                btnGenerar.setDisable(false);
                progresoCarga.setVisible(false);
            });
        });

        reporteTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                btnGenerar.setDisable(false);
                progresoCarga.setVisible(false);
                lblEstado.setText("Error al generar reporte: " + reporteTask.getException().getMessage());
            });
        });

        executorService.submit(reporteTask);
    }
}
