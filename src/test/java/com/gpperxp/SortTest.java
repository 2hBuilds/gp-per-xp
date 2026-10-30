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
import static org.junit.Assert.assertTrue;

/**
 * {@link Sort}: the three orders, the profit and above-level filters, and the groups, at player level 10 unless
 * stated. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500, 13 -> 49, 14 -> 400,000,000; 99 is unpriced.
 */
public class SortTest
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
		map.put(13, 49L);
		map.put(14, 400000000L);
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

	/** The seven fixture methods, in the order the plan passes them: Epsilon, Zeta, Alpha, Theta, Gamma, Delta, Beta. */
	private static List<Row> fixtureRows()
	{
		final List<Row> rows = new ArrayList<>();
		rows.add(Costs.row(method("Epsilon", 40, 100.0, 1000, one(10), NONE), PRICE, false));
		rows.add(Costs.row(method("Zeta", 1, 10.0, 1000, one(99), NONE), PRICE, false));
		rows.add(Costs.row(method("Alpha", 5, 10.0, 1000, one(10), NONE), PRICE, false));
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
	public void cheapestFirst()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Delta", "Beta", "Alpha", "Gamma", "Zeta", "Theta", "Epsilon"), names(sorted));
	}

	@Test
	public void fastestFirst()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.FASTEST_XP_H, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Beta", "Gamma", "Alpha", "Delta", "Zeta", "Theta", "Epsilon"), names(sorted));
	}

	@Test
	public void bestGpPerHour()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.BEST_GP_H, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Delta", "Alpha", "Beta", "Gamma", "Zeta", "Theta", "Epsilon"), names(sorted));
	}

	@Test
	public void profitHidden()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), false, true, false);
		assertEquals(Arrays.asList("Beta", "Alpha", "Gamma", "Zeta", "Theta", "Epsilon"), names(sorted));
	}

	@Test
	public void aboveHidden()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, false, false);
		assertEquals(Arrays.asList("Delta", "Beta", "Alpha", "Gamma", "Zeta"), names(sorted));
	}

	@Test
	public void allDoableAtNinetyNine()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(99), true, true, false);
		assertEquals(Arrays.asList("Delta", "Epsilon", "Beta", "Alpha", "Theta", "Gamma", "Zeta"), names(sorted));
	}

	@Test
	public void tiesBreakByLevelThenName()
	{
		final List<Row> rows = new ArrayList<>();
		rows.add(Costs.row(method("Alpha", 5, 10.0, 1000, one(10), NONE), PRICE, false));
		rows.add(Costs.row(method("Eta", 2, 10.0, 1000, one(10), NONE), PRICE, false));
		rows.add(Costs.row(method("Aardvark", 5, 10.0, 1000, one(10), NONE), PRICE, false));
		final List<Row> sorted = Sort.arrange(rows, Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, false);
		assertEquals(Arrays.asList("Eta", "Aardvark", "Alpha"), names(sorted));
	}

	@Test
	public void inputUntouched()
	{
		final List<Row> rows = fixtureRows();
		final List<String> before = names(rows);
		Sort.arrange(rows, Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, false);
		assertEquals(before, names(rows));
		assertTrue(Sort.arrange(new ArrayList<Row>(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, false).isEmpty());
	}
}
