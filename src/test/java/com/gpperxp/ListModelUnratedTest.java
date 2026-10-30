package com.gpperxp;

import java.util.ArrayList;
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
 * {@link ListModel}: the sidebar lines for Kappa (no stated hourly rate), for a free method (no ingredients and no
 * products) and for the rated Beta and Delta, at player xp 1154 (level 10). Fixture prices: 10 -> 100, 11 -> 10,
 * 12 -> 500, 99 -> 0; 99 is unpriced.
 */
public class ListModelUnratedTest
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

	private static TrainingMethod method(String name, int level, double xpPerAction, int actionsPerHour,
		List<ItemQty> inputs, List<ItemQty> outputs)
	{
		return new TrainingMethod("Test", name, level, xpPerAction, inputs, outputs, actionsPerHour, "src", null);
	}

	/**
	 * The eight fixture methods, in the order the plan passes them: Epsilon, Zeta, Alpha, Kappa, Theta, Gamma, Delta,
	 * Beta.
	 */
	private static List<TrainingMethod> fixtureMethods()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(method("Epsilon", 40, 100.0, 1000, one(10), NONE));
		methods.add(method("Zeta", 1, 10.0, 1000, one(99), NONE));
		methods.add(method("Alpha", 5, 10.0, 1000, one(10), NONE));
		methods.add(method("Kappa", 5, 10.0, 0, one(10), NONE));
		methods.add(method("Theta", 30, 10.0, 100, one(10), NONE));
		methods.add(method("Gamma", 8, 20.0, 1000, one(12), NONE));
		methods.add(method("Delta", 3, 10.0, 500, one(11), one(12)));
		methods.add(method("Beta", 5, 50.0, 2000, one(10), NONE));
		return methods;
	}

	/** Free lap: skill Test, level 1, 20 xp per action, no ingredients and no products, the given actions per hour. */
	private static TrainingMethod freeLap(int actionsPerHour)
	{
		return new TrainingMethod("Test", "Free lap", 1, 20.0, NONE, NONE, actionsPerHour, "src", "free");
	}

	private static List<ListLine> build(List<TrainingMethod> methods, Sort sort)
	{
		return ListModel.build(methods, PRICE, false, sort, LEVEL_TEN_XP, true, true, false, Standing.ALL, true);
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
	public void unratedTexts()
	{
		final ListLine kappa = line(build(fixtureMethods(), Sort.CHEAPEST_GP_XP), "Kappa");
		assertEquals("Lv 5", kappa.level);
		assertEquals("-10.0", kappa.gpXp);
		assertEquals("-", kappa.xpH);
		assertEquals("-", kappa.gpH);
		assertNotEquals(Money.EARN, kappa.money);
		assertFalse(kappa.greyed);
		assertFalse(kappa.unpriced);
	}

	@Test
	public void freeRowTexts()
	{
		final List<TrainingMethod> methods = fixtureMethods();
		methods.add(freeLap(3000));
		final ListLine free = line(build(methods, Sort.CHEAPEST_GP_XP), "Free lap");
		assertEquals("free", free.gpXp);
		assertEquals("60k", free.xpH);
		assertEquals("free", free.gpH);
		assertNotEquals(Money.EARN, free.money);
		assertFalse(free.unpriced);
	}

	@Test
	public void freeUnratedRow()
	{
		final List<TrainingMethod> methods = fixtureMethods();
		methods.add(freeLap(0));
		final ListLine free = line(build(methods, Sort.CHEAPEST_GP_XP), "Free lap");
		assertEquals("free", free.gpXp);
		assertEquals("-", free.xpH);
		assertEquals("free", free.gpH);
		assertNotEquals(Money.EARN, free.money);
		assertFalse(free.unpriced);
	}

	@Test
	public void ratedRowsUnchanged()
	{
		final List<ListLine> lines = build(fixtureMethods(), Sort.CHEAPEST_GP_XP);
		final ListLine beta = line(lines, "Beta");
		assertEquals("-2.0", beta.gpXp);
		assertEquals("100k", beta.xpH);
		assertEquals("-200k", beta.gpH);
		assertNotEquals(Money.EARN, beta.money);
		final ListLine delta = line(lines, "Delta");
		assertEquals("+49.0", delta.gpXp);
		assertEquals("5k", delta.xpH);
		assertEquals("+245k", delta.gpH);
		assertEquals(Money.EARN, delta.money);
	}
}
