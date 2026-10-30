package com.gpperxp;

import java.awt.Color;
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
 * Spending shows red with "-", earning green with "+" (lot Y). Fixture prices: 10 -> 100, 20 -> 508, 21 -> 1000, 22 -> 3000.
 * Player xp 1154 (level 10).
 */
public class RedGreenTest
{
	private static final Color GREEN = new Color(0, 200, 83);
	private static final Color RED = new Color(224, 98, 88);
	private static final Color GREY = Color.GRAY;

	private static final Map<Integer, Long> PRICES = prices();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);
	private static final List<ItemQty> NONE = Collections.emptyList();

	private static Map<Integer, Long> prices()
	{
		final Map<Integer, Long> map = new HashMap<>();
		map.put(10, 100L);
		map.put(20, 508L);
		map.put(21, 1000L);
		map.put(22, 3000L);
		return map;
	}

	private static List<ItemQty> one(int itemId)
	{
		return Collections.singletonList(new ItemQty(itemId, 1.0));
	}

	/** 4 xp and one 508 gp product per action, 11,417 actions an hour: +127.0 gp/xp, 45.7k xp/h, +5.8m gp/h. */
	private static TrainingMethod earner()
	{
		return new TrainingMethod("Test", "Earner", 5, 4.0, NONE, one(20), 11417, "src", null);
	}

	/** 43.5 xp and one 100 gp input per action, 1,340 actions an hour: -2.3 gp/xp, -134k gp/h. */
	private static TrainingMethod spender()
	{
		return new TrainingMethod("Test", "Spender", 5, 43.5, one(10), NONE, 1340, "src", null);
	}

	private static TrainingMethod free()
	{
		return new TrainingMethod("Test", "Free", 5, 10.0, NONE, NONE, 1000, "src", null);
	}

	private static TrainingMethod unstated()
	{
		return new TrainingMethod("Test", "Unstated", 5, 10.0, NONE, NONE, 1000, "src", null).withCostsNotStated(true);
	}

	/** Earns 1,000 gp per action, 10 xp, a wiki range of 100 to 150 actions an hour: +10k..15k gp/h. */
	private static TrainingMethod rangeEarner()
	{
		return new TrainingMethod("Test", "RangeEarner", 5, 10.0, NONE, one(21), 0, "src", null)
			.withV2(100, 150, null, 0, null, null);
	}

	private static TrainingMethod aboutEarner()
	{
		return new TrainingMethod("Test", "AboutEarner", 5, 10.0, NONE, one(21), 120, "src", null)
			.withV2(0, 0, "about", 0, null, null);
	}

	private static TrainingMethod upToEarner()
	{
		return new TrainingMethod("Test", "UpToEarner", 5, 10.0, NONE, one(21), 0, "src", null)
			.withV2(0, 200, null, 0, null, null);
	}

	private static TrainingMethod atLeastEarner()
	{
		return new TrainingMethod("Test", "AtLeastEarner", 5, 10.0, NONE, one(21), 0, "src", null)
			.withV2(100, 0, null, 0, null, null);
	}

	private static List<ListLine> build(TrainingMethod... methods)
	{
		return ListModel.build(Arrays.asList(methods), PRICE, false, Sort.CHEAPEST_GP_XP, 1154, true, true, false,
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
		return CardModel.of(Costs.row(m, PRICE, false), PRICE, false, 1154, 0);
	}

	private static String fact(CardModel model, String label)
	{
		for (String[] fact : model.facts)
		{
			if (fact[0].equals(label))
			{
				return fact[1];
			}
		}
		fail("no fact " + label);
		return null;
	}

	@Test
	public void earningRowGetsPlusAndEarnState()
	{
		final ListLine l = line(build(earner()), "Earner");
		assertEquals("+127.0", l.gpXp);
		assertEquals("+5.8m", l.gpH);
		assertEquals(Money.EARN, l.money);
	}

	@Test
	public void spendingRowKeepsMinusAndSpendState()
	{
		final ListLine l = line(build(spender()), "Spender");
		assertEquals("-2.3", l.gpXp);
		assertEquals("-134k", l.gpH);
		assertEquals(Money.SPEND, l.money);
	}

	@Test
	public void freeRowIsNeutral()
	{
		final ListLine l = line(build(free()), "Free");
		assertEquals("free", l.gpXp);
		assertEquals(Money.NEUTRAL, l.money);
	}

	@Test
	public void costNotStatedRowIsNeutral()
	{
		final ListLine l = line(build(unstated()), "Unstated");
		assertEquals("-", l.gpXp);
		assertEquals("-", l.gpH);
		assertEquals(Money.NEUTRAL, l.money);
	}

	@Test
	public void earningShapesGetPlusInTheList()
	{
		final List<ListLine> lines = build(rangeEarner(), aboutEarner(), upToEarner(), atLeastEarner());
		assertEquals("+100-150k", line(lines, "RangeEarner").gpH);
		assertEquals("~+120k", line(lines, "AboutEarner").gpH);
		assertEquals("<+200k", line(lines, "UpToEarner").gpH);
		assertEquals("+100k+", line(lines, "AtLeastEarner").gpH);
	}

	@Test
	public void earningShapesGetPlusOnTheCard()
	{
		assertEquals("+100k to +150k", fact(card(rangeEarner()), "GP per hour"));
		assertEquals("about +120k", fact(card(aboutEarner()), "GP per hour"));
		assertEquals("up to +200k", fact(card(upToEarner()), "GP per hour"));
		assertEquals("at least +100k", fact(card(atLeastEarner()), "GP per hour"));
	}

	@Test
	public void cardOfTheEarningRowIsGreen()
	{
		final CardModel model = card(earner());
		assertEquals("+127.0", fact(model, "GP per XP"));
		assertEquals("+5.8m", fact(model, "GP per hour"));
		assertEquals(GREEN, CardPainter.factColour("GP per XP", fact(model, "GP per XP")));
		assertEquals(GREEN, CardPainter.factColour("GP per hour", fact(model, "GP per hour")));
		assertEquals(Money.EARN, model.trip.money);
		assertEquals(GREEN, CardPainter.tripColour(model.trip));
	}

	@Test
	public void cardOfTheSpendingRowIsRed()
	{
		final CardModel model = card(spender());
		assertEquals("-2.3", fact(model, "GP per XP"));
		assertEquals("-134k", fact(model, "GP per hour"));
		assertEquals(RED, CardPainter.factColour("GP per XP", "-2.3"));
		assertEquals(RED, CardPainter.factColour("GP per hour", "-134k"));
		assertEquals(Money.SPEND, model.trip.money);
		assertEquals(RED, CardPainter.tripColour(model.trip));
	}

	@Test
	public void freeAndDashAreGreyAndOtherFactsStayWhite()
	{
		assertEquals(GREY, CardPainter.factColour("GP per XP", "free"));
		assertEquals(GREY, CardPainter.factColour("GP per hour", "-"));
		assertEquals(Color.WHITE, CardPainter.factColour("GP per XP", "not stated"));
		assertEquals(Color.WHITE, CardPainter.factColour("GP per HP", "12.0"));
		assertEquals(Color.WHITE, CardPainter.factColour("XP per hour", "-5k"));
	}

	@Test
	public void listColoursFollowTheState()
	{
		assertEquals(GREEN, ListRow.gpColour(Money.EARN, "+127.0"));
		assertEquals(RED, ListRow.gpColour(Money.SPEND, "-2.3"));
		assertEquals(GREY, ListRow.gpColour(Money.NEUTRAL, "free"));
		assertEquals(GREY, ListRow.gpColour(Money.NEUTRAL, "-"));
	}
}
