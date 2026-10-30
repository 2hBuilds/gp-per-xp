package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** {@link PriceBook}: the client thread fills it, any thread reads it, and reading never asks the source. */
public class PriceBookTest
{
	@Test
	public void unreadPriceIsZero()
	{
		final PriceBook book = new PriceBook(id -> id * 100L);
		assertEquals(0L, book.price(10));
	}

	@Test
	public void refreshReadsEveryId()
	{
		final PriceBook book = new PriceBook(id -> id * 100L);
		book.refresh(Arrays.asList(10, 11));
		assertEquals(1000L, book.price(10));
		assertEquals(1100L, book.price(11));
		assertEquals(0L, book.price(12));
	}

	@Test
	public void readingNeverCallsTheSource()
	{
		final AtomicInteger calls = new AtomicInteger();
		final PriceBook book = new PriceBook(id ->
		{
			calls.incrementAndGet();
			return 5L;
		});
		book.refresh(Arrays.asList(10, 11));
		assertEquals(2, calls.get());
		for (int i = 0; i < 5; i++)
		{
			book.price(10);
			book.price(99);
		}
		assertEquals(2, calls.get());
	}

	@Test
	public void refreshReportsChange()
	{
		final AtomicLong value = new AtomicLong(7);
		final PriceBook book = new PriceBook(id -> value.get());
		assertTrue(book.refresh(Arrays.asList(10)));
		assertFalse(book.refresh(Arrays.asList(10)));
		value.set(8);
		assertTrue(book.refresh(Arrays.asList(10)));
		assertEquals(8L, book.price(10));
	}

	@Test
	public void idsOfCollectsInputsAndOutputs()
	{
		final String json = "{\"methods\":[{\"skill\":\"Herblore\",\"name\":\"A\",\"level\":3,\"xpPerAction\":25,"
			+ "\"inputs\":[{\"itemId\":91,\"qty\":1},{\"itemId\":221,\"qty\":1}],"
			+ "\"outputs\":[{\"itemId\":121,\"qty\":1}],\"actionsPerHour\":2000,\"source\":\"s\"},"
			+ "{\"skill\":\"Prayer\",\"name\":\"B\",\"level\":1,\"xpPerAction\":4.5,"
			+ "\"inputs\":[{\"itemId\":526,\"qty\":1},{\"itemId\":91,\"qty\":1}],\"actionsPerHour\":5000,\"source\":\"s\"}]}";
		final Methods methods = Methods.load(new Gson(), new StringReader(json));
		assertEquals(new HashSet<>(Arrays.asList(91, 221, 121, 526)), PriceBook.idsOf(methods));
	}
}
