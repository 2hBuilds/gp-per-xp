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
 * The reversed order of {@link Sort#arrange} and {@link ListModel#build} at the SortTest fixtures: player level 10 (xp
 * 1154 for the list model), profit and above shown, tax off. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500, 13 -> 49,
 * 14 -> 400,000,000; 99 is unpriced.
 */
public class SortReverseTest
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
	private static List<TrainingMethod> fixtureMethods()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(method("Epsilon", 40, 100.0, 1000, one(10), NONE));
		methods.add(method("Zeta", 1, 10.0, 1000, one(99), NONE));
		methods.add(method("Alpha", 5, 10.0, 1000, one(10), NONE));
		methods.add(method("Theta", 30, 10.0, 100, one(10), NONE));
		methods.add(method("Gamma", 8, 20.0, 1000, one(12), NONE));
		methods.add(method("Delta", 3, 10.0, 500, one(11), one(12)));
		methods.add(method("Beta", 5, 50.0, 2000, one(10), NONE));
		return methods;
	}

	/** The fixture methods as priced rows, in the same order as {@link #fixtureMethods()}. */
	private static List<Row> fixtureRows()
	{
		final List<Row> rows = new ArrayList<>();
		for (TrainingMethod candidate : fixtureMethods())
		{
			rows.add(Costs.row(candidate, PRICE, false));
		}
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

	private static List<String> lineNames(List<ListLine> lines)
	{
		final List<String> names = new ArrayList<>();
		for (ListLine line : lines)
		{
			names.add(line.name);
		}
		return names;
	}

	@Test
	public void cheapestReversed()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.CHEAPEST_GP_XP, Sort.atLevel(10), true, true, true);
		assertEquals(Arrays.asList("Gamma", "Alpha", "Beta", "Delta", "Zeta", "Theta", "Epsilon"), names(sorted));
	}

	@Test
	public void fastestReversed()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.FASTEST_XP_H, Sort.atLevel(10), true, true, true);
		assertEquals(Arrays.asList("Delta", "Alpha", "Gamma", "Beta", "Zeta", "Theta", "Epsilon"), names(sorted));
	}

	@Test
	public void bestGpHourReversed()
	{
		final List<Row> sorted = Sort.arrange(fixtureRows(), Sort.BEST_GP_H, Sort.atLevel(10), true, true, true);
		assertEquals(Arrays.asList("Gamma", "Beta", "Alpha", "Delta", "Zeta", "Theta", "Epsilon"), names(sorted));
	}

	@Test
	public void listModelReversed()
	{
		final List<ListLine> lines = ListModel.build(fixtureMethods(), PRICE, false, Sort.CHEAPEST_GP_XP,
			LEVEL_TEN_XP, true, true, true, Standing.ALL, true);
		assertEquals(Arrays.asList("Gamma", "Alpha", "Beta", "Delta", "Zeta", "Theta", "Epsilon"), lineNames(lines));
	}
}
