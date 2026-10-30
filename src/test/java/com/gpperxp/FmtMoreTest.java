package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** {@link Fmt}: thousands commas, quantities and times as the hover card shows them. */
public class FmtMoreTest
{
	@Test
	public void groupedCases()
	{
		assertEquals("0", Fmt.grouped(0));
		assertEquals("1,200", Fmt.grouped(1200));
		assertEquals("1,234,567", Fmt.grouped(1234567));
		assertEquals("-5", Fmt.grouped(-5));
		assertEquals("-1,234", Fmt.grouped(-1234));
	}

	@Test
	public void qtyCases()
	{
		assertEquals("1", Fmt.qty(1));
		assertEquals("1,200", Fmt.qty(1200));
		assertEquals("0.5", Fmt.qty(0.5));
		assertEquals("9.84", Fmt.qty(9.84));
		assertEquals("0.0014", Fmt.qty(0.00137));
		assertEquals("2.5", Fmt.qty(2.5));
	}

	@Test
	public void timeCases()
	{
		assertEquals("<1 min", Fmt.time(0.00333));
		assertEquals("30 min", Fmt.time(0.5));
		assertEquals("7 min", Fmt.time(0.110833));
		assertEquals("1 h", Fmt.time(0.9999));
		assertEquals("1 h 30 min", Fmt.time(1.5));
		assertEquals("2 h", Fmt.time(2.0));
		assertEquals("26 h 15 min", Fmt.time(26.25));
	}
}
