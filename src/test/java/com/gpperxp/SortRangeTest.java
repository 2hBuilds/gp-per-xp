package com.gpperxp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * {@link Sort} with the wiki rate shapes: Upper (up to), Runner (per run), Stated, Abouter (about) and Ranger (range).
 * All are level 5; the player is level 10. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class SortRangeTest
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

	private static TrainingMethod upper()
	{
		return new TrainingMethod("Test", "Upper", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(0, 2000, null, 0, null, null);
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

	private static TrainingMethod abouter()
	{
		return new TrainingMethod("Test", "Abouter", 5, 10.0, one(10), NONE, 1200, "src", null)
			.withV2(0, 0, "about", 0, null, null);
	}

	private static TrainingMethod ranger()
	{
		return new TrainingMethod("Test", "Ranger", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 1500, null, 0, null, null);
	}

	/** The fixture rows in the order Upper, Runner, Stated, Abouter, Ranger. */
	private static List<Row> fixtureRows()
	{
		final List<Row> rows = new ArrayList<>();
		rows.add(Costs.row(upper(), PRICE, false));
		rows.add(Costs.row(runner(), PRICE, false));
		rows.add(Costs.row(stated(), PRICE, false));
		rows.add(Costs.row(abouter(), PRICE, false));
		rows.add(Costs.row(ranger(), PRICE, false));
		return rows;
	}

	private static List<String> names(List<Row> rows)
	{
		final List<String> names = new ArrayList<>();
		for (Row row : rows)
		{
			names.add(row.method.name);
		}
		return names;
	}

	@Test
	public void fastestXpPerHour()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.FASTEST_XP_H, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Upper", "Abouter", "Ranger", "Stated", "Runner"), names(sorted));
	}

	@Test
	public void fastestXpPerHourReversed()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.FASTEST_XP_H, Sort.atLevel(10), true, true, true);
		assertEquals(Arrays.asList("Stated", "Ranger", "Abouter", "Upper", "Runner"), names(sorted));
	}

	@Test
	public void bestGpPerHour()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.BEST_GP_H, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Ranger", "Stated", "Abouter", "Upper", "Runner"), names(sorted));
	}

	@Test
	public void cheapestGpPerXp()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Runner", "Abouter", "Ranger", "Stated", "Upper"), names(sorted));
	}

	@Test
	public void failingPredicateGoesLast()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP,
			row -> !"Ranger".equals(row.method.name), true, true, false);
		assertEquals(Arrays.asList("Runner", "Abouter", "Stated", "Upper", "Ranger"), names(sorted));
	}
}
