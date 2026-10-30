package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * {@link ListModel} with the "at least" rate shape and the xp-per-hour-only rows, at player xp 1154 (level 10). Fixture
 * prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class ListModelAtLeastTest
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

	/** Floor: level 5, 10 xp per action, "at least" 1000 actions per hour, one Herb (100 gp) per action. */
	private static TrainingMethod floor()
	{
		return new TrainingMethod("Test", "Floor", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 0, null, 0, null, null);
	}

	/** Hourly: level 5, 1 xp per action, no items, a wiki range of 45,000 to 55,000 xp per hour, rate unit "xp". */
	private static TrainingMethod hourly()
	{
		return new TrainingMethod("Test", "Hourly", 5, 1.0, NONE, NONE, 0, "src", null)
			.withV2(45000, 55000, null, 0, null, null)
			.withRateUnit("xp");
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
	public void atLeastAndXpOnlyTexts()
	{
		final List<ListLine> lines = ListModel.build(Arrays.asList(floor(), hourly()), PRICE, false,
			Sort.CHEAPEST_GP_XP, LEVEL_TEN_XP, true, true, false, Standing.ALL, false);

		final ListLine floorLine = line(lines, "Floor");
		assertEquals("-10.0", floorLine.gpXp);
		assertEquals("10k+", floorLine.xpH);
		assertEquals("-100k+", floorLine.gpH);

		final ListLine hourlyLine = line(lines, "Hourly");
		assertEquals("free", hourlyLine.gpXp);
		assertEquals("45-55k", hourlyLine.xpH);
		assertEquals("free", hourlyLine.gpH);
	}
}
