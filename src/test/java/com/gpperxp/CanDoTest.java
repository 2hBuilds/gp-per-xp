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
 * {@link Gates}, {@link Standing} and {@link Player} as the gates of a method see them, and the list's greying of a
 * gated row. Skill "Test", tax off. Fixture prices: 10 -> 100.
 */
public class CanDoTest
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

	/** A standing with Magic at the given level, and Dragon Slayer II done or not. */
	private static Standing standing(final int magic, final boolean dragonSlayerDone)
	{
		return new Standing()
		{
			@Override
			public int level(String skill)
			{
				return "Magic".equals(skill) ? magic : -1;
			}

			@Override
			public boolean questDone(String quest)
			{
				return !"Dragon Slayer II".equals(quest) || dragonSlayerDone;
			}
		};
	}

	private static Gate magic90()
	{
		return new Gate("Magic", 90, null, null, null);
	}

	private static Gate dragonSlayer()
	{
		return new Gate(null, 0, "Dragon Slayer II", null, null);
	}

	private static TrainingMethod gated(Gate... gates)
	{
		return new TrainingMethod("Test", "Gated", 5, 10.0, Collections.singletonList(new ItemQty(10, 1.0)), NONE,
			1000, "src", null).withV2(0, 0, null, 0, Arrays.asList(gates), null);
	}

	private static List<ListLine> build(List<TrainingMethod> methods, Standing standing, boolean showAbove)
	{
		return ListModel.build(methods, PRICE, false, Sort.CHEAPEST_GP_XP, 1154, true, showAbove, false, standing,
			false);
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
		throw new AssertionError("no line named " + name);
	}

	@Test
	public void magicGateMet()
	{
		assertEquals(Collections.emptyList(), Gates.missing(gated(magic90()), standing(90, true)));
	}

	@Test
	public void magicGateShort()
	{
		assertEquals(Collections.singletonList("Magic 90"), Gates.missing(gated(magic90()), standing(89, true)));
	}

	@Test
	public void questNotDone()
	{
		assertEquals(Collections.singletonList("Dragon Slayer II"),
			Gates.missing(gated(dragonSlayer()), standing(1, false)));
	}

	@Test
	public void bothUnmet()
	{
		assertEquals(Arrays.asList("Magic 90", "Dragon Slayer II"),
			Gates.missing(gated(magic90(), dragonSlayer()), standing(89, false)));
	}

	@Test
	public void diaryAndTextNeverMissing()
	{
		final Gate diary = new Gate(null, 0, null, "Ardougne hard", null);
		final Gate text = new Gate(null, 0, null, null, "Smiths' Uniform");
		assertEquals(Collections.emptyList(), Gates.missing(gated(diary, text), standing(1, false)));
	}

	@Test
	public void standingAllMeetsEveryGate()
	{
		assertEquals(Collections.emptyList(), Gates.missing(gated(magic90()), Standing.ALL));
		assertEquals(Collections.emptyList(), Gates.missing(gated(dragonSlayer()), Standing.ALL));
		assertEquals(Collections.emptyList(),
			Gates.missing(gated(magic90(), dragonSlayer()), Standing.ALL));
	}

	@Test
	public void playerStanding()
	{
		final Player player = name -> 1154;
		assertEquals(10, player.level("Magic"));
		assertTrue(player.questDone("Dragon Slayer II"));
		final Player unknown = name -> -1;
		assertEquals(-1, unknown.level("Magic"));
	}

	@Test
	public void magicGateGreysTheRow()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(gated(magic90()));
		final ListLine gated = line(build(methods, standing(50, true), true), "Gated");
		assertTrue(gated.greyed);
		assertEquals("Magic 90", gated.level);
	}

	@Test
	public void questGateGreysTheRow()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(gated(dragonSlayer()));
		final ListLine gated = line(build(methods, standing(1, false), true), "Gated");
		assertTrue(gated.greyed);
		assertEquals("Quest", gated.level);
	}

	@Test
	public void gatedRowHiddenWithoutShowAbove()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(gated(magic90()));
		assertTrue(build(methods, standing(50, true), false).isEmpty());
	}

	@Test
	public void diaryGateDoesNotGreyTheRow()
	{
		final List<TrainingMethod> methods = new ArrayList<>();
		methods.add(gated(new Gate(null, 0, null, "Ardougne hard", null)));
		final ListLine gated = line(build(methods, standing(1, false), true), "Gated");
		assertFalse(gated.greyed);
		assertEquals("Lv 5", gated.level);
	}
}
