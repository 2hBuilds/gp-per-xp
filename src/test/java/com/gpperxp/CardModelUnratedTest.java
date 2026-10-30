package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * {@link CardModel}: the hover card of Kappa (no stated hourly rate), of Free lap (no ingredients and no products) and
 * of the rated M1 of CardModelTest. Player xp 1154, tax off, target level 20. Fixture prices: 10 -> 100, 11 -> 10,
 * 12 -> 500, 99 -> 0.
 */
public class CardModelUnratedTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);
	private static final List<ItemQty> NONE = Collections.emptyList();

	private static Map<Integer, Long> fixturePrices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(11, 10L);
		map.put(12, 500L);
		map.put(99, 0L);
		return map;
	}

	/** Kappa: level 5, 10 xp per action, no stated hourly rate (actionsPerHour 0), one Herb (100 gp) per action. */
	private static TrainingMethod kappa()
	{
		return new TrainingMethod("Test", "Kappa", 5, 10.0,
			Collections.singletonList(new ItemQty(10, "Herb", 1)), NONE, 0, "src", null);
	}

	/** Free lap: skill Test, level 1, 20 xp per action, 3000 actions per hour, no ingredients and no products. */
	private static TrainingMethod freeLap()
	{
		return new TrainingMethod("Test", "Free lap", 1, 20.0, NONE, NONE, 3000, "src", "free");
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
	public void unratedFacts()
	{
		final CardModel model = CardModel.of(Costs.row(kappa(), PRICE, false), PRICE, false, 1154, 20);
		assertEquals(5, model.facts.size());
		assertFact(model, 0, "XP per action", "10");
		assertFact(model, 1, "Actions per hour", "not stated");
		assertFact(model, 2, "XP per hour", "-");
		assertFact(model, 3, "GP per XP", "-10.0");
		assertFact(model, 4, "GP per hour", "-");
	}

	@Test
	public void unratedTrip()
	{
		final CardModel model = CardModel.of(Costs.row(kappa(), PRICE, false), PRICE, false, 1154, 20);
		assertNotNull(model.trip);
		assertEquals("To level 20", model.trip.heading);
		assertEquals("3,316 xp", model.trip.xp);
		assertEquals("332 actions", model.trip.actions);
		assertEquals("-33.2k gp", model.trip.cost);
		assertEquals("not stated", model.trip.time);
		assertNotEquals(Money.EARN, model.trip.money);
	}

	@Test
	public void freeFacts()
	{
		final CardModel model = CardModel.of(Costs.row(freeLap(), PRICE, false), PRICE, false, 1154, 20);
		assertFact(model, 2, "XP per hour", "60k");
		assertFact(model, 3, "GP per XP", "free");
		assertFact(model, 4, "GP per hour", "free");
	}

	@Test
	public void freeTrip()
	{
		final CardModel model = CardModel.of(Costs.row(freeLap(), PRICE, false), PRICE, false, 1154, 20);
		assertNotNull(model.trip);
		assertEquals("166 actions", model.trip.actions);
		assertEquals("free", model.trip.cost);
		assertEquals("3 min", model.trip.time);
	}

	@Test
	public void ratedUnchanged()
	{
		final CardModel model = CardModel.of(Costs.row(m1(), PRICE, false), PRICE, false, 1154, 20);
		assertFact(model, 1, "Actions per hour", "1,200 (typical)");
	}
}
