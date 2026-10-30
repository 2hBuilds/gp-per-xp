package com.gpperxp;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** The compact methods file: reading it, and keeping it small. */
public class CompactDataTest
{
	private static final String BASE = "https://oldschool.runescape.wiki/w/";

	private static final String TWO_ROWS = "{\"updated\":\"2026-10-30\",\"base\":\"" + BASE + "\","
		+ "\"pages\":[\"Magic_training\",\"https://example.org/other\"],"
		+ "\"items\":{\"2347\":\"Hammer\",\"440\":\"Iron ore\"},"
		+ "\"methods\":[\n"
		+ "{\"s\":\"Magic\",\"n\":\"Full row\",\"l\":55,\"x\":98.5,\"r\":1500,\"lo\":1000,\"hi\":2000,\"k\":\"about\","
		+ "\"rl\":70,\"u\":\"xp\",\"q\":[{\"s\":\"Magic\",\"l\":90},{\"qu\":\"Dream Mentor\"},{\"d\":\"Ardougne hard\"},"
		+ "{\"t\":\"a staff\"}],\"on\":\"runes\",\"c\":1,\"rec\":1,\"p\":0,\"a\":\"#Iron\",\"nt\":\"a note\"},\n"
		+ "{\"s\":\"Smithing\",\"n\":\"Plain row\",\"l\":15,\"x\":12.5,\"in\":[[2347,1]],\"out\":[[440,2.5]],"
		+ "\"r\":300,\"p\":1}\n]}";

	@Test
	public void everyFieldOfAFullRowIsRead()
	{
		final TrainingMethod m = Methods.load(new Gson(), new StringReader(TWO_ROWS)).all().get(0);
		assertEquals("Magic", m.skill);
		assertEquals("Full row", m.name);
		assertEquals(55, m.level);
		assertEquals(98.5, m.xpPerAction, 0);
		assertEquals(1500, m.actionsPerHour, 0);
		assertEquals(1000, m.rateLow, 0);
		assertEquals(2000, m.rateHigh, 0);
		assertEquals("about", m.rateKind);
		assertEquals(70, m.rateLevel);
		assertEquals("xp", m.rateUnit);
		assertEquals(4, m.requires.size());
		assertEquals("Magic", m.requires.get(0).skill);
		assertEquals(90, m.requires.get(0).level);
		assertEquals("Dream Mentor", m.requires.get(1).quest);
		assertEquals("Ardougne hard", m.requires.get(2).diary);
		assertEquals("a staff", m.requires.get(3).text);
		assertNull(m.requires.get(3).skill);
		assertEquals("runes", m.outputsNote);
		assertTrue(m.costsNotStated);
		assertTrue(m.recommended);
		assertEquals(BASE + "Magic_training#Iron", m.source);
		assertEquals("a note", m.note);
		assertTrue(m.inputs.isEmpty());
		assertTrue(m.outputs.isEmpty());
	}

	@Test
	public void aPlainRowGetsItsItemNamesAndDefaults()
	{
		final Methods methods = Methods.load(new Gson(), new StringReader(TWO_ROWS));
		assertEquals("2026-10-30", methods.updated());
		final TrainingMethod m = methods.all().get(1);
		assertEquals("Smithing", m.skill);
		assertEquals(1, m.inputs.size());
		assertEquals(2347, m.inputs.get(0).itemId);
		assertEquals("Hammer", m.inputs.get(0).item);
		assertEquals(1, m.inputs.get(0).qty, 0);
		assertEquals(440, m.outputs.get(0).itemId);
		assertEquals("Iron ore", m.outputs.get(0).item);
		assertEquals(2.5, m.outputs.get(0).qty, 0);
		assertEquals("https://example.org/other", m.source);
		assertNull(m.note);
		assertNull(m.rateKind);
		assertNull(m.rateUnit);
		assertNull(m.outputsNote);
		assertEquals(0, m.rateLow, 0);
		assertEquals(0, m.rateHigh, 0);
		assertEquals(0, m.rateLevel);
		assertTrue(m.requires.isEmpty());
		assertFalse(m.costsNotStated);
		assertFalse(m.recommended);
	}

	@Test
	public void tablesAndBareIdsAreRead()
	{
		final String json = "{\"base\":\"" + BASE + "\",\"pages\":[\"P\"],\"items\":{\"7\":\"Seven\"},"
			+ "\"skills\":[\"Fletching\"],\"anchors\":[\"#A\"],\"notes\":[\"shared\"],\"outs\":[\"bits\"],"
			+ "\"reqs\":[[{\"qu\":\"Q\"}]],\"methods\":["
			+ "{\"s\":0,\"n\":\"x\",\"in\":[7],\"p\":0,\"a\":0,\"nt\":0,\"on\":0,\"q\":0}]}";
		final TrainingMethod m = Methods.load(new Gson(), new StringReader(json)).all().get(0);
		assertEquals("Fletching", m.skill);
		assertEquals(BASE + "P#A", m.source);
		assertEquals("shared", m.note);
		assertEquals("bits", m.outputsNote);
		assertEquals("Q", m.requires.get(0).quest);
		assertEquals("Seven", m.inputs.get(0).item);
		assertEquals(1, m.inputs.get(0).qty, 0);
	}

	@Test
	public void bundledFileIsUnder450000Bytes() throws IOException
	{
		long size = 0;
		try (InputStream in = CompactDataTest.class.getResourceAsStream("/com/gpperxp/methods.json"))
		{
			final byte[] buffer = new byte[65536];
			for (int n = in.read(buffer); n >= 0; n = in.read(buffer))
			{
				size += n;
			}
		}
		assertTrue("methods.json is " + size + " bytes", size < 450_000);
	}
}
