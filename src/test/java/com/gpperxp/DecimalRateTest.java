package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Decimal hourly rates: the wiki states some rates as decimals (9.5 trials an hour), and the sums, the trip, the card
 * and the methods file read them as they are. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500, 99 -> 0.
 */
public class DecimalRateTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);

	private static Map<Integer, Long> fixturePrices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(11, 10L);
		map.put(12, 500L);
		map.put(99, 0L);
		return map;
	}

	/** Herblore level 3, 100 xp per action, 9.5 actions per hour: Herb and Vial make a Potion. */
	private static TrainingMethod decimal()
	{
		return new TrainingMethod("Herblore", "Test potion", 3, 100.0,
			Arrays.asList(new ItemQty(10, "Herb", 1), new ItemQty(11, "Vial", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 9.5, "src", null);
	}

	@Test
	public void xpPerHourWithDecimalRate()
	{
		assertEquals(950.0, Costs.xpPerHour(decimal()), 1e-9);
	}

	@Test
	public void tripHoursWithDecimalRate()
	{
		final Trip trip = Costs.trip(decimal(), PRICE, false, 0, 950);
		assertEquals(10L, trip.actions);
		assertEquals(10 / 9.5, trip.hours, 1e-9);
	}

	@Test
	public void cardShowsDecimalRate()
	{
		final CardModel model = CardModel.of(Costs.row(decimal(), PRICE, false), PRICE, false, 1154, 20);
		assertEquals("Actions per hour", model.facts.get(1)[0]);
		assertEquals("9.5 (typical)", model.facts.get(1)[1]);
	}

	@Test
	public void jsonReadsDecimalRate()
	{
		final String json = "{\"methods\":[{\"skill\":\"Herblore\",\"name\":\"Test potion\",\"level\":3,"
			+ "\"xpPerAction\":100,\"inputs\":[{\"itemId\":10,\"qty\":1}],\"outputs\":[{\"itemId\":12,\"qty\":1}],"
			+ "\"actionsPerHour\":9.5,\"source\":\"src\"}]}";
		final Methods methods = Methods.load(new Gson(), new StringReader(json));
		assertEquals(1, methods.all().size());
		assertEquals(9.5, methods.all().get(0).actionsPerHour, 1e-9);
	}
}
