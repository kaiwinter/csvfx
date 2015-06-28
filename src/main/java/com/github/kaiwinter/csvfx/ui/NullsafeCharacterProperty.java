package com.github.kaiwinter.csvfx.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

import com.opencsv.CSVParser;

public final class NullsafeCharacterProperty extends SimpleStringProperty {

	private static final String TAB_STRING = "\\t";
	private static final char TAB_CHAR = '\t';

	public NullsafeCharacterProperty(String string) {
		super(string);
	}

	public NullsafeCharacterProperty(String string, ChangeListener<Object> inputParameterChangedListener) {
		this(string);
		addListener(inputParameterChangedListener);
	}

	public char getChar() {
		String superString = super.get();
		if (superString == null || superString.isEmpty()) {
			return CSVParser.NULL_CHARACTER;
		}

		if (TAB_STRING.equals(superString)) {
			return TAB_CHAR;
		}

		return superString.charAt(0);
	}
}
