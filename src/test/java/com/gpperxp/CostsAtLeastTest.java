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
import static org.junit.Assert.assertTrue;

/**
 * {@link Costs}, {@link Pace} and {@link Sort} with the "at least" rate shape and the xp-per-hour-only rows. Skill
 * "Test", tax off. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500. Player xp 1154 (level 10); target xp 4470 (level
 * 20), 3,316 xp to go.
 */
public class CostsAtLeastTest
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

	/** Stated: level 5, 10 xp per action, 1000 actions per hour, one Herb (100 gp) per action. */
	private static TrainingMethod stated()
	{
		return new TrainingMethod("Test", "Stated", 5, 10.0, one(10), NONE, 1000, "src", null);
	}

	/** Upper: level 5, 10 xp per action, "up to 2000" actions per hour. */
	private static TrainingMethod upper()
	{
		return new TrainingMethod("Test", "Upper", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(0, 2000, null, 0, null, null);
	}

	/** Abouter: level 5, 10 xp per action, "about" 1200 actions per hour. */
	private static TrainingMethod abouter()
	{
		return new TrainingMethod("Test", "Abouter", 5, 10.0, one(10), NONE, 1200, "src", null)
			.withV2(0, 0, "about", 0, null, null);
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

	@Test
	public void kindAndRated()
	{
		assertEquals(RateKind.AT_LEAST, Costs.kind(floor()));
		assertTrue(Costs.rated(floor()));
	}

	@Test
	public void atLeastRates()
	{
		final TrainingMethod f = floor();
		assertEquals(10000.0, Costs.xpPerHour(f), 1e-9);
		assertEquals(0.0, Costs.xpPerHourHigh(f), 1e-9);
		assertEquals(100000.0, Costs.gpPerHour(f, PRICE, false), 1e-9);
	}

	@Test
	public void atLeastTrip()
	{
		final Trip t = Costs.trip(floor(), PRICE, false, 1154, 4470);
		assertEquals(332, t.actions);
		assertEquals(0.0, t.hours, 1e-9);
		assertEquals(332 / 1000.0, t.hoursSlow, 1e-9);
	}

	@Test
	public void atLeastPace()
	{
		assertEquals(500.0, Pace.apply(floor(), 50).rateLow, 1e-9);
	}

	@Test
	public void xpOnlyRowIsPerXpAndPaces()
	{
		final TrainingMethod h = hourly();
		final TrainingMethod paced = Pace.apply(h, 80);
		assertEquals(36000.0, paced.rateLow, 1e-9);
		assertEquals(44000.0, paced.rateHigh, 1e-9);
		assertEquals("xp", paced.rateUnit);
		assertTrue(Costs.perXp(h));
		assertFalse(Costs.perXp(floor()));
	}

	@Test
	public void copiesKeepRateUnit()
	{
		final TrainingMethod h = hourly();
		assertEquals("xp", h.withV2(45000, 55000, null, 0, null, null).rateUnit);
		assertEquals("xp", h.withRates(0, 45000, 55000).rateUnit);
	}

	@Test
	public void sortFloorBetweenAboutAndStated()
	{
		final List<Row> rows = new ArrayList<>();
		rows.add(Costs.row(stated(), PRICE, false));
		rows.add(Costs.row(floor(), PRICE, false));
		rows.add(Costs.row(abouter(), PRICE, false));
		rows.add(Costs.row(upper(), PRICE, false));
		final List<Row> sorted = Sort.arrange(rows, Sort.FASTEST_XP_H, Sort.atLevel(10), true, true, false);
		final List<String> names = new ArrayList<>();
		for (Row row : sorted)
		{
			names.add(row.method.name);
		}
		assertEquals(Arrays.asList("Upper", "Abouter", "Floor", "Stated"), names);
	}
}
