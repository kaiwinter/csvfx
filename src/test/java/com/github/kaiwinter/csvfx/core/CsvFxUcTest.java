package com.github.kaiwinter.csvfx.core;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.kaiwinter.csvfx.core.CsvFileContent;
import com.github.kaiwinter.csvfx.core.CsvFxUc;
import com.github.kaiwinter.csvfx.core.CsvParserConfig;

public final class CsvFxUcTest {

	@Test
	public void testParseHeader() throws UnsupportedEncodingException, IOException, URISyntaxException {
		File selectedFile = getTabSeparatedFile();

		int maxLines = -1;
		CsvFileContent parseFile = new CsvFxUc().readFile(selectedFile, getDefaultFileConfig(), maxLines);
		assertEquals(3, parseFile.header.length);
		assertEquals("Column1", parseFile.header[0]);
		assertEquals("Column2", parseFile.header[1]);
		assertEquals("Column3", parseFile.header[2]);
	}

	@Test
	public void testParseFile() throws UnsupportedEncodingException, IOException, URISyntaxException {
		File selectedFile = getTabSeparatedFile();

		int maxLines = -1;
		CsvFileContent parseFile = new CsvFxUc().readFile(selectedFile, getDefaultFileConfig(), maxLines);
		Assert.assertEquals(3, parseFile.header.length);
		Assert.assertEquals(2, parseFile.rows.size());
	}

	@Test(expected = UnsupportedEncodingException.class)
	public void testParseFileUnsupportedEncoding() throws UnsupportedEncodingException, IOException, URISyntaxException {

		CsvParserConfig defaultFileConfig = getDefaultFileConfig();
		defaultFileConfig.encoding = "unknown";
		File selectedFile = getTabSeparatedFile();

		int maxLines = 1;
		new CsvFxUc().readFile(selectedFile, defaultFileConfig, maxLines);
	}

	@Test
	public void testParseFileWithMaxLines() throws UnsupportedEncodingException, IOException, URISyntaxException {
		File selectedFile = getTabSeparatedFile();

		int maxLines = 1;
		CsvFileContent parseFile = new CsvFxUc().readFile(selectedFile, getDefaultFileConfig(), maxLines);
		Assert.assertEquals(3, parseFile.header.length);
		Assert.assertEquals(1, parseFile.rows.size());
	}

	/**
	 * Parses a tab separated file and writes it as a semicolon separated file and checks the result. The output file also contains a
	 * quoting char.
	 */
	@Test
	public void testEditRecodeFile() throws URISyntaxException, UnsupportedEncodingException, IOException {
		File selectedFile = getTabSeparatedFile();

		int maxLines = -1;
		CsvFxUc csvFxUc = new CsvFxUc();
		CsvFileContent parseFile = csvFxUc.readFile(selectedFile, getDefaultFileConfig(), maxLines);
		File tempFile = File.createTempFile("csvfxtest", null);

		CsvParserConfig defaultFileConfig = getDefaultFileConfig();
		defaultFileConfig.separator = ';';
		defaultFileConfig.quote = '"';
		csvFxUc.editableCopy(tempFile, defaultFileConfig, parseFile);

		File semicolonSeparatedFile = getSemicolonSeparatedFile();
		Assert.assertTrue(FileUtils.contentEqualsIgnoreEOL(tempFile, semicolonSeparatedFile, "utf-8"));

	}

	/**
	 * Parses a tab separated file and writes it as a semicolon separated file and checks the result. The output file also contains a
	 * quoting char.
	 */
	@Test
	public void testStreamRecodeFile() throws URISyntaxException, UnsupportedEncodingException, IOException {
		File selectedFile = getTabSeparatedFile();

		CsvFxUc csvFxUc = new CsvFxUc();
		File tempFile = File.createTempFile("csvfxtest", null);

		CsvParserConfig outputConfig = getDefaultFileConfig();
		outputConfig.separator = ';';
		outputConfig.quote = '"';
		csvFxUc.streamCopy(selectedFile, tempFile, getDefaultFileConfig(), outputConfig);

		File semicolonSeparatedFile = getSemicolonSeparatedFile();
		Assert.assertTrue(FileUtils.contentEqualsIgnoreEOL(tempFile, semicolonSeparatedFile, "utf-8"));

	}

	private File getTabSeparatedFile() throws URISyntaxException {
		return new File(CsvFxUcTest.class.getResource("tab_separated.csv").toURI());
	}

	private File getSemicolonSeparatedFile() throws URISyntaxException {
		return new File(CsvFxUcTest.class.getResource("semicolon_separated_quoted.csv").toURI());
	}

	private CsvParserConfig getDefaultFileConfig() {
		CsvParserConfig fileConfig = new CsvParserConfig();
		fileConfig.encoding = "UTF-8";
		fileConfig.separator = '\t';

		return fileConfig;
	}
}
