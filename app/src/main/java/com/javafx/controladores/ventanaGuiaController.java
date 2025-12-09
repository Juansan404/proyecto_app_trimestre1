package com.javafx.controladores;

import com.dlsc.pdfviewfx.PDFView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class ventanaGuiaController {

    @FXML
    private StackPane pdfContainer;

    @FXML
    public void initialize() {
        try {
            PDFView pdfView = new PDFView();
            pdfView.load(getClass().getResourceAsStream("/guia.pdf"));
            pdfContainer.getChildren().add(pdfView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
