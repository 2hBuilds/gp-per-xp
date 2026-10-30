package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link Costs} with a {@link SidedPrices}: the outputs are priced at the sell side and the inputs at the buy side. The
 * fixture is M1 (Herblore "Test potion", 25 xp): Herb 100 and Vial 10 bought, a Potion bought at 520 and sold at 480.
 */
public class CostsSidedTest
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
	public void outputsUseTheSellSide()
	{
		final Sided prices = sided(480L);
		assertEquals(-370.0, Costs.costPerAction(m1(), prices, false), 1e-6);
		assertEquals(-14.8, Costs.gpPerXp(m1(), prices, false), 1e-6);
	}

	@Test
	public void taxAppliesToTheSellSide()
	{
		assertEquals(-361.0, Costs.costPerAction(m1(), sided(480L), true), 1e-6);
	}

	@Test
	public void plainFunctionUnchanged()
	{
		final Map<Integer, Long> plain = new HashMap<>();
		plain.put(10, 100L);
		plain.put(11, 10L);
		plain.put(12, 500L);
		final IntToLongFunction price = id -> plain.getOrDefault(id, 0L);
		assertEquals(-390.0, Costs.costPerAction(m1(), price, false), 1e-6);
	}

	@Test
	public void unpricedWhenTheSellSideIsZero()
	{
		assertTrue(Costs.priced(m1(), sided(480L)));
		assertFalse(Costs.priced(m1(), sided(0L)));
	}

	@Test
	public void sellSideHelper()
	{
		assertEquals(480L, Costs.sellSide(sided(480L)).applyAsLong(12));
		final IntToLongFunction plain = id -> 7L;
		assertSame(plain, Costs.sellSide(plain));
	}
}
