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

/** {@link Costs}: the GE tax, and the cost, xp, time and trip maths for a method at fixture prices. */
public class CostsTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);

	private static Map<Integer, Long> fixturePrices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(11, 10L);
		map.put(12, 500L);
		map.put(13, 49L);
		map.put(14, 400000000L);
		return map;
	}

	/** M1: Herblore "Test potion", level 3, 25 xp, 1200 an hour; 100 gp + 10 gp in, 500 gp out. */
	private static TrainingMethod m1()
	{
		return new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Arrays.asList(new ItemQty(10, 1), new ItemQty(11, 1)),
			Collections.singletonList(new ItemQty(12, 1)), 1200, "src", null);
	}

	private static TrainingMethod unpricedInput()
	{
		return new TrainingMethod("Herblore", "Unpriced input", 1, 1.0,
			Collections.singletonList(new ItemQty(99, 1)),
			Collections.singletonList(new ItemQty(12, 1)), 100, "src", null);
	}

	private static TrainingMethod unpricedOutput()
	{
		return new TrainingMethod("Herblore", "Unpriced output", 1, 1.0,
			Collections.singletonList(new ItemQty(10, 1)),
			Collections.singletonList(new ItemQty(99, 1)), 100, "src", null);
	}

	@Test
	public void costNoTax()
	{
		final TrainingMethod m = m1();
		assertEquals(-390.0, Costs.costPerAction(m, PRICE, false), 1e-6);
		assertEquals(-15.6, Costs.gpPerXp(m, PRICE, false), 1e-6);
	}

	@Test
	public void costWithTax()
	{
		final TrainingMethod m = m1();
		assertEquals(-380.0, Costs.costPerAction(m, PRICE, true), 1e-6);
		assertEquals(-15.2, Costs.gpPerXp(m, PRICE, true), 1e-6);
	}

	@Test
	public void sellPriceCases()
	{
		assertEquals(49, Costs.sellPrice(49, true));
		assertEquals(49, Costs.sellPrice(50, true));
		assertEquals(98, Costs.sellPrice(99, true));
		assertEquals(98, Costs.sellPrice(100, true));
		assertEquals(0, Costs.sellPrice(0, true));
		assertEquals(395000000, Costs.sellPrice(400000000, true));
		assertEquals(500, Costs.sellPrice(500, false));
	}

	@Test
	public void taxCapApplies()
	{
		final TrainingMethod cap = new TrainingMethod("Smithing", "Cap", 1, 1.0,
			Collections.singletonList(new ItemQty(11, 1)),
			Collections.singletonList(new ItemQty(14, 1)), 1, "src", null);
		assertEquals(-394999990.0, Costs.costPerAction(cap, PRICE, true), 1e-6);
	}

	@Test
	public void quantitiesMultiply()
	{
		final TrainingMethod qty = new TrainingMethod("Prayer", "Qty", 1, 10.0,
			Collections.singletonList(new ItemQty(10, 2)),
			Collections.singletonList(new ItemQty(11, 3)), 100, "src", null);
		assertEquals(170.0, Costs.costPerAction(qty, PRICE, false), 1e-6);
		assertEquals(17.0, Costs.gpPerXp(qty, PRICE, false), 1e-6);
		assertEquals(170.0, Costs.costPerAction(qty, PRICE, true), 1e-6);
		assertEquals(17.0, Costs.gpPerXp(qty, PRICE, true), 1e-6);
	}

	@Test
	public void ratesPerHour()
	{
		final TrainingMethod m = m1();
		assertEquals(30000.0, Costs.xpPerHour(m), 1e-6);
		assertEquals(-468000.0, Costs.gpPerHour(m, PRICE, false), 1e-6);
	}

	@Test
	public void pricedFlag()
	{
		assertTrue(Costs.priced(m1(), PRICE));
		assertFalse(Costs.priced(unpricedInput(), PRICE));
		assertFalse(Costs.priced(unpricedOutput(), PRICE));
	}

	@Test
	public void rowFields()
	{
		final TrainingMethod m = m1();
		final Row row = Costs.row(m, PRICE, false);
		assertSame(m, row.method);
		assertEquals(-15.6, row.gpPerXp, 1e-6);
		assertEquals(30000.0, row.xpPerHour, 1e-6);
		assertEquals(-468000.0, row.gpPerHour, 1e-6);
		assertTrue(row.priced);
		assertFalse(Costs.row(unpricedInput(), PRICE, false).priced);
	}

	@Test
	public void tripRoundsUp()
	{
		final Trip trip = Costs.trip(m1(), PRICE, false, 0, 83);
		assertEquals(4L, trip.actions);
		assertEquals(-1560.0, trip.gp, 1e-6);
		assertEquals(4 / 1200.0, trip.hours, 1e-6);
	}

	@Test
	public void tripExact()
	{
		final Trip trip = Costs.trip(m1(), PRICE, false, 0, 100);
		assertEquals(4L, trip.actions);
	}

	@Test
	public void tripNothingNeeded()
	{
		final Trip level = Costs.trip(m1(), PRICE, false, 83, 83);
		assertEquals(0L, level.actions);
		assertEquals(0.0, level.gp, 1e-6);
		assertEquals(0.0, level.hours, 1e-6);

		final Trip above = Costs.trip(m1(), PRICE, false, 100, 83);
		assertEquals(0L, above.actions);
		assertEquals(0.0, above.gp, 1e-6);
		assertEquals(0.0, above.hours, 1e-6);
	}
}
