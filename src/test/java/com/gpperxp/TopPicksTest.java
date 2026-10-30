package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
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
 * {@link TopPicks} over the E1/E2 fixture rows, the Gather, Course and Gated rows, and the recommended flag. Skill
 * "Test", tax off, player xp 1154 (level 10) unless stated. Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class TopPicksTest
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

	/** A standing with Magic at the given level and every quest done. */
	private static Standing magic(final int level)
	{
		return new Standing()
		{
			@Override
			public int level(String skill)
			{
				return "Magic".equals(skill) ? level : -1;
			}

			@Override
			public boolean questDone(String quest)
			{
				return true;
			}
		};
	}

	private static Gate magic90()
	{
		return new Gate("Magic", 90, null, null, null);
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
		return new TrainingMethod("Test", "Stated", 5, 10.0, one(10), NONE, 1000, "src", null)
			.withRecommended(true);
	}

	private static TrainingMethod abouter()
	{
		return new TrainingMethod("Test", "Abouter", 5, 10.0, one(10), NONE, 1200, "src", null)
			.withV2(0, 0, "about", 0, null, null)
			.withRecommended(true);
	}

	private static TrainingMethod ranger()
	{
		return new TrainingMethod("Test", "Ranger", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 1500, null, 0, null, null);
	}

	/** Floor: level 5, 10 xp per action, "at least" 1000 actions per hour, one Herb (100 gp) per action. */
	private static TrainingMethod floor()
	{
		return new TrainingMethod("Test", "Floor", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 0, null, 0, null, null);
	}

	/** Gather: level 5, 10 xp per action, 2000 actions per hour, one Rune (500 gp) out per action, nothing in. */
	private static TrainingMethod gather()
	{
		return new TrainingMethod("Test", "Gather", 5, 10.0, NONE, one(12), 2000, "src", null);
	}

	/** Course: level 1, 500 xp per action, no ingredients and no products, a wiki range of 5 to 6 actions per hour. */
	private static TrainingMethod course()
	{
		return new TrainingMethod("Test", "Course", 1, 500.0, NONE, NONE, 0, "src", null)
			.withV2(5, 6, null, 0, null, null);
	}

	/** Gated: level 5, 100 xp per action, 1000 actions per hour, one Herb per action, needs Magic 90, recommended. */
	private static TrainingMethod gated()
	{
		return new TrainingMethod("Test", "Gated", 5, 100.0, one(10), NONE, 1000, "src", null)
			.withV2(0, 0, null, 0, Arrays.asList(magic90()), null)
			.withRecommended(true);
	}

	/** The fixture methods: Upper, Runner, Stated, Abouter, Ranger, Floor, Gather, Course, Gated. */
	private static List<TrainingMethod> fixtureMethods()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(upper());
		methods.add(runner());
		methods.add(stated());
		methods.add(abouter());
		methods.add(ranger());
		methods.add(floor());
		methods.add(gather());
		methods.add(course());
		methods.add(gated());
		return methods;
	}

	private static TopPicks picks(Standing standing, boolean includeProfit)
	{
		return TopPicks.build(fixtureMethods(), PRICE, false, LEVEL_TEN_XP, standing, includeProfit);
	}

	private static List<String> names(List<ListLine> lines)
	{
		final List<String> out = new ArrayList<>();
		for (ListLine line : lines)
		{
			out.add(line.row.method.name);
		}
		return out;
	}

	@Test
	public void fastestIsHighestXpPerHourByName()
	{
		final TopPicks top = picks(magic(50), true);
		assertEquals(Arrays.asList("Gather", "Upper", "Abouter"), names(top.fastest));
		assertEquals("20k", top.fastest.get(0).xpH);
	}

	@Test
	public void bestGpIsLeastCostPerHour()
	{
		final TopPicks top = picks(magic(50), true);
		assertEquals(Arrays.asList("Gather", "Course", "Floor"), names(top.bestGp));
	}

	@Test
	public void cheapestIsLowestGpPerXp()
	{
		final TopPicks top = picks(magic(50), true);
		assertEquals(Arrays.asList("Gather", "Runner", "Course"), names(top.cheapest));
	}

	@Test
	public void suggestedIsRecommendedAndDoable()
	{
		final TopPicks top = picks(magic(50), true);
		assertEquals(Arrays.asList("Abouter", "Stated"), names(top.suggested));
	}

	@Test
	public void noLevelStandingAllShowsEveryLevel()
	{
		final TopPicks top = TopPicks.build(fixtureMethods(), PRICE, false, -1, Standing.ALL, true);
		assertEquals(Arrays.asList("Gated", "Gather", "Upper"), names(top.fastest));
		assertEquals(Arrays.asList("Abouter", "Gated", "Stated"), names(top.suggested));
	}

	@Test
	public void profitRowsDroppedWithoutIncludeProfit()
	{
		final TopPicks top = picks(magic(50), false);
		assertEquals(Arrays.asList("Upper", "Abouter", "Floor"), names(top.fastest));
		assertEquals(Arrays.asList("Course", "Abouter", "Floor"), names(top.cheapest));
	}

	@Test
	public void everyListHasAtMostThreeLinesAndRecommendedIsRead()
	{
		final TopPicks top = picks(magic(50), true);
		assertTrue(top.fastest.size() <= 3);
		assertTrue(top.bestGp.size() <= 3);
		assertTrue(top.cheapest.size() <= 3);
		assertTrue(top.suggested.size() <= 3);

		final String json = "{\"updated\":\"2026-10-30\",\"methods\":[{"
			+ "\"skill\":\"Test\",\"name\":\"Marked row\",\"level\":5,\"xpPerAction\":10.0,"
			+ "\"inputs\":[],\"outputs\":[],\"actionsPerHour\":1000,\"source\":\"https://oldschool.runescape.wiki/x\","
			+ "\"note\":null,\"recommended\":true}]}";
		final TrainingMethod marked = Methods.load(new Gson(), new StringReader(json)).all().get(0);
		assertTrue(marked.recommended);
	}
}
