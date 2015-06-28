package com.github.kaiwinter.csvfx.ui;

import org.junit.Assert;
import org.junit.Test;

import com.github.kaiwinter.csvfx.ui.NullsafeCharacterProperty;
import com.opencsv.CSVParser;

public class NullsafeCharacterPropertyTest {

	@Test
	public void testNull() {
		NullsafeCharacterProperty property = new NullsafeCharacterProperty(null);
		Assert.assertEquals(CSVParser.NULL_CHARACTER, property.getChar());
	}

	@Test
	public void testEmpty() {
		NullsafeCharacterProperty property = new NullsafeCharacterProperty("");
		Assert.assertEquals(CSVParser.NULL_CHARACTER, property.getChar());
	}

	@Test
	public void testTab() {
		NullsafeCharacterProperty property = new NullsafeCharacterProperty("\\t");
		Assert.assertEquals('\t', property.getChar());
	}

	@Test
	public void testOther() {
		NullsafeCharacterProperty property = new NullsafeCharacterProperty("abc");
		Assert.assertEquals('a', property.getChar());
	}
}
