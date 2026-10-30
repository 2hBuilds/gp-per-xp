package com.gpperxp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * {@link PriceBook} in live trade mode: a guide price is read through {@link PriceBook#refresh}, and a print answers only
 * in trades mode. The prints are timed near the real clock, because PriceBook reads that clock itself.
 */
public class PriceBookTradesTest
{
	/** One print for item {@code id}: a high and a low, both traded a minute before the real clock. */
	private static Map<Integer, Print> printFor(int id, long high, long low)
	{
		final long now = System.currentTimeMillis() / 1000L;
		final Map<Integer, Print> prints = new HashMap<>();
		prints.put(id, new Print(high, now - 60L, low, now - 60L));
		return prints;
	}

	@Test
	public void guideModeIgnoresPrints()
	{
		final PriceBook book = new PriceBook(id -> 1000L);
		book.refresh(Arrays.asList(10));
		book.setPrints(printFor(10, 1100L, 1050L));
		book.setUseTrades(false);
		assertEquals(1000L, book.price(10));
		assertEquals(1000L, book.sell(10));
	}

	@Test
	public void tradesModeUsesPrints()
	{
		final PriceBook book = new PriceBook(id -> 1000L);
		book.refresh(Arrays.asList(10));
		book.setPrints(printFor(10, 1100L, 1050L));
		book.setUseTrades(true);
		assertEquals(1100L, book.price(10));
		assertEquals(1050L, book.sell(10));
		assertEquals(1100L, book.applyAsLong(10));
	}

	@Test
	public void noPrintUsesGuide()
	{
		final PriceBook book = new PriceBook(id -> 1000L);
		book.refresh(Arrays.asList(10, 11));
		book.setPrints(printFor(10, 1100L, 1050L));
		book.setUseTrades(true);
		assertEquals(1000L, book.price(11));
		assertEquals(1000L, book.sell(11));
	}

	@Test
	public void readingNeverCallsTheSource()
	{
		final AtomicInteger calls = new AtomicInteger();
		final PriceBook book = new PriceBook(id ->
		{
			calls.incrementAndGet();
			return 1000L;
		});
		book.refresh(Arrays.asList(10));
		assertEquals(1, calls.get());
		book.setPrints(printFor(10, 1100L, 1050L));
		book.setUseTrades(true);
		for (int i = 0; i < 5; i++)
		{
			book.price(10);
			book.sell(10);
			book.price(99);
			book.sell(99);
		}
		book.setUseTrades(false);
		book.price(10);
		book.sell(10);
		assertEquals(1, calls.get());
	}
}
