package com.gpperxp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link Costs} and {@link Pace} with the wiki rate shapes. Skill "Test", tax off. Fixture prices: 10 -> 100, 11 -> 10,
 * 12 -> 500. Player xp 1154 (level 10); target xp 4470 (level 20), 3,316 xp to go.
 */
public class CostsRangeTest
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

	/** Ranger: level 5, 10 xp per action, a wiki range of 1000 to 1500 actions per hour. */
	private static TrainingMethod ranger()
	{
		return new TrainingMethod("Test", "Ranger", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 1500, null, 0, null, null);
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

	/** Runner: level 5, 500 xp per run, one Vial (10 gp) in and one Potion (500 gp) out per run. */
	private static TrainingMethod runner()
	{
		return new TrainingMethod("Test", "Runner", 5, 500.0, one(11), one(12), 0, "src", null)
			.withV2(0, 0, "perRun", 0, null, null);
	}

	@Test
	public void kinds()
	{
		assertEquals(RateKind.STATED, Costs.kind(stated()));
		assertEquals(RateKind.RANGE, Costs.kind(ranger()));
		assertEquals(RateKind.UPTO, Costs.kind(upper()));
		assertEquals(RateKind.ABOUT, Costs.kind(abouter()));
		assertEquals(RateKind.PER_RUN, Costs.kind(runner()));
		final TrainingMethod bare = new TrainingMethod("Test", "Bare", 5, 10.0, one(10), NONE, 0, "src", null);
		assertEquals(RateKind.NONE, Costs.kind(bare));
		final TrainingMethod noneKind = bare.withV2(0, 0, "none", 0, null, null);
		assertEquals(RateKind.NONE, Costs.kind(noneKind));
	}

	@Test
	public void ratedKinds()
	{
		assertTrue(Costs.rated(stated()));
		assertTrue(Costs.rated(ranger()));
		assertTrue(Costs.rated(abouter()));
		assertTrue(Costs.rated(upper()));
		assertFalse(Costs.rated(runner()));
	}

	@Test
	public void rangeRates()
	{
		final TrainingMethod r = ranger();
		assertEquals(10000.0, Costs.xpPerHour(r), 1e-9);
		assertEquals(15000.0, Costs.xpPerHourHigh(r), 1e-9);
		assertEquals(100000.0, Costs.gpPerHour(r, PRICE, false), 1e-9);
		assertEquals(150000.0, Costs.gpPerHourHigh(r, PRICE, false), 1e-9);
	}

	@Test
	public void upToRates()
	{
		final TrainingMethod u = upper();
		assertEquals(0.0, Costs.xpPerHour(u), 1e-9);
		assertEquals(20000.0, Costs.xpPerHourHigh(u), 1e-9);
	}

	@Test
	public void statedHighIsThePoint()
	{
		assertEquals(10000.0, Costs.xpPerHourHigh(stated()), 1e-9);
	}

	@Test
	public void rangeTrip()
	{
		final Trip t = Costs.trip(ranger(), PRICE, false, 1154, 4470);
		assertEquals(332, t.actions);
		assertEquals(332 / 1500.0, t.hours, 1e-9);
		assertEquals(332 / 1000.0, t.hoursSlow, 1e-9);
	}

	@Test
	public void upToTrip()
	{
		final Trip t = Costs.trip(upper(), PRICE, false, 1154, 4470);
		assertEquals(332 / 2000.0, t.hours, 1e-9);
		assertEquals(0.0, t.hoursSlow, 1e-9);
	}

	@Test
	public void statedTripHasOneTime()
	{
		final Trip t = Costs.trip(stated(), PRICE, false, 1154, 4470);
		assertEquals(332 / 1000.0, t.hours, 1e-9);
		assertEquals(332 / 1000.0, t.hoursSlow, 1e-9);
	}

	@Test
	public void perRunTrip()
	{
		final Trip t = Costs.trip(runner(), PRICE, false, 1154, 4470);
		assertEquals(7, t.actions);
		assertEquals(-3430.0, t.gp, 1e-9);
		assertEquals(0.0, t.hours, 1e-9);
	}

	@Test
	public void paceScalesEveryRateShape()
	{
		assertEquals(800.0, Pace.apply(ranger(), 80).rateLow, 1e-9);
		assertEquals(1200.0, Pace.apply(ranger(), 80).rateHigh, 1e-9);
		assertEquals(1000.0, Pace.apply(upper(), 50).rateHigh, 1e-9);
	}

	@Test
	public void paceLeavesAZeroRowAlone()
	{
		final TrainingMethod r = runner();
		assertSame(r, Pace.apply(r, 50));
	}

	@Test
	public void paceKeepsRequiresRateKindLevelAndNote()
	{
		final List<Gate> requires = Arrays.asList(
			new Gate("Magic", 90, null, null, null),
			new Gate(null, 0, "Dragon Slayer II", null, null));
		final TrainingMethod full = new TrainingMethod("Test", "Full", 5, 10.0, one(10), NONE, 0, "src", "note")
			.withV2(1000, 1500, "range", 70, requires, "supply crates");
		final TrainingMethod paced = Pace.apply(full, 80);
		assertEquals(requires, paced.requires);
		assertEquals("range", paced.rateKind);
		assertEquals(70, paced.rateLevel);
		assertEquals("supply crates", paced.outputsNote);
	}
}
