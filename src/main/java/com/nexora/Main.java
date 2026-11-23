package com.nexora;

import javafx.application.Application;

/**
 * Punto de entrada principal de la aplicación NEXORA.
 * El lanzador separado garantiza compatibilidad total con el ClassLoader de JavaFX.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
