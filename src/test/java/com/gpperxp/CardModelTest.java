package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link CardModel}: the text of the hover card for the fixture method "Test potion". Fixture prices: 10 -> 100,
 * 11 -> 10, 12 -> 500, 99 -> 0. Unless stated, the model is built with tax off, target level 0 and xp 1154.
 */
public class CardModelTest
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

	private static Row m1Row(boolean tax)
	{
		return Costs.row(m1(), PRICE, tax);
	}

	private static void assertFact(CardModel model, int index, String label, String value)
	{
		assertEquals(label, model.facts.get(index)[0]);
		assertEquals(value, model.facts.get(index)[1]);
	}

	@Test
	public void titleAndSubtitle()
	{
		final CardModel model = CardModel.of(m1Row(false), PRICE, false, 1154, 0);
		assertEquals("Test potion", model.title);
		assertEquals("Herblore, level 3", model.subtitle);
	}

	@Test
	public void facts()
	{
		final CardModel model = CardModel.of(m1Row(false), PRICE, false, 1154, 0);
		assertEquals(5, model.facts.size());
		assertFact(model, 0, "XP per action", "25");
		assertFact(model, 1, "Actions per hour", "1,200 (typical)");
		assertFact(model, 2, "XP per hour", "30k");
		assertFact(model, 3, "GP per XP", "+15.6");
		assertFact(model, 4, "GP per hour", "+468k");
	}

	@Test
	public void usesAndMakes()
	{
		final CardModel model = CardModel.of(m1Row(false), PRICE, false, 1154, 0);
		assertEquals(2, model.uses.size());
		assertEquals("1 x Herb", model.uses.get(0).text);
		assertEquals("1 x Vial", model.uses.get(1).text);
		assertEquals("100", model.uses.get(0).price);
		assertEquals("10", model.uses.get(1).price);
		assertEquals(1, model.makes.size());
		assertEquals("1 x Potion", model.makes.get(0).text);
		assertEquals("500", model.makes.get(0).price);
		assertEquals("", model.makesNote);
	}

	@Test
	public void taxChangesMakes()
	{
		final CardModel model = CardModel.of(m1Row(true), PRICE, true, 1154, 0);
		assertEquals("490", model.makes.get(0).price);
		assertEquals("after 2% GE tax", model.makesNote);
		assertFact(model, 3, "GP per XP", "+15.2");
	}

	@Test
	public void tripNextLevel()
	{
		final CardModel model = CardModel.of(m1Row(false), PRICE, false, 1154, 0);
		assertNotNull(model.trip);
		assertEquals("To level 11", model.trip.heading);
		assertEquals("204 xp", model.trip.xp);
		assertEquals("9 actions", model.trip.actions);
		assertEquals("+3.5k gp", model.trip.cost);
		assertEquals("<1 min", model.trip.time);
		assertEquals(Money.EARN, model.trip.money);
	}

	@Test
	public void tripToTwenty()
	{
		final CardModel model = CardModel.of(m1Row(false), PRICE, false, 1154, 20);
		assertNotNull(model.trip);
		assertEquals("To level 20", model.trip.heading);
		assertEquals("3,316 xp", model.trip.xp);
		assertEquals("133 actions", model.trip.actions);
		assertEquals("+51.9k gp", model.trip.cost);
		assertEquals("7 min", model.trip.time);
	}

	@Test
	public void noTripWhenXpUnknown()
	{
		assertNull(CardModel.of(m1Row(false), PRICE, false, -1, 0).trip);
	}

	@Test
	public void noTripWhenTargetPassed()
	{
		assertNull(CardModel.of(m1Row(false), PRICE, false, 5000, 15).trip);
	}

	@Test
	public void unpricedRow()
	{
		final TrainingMethod m = new TrainingMethod("Herblore", "Mystery potion", 3, 25.0,
			Collections.singletonList(new ItemQty(99, "Mystery herb", 1)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null);
		final CardModel model = CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 0);
		assertEquals("?", model.uses.get(0).price);
		assertEquals("?", model.facts.get(3)[1]);
		assertEquals("?", model.facts.get(4)[1]);
		assertNotNull(model.trip);
		assertEquals("?", model.trip.cost);
		assertNotEquals(Money.EARN, model.trip.money);
	}

	@Test
	public void fractionalQty()
	{
		final TrainingMethod m = new TrainingMethod("Herblore", "Test potion", 3, 25.0,
			Collections.singletonList(new ItemQty(10, "Herb", 0.5)),
			Collections.singletonList(new ItemQty(12, "Potion", 1)), 1200, "src", null);
		final CardModel model = CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 0);
		assertEquals("0.5 x Herb", model.uses.get(0).text);
	}
}
