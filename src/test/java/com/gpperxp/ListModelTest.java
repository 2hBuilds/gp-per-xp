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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link ListModel}: the sidebar lines for skill "Test" at the SortTest fixtures. Player xp 1154 is level 10 unless
 * stated. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500, 13 -> 49, 14 -> 400,000,000; 99 is unpriced.
 */
public class ListModelTest
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

	private static List<ListLine> build(int playerXp, Sort sort, boolean includeProfit, boolean showAbove)
	{
		return ListModel.build(fixtureMethods(), PRICE, false, sort, playerXp, includeProfit, showAbove, false, Standing.ALL, true);
	}

	private static List<ListLine> defaultLines()
	{
		return build(LEVEL_TEN_XP, Sort.CHEAPEST_GP_XP, true, true);
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

	@Test
	public void orderAndNames()
	{
		assertEquals(Arrays.asList("Delta", "Beta", "Alpha", "Gamma", "Zeta", "Theta", "Epsilon"),
			names(defaultLines()));
	}

	@Test
	public void deltaTexts()
	{
		final ListLine delta = line(defaultLines(), "Delta");
		assertEquals("Lv 3", delta.level);
		assertEquals("+49.0", delta.gpXp);
		assertEquals("5k", delta.xpH);
		assertEquals("+245k", delta.gpH);
		assertEquals(Money.EARN, delta.money);
		assertFalse(delta.greyed);
		assertFalse(delta.unpriced);
	}

	@Test
	public void betaTexts()
	{
		final ListLine beta = line(defaultLines(), "Beta");
		assertEquals("Lv 5", beta.level);
		assertEquals("-2.0", beta.gpXp);
		assertEquals("100k", beta.xpH);
		assertEquals("-200k", beta.gpH);
		assertNotEquals(Money.EARN, beta.money);
		assertFalse(beta.greyed);
	}

	@Test
	public void zetaUnpriced()
	{
		final ListLine zeta = line(defaultLines(), "Zeta");
		assertEquals("?", zeta.gpXp);
		assertEquals("?", zeta.xpH);
		assertEquals("?", zeta.gpH);
		assertTrue(zeta.unpriced);
		assertNotEquals(Money.EARN, zeta.money);
		assertFalse(zeta.greyed);
	}

	@Test
	public void aboveIsGrey()
	{
		final List<ListLine> lines = defaultLines();
		final ListLine theta = line(lines, "Theta");
		assertEquals("Lv 30", theta.level);
		assertTrue(theta.greyed);
		assertTrue(line(lines, "Epsilon").greyed);
		assertFalse(line(lines, "Gamma").greyed);
	}

	@Test
	public void unknownXpGreysNothing()
	{
		final List<ListLine> lines = build(-1, Sort.CHEAPEST_GP_XP, true, true);
		for (ListLine candidate : lines)
		{
			assertFalse(candidate.name, candidate.greyed);
		}
		assertEquals(Arrays.asList("Delta", "Epsilon", "Beta", "Alpha", "Theta", "Gamma", "Zeta"), names(lines));
	}

	@Test
	public void filtersPass()
	{
		assertEquals(Arrays.asList("Beta", "Alpha", "Gamma", "Zeta", "Theta", "Epsilon"),
			names(build(LEVEL_TEN_XP, Sort.CHEAPEST_GP_XP, false, true)));
		assertEquals(Arrays.asList("Delta", "Beta", "Alpha", "Gamma", "Zeta"),
			names(build(LEVEL_TEN_XP, Sort.CHEAPEST_GP_XP, true, false)));
	}

	@Test
	public void sortsPass()
	{
		assertEquals(Arrays.asList("Beta", "Gamma", "Alpha", "Delta", "Zeta", "Theta", "Epsilon"),
			names(build(LEVEL_TEN_XP, Sort.FASTEST_XP_H, true, true)));
	}
}
