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
 * {@link ListFilter}: the search box keeps the lines whose name contains its text. The lines are the
 * {@link ListModelTest} fixture at player xp 1154, cheapest first: Delta, Beta, Alpha, Gamma, Zeta, Theta, Epsilon.
 */
public class ListFilterTest
{
	private static final Map<Integer, Long> PRICES = fixturePrices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);
	private static final List<ItemQty> NONE = Collections.emptyList();
	private static final int LEVEL_TEN_XP = 1154;
	private static final List<String> ALL_NAMES =
		Arrays.asList("Delta", "Beta", "Alpha", "Gamma", "Zeta", "Theta", "Epsilon");

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

	/** The seven fixture methods, in the order they are added. */
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

	/** The sidebar lines for the fixture at player xp 1154, cheapest first, with the profit and above-level rows. */
	private static List<ListLine> fixtureLines()
	{
		return ListModel.build(fixtureMethods(), PRICE, false, Sort.CHEAPEST_GP_XP, LEVEL_TEN_XP, true, true, false, Standing.ALL,
			true);
	}

	private static List<String> names(List<ListLine> lines)
	{
		final List<String> names = new ArrayList<>();
		for (ListLine candidate : lines)
		{
			names.add(candidate.name);
		}
		return names;
	}

	@Test
	public void emptyKeepsAll()
	{
		final List<ListLine> lines = fixtureLines();
		assertEquals(ALL_NAMES, names(ListFilter.apply(lines, "")));
		assertEquals(ALL_NAMES, names(ListFilter.apply(lines, "   ")));
		assertEquals(ALL_NAMES, names(ListFilter.apply(lines, null)));
	}

	@Test
	public void matchesAnyCase()
	{
		assertEquals(Collections.singletonList("Gamma"), names(ListFilter.apply(fixtureLines(), "GAMMA")));
	}

	@Test
	public void trimsAndKeepsOrder()
	{
		assertEquals(Arrays.asList("Delta", "Beta", "Zeta", "Theta"),
			names(ListFilter.apply(fixtureLines(), "  ta ")));
	}

	@Test
	public void noMatchGivesEmpty()
	{
		assertEquals(Collections.<String>emptyList(), names(ListFilter.apply(fixtureLines(), "zzz")));
	}
}
