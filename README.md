# NEXORA — Sistema Inteligente de Gestión de Inventario y Ventas

> **Universidad Nacional de San Agustín (UNSA)**  
> **Facultad de Ingeniería de Producción y Servicios**  
> **Escuela Profesional de Ingeniería de Sistemas**  
> **Curso:** 26B Sistemas: Tecnología de Objetos GB  
> **Docente:** Roni Guillermo Apaza Aceituno | **Grupo:** B  
> **Integrantes:**  
> - Quispe Rupaylla Fabrizio Alonso (CUI: 20242337)  
> - Quispesayhua Hancco Joseph Brayan (CUI: 20242336)  
> - Chirinos Rojas Kennedy (CUI: 20243387)  
> - Quispe Puma Usiel Suriel (CUI: 20243392)  

---

## 📌 Descripción del Proyecto

**NEXORA** es una aplicación de escritorio diseñada para pequeños y medianos comercios (minimarkets, bodegas, ferreterías, boticas), orientada a resolver las tres problemáticas críticas de gestión comercial:
1. **Stock inexacto**: Control de existencias en tiempo real y transacciones ACID.
2. **Quiebres de inventario**: Sistema reactivo de alertas automáticas cuando el stock llega o cae por debajo del stock mínimo.
3. **Decisiones sin datos**: Dashboard visual con indicadores clave de rendimiento (KPIs), gráficas y reportes de rentabilidad y utilidad bruta en segundo plano.

---

## 🏛️ Demostración Práctica de Tecnología de Objetos

Cada concepto fundamental y avanzado de la programación orientada a objetos se encuentra implementado en el núcleo de la aplicación y respaldado con pruebas unitarias en **JUnit 5**:

| Concepto de POO / GoF | Implementación en NEXORA | Evidencia y Verificación |
| :--- | :--- | :--- |
| **Encapsulación** | Atributos privados en `Producto`; el stock solo cambia mediante `descontarStock()` y `agregarStock()`, validando cantidades positivas y disponibilidad. | `EncapsulacionYPolimorfismoTest.java` lanza `StockInsuficienteException` al descontar más del stock existente. |
| **Herencia y Polimorfismo** | Clase abstracta `Producto` especializada en `ProductoUnitario` y `ProductoPerecible` (con fecha de caducidad). Cada una implementa `calcularPrecio()` con lógica polimórfica (descuento automático en productos próximos a vencer). | `EncapsulacionYPolimorfismoTest.java` comprueba resultados diferenciados invocando el mismo método `calcularPrecio()`. |
| **Abstracción** | Interfaces `DescuentoStrategy`, `StockObserver`, `Repositorio<T, ID>`. Los servicios interactúan con contratos abstractos. | Servicios desacoplados de detalles de implementación de base de datos o de UI. |
| **Factory Method** | `ProductoFactory` crea la subclase adecuada (`ProductoUnitario` o `ProductoPerecible`) a partir del tipo leído de SQLite o del formulario. | `ProductoFactoryTest.java` verifica la instanciación según el tipo. |
| **Strategy** | Algoritmos de descuento intercambiables (`SinDescuento`, `DescuentoPorcentaje`, `DescuentoPorVolumen`). El POS conmuta la regla en tiempo de ejecución. | `DescuentoStrategyTest.java` valida cada fórmula de cálculo de descuento. |
| **Observer** | `StockSubject` y `AlertaStockBajo` monitorean cuando `stock <= stockMinimo` emitiendo notificaciones inmediatas a la bitácora y al dashboard. | `StockObserverTest.java` verifica el disparo del evento al cruzar el umbral. |
| **Programación Genérica** | `Repositorio<T, ID>` provee operaciones CRUD parametrizadas reutilizadas por todas las entidades. | `RepositorioGenericoTest.java` demuestra reutilización idéntica con `Categoria` y `Proveedor`. |
| **Multithreading** | Generación de reportes analíticos pesados en segundo plano con `Task<T>` y `ExecutorService`, refrescando la UI con `Platform.runLater()`. | `ReporteController.java` mantiene la interfaz 100% fluida mientras calcula costos y utilidades. |
| **Transacciones ACID** | `connection.setAutoCommit(false)`, `commit()` y `rollback()` para garantizar que ventas y compras se guarden completas o no se guarde nada. | `TransaccionVentaTest.java` comprueba reversión atómica si algún ítem falla. |

---

## 🛠️ Tecnologías Empleadas

- **Lenguaje:** Java 21 LTS (OpenJDK / Eclipse Adoptium Temurin-21)
- **GUI:** JavaFX 21 (Controls, FXML) con hoja de estilos CSS moderna (`styles.css`)
- **Base de Datos:** SQLite 3 vía JDBC (`sqlite-jdbc`)
- **Testing:** JUnit 5 Jupiter
- **Build Tool:** Apache Maven 3.9 + Maven Wrapper (`mvnw.cmd`)

---

## 🚀 Instrucciones de Ejecución

### 1. Requisitos Previos
Tener instalado Java 21 (JDK 21 LTS).

### 2. Ejecutar las Pruebas Unitarias Automatizadas
Para correr toda la suite de pruebas unitarias que verifican los patrones y reglas de negocio:
```powershell
.\mvnw.cmd test
```

### 3. Ejecutar la Aplicación NEXORA
Para compilar y abrir la aplicación de escritorio con interfaz gráfica:
```powershell
.\mvnw.cmd javafx:run
```

*(La base de datos SQLite `nexora.db` se crea y puebla automáticamente en el primer arranque con datos realistas de catálogo, categorías, proveedores y ventas iniciales).*

---

## 📂 Módulos del Sistema

1. **📊 Dashboard**: Indicadores en tiempo real (Ventas del día, Ventas del mes, Total de productos, Alertas activas), gráfico de barras de ventas recientes, tabla de productos críticos y bitácora del patrón Observer.
2. **🛒 Punto de Venta (POS)**: Buscador instantáneo, carrito dinámico, selector interactivo de estrategia de descuento (Patrón Strategy), validación de existencias y cobro atómico.
3. **📦 Catálogo de Productos**: Mantenimiento completo (CRUD), asignación de categoría, selección de tipo unitario/perecible con selector de fecha de vencimiento y desactivación lógica.
4. **🏷️ Categorías**: Organización del catálogo y protección referencial (RF-02: impide eliminar categorías con productos asociados).
5. **📥 Compras y Reabastecimiento**: Registro de compras a proveedores que incrementan stock automáticamente y calculan costos de inversión.
6. **🏢 Proveedores**: Directorio con RUC, teléfono, contacto y razón social.
7. **📋 Kardex de Inventario**: Trazabilidad completa de entradas, salidas y ajustes manuales por merma o cuadre.
8. **📈 Reportes Financieros (Multithreading)**: Filtro por rango de fechas, cálculo asíncrono de ingresos totales, costo de mercadería vendida, utilidad bruta y porcentaje de margen de ganancia.
