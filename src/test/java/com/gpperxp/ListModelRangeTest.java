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
 * {@link ListModel} with the wiki rate shapes and the level bands, at player xp 1154 (level 10) unless stated. Fixture
 * prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class ListModelRangeTest
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

	/** Course: level 1, 500 xp per action, no ingredients and no products, a wiki range of 5 to 6 actions per hour. */
	private static TrainingMethod course()
	{
		return new TrainingMethod("Test", "Course", 1, 500.0, NONE, NONE, 0, "src", null)
			.withV2(5, 6, null, 0, null, null);
	}

	/** Iron: one band of the level 15 method, 35 xp per action, one Herb (100 gp) per action. */
	private static TrainingMethod iron(double actionsPerHour, int rateLevel)
	{
		return new TrainingMethod("Test", "Iron", 15, 35.0, one(10), NONE, actionsPerHour, "src", null)
			.withV2(0, 0, null, rateLevel, null, null);
	}

	/** The fixture methods in the order Upper, Runner, Stated, Abouter, Ranger. */
	private static List<TrainingMethod> fixtureMethods()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(upper());
		methods.add(runner());
		methods.add(stated());
		methods.add(abouter());
		methods.add(ranger());
		return methods;
	}

	private static List<ListLine> build(List<TrainingMethod> methods)
	{
		return ListModel.build(methods, PRICE, false, Sort.CHEAPEST_GP_XP, LEVEL_TEN_XP, true, true, false,
			Standing.ALL, false);
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
	public void rangeRowTexts()
	{
		final ListLine ranger = line(build(fixtureMethods()), "Ranger");
		assertEquals("-10.0", ranger.gpXp);
		assertEquals("10-15k", ranger.xpH);
		assertEquals("-100-150k", ranger.gpH);
		assertNotEquals(Money.EARN, ranger.money);
	}

	@Test
	public void upToRowTexts()
	{
		final ListLine upper = line(build(fixtureMethods()), "Upper");
		assertEquals("<20k", upper.xpH);
		assertEquals("<-200k", upper.gpH);
	}

	@Test
	public void aboutRowTexts()
	{
		final ListLine abouter = line(build(fixtureMethods()), "Abouter");
		assertEquals("~12k", abouter.xpH);
		assertEquals("~-120k", abouter.gpH);
	}

	@Test
	public void perRunRowTexts()
	{
		final ListLine runner = line(build(fixtureMethods()), "Runner");
		assertEquals("+1.0", runner.gpXp);
		assertEquals("per run", runner.xpH);
		assertEquals("per run", runner.gpH);
		assertEquals(Money.EARN, runner.money);
	}

	@Test
	public void statedRowTexts()
	{
		final ListLine stated = line(build(fixtureMethods()), "Stated");
		assertEquals("10k", stated.xpH);
		assertEquals("-100k", stated.gpH);
	}

	@Test
	public void freeRangeRowTexts()
	{
		final List<TrainingMethod> methods = fixtureMethods();
		methods.add(course());
		final ListLine course = line(build(methods), "Course");
		assertEquals("free", course.gpXp);
		assertEquals("2.5-3k", course.xpH);
		assertEquals("free", course.gpH);
	}

	@Test
	public void levelBandsShowOneBand()
	{
		final List<TrainingMethod> bands = Arrays.asList(iron(1000, 15), iron(2000, 70));
		final List<ListLine> at50 = ListModel.build(bands, PRICE, false, Sort.CHEAPEST_GP_XP, 101333, true, true,
			false, Standing.ALL, false);
		assertEquals(1, at50.size());
		assertEquals("Iron (rate at 15)", at50.get(0).name);

		final List<ListLine> at75 = ListModel.build(bands, PRICE, false, Sort.CHEAPEST_GP_XP, 1210421, true, true,
			false, Standing.ALL, false);
		assertEquals(1, at75.size());
		assertEquals("Iron (rate at 70)", at75.get(0).name);

		final List<ListLine> at10 = ListModel.build(bands, PRICE, false, Sort.CHEAPEST_GP_XP, LEVEL_TEN_XP, true,
			true, false, Standing.ALL, false);
		assertEquals(1, at10.size());
		assertEquals("Iron (rate at 15)", at10.get(0).name);
		assertTrue(at10.get(0).greyed);

		final List<ListLine> unknown = ListModel.build(bands, PRICE, false, Sort.CHEAPEST_GP_XP, -1, true, true,
			false, Standing.ALL, false);
		assertEquals(1, unknown.size());
		assertEquals("Iron (rate at 70)", unknown.get(0).name);
	}

	@Test
	public void otherBandsShowsEveryBand()
	{
		final List<TrainingMethod> bands = Arrays.asList(iron(1000, 15), iron(2000, 70));
		final List<ListLine> lines = ListModel.build(bands, PRICE, false, Sort.CHEAPEST_GP_XP, 101333, true, true,
			false, Standing.ALL, true);
		assertEquals(2, lines.size());
		line(lines, "Iron (rate at 15)");
		line(lines, "Iron (rate at 70)");
	}
}
