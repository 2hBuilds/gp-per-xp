package com.gpperxp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link TradePrices}: the buy and sell prices of one item, from its guide price and its latest print. Unless stated the
 * guide price is 1000 and now is 1,000,000 seconds; a print's times are seconds before now.
 */
public class TradePricesTest
{
	private static final long GUIDE = 1000L;
	private static final long NOW = 1_000_000L;

	@Test
	public void bothFresh()
	{
		final Quote quote = TradePrices.choose(GUIDE, new Print(1100L, NOW - 60, 1050L, NOW - 30), NOW);
		assertEquals(1100L, quote.buy);
		assertEquals(1050L, quote.sell);
		assertTrue(quote.live);
	}

	@Test
	public void staleSideUsesGuide()
	{
		final Quote quote = TradePrices.choose(GUIDE, new Print(1100L, NOW - 90_000, 1050L, NOW - 30), NOW);
		assertEquals(1000L, quote.buy);
		assertEquals(1050L, quote.sell);
		assertTrue(quote.live);
	}

	@Test
	public void ageBoundary()
	{
		final Quote edge = TradePrices.choose(GUIDE, new Print(1100L, NOW - 86_400, 1050L, NOW - 86_400), NOW);
		assertEquals(1100L, edge.buy);
		assertEquals(1050L, edge.sell);
		assertTrue(edge.live);

		final Quote past = TradePrices.choose(GUIDE, new Print(1100L, NOW - 86_401, 1050L, NOW - 86_401), NOW);
		assertEquals(1000L, past.buy);
		assertEquals(1000L, past.sell);
		assertFalse(past.live);
	}

	@Test
	public void farFromGuideIgnored()
	{
		final Quote quote = TradePrices.choose(GUIDE, new Print(4000L, NOW - 60, 300L, NOW - 30), NOW);
		assertEquals(1000L, quote.buy);
		assertEquals(1000L, quote.sell);
		assertFalse(quote.live);
	}

	/** The range runs from 1000 / 3 = 333 (integer arithmetic) to 1000 x 3 = 3000; each side is checked on its own. */
	@Test
	public void ratioBoundary()
	{
		final Quote top = TradePrices.choose(GUIDE, new Print(3000L, NOW - 60, 1050L, NOW - 30), NOW);
		assertEquals(3000L, top.buy);
		assertEquals(1050L, top.sell);

		final Quote overTop = TradePrices.choose(GUIDE, new Print(3001L, NOW - 60, 1050L, NOW - 30), NOW);
		assertEquals(1000L, overTop.buy);
		assertEquals(1050L, overTop.sell);

		final Quote bottom = TradePrices.choose(GUIDE, new Print(1100L, NOW - 60, 333L, NOW - 30), NOW);
		assertEquals(1100L, bottom.buy);
		assertEquals(333L, bottom.sell);

		final Quote underBottom = TradePrices.choose(GUIDE, new Print(1100L, NOW - 60, 332L, NOW - 30), NOW);
		assertEquals(1100L, underBottom.buy);
		assertEquals(1000L, underBottom.sell);
	}

	@Test
	public void invertedUsesGuide()
	{
		final Quote quote = TradePrices.choose(GUIDE, new Print(900L, NOW - 60, 1100L, NOW - 30), NOW);
		assertEquals(1000L, quote.buy);
		assertEquals(1000L, quote.sell);
		assertFalse(quote.live);
	}

	@Test
	public void nullsUseGuide()
	{
		final Quote quote = TradePrices.choose(GUIDE, new Print(null, null, null, null), NOW);
		assertEquals(1000L, quote.buy);
		assertEquals(1000L, quote.sell);
		assertFalse(quote.live);
	}

	@Test
	public void nullPrintUsesGuide()
	{
		final Quote quote = TradePrices.choose(GUIDE, null, NOW);
		assertEquals(1000L, quote.buy);
		assertEquals(1000L, quote.sell);
		assertFalse(quote.live);
	}

	@Test
	public void noGuideAcceptsFreshPrints()
	{
		final Quote quote = TradePrices.choose(0L, new Print(1100L, NOW - 60, 1050L, NOW - 30), NOW);
		assertEquals(1100L, quote.buy);
		assertEquals(1050L, quote.sell);
		assertTrue(quote.live);
	}

	@Test
	public void noGuideNoPrint()
	{
		final Quote quote = TradePrices.choose(0L, null, NOW);
		assertEquals(0L, quote.buy);
		assertEquals(0L, quote.sell);
		assertFalse(quote.live);
	}
}
