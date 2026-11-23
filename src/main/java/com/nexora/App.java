package com.nexora;

import com.nexora.util.DatabaseConnection;
import com.nexora.util.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inicializar base de datos SQLite y seed inicial
            DatabaseInitializer.initialize();

            // Cargar layout principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main_layout.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("NEXORA - Sistema Inteligente de Gestión de Inventario y Ventas (UNSA)");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);

            primaryStage.setOnCloseRequest(e -> {
                DatabaseConnection.closeConnection();
            });

            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación NEXORA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
