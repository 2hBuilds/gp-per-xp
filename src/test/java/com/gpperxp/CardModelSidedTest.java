package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * {@link CardModel} with a {@link SidedPrices}: the "Uses" prices show the buy side, the "Makes" prices the sell side, and
 * the trip's shopping lists use the same sides. The fixture is M1 as in {@link CostsSidedTest}: buy 100, 10 and 520, sell
 * 480.
 */
public class CardModelSidedTest
{
	/** M1: Herblore "Test potion", level 3, 25 xp, 1200 an hour; Herb and Vial in, one Potion out. */
	private static TrainingMethod m1()
	{
		return new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Arrays.asList(new ItemQty(10, "Herb", 1), new ItemQty(11, "Vial", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null);
	}

	/** Buy: 10 -> 100, 11 -> 10, 12 -> 520. Sell: 12 -> the given price. */
	private static Sided sided(long potionSell)
	{
		final Map<Integer, Long> buy = new HashMap<>();
		buy.put(10, 100L);
		buy.put(11, 10L);
		buy.put(12, 520L);
		final Map<Integer, Long> sell = new HashMap<>();
		sell.put(12, potionSell);
		return new Sided(buy, sell);
	}

	@Test
	public void usesShowTheBuySide()
	{
		final Sided prices = sided(480L);
		final CardModel model = CardModel.of(Costs.row(m1(), prices, false), prices, false, 1154, 20);
		assertEquals("100", model.uses.get(0).price);
		assertEquals("10", model.uses.get(1).price);
	}

	@Test
	public void makesShowsTheSellSide()
	{
		final Sided prices = sided(480L);
		final CardModel model = CardModel.of(Costs.row(m1(), prices, false), prices, false, 1154, 20);
		assertEquals("480", model.makes.get(0).price);
	}

	@Test
	public void taxOnMakes()
	{
		final Sided prices = sided(480L);
		final CardModel model = CardModel.of(Costs.row(m1(), prices, true), prices, true, 1154, 20);
		assertEquals("471", model.makes.get(0).price);
	}

	@Test
	public void shoppingUsesSides()
	{
		final Sided prices = sided(480L);
		final CardModel model = CardModel.of(Costs.row(m1(), prices, false), prices, false, 1154, 20);
		assertEquals("133 actions", model.trip.actions);
		assertEquals("133 x Herb", model.buy.get(0).text);
		assertEquals("13.3k", model.buy.get(0).price);
		assertEquals("133 x Vial", model.buy.get(1).text);
		assertEquals("1.3k", model.buy.get(1).price);
		assertEquals("133 x Potion", model.get.get(0).text);
		assertEquals("63.8k", model.get.get(0).price);
	}
}
