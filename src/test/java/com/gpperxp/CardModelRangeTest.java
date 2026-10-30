package com.gpperxp;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link CardModel} with the wiki rate shapes, at player xp 1154 and target level 20 (4,470 xp). Skill "Test", tax off.
 * Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class CardModelRangeTest
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

	private static TrainingMethod ranger()
	{
		return new TrainingMethod("Test", "Ranger", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 1500, null, 0, null, null);
	}

	private static TrainingMethod upper()
	{
		return new TrainingMethod("Test", "Upper", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(0, 2000, null, 0, null, null);
	}

	private static TrainingMethod abouter()
	{
		return new TrainingMethod("Test", "Abouter", 5, 10.0, one(10), NONE, 1200, "src", null)
			.withV2(0, 0, "about", 0, null, null);
	}

	private static TrainingMethod runner()
	{
		return new TrainingMethod("Test", "Runner", 5, 500.0, one(11), one(12), 0, "src", null)
			.withV2(0, 0, "perRun", 0, null, null);
	}

	private static TrainingMethod stated()
	{
		return new TrainingMethod("Test", "Stated", 5, 10.0, one(10), NONE, 1000, "src", null);
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
	public void rangeCard()
	{
		final CardModel model = card(ranger());
		assertFact(model, 1, "Actions per hour", "1,000-1,500 (wiki range)");
		assertFact(model, 2, "XP per hour", "10k-15k");
		assertFact(model, 4, "GP per hour", "-100k to -150k");
		assertEquals("13 min to 20 min", model.trip.time);
	}

	@Test
	public void upToCard()
	{
		final CardModel model = card(upper());
		assertFact(model, 1, "Actions per hour", "up to 2,000");
		assertFact(model, 2, "XP per hour", "up to 20k");
		assertFact(model, 4, "GP per hour", "up to -200k");
		assertEquals("at least 10 min", model.trip.time);
	}

	@Test
	public void aboutCard()
	{
		final CardModel model = card(abouter());
		assertFact(model, 1, "Actions per hour", "about 1,200");
		assertFact(model, 2, "XP per hour", "about 12k");
		assertFact(model, 4, "GP per hour", "about -120k");
		assertEquals("about 17 min", model.trip.time);
	}

	@Test
	public void perRunCard()
	{
		final CardModel model = card(runner());
		assertFact(model, 0, "XP per run", "500");
		assertFact(model, 1, "Actions per hour", "per run");
		assertFact(model, 2, "XP per hour", "-");
		assertFact(model, 3, "GP per XP", "+1.0");
		assertFact(model, 4, "GP per hour", "-");
		assertEquals("7 runs", model.trip.actions);
		assertEquals("+3.4k gp", model.trip.cost);
		assertEquals(Money.EARN, model.trip.money);
		assertEquals("not stated", model.trip.time);
	}

	@Test
	public void pacedRangeCard()
	{
		final CardModel model = CardModel.of(Costs.row(Pace.apply(ranger(), 80), PRICE, false), PRICE, false, 1154, 20)
			.withPace(80);
		assertFact(model, 1, "Actions per hour", "800-1,200 (80% of wiki range)");
	}

	@Test
	public void pacedAboutCard()
	{
		final CardModel model = CardModel.of(Costs.row(Pace.apply(abouter(), 80), PRICE, false), PRICE, false, 1154, 20)
			.withPace(80);
		assertFact(model, 1, "Actions per hour", "about 960 (80% of typical)");
	}

	@Test
	public void pacedUpToCard()
	{
		final CardModel model = CardModel.of(Costs.row(Pace.apply(upper(), 80), PRICE, false), PRICE, false, 1154, 20)
			.withPace(80);
		assertFact(model, 1, "Actions per hour", "up to 1,600 (80% of wiki figure)");
	}

	@Test
	public void statedCardUnchanged()
	{
		assertFact(card(stated()), 1, "Actions per hour", "1,000 (typical)");
	}
}
