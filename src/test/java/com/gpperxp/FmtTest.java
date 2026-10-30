package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** {@link Fmt}: the short rate text (k and m) and the gp/xp text (one decimal under 1000). */
public class FmtTest
{
	@Test
	public void rateCases()
	{
		assertEquals("rate(0)", "0", Fmt.rate(0));
		assertEquals("rate(999)", "999", Fmt.rate(999));
		assertEquals("rate(1000)", "1k", Fmt.rate(1000));
		assertEquals("rate(1500)", "1.5k", Fmt.rate(1500));
		assertEquals("rate(5000)", "5k", Fmt.rate(5000));
		assertEquals("rate(62500)", "62.5k", Fmt.rate(62500));
		assertEquals("rate(100000)", "100k", Fmt.rate(100000));
		assertEquals("rate(312500)", "313k", Fmt.rate(312500));
		assertEquals("rate(530022)", "530k", Fmt.rate(530022));
		assertEquals("rate(999999)", "1m", Fmt.rate(999999));
		assertEquals("rate(1000000)", "1m", Fmt.rate(1000000));
		assertEquals("rate(1338750)", "1.3m", Fmt.rate(1338750));
		assertEquals("rate(-245000)", "-245k", Fmt.rate(-245000));
		assertEquals("rate(-1234567)", "-1.2m", Fmt.rate(-1234567));
		assertEquals("rate(-0.2)", "0", Fmt.rate(-0.2));
	}

	@Test
	public void gpXpCases()
	{
		assertEquals("gpXp(12.5)", "12.5", Fmt.gpXp(12.5));
		assertEquals("gpXp(25)", "25.0", Fmt.gpXp(25));
		assertEquals("gpXp(-15.6)", "-15.6", Fmt.gpXp(-15.6));
		assertEquals("gpXp(0)", "0.0", Fmt.gpXp(0));
		assertEquals("gpXp(-0.04)", "0.0", Fmt.gpXp(-0.04));
		assertEquals("gpXp(999.94)", "999.9", Fmt.gpXp(999.94));
		assertEquals("gpXp(1234)", "1.2k", Fmt.gpXp(1234));
		assertEquals("gpXp(-1234)", "-1.2k", Fmt.gpXp(-1234));
	}
}
