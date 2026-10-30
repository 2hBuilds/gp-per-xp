package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** {@link Fmt#range}, {@link Fmt#qtyRange} and {@link Fmt#timeRange}: the texts of a wiki range. */
public class FmtRangeTest
{
	@Test
	public void rateRangeSmallerSizeFirst()
	{
		assertEquals("45k-55k", Fmt.range(45000, 55000));
		assertEquals("45k-55k", Fmt.range(55000, 45000));
	}

	@Test
	public void negativeRangeHasOneSign()
	{
		assertEquals("-100k to -150k", Fmt.range(-100000, -150000));
		assertEquals("-100k to -150k", Fmt.range(-150000, -100000));
	}

	@Test
	public void equalEndsGiveOneText()
	{
		assertEquals("50k", Fmt.range(50000, 50000));
		assertEquals("1.2k", Fmt.range(1200, 1240));
	}

	@Test
	public void smallRange()
	{
		assertEquals("0-500", Fmt.range(0, 500));
	}

	@Test
	public void qtyRangeUsesQtyTexts()
	{
		assertEquals("1,800-2,200", Fmt.qtyRange(1800, 2200));
		assertEquals("9.5-12", Fmt.qtyRange(9.5, 12));
		assertEquals("5", Fmt.qtyRange(5, 5));
	}

	@Test
	public void timeRangeUsesTimeTexts()
	{
		assertEquals("13 min to 20 min", Fmt.timeRange(332 / 1500.0, 332 / 1000.0));
		assertEquals("1 h to 1 h 30 min", Fmt.timeRange(1.0, 1.5));
		assertEquals("30 min", Fmt.timeRange(0.5, 0.5));
	}
}
