package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link CardModel#withPace}: the "Actions per hour" fact of the card at a pace. Fixture prices: 10 -> 100, 11 -> 10,
 * 12 -> 500, 99 -> 0. The method is "Test potion" (1200 actions per hour) unless stated; the trip runs from xp 1154 to
 * level 20.
 */
public class CardModelPaceTest
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

	/** Herblore level 3, 25 xp per action, 1200 actions per hour: Herb and Vial make a Potion. */
	private static TrainingMethod m1()
	{
		return new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Arrays.asList(new ItemQty(10, "Herb", 1), new ItemQty(11, "Vial", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null);
	}

	private static void assertFact(CardModel model, int index, String label, String value)
	{
		assertEquals(label, model.facts.get(index)[0]);
		assertEquals(value, model.facts.get(index)[1]);
	}

	@Test
	public void hundredUnchanged()
	{
		final CardModel model = CardModel.of(Costs.row(m1(), PRICE, false), PRICE, false, 1154, 20).withPace(100);
		assertFact(model, 1, "Actions per hour", "1,200 (typical)");
	}

	@Test
	public void eightyLabel()
	{
		final CardModel model = CardModel.of(Costs.row(Pace.apply(m1(), 80), PRICE, false), PRICE, false, 1154, 20)
			.withPace(80);
		assertFact(model, 1, "Actions per hour", "960 (80% of typical)");
	}

	@Test
	public void unratedUnchanged()
	{
		final TrainingMethod unrated = new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Arrays.asList(new ItemQty(10, "Herb", 1), new ItemQty(11, "Vial", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 0, "src", null);
		final CardModel model = CardModel.of(Costs.row(unrated, PRICE, false), PRICE, false, 1154, 20);
		assertSame(model, model.withPace(80));
	}
}
