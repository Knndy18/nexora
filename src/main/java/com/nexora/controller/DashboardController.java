package com.nexora.controller;

import com.nexora.model.Producto;
import com.nexora.observer.AlertaStockBajo;
import com.nexora.service.ReporteService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasMes;
    @FXML private Label lblProductosTotal;
    @FXML private Label lblAlertasBajoStock;
    @FXML private BarChart<String, Number> chartVentas;

    @FXML private TableView<Producto> tblStockCritico;
    @FXML private TableColumn<Producto, String> colCodigoCritico;
    @FXML private TableColumn<Producto, String> colNombreCritico;
    @FXML private TableColumn<Producto, Number> colStockActual;
    @FXML private TableColumn<Producto, Number> colStockMinimo;

    @FXML private ListView<String> lstBitacoraObserver;

    private final ReporteService reporteService = new ReporteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablaStockCritico();
        cargarDatosDashboard();
    }

    private void configurarTablaStockCritico() {
        colCodigoCritico.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        colNombreCritico.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colStockActual.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStock()));
        colStockMinimo.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStockMinimo()));
    }

    @FXML
    public void cargarDatosDashboard() {
        ReporteService.DashboardResumenDTO resumen = reporteService.obtenerResumenDashboard();

        lblVentasHoy.setText(String.format("S/. %.2f", resumen.getTotalVentasHoy()));
        lblVentasMes.setText(String.format("S/. %.2f", resumen.getTotalVentasMes()));
        lblProductosTotal.setText(String.valueOf(resumen.getTotalProductosActivos()));
        lblAlertasBajoStock.setText(String.valueOf(resumen.getAlertasStockBajo()));

        // Configuración de Gráfico de Ventas
        chartVentas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas (S/.)");
        if (resumen.getVentasDias() != null) {
            for (ReporteService.VentaDiaDTO v : resumen.getVentasDias()) {
                series.getData().add(new XYChart.Data<>(v.getFecha(), v.getTotal()));
            }
        }
        chartVentas.getData().add(series);

        // Tabla de productos en nivel crítico
        if (resumen.getProductosCriticos() != null) {
            tblStockCritico.setItems(FXCollections.observableArrayList(resumen.getProductosCriticos()));
        }

        // Bitácora del patrón Observer
        ObservableList<String> alertas = FXCollections.observableArrayList();
        List<AlertaStockBajo.RegistroAlerta> lista = AlertaStockBajo.getInstancia().getAlertasActivas();
        if (lista.isEmpty()) {
            alertas.add("No hay alertas recientes generadas por el sistema.");
        } else {
            for (AlertaStockBajo.RegistroAlerta reg : lista) {
                alertas.add(reg.getMensaje());
            }
        }
        lstBitacoraObserver.setItems(alertas);
    }
}
