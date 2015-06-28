package com.github.kaiwinter.csvfx.ui;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import com.github.kaiwinter.csvfx.core.CsvFileContent;
import com.github.kaiwinter.csvfx.core.CsvFxUc;
import com.github.kaiwinter.csvfx.core.CsvParserConfig;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CsvFxController implements Initializable {

	private static final Integer INITIAL_LIMIT = 10;
	private static final Effect INVALID_EFFECT = new DropShadow(BlurType.GAUSSIAN, Color.RED, 5, 0.8, 0, 0);
	private static final Tooltip INVALID_LIMIT_TOOLTIP = new Tooltip("Invalid - no limit will be applied");

	@FXML
	private TextField inputFilepath;
	@FXML
	private TextField outputFilepath;

	@FXML
	private ComboBox<String> inputSeparator;
	@FXML
	private ComboBox<String> inputQuote;
	@FXML
	private ComboBox<String> inputEscape;
	@FXML
	private ComboBox<String> inputEncoding;

	@FXML
	private ComboBox<String> outputSeparator;
	@FXML
	private ComboBox<String> outputQuote;
	@FXML
	private ComboBox<String> outputEscape;
	@FXML
	private ComboBox<String> outputEncoding;

	@FXML
	private TableView<String[]> fileContent;

	@FXML
	private ToggleGroup copyMode;

	@FXML
	private Toggle editableCopyMode;
	@FXML
	private Toggle streamCopyMode;

	@FXML
	private Button saveButton;

	@FXML
	private CheckBox limitPreviewBox;

	@FXML
	private TextField limitPreviewText;

	private Stage mainPresenter;

	private NullsafeCharacterProperty inputSeparatorChar;
	private NullsafeCharacterProperty inputQuoteChar;
	private NullsafeCharacterProperty inputEscapeChar;
	private NullsafeCharacterProperty inputEncodingString;

	private NullsafeCharacterProperty outputSeparatorChar;
	private NullsafeCharacterProperty outputQuoteChar;
	private NullsafeCharacterProperty outputEscapeChar;
	private NullsafeCharacterProperty outputEncodingString;

	private SimpleBooleanProperty limitPreviewBoxBoolean;

	private File inputFile;
	private File outputFile;

	private Integer limitForPreview = INITIAL_LIMIT;

	private CsvFxUc uc;

	@FXML
	public void openFileAction(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open CSV File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv", "*.txt"),
				new ExtensionFilter("All Files", "*.*"));

		// if dialog gets canceled use currently opened file
		File newFile = fileChooser.showOpenDialog(mainPresenter);
		if (newFile != null) {
			inputFile = newFile;
			String path = inputFile.getAbsolutePath();
			inputFilepath.setText(path);
			int lastIndexOf = path.lastIndexOf('.');
			String tempFileName = path.substring(0, lastIndexOf) + "-converted" + path.substring(lastIndexOf);
			outputFilepath.setText(tempFileName);
			outputFile = new File(tempFileName);
			boolean success = readFile();
			if (success) {
				saveButton.setDisable(false);
			}
		}
	}

	private boolean readFile() {
		fileContent.setItems(FXCollections.<String[]> observableArrayList());
		fileContent.getColumns().clear();

		int maxLines = -1;
		if (!isEditableCopyMode() && limitPreviewBoxBoolean.get()) {
			maxLines = limitForPreview;
		}
		CsvParserConfig inputFileConfig = getInputFileConfig();
		CsvFileContent parseFile;
		try {
			parseFile = uc.readFile(inputFile, inputFileConfig, maxLines);
		} catch (UnsupportedEncodingException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error reading file");
			alert.setContentText("Unsupported encoding: " + e.getMessage());
			alert.showAndWait();

			return false;
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error parsing file");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
			return false;
		}

		// Create columns
		final Map<TableColumn<String[], String>, Integer> tableColumnToArrayIndex = new HashMap<>();
		int i = 0;
		// Create columns/headers
		for (String string : parseFile.header) {
			TableColumn<String[], String> tc = new TableColumn<>(string);
			tc.setSortable(false);
			tc.setCellFactory(TextFieldTableCell.<String[]> forTableColumn());
			tc.setOnEditCommit(new EventHandler<CellEditEvent<String[], String>>() {

				@Override
				public void handle(CellEditEvent<String[], String> t) {
					int column = t.getTablePosition().getColumn();
					t.getRowValue()[column] = t.getNewValue();
				}

			});

			tableColumnToArrayIndex.put(tc, i);
			i++;

			Callback<CellDataFeatures<String[], String>, ObservableValue<String>> value = new PropertyValueFactory<String[], String>(
					string) {
				@Override
				public ObservableValue<String> call(CellDataFeatures<String[], String> param) {
					TableColumn<String[], String> tableColumn = param.getTableColumn();
					Integer integer = tableColumnToArrayIndex.get(tableColumn);
					String[] value = param.getValue();
					if (integer >= value.length) {
						return null;
					}
					return new NullsafeCharacterProperty(value[integer]);
				}
			};
			tc.setCellValueFactory(value);
			fileContent.getColumns().add(tc);
		}

		// Add data
		fileContent.getItems().addAll(parseFile.rows);
		fileContent.setEditable(isEditableCopyMode());

		return true;
	}

	private CsvParserConfig getInputFileConfig() {
		CsvParserConfig inputFileConfig = new CsvParserConfig();
		inputFileConfig.separator = inputSeparatorChar.getChar();
		inputFileConfig.quote = inputQuoteChar.getChar();
		inputFileConfig.escape = inputEscapeChar.getChar();
		inputFileConfig.encoding = inputEncodingString.get();
		return inputFileConfig;
	}

	private CsvParserConfig getOutputFileConfig() {
		CsvParserConfig outputFileConfig = new CsvParserConfig();
		outputFileConfig.separator = outputSeparatorChar.getChar();
		outputFileConfig.quote = outputQuoteChar.getChar();
		outputFileConfig.escape = outputEscapeChar.getChar();
		outputFileConfig.encoding = outputEncodingString.get();
		return outputFileConfig;
	}

	@FXML
	public void modeToggled(ActionEvent event) {
		boolean editableCopyMode = isEditableCopyMode();

		limitPreviewBox.setDisable(editableCopyMode);
		limitPreviewText.setDisable(editableCopyMode);
		fileContent.setEditable(editableCopyMode);

		// refresh view, force changed event
		inputParameterChangedListener.changed(null, false, true);
	}

	@FXML
	public void saveFileAction(ActionEvent event) {
		assert outputFile != null;

		// Check if output file already exists
		if (outputFile.exists()) {
			String message = "The file '" + outputFile.getName() + "' already exists, overwrite?";
			String title = "Overwrite File?";

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(title);
			alert.setContentText(message);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() != ButtonType.OK) {
				return;
			}
		}

		try {
			if (isEditableCopyMode()) {
				String[] header = getTableHeader();
				CsvFileContent csvFileContent = new CsvFileContent();
				csvFileContent.header = getTableHeader();
				csvFileContent.rows = fileContent.getItems();
				uc.editableCopy(outputFile, getOutputFileConfig(), csvFileContent);
			} else {
				uc.streamCopy(inputFile, outputFile, getInputFileConfig(), getOutputFileConfig());
			}

		} catch (UnsupportedEncodingException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error saving file");
			alert.setContentText("Unsupported encoding: " + e.getMessage());
			alert.showAndWait();

		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error saving file");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
		}
	}

	private String[] getTableHeader() {
		ObservableList<TableColumn<String[], ?>> columns = fileContent.getColumns();
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			String headerText = columns.get(i).getText();
			header[i] = headerText;
		}
		return header;
	}

	public void setMainPresenter(Stage primaryStage) {
		this.mainPresenter = primaryStage;
	}

	/**
	 * Re-reads the file after the configuration was changed
	 */
	private ChangeListener<Object> inputParameterChangedListener = new ChangeListener<Object>() {

		@Override
		public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
			if (oldValue.equals(newValue)) {
				// Value wasn't changed
				return;
			} else if (inputFile == null) {
				// No file loaded so don't refresh table
				return;
			}

			readFile();
		}
	};

	@Override
	public void initialize(URL url, ResourceBundle bundle) {
		// hack for forbid reordering
		fileContent.getColumns().addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(Change<?> change) {
				change.next();
				if (change.wasReplaced()) {
					@SuppressWarnings("unchecked")
					List<TableColumn<String[], String>> removed = (List<TableColumn<String[], String>>) change
							.getRemoved();
					fileContent.getColumns().clear();
					fileContent.getColumns().addAll(removed);
				}
			}
		});

		limitPreviewText.setText(String.valueOf(INITIAL_LIMIT));

		ObservableList<String> separator = FXCollections.observableArrayList(",", ";", "\\t");
		inputSeparator.setItems(separator);
		outputSeparator.setItems(separator);

		ObservableList<String> quote = FXCollections.observableArrayList("", "'", "\"");
		inputQuote.setItems(quote);
		outputQuote.setItems(quote);

		ObservableList<String> escape = FXCollections.observableArrayList("", "\\");
		inputEscape.setItems(escape);
		outputEscape.setItems(escape);

		ObservableList<String> encoding = FXCollections.observableArrayList("CP1252", "UTF-8");
		inputEncoding.setItems(encoding);
		outputEncoding.setItems(encoding);

		// Bindings for input combo boxes
		inputSeparatorChar = new NullsafeCharacterProperty(",", inputParameterChangedListener);
		inputSeparator.valueProperty().bindBidirectional(inputSeparatorChar);

		inputQuoteChar = new NullsafeCharacterProperty("", inputParameterChangedListener);
		inputQuote.valueProperty().bindBidirectional(inputQuoteChar);

		inputEscapeChar = new NullsafeCharacterProperty("", inputParameterChangedListener);
		inputEscape.valueProperty().bindBidirectional(inputEscapeChar);

		inputEncodingString = new NullsafeCharacterProperty("CP1252", inputParameterChangedListener);
		inputEncoding.valueProperty().bindBidirectional(inputEncodingString);

		limitPreviewBoxBoolean = new SimpleBooleanProperty(true);
		limitPreviewBox.selectedProperty().bindBidirectional(limitPreviewBoxBoolean);
		limitPreviewBoxBoolean.addListener(inputParameterChangedListener);

		// Bindings for output combo boxes
		outputSeparatorChar = new NullsafeCharacterProperty(";");
		outputSeparator.valueProperty().bindBidirectional(outputSeparatorChar);

		outputQuoteChar = new NullsafeCharacterProperty("\"");
		outputQuote.valueProperty().bindBidirectional(outputQuoteChar);

		outputEscapeChar = new NullsafeCharacterProperty("\\");
		outputEscape.valueProperty().bindBidirectional(outputEscapeChar);

		outputEncodingString = new NullsafeCharacterProperty("UTF-8");
		outputEncoding.valueProperty().bindBidirectional(outputEncodingString);

		uc = new CsvFxUc();
	}

	@FXML
	public void limitPreviewBoxToggled() {
		boolean selected = limitPreviewBox.isSelected();
		limitPreviewText.setEditable(selected);
		limitPreviewText.setDisable(!selected);
	}

	@FXML
	public void limitPreviewTextEntered(KeyEvent event) {
		// On enter refresh table
		if (KeyCode.ENTER.equals(event.getCode())) {
			// force changed event
			inputParameterChangedListener.changed(null, false, true);
			return;
		}

		// validate
		String text = limitPreviewText.getText();
		try {
			limitForPreview = Integer.valueOf(text);
			limitPreviewText.setEffect(null);
			limitPreviewText.setTooltip(null);
			INVALID_LIMIT_TOOLTIP.hide();
		} catch (NumberFormatException e) {
			limitForPreview = -1;
			limitPreviewText.setEffect(INVALID_EFFECT);

			if (!INVALID_LIMIT_TOOLTIP.isShowing()) {
				limitPreviewText.setTooltip(INVALID_LIMIT_TOOLTIP);
				INVALID_LIMIT_TOOLTIP.setAutoHide(true);
				INVALID_LIMIT_TOOLTIP.show(mainPresenter.getScene().getWindow(),
						limitPreviewText.getBoundsInParent().getMaxX() + mainPresenter.getX() + 10,
						limitPreviewText.getBoundsInParent().getMaxY() + mainPresenter.getY() - 10);
			}
		}
	}

	private boolean isEditableCopyMode() {
		CopyMode copyMode = getCopyMode();
		boolean editableCopyMode = copyMode == CopyMode.EditableCopy;

		return editableCopyMode;
	}

	private CopyMode getCopyMode() {
		if (copyMode.getSelectedToggle() == streamCopyMode) {
			return CopyMode.StreamCopy;
		}
		return CopyMode.EditableCopy;
	}

	enum CopyMode {
		/**
		 * 
		 */
		StreamCopy,

		/**
		 * 
		 */
		EditableCopy
	}

}
