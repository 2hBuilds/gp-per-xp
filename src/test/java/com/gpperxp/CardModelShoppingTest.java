package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link CardModel}: the "To buy" and "You get" lists of the trip block. Fixture prices: 10 -> 100, 11 -> 10,
 * 12 -> 500, 99 -> 0. The trip runs from xp 1154 to level 20 (133 actions) unless stated.
 */
public class CardModelShoppingTest
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

	private static CardModel m1Model(boolean tax, int currentXp)
	{
		return CardModel.of(Costs.row(m1(), PRICE, tax), PRICE, tax, currentXp, 20);
	}

	@Test
	public void buyLines()
	{
		final CardModel model = m1Model(false, 1154);
		assertEquals(2, model.buy.size());
		assertEquals("133 x Herb", model.buy.get(0).text);
		assertEquals("133 x Vial", model.buy.get(1).text);
		assertEquals("13.3k", model.buy.get(0).price);
		assertEquals("1.3k", model.buy.get(1).price);
	}

	@Test
	public void getLines()
	{
		final CardModel model = m1Model(false, 1154);
		assertEquals(1, model.get.size());
		assertEquals("133 x Potion", model.get.get(0).text);
		assertEquals("66.5k", model.get.get(0).price);
	}

	@Test
	public void taxLowersWhatYouGet()
	{
		final CardModel model = m1Model(true, 1154);
		assertEquals("65.2k", model.get.get(0).price);
	}

	@Test
	public void noTripNoLists()
	{
		final CardModel model = m1Model(false, -1);
		assertTrue(model.buy.isEmpty());
		assertTrue(model.get.isEmpty());
	}

	@Test
	public void unpricedShowsQuestionMark()
	{
		final TrainingMethod m = new TrainingMethod("Herblore", "Mystery potion", 3, 25.0,
			Collections.singletonList(new ItemQty(99, "Mystery herb", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null);
		final CardModel model = CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 20);
		assertEquals("?", model.buy.get(0).price);
	}

	@Test
	public void fractionalQuantity()
	{
		final TrainingMethod m = new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Collections.singletonList(new ItemQty(10, "Herb", 0.5)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null);
		final CardModel model = CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 20);
		assertEquals("66.5 x Herb", model.buy.get(0).text);
	}
}
