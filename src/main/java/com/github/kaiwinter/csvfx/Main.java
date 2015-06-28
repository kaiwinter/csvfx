package com.github.kaiwinter.csvfx;

import java.io.IOException;

import com.github.kaiwinter.csvfx.ui.CsvFxController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		AnchorPane page = (AnchorPane) loader.load(getClass().getResourceAsStream("ui/CsvFx.fxml"));
		Scene scene = new Scene(page);
		CsvFxController controller = loader.getController();
		controller.setMainPresenter(primaryStage);

		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setTitle("CSV Converter");
	}
}
