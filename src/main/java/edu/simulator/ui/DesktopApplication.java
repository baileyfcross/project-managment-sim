package edu.simulator.ui;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;

public class DesktopApplication extends Application {
    @Override
    public void start(Stage stage) {
        JavaBridge bridge = new JavaBridge();
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", bridge);
                webEngine.executeScript("window.dispatchEvent(new Event('javaBridgeReady'))");
            }
        });

        URL resource = getClass().getResource("/web/index.html");
        if (resource == null) {
            resource = getClass().getResource("/index.html");
        }
        if (resource != null) {
            webEngine.load(resource.toExternalForm());
        } else {
            String fallback = "<html><body><h1>Software Project Management Simulator</h1><p>Frontend assets are being generated.</p></body></html>";
            webEngine.loadContent(fallback);
        }

        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Software Project Management Simulator");
        stage.show();
    }

}
