package com.gpperxp;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link CardModel} with the "at least" rate shape and the xp-per-hour-only rows, at player xp 1154 and target level 20
 * (3,316 xp to go). Skill "Test", tax off. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class CardModelXpOnlyTest
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

	private static List<ItemQty> one(int itemId)
	{
		return Collections.singletonList(new ItemQty(itemId, 1.0));
	}

	/** Floor: level 5, 10 xp per action, "at least" 1000 actions per hour, one Herb (100 gp) per action. */
	private static TrainingMethod floor()
	{
		return new TrainingMethod("Test", "Floor", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 0, null, 0, null, null);
	}

	/** Hourly: level 5, 1 xp per action, no items, a wiki range of 45,000 to 55,000 xp per hour, rate unit "xp". */
	private static TrainingMethod hourly()
	{
		return new TrainingMethod("Test", "Hourly", 5, 1.0, NONE, NONE, 0, "src", null)
			.withV2(45000, 55000, null, 0, null, null)
			.withRateUnit("xp");
	}

	private static CardModel card(TrainingMethod m)
	{
		return CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 20);
	}

	private static void assertFact(CardModel model, int index, String label, String value)
	{
		assertEquals(label, model.facts.get(index)[0]);
		assertEquals(value, model.facts.get(index)[1]);
	}

	@Test
	public void atLeastCard()
	{
		final CardModel model = card(floor());
		assertFact(model, 1, "Actions per hour", "at least 1,000");
		assertFact(model, 2, "XP per hour", "at least 10k");
		assertFact(model, 4, "GP per hour", "at least -100k");
		assertEquals("at most 20 min", model.trip.time);
	}

	@Test
	public void pacedAtLeastCard()
	{
		final CardModel model = CardModel.of(Costs.row(Pace.apply(floor(), 50), PRICE, false), PRICE, false, 1154, 20)
			.withPace(50);
		assertFact(model, 1, "Actions per hour", "at least 500 (50% of wiki figure)");
	}

	@Test
	public void xpOnlyCard()
	{
		final CardModel model = card(hourly());
		assertFact(model, 0, "XP per action", "not stated");
		assertFact(model, 1, "Actions per hour", "not stated");
		assertFact(model, 2, "XP per hour", "45k-55k");
		assertFact(model, 3, "GP per XP", "free");
		assertFact(model, 4, "GP per hour", "free");
		assertEquals("3,316 xp", model.trip.xp);
		assertEquals("not stated", model.trip.actions);
		assertEquals("free", model.trip.cost);
		assertEquals("4 min", model.trip.time);
	}

	@Test
	public void pacedXpOnlyCardIsUnchangedByWithPace()
	{
		final CardModel model = card(Pace.apply(hourly(), 80));
		assertSame(model, model.withPace(80));
		assertFact(model, 2, "XP per hour", "36k-44k");
	}
}
