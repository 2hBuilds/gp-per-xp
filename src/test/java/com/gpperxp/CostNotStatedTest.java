package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Rows whose cost the wiki does not state ({@code costsNotStated}), at player xp 1154 and target level 20. Skill "Test",
 * tax off. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class CostNotStatedTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);
	private static final List<ItemQty> NONE = Collections.emptyList();
	private static final int LEVEL_TEN_XP = 1154;

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

	/** Stated: level 5, 10 xp per action, 1,000 actions per hour, one Herb (100 gp) per action. */
	private static TrainingMethod stated()
	{
		return new TrainingMethod("Test", "Stated", 5, 10.0, one(10), NONE, 1000, "src", null);
	}

	/** Fly: level 5, 10 xp per action, 1,000 actions per hour, no items, cost not stated by the wiki. */
	private static TrainingMethod fly()
	{
		return new TrainingMethod("Test", "Fly", 5, 10.0, NONE, NONE, 1000, "src", null)
			.withCostsNotStated(true);
	}

	private static ListLine line(List<ListLine> lines, String name)
	{
		for (ListLine candidate : lines)
		{
			if (candidate.name.equals(name))
			{
				return candidate;
			}
		}
		fail("no line named " + name);
		return null;
	}

	private static TrainingMethod load(String json)
	{
		return Methods.load(new Gson(), new StringReader(json)).all().get(0);
	}

	private static String row(String name, String extra)
	{
		return "{\"updated\":\"2026-10-30\",\"methods\":[{"
			+ "\"skill\":\"Test\",\"name\":\"" + name + "\",\"level\":5,\"xpPerAction\":10.0,"
			+ "\"inputs\":[],\"outputs\":[],\"actionsPerHour\":1000,\"source\":\"https://oldschool.runescape.wiki/x\","
			+ "\"note\":null" + extra + "}]}";
	}

	@Test
	public void costStatedFollowsTheFlag()
	{
		assertFalse(Costs.costStated(fly()));
		assertTrue(Costs.costStated(stated()));
	}

	@Test
	public void unratedCostSortsLastInGpSorts()
	{
		final List<TrainingMethod> methods = Arrays.asList(fly(), stated());
		final List<ListLine> lines = ListModel.build(methods, PRICE, false, Sort.CHEAPEST_GP_XP, LEVEL_TEN_XP,
			true, true, false, Standing.ALL, false);

		assertEquals("Stated", lines.get(0).name);
		assertEquals("Fly", lines.get(1).name);

		final ListLine fly = line(lines, "Fly");
		assertEquals("-", fly.gpXp);
		assertEquals("10k", fly.xpH);
		assertEquals("-", fly.gpH);
		assertNotEquals(Money.EARN, fly.money);
		assertFalse(fly.unpriced);
	}

	@Test
	public void reversedOrderKeepsUnratedLastInGpSorts()
	{
		final List<TrainingMethod> methods = Arrays.asList(fly(), stated());

		final List<ListLine> cheapest = ListModel.build(methods, PRICE, false, Sort.CHEAPEST_GP_XP, LEVEL_TEN_XP,
			true, true, true, Standing.ALL, false);
		assertEquals("Stated", cheapest.get(0).name);
		assertEquals("Fly", cheapest.get(1).name);

		final List<ListLine> bestGpH = ListModel.build(methods, PRICE, false, Sort.BEST_GP_H, LEVEL_TEN_XP,
			true, true, true, Standing.ALL, false);
		assertEquals("Stated", bestGpH.get(0).name);
		assertEquals("Fly", bestGpH.get(1).name);

		final List<ListLine> fastestXpH = ListModel.build(methods, PRICE, false, Sort.FASTEST_XP_H, LEVEL_TEN_XP,
			true, true, false, Standing.ALL, false);
		assertEquals("Fly", fastestXpH.get(0).name);
		assertEquals("Stated", fastestXpH.get(1).name);
	}

	@Test
	public void unratedRowKeptWithoutProfit()
	{
		final List<ListLine> lines = ListModel.build(Arrays.asList(fly(), stated()), PRICE, false,
			Sort.CHEAPEST_GP_XP, LEVEL_TEN_XP, false, true, false, Standing.ALL, false);
		assertEquals("Fly", line(lines, "Fly").name);
	}

	@Test
	public void cardSaysNotStated()
	{
		final CardModel model = CardModel.of(Costs.row(fly(), PRICE, false), PRICE, false, LEVEL_TEN_XP, 20);

		assertEquals("Actions per hour", model.facts.get(1)[0]);
		assertEquals("1,000 (typical)", model.facts.get(1)[1]);
		assertEquals("GP per XP", model.facts.get(3)[0]);
		assertEquals("not stated", model.facts.get(3)[1]);
		assertEquals("GP per hour", model.facts.get(4)[0]);
		assertEquals("not stated", model.facts.get(4)[1]);

		assertEquals("not stated", model.trip.cost);
		assertEquals("20 min", model.trip.time);
		assertNotEquals(Money.EARN, model.trip.money);
	}

	@Test
	public void loadReadsTheFlagAndPaceKeepsIt()
	{
		final TrainingMethod flagged = load(row("Fly", ",\"costsNotStated\":true"));
		assertTrue(flagged.costsNotStated);

		final TrainingMethod plain = load(row("Stated", ""));
		assertFalse(plain.costsNotStated);

		assertTrue(Pace.apply(fly(), 50).costsNotStated);
	}
}
