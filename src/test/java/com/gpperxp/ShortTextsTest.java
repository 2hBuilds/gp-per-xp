package com.gpperxp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The short rate texts of the list and the long rate texts of the card, at player xp 1154 (level 10) and target level 20
 * (4,470 xp). Fixture prices: 10 -> 100, 11 -> 10, 12 -> 500.
 */
public class ShortTextsTest
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

	/** Floor: level 5, 10 xp per action, "at least" 1000 actions per hour, one Herb (100 gp) per action. */
	private static TrainingMethod floor()
	{
		return new TrainingMethod("Test", "Floor", 5, 10.0, one(10), NONE, 0, "src", null)
			.withV2(1000, 0, null, 0, null, null);
	}

	/** Course: level 1, 500 xp per action, no ingredients and no products, a wiki range of 5 to 6 actions per hour. */
	private static TrainingMethod course()
	{
		return new TrainingMethod("Test", "Course", 1, 500.0, NONE, NONE, 0, "src", null)
			.withV2(5, 6, null, 0, null, null);
	}

	/** The fixture methods in the order Upper, Runner, Stated, Abouter, Ranger, Floor, Course. */
	private static List<TrainingMethod> fixtureMethods()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(upper());
		methods.add(runner());
		methods.add(stated());
		methods.add(abouter());
		methods.add(ranger());
		methods.add(floor());
		methods.add(course());
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

	private static CardModel card(TrainingMethod m)
	{
		return CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 20);
	}

	private static void assertFact(CardModel model, int index, String label, String value)
	{
		assertEquals(label, model.facts.get(index)[0]);
		assertEquals(value, model.facts.get(index)[1]);
	}

	/** T1: the short range form of Fmt.shortRange. */
	@Test
	public void shortRangeTexts()
	{
		assertEquals("10-15k", Fmt.shortRange(10000, 15000));
		assertEquals("10-15k", Fmt.shortRange(15000, 10000));
		assertEquals("-100-150k", Fmt.shortRange(-100000, -150000));
		assertEquals("2.5-3k", Fmt.shortRange(2500, 3000));
		assertEquals("1-1.5m", Fmt.shortRange(1000000, 1500000));
		assertEquals("800-1.2k", Fmt.shortRange(800, 1200));
		assertEquals("500k-1.2m", Fmt.shortRange(500000, 1200000));
		assertEquals("50k", Fmt.shortRange(50000, 50000));
	}

	/** T2: the short list texts of every rate shape. */
	@Test
	public void shortListTexts()
	{
		final List<ListLine> lines = build(fixtureMethods());

		final ListLine ranger = line(lines, "Ranger");
		assertEquals("10-15k", ranger.xpH);
		assertEquals("-100-150k", ranger.gpH);

		final ListLine upper = line(lines, "Upper");
		assertEquals("<20k", upper.xpH);
		assertEquals("<-200k", upper.gpH);

		final ListLine abouter = line(lines, "Abouter");
		assertEquals("~12k", abouter.xpH);
		assertEquals("~-120k", abouter.gpH);

		final ListLine floorLine = line(lines, "Floor");
		assertEquals("10k+", floorLine.xpH);
		assertEquals("-100k+", floorLine.gpH);

		final ListLine runner = line(lines, "Runner");
		assertEquals("per run", runner.xpH);
		assertEquals("per run", runner.gpH);

		final ListLine stated = line(lines, "Stated");
		assertEquals("10k", stated.xpH);
		assertEquals("-100k", stated.gpH);

		final ListLine course = line(lines, "Course");
		assertEquals("2.5-3k", course.xpH);
		assertEquals("free", course.gpH);
	}

	/** T3: the long card texts are unchanged by the list change. */
	@Test
	public void longCardTexts()
	{
		final CardModel rangeCard = card(ranger());
		assertFact(rangeCard, 2, "XP per hour", "10k-15k");
		assertFact(rangeCard, 4, "GP per hour", "-100k to -150k");

		final CardModel aboutCard = card(abouter());
		assertFact(aboutCard, 2, "XP per hour", "about 12k");
	}
}
