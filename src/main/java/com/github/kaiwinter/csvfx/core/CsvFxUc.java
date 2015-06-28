package com.github.kaiwinter.csvfx.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CsvFxUc {
	public CsvFileContent readFile(File selectedFile, CsvParserConfig inputFileConfig, int maxLines)
			throws IOException, UnsupportedEncodingException {

		CsvFileContent csvFileContent = new CsvFileContent();

		try (InputStream is = new FileInputStream(selectedFile);
				Reader reader = new InputStreamReader(is, inputFileConfig.encoding);
				CSVReader csvreader = new CSVReader(reader, inputFileConfig.separator, inputFileConfig.quote,
						inputFileConfig.escape);) {

			// read header, create columns
			String[] readNext = csvreader.readNext();
			if (readNext == null) {
				return null;
			}
			csvFileContent.header = readNext;

			// add data rows
			csvFileContent.rows = new ArrayList<>();
			int counter = 0;
			boolean limitLines = maxLines > 0;
			while ((readNext = csvreader.readNext()) != null //
					&& (!limitLines || (limitLines && counter++ < maxLines))) {
				csvFileContent.rows.add(readNext);
			}

			return csvFileContent;
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
	}

	/**
	 * Loads the input file and writes it to the output file.
	 * 
	 * @throws IOException
	 */
	public void streamCopy(File inputFile, File outputFile, CsvParserConfig inputFileConfig,
			CsvParserConfig outputFileConfig) throws IOException, UnsupportedEncodingException {

		// Stream file from input to output
		try (InputStream is = new FileInputStream(inputFile);
				Reader reader = new InputStreamReader(is, inputFileConfig.encoding);
				CSVReader csvreader = new CSVReader(reader, inputFileConfig.separator, inputFileConfig.quote,
						inputFileConfig.escape);

				OutputStream os = new FileOutputStream(outputFile);
				Writer writer = new OutputStreamWriter(os, outputFileConfig.encoding);
				CSVWriter csvwriter = new CSVWriter(writer, outputFileConfig.separator, outputFileConfig.quote,
						outputFileConfig.escape);) {

			for (String[] readNext; (readNext = csvreader.readNext()) != null;) {
				csvwriter.writeNext(readNext);
			}
		}
	}

	/**
	 * Writes the data from the table to the output file.
	 */
	public void editableCopy(File outputFile, CsvParserConfig outputFileConfig, CsvFileContent csvFileContent)
			throws IOException, UnsupportedEncodingException {
		try (OutputStream os = new FileOutputStream(outputFile);
				Writer writer = new OutputStreamWriter(os, outputFileConfig.encoding);
				CSVWriter csvwriter = new CSVWriter(writer, outputFileConfig.separator, outputFileConfig.quote,
						outputFileConfig.escape);) {

			csvwriter.writeNext(csvFileContent.header);

			for (String[] item : csvFileContent.rows) {
				csvwriter.writeNext(item);
			}
		}
	}

}
