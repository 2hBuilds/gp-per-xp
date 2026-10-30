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
 * {@link Sort} with Kappa, a method that states no hourly rate (actionsPerHour 0), among the SortTest fixtures. Player
 * level 10 unless stated. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500, 99 -> 0; 99 is unpriced.
 */
public class SortUnratedTest
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

	private static TrainingMethod method(String name, int level, double xpPerAction, int actionsPerHour,
		List<ItemQty> inputs, List<ItemQty> outputs)
	{
		return new TrainingMethod("Test", name, level, xpPerAction, inputs, outputs, actionsPerHour, "src", null);
	}

	/**
	 * The eight fixture methods, in the order the plan passes them: Epsilon, Zeta, Alpha, Kappa, Theta, Gamma, Delta,
	 * Beta.
	 */
	private static List<Row> fixtureRows()
	{
		final List<Row> rows = new ArrayList<>();
		rows.add(Costs.row(method("Epsilon", 40, 100.0, 1000, one(10), NONE), PRICE, false));
		rows.add(Costs.row(method("Zeta", 1, 10.0, 1000, one(99), NONE), PRICE, false));
		rows.add(Costs.row(method("Alpha", 5, 10.0, 1000, one(10), NONE), PRICE, false));
		rows.add(Costs.row(method("Kappa", 5, 10.0, 0, one(10), NONE), PRICE, false));
		rows.add(Costs.row(method("Theta", 30, 10.0, 100, one(10), NONE), PRICE, false));
		rows.add(Costs.row(method("Gamma", 8, 20.0, 1000, one(12), NONE), PRICE, false));
		rows.add(Costs.row(method("Delta", 3, 10.0, 500, one(11), one(12)), PRICE, false));
		rows.add(Costs.row(method("Beta", 5, 50.0, 2000, one(10), NONE), PRICE, false));
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
	public void cheapestKeepsUnrated()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Delta", "Beta", "Alpha", "Kappa", "Gamma", "Zeta", "Theta", "Epsilon"),
			names(sorted));
	}

	@Test
	public void fastestPutsUnratedAfterRated()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.FASTEST_XP_H, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Beta", "Gamma", "Alpha", "Delta", "Kappa", "Zeta", "Theta", "Epsilon"),
			names(sorted));
	}

	@Test
	public void bestGpPerHourPutsUnratedAfterRated()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.BEST_GP_H, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Delta", "Alpha", "Beta", "Gamma", "Kappa", "Zeta", "Theta", "Epsilon"),
			names(sorted));
	}

	@Test
	public void reversedFastestKeepsUnratedAfter()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.FASTEST_XP_H, Sort.atLevel(10), true, true, true);
		assertEquals(Arrays.asList("Delta", "Alpha", "Gamma", "Beta", "Kappa", "Zeta", "Theta", "Epsilon"),
			names(sorted));
	}

	@Test
	public void reversedCheapestReversesAll()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, true);
		assertEquals(Arrays.asList("Gamma", "Kappa", "Alpha", "Beta", "Delta", "Zeta", "Theta", "Epsilon"),
			names(sorted));
	}
}
