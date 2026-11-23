package com.nexora.service;

import com.nexora.model.Producto;
import com.nexora.model.Venta;
import com.nexora.repository.ProductoRepository;
import com.nexora.repository.VentaRepository;
import com.nexora.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para el Dashboard y Reportes Financieros y de Inventario.
 * Provee datos analíticos calculados mediante agregaciones SQL.
 */
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public ReporteService() {
        this(new VentaRepository(), new ProductoRepository());
    }

    public ReporteService(VentaRepository ventaRepo, ProductoRepository prodRepo) {
        this.ventaRepository = ventaRepo;
        this.productoRepository = prodRepo;
    }

    // DTOs para estructurar las respuestas de reportes y dashboard
    public static class VentaDiaDTO {
        private final String fecha;
        private final double total;

        public VentaDiaDTO(String fecha, double total) {
            this.fecha = fecha;
            this.total = total;
        }

        public String getFecha() { return fecha; }
        public double getTotal() { return total; }
    }

    public static class ProductoRankingDTO {
        private final String codigo;
        private final String nombre;
        private final int cantidadVendida;
        private final double totalRecaudado;

        public ProductoRankingDTO(String codigo, String nombre, int cantidadVendida, double totalRecaudado) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.cantidadVendida = cantidadVendida;
            this.totalRecaudado = totalRecaudado;
        }

        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public int getCantidadVendida() { return cantidadVendida; }
        public double getTotalRecaudado() { return totalRecaudado; }
    }

    public static class DashboardResumenDTO {
        private double totalVentasHoy;
        private double totalVentasMes;
        private int totalProductosActivos;
        private int alertasStockBajo;
        private List<Producto> productosCriticos;
        private List<VentaDiaDTO> ventasDias;

        // Getters y Setters
        public double getTotalVentasHoy() { return totalVentasHoy; }
        public void setTotalVentasHoy(double totalVentasHoy) { this.totalVentasHoy = totalVentasHoy; }

        public double getTotalVentasMes() { return totalVentasMes; }
        public void setTotalVentasMes(double totalVentasMes) { this.totalVentasMes = totalVentasMes; }

        public int getTotalProductosActivos() { return totalProductosActivos; }
        public void setTotalProductosActivos(int totalProductosActivos) { this.totalProductosActivos = totalProductosActivos; }

        public int getAlertasStockBajo() { return alertasStockBajo; }
        public void setAlertasStockBajo(int alertasStockBajo) { this.alertasStockBajo = alertasStockBajo; }

        public List<Producto> getProductosCriticos() { return productosCriticos; }
        public void setProductosCriticos(List<Producto> productosCriticos) { this.productosCriticos = productosCriticos; }

        public List<VentaDiaDTO> getVentasDias() { return ventasDias; }
        public void setVentasDias(List<VentaDiaDTO> ventasDias) { this.ventasDias = ventasDias; }
    }

    public static class ReporteFinancieroDTO {
        private double ingresosTotales;
        private double costoTotalEstimado;
        private double utilidadBruta;
        private double margenPorcentaje;
        private List<ProductoRankingDTO> topProductos;
        private List<Venta> listaVentas;

        public double getIngresosTotales() { return ingresosTotales; }
        public void setIngresosTotales(double ingresosTotales) { this.ingresosTotales = ingresosTotales; }

        public double getCostoTotalEstimado() { return costoTotalEstimado; }
        public void setCostoTotalEstimado(double costoTotalEstimado) { this.costoTotalEstimado = costoTotalEstimado; }

        public double getUtilidadBruta() { return utilidadBruta; }
        public void setUtilidadBruta(double utilidadBruta) { this.utilidadBruta = utilidadBruta; }

        public double getMargenPorcentaje() { return margenPorcentaje; }
        public void setMargenPorcentaje(double margenPorcentaje) { this.margenPorcentaje = margenPorcentaje; }

        public List<ProductoRankingDTO> getTopProductos() { return topProductos; }
        public void setTopProductos(List<ProductoRankingDTO> topProductos) { this.topProductos = topProductos; }

        public List<Venta> getListaVentas() { return listaVentas; }
        public void setListaVentas(List<Venta> listaVentas) { this.listaVentas = listaVentas; }
    }

    /**
     * Obtiene el resumen general para la pantalla del Dashboard.
     */
    public DashboardResumenDTO obtenerResumenDashboard() {
        DashboardResumenDTO dto = new DashboardResumenDTO();
        String hoyStr = LocalDate.now().toString(); // YYYY-MM-DD
        String mesStr = hoyStr.substring(0, 7);     // YYYY-MM

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ventas de hoy
            String sqlHoy = "SELECT COALESCE(SUM(total), 0) FROM venta WHERE fecha LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlHoy)) {
                ps.setString(1, hoyStr + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) dto.setTotalVentasHoy(rs.getDouble(1));
                }
            }

            // Ventas del mes
            String sqlMes = "SELECT COALESCE(SUM(total), 0) FROM venta WHERE fecha LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlMes)) {
                ps.setString(1, mesStr + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) dto.setTotalVentasMes(rs.getDouble(1));
                }
            }

            // Ventas agrupadas por los últimos 7 días
            List<VentaDiaDTO> dias = new ArrayList<>();
            String sqlDias = "SELECT SUBSTR(fecha, 1, 10) as dia, SUM(total) as diario " +
                    "FROM venta GROUP BY SUBSTR(fecha, 1, 10) ORDER BY dia DESC LIMIT 7";
            try (PreparedStatement ps = conn.prepareStatement(sqlDias);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dias.add(new VentaDiaDTO(rs.getString("dia"), rs.getDouble("diario")));
                }
            }
            dto.setVentasDias(dias);

            // Productos activos y bajo stock
            List<Producto> productosActivos = productoRepository.listarActivos();
            dto.setTotalProductosActivos(productosActivos.size());

            List<Producto> bajoStock = productoRepository.listarBajoStock();
            dto.setAlertasStockBajo(bajoStock.size());
            dto.setProductosCriticos(bajoStock);

        } catch (Exception e) {
            System.err.println("Error al calcular resumen de dashboard: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Genera el reporte financiero detallado por rango de fechas (o histórico).
     */
    public ReporteFinancieroDTO generarReporteFinanciero(LocalDate fechaInicio, LocalDate fechaFin) {
        ReporteFinancieroDTO dto = new ReporteFinancieroDTO();
        String inicioStr = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 00:00:00";
        String finStr = fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 23:59:59";

        List<Venta> ventas = ventaRepository.listarPorRangoFechas(inicioStr, finStr);
        dto.setListaVentas(ventas);

        double totalIngresos = 0.0;
        for (Venta v : ventas) {
            totalIngresos += v.getTotal();
        }
        dto.setIngresosTotales(Math.round(totalIngresos * 100.0) / 100.0);

        // Costo estimado de ventas y productos más vendidos en el periodo
        String sqlTop = "SELECT p.codigo, p.nombre, SUM(dv.cantidad) as total_vendido, " +
                "SUM(dv.subtotal) as total_recaudado, " +
                "SUM(dv.cantidad * p.precio_compra) as total_costo " +
                "FROM detalle_venta dv " +
                "JOIN venta v ON dv.id_venta = v.id " +
                "JOIN producto p ON dv.id_producto = p.id " +
                "WHERE v.fecha >= ? AND v.fecha <= ? " +
                "GROUP BY p.id ORDER BY total_vendido DESC";

        List<ProductoRankingDTO> topList = new ArrayList<>();
        double costoTotal = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlTop)) {
            ps.setString(1, inicioStr);
            ps.setString(2, finStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topList.add(new ProductoRankingDTO(
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getInt("total_vendido"),
                            rs.getDouble("total_recaudado")
                    ));
                    costoTotal += rs.getDouble("total_costo");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al calcular top productos y costo de venta: " + e.getMessage());
        }

        dto.setTopProductos(topList);
        dto.setCostoTotalEstimado(Math.round(costoTotal * 100.0) / 100.0);

        double utilidad = Math.round((totalIngresos - costoTotal) * 100.0) / 100.0;
        dto.setUtilidadBruta(utilidad);

        double margen = (totalIngresos > 0) ? (utilidad / totalIngresos) * 100.0 : 0.0;
        dto.setMargenPorcentaje(Math.round(margen * 10.0) / 10.0);

        return dto;
    }
}
