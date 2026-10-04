package edu.simulator;

import edu.simulator.ui.DesktopApplication;
import javafx.application.Application;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Application.launch(DesktopApplication.class, args);
    }
}