package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** The rate unit and the "at least" shape as {@link Methods} reads them from the data file. */
public class MethodsXpUnitTest
{
	private static final String XP_ROW = "{\"updated\":\"2026-10-30\",\"methods\":[{"
		+ "\"skill\":\"Test\",\"name\":\"Hourly row\",\"level\":5,\"xpPerAction\":1,"
		+ "\"inputs\":[],\"outputs\":[],\"actionsPerHour\":0,\"source\":\"https://oldschool.runescape.wiki/x\","
		+ "\"note\":null,\"rateLow\":45000,\"rateHigh\":55000,\"rateUnit\":\"xp\"}]}";

	private static final String AT_LEAST_ROW = "{\"updated\":\"2026-10-30\",\"methods\":[{"
		+ "\"skill\":\"Test\",\"name\":\"Floor row\",\"level\":5,\"xpPerAction\":10.0,"
		+ "\"inputs\":[],\"outputs\":[],\"actionsPerHour\":0,\"source\":\"https://oldschool.runescape.wiki/x\","
		+ "\"note\":null,\"rateLow\":200000}]}";

	private static final String V1_ROW = "{\"updated\":\"2026-10-30\",\"methods\":[{"
		+ "\"skill\":\"Test\",\"name\":\"Old row\",\"level\":5,\"xpPerAction\":10.0,"
		+ "\"inputs\":[],\"outputs\":[],\"actionsPerHour\":1000,\"source\":\"https://oldschool.runescape.wiki/x\","
		+ "\"note\":null}]}";

	private static TrainingMethod load(String json)
	{
		return Methods.load(new Gson(), new StringReader(json)).all().get(0);
	}

	@Test
	public void xpUnitRangeRowIsRead()
	{
		final TrainingMethod m = load(XP_ROW);
		assertEquals("xp", m.rateUnit);
		assertEquals(1.0, m.xpPerAction, 1e-9);
		assertEquals(45000.0, m.rateLow, 1e-9);
		assertEquals(55000.0, m.rateHigh, 1e-9);
		assertEquals(RateKind.RANGE, Costs.kind(m));
		assertTrue(Costs.perXp(m));
	}

	@Test
	public void atLeastRowIsRead()
	{
		final TrainingMethod m = load(AT_LEAST_ROW);
		assertEquals(200000.0, m.rateLow, 1e-9);
		assertEquals(0.0, m.rateHigh, 1e-9);
		assertEquals(RateKind.AT_LEAST, Costs.kind(m));
	}

	@Test
	public void v1RowHasNoRateUnit()
	{
		final TrainingMethod m = load(V1_ROW);
		assertNull(m.rateUnit);
		assertFalse(Costs.perXp(m));
	}
}
