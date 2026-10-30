package com.gpperxp;

import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Lot U: billions, grouped decimals, card ranges, "1 action", empty sections and two-line names. */
public class PolishTest
{
	private static final Map<Integer, Long> PRICES = new HashMap<>();
	private static final IntToLongFunction PRICE = id -> PRICES.getOrDefault(id, 0L);
	private static final List<ItemQty> NONE = Collections.emptyList();

	static
	{
		PRICES.put(10, 100_000L);
		PRICES.put(11, 10L);
		PRICES.put(12, 500L);
		PRICES.put(13, 100L);
	}

	private static List<ItemQty> one(int itemId)
	{
		return Collections.singletonList(new ItemQty(itemId, 1.0));
	}

	/** Ranger: 10 xp and one item per action, 1,000-1,500 actions per hour. */
	private static TrainingMethod ranger(int item)
	{
		return new TrainingMethod("Test", "Ranger", 5, 10.0, one(item), NONE, 0, "src", null)
			.withV2(1000, 1500, null, 0, null, null);
	}

	private static TrainingMethod runner()
	{
		return new TrainingMethod("Test", "Runner", 5, 500.0, one(11), one(12), 0, "src", null)
			.withV2(0, 0, "perRun", 0, null, null);
	}

	private static TrainingMethod hourly(List<ItemQty> inputs)
	{
		return new TrainingMethod("Test", "Hourly", 5, 1.0, inputs, NONE, 0, "src", null)
			.withV2(45000, 55000, null, 0, null, null)
			.withRateUnit("xp");
	}

	private static CardModel card(TrainingMethod m, int currentXp)
	{
		return CardModel.of(Costs.row(m, PRICE, false), PRICE, false, currentXp, 20);
	}

	@Test
	public void t1Billions()
	{
		assertEquals("1b", Fmt.rate(999_950_000));
		assertEquals("1.3b", Fmt.rate(1_300_000_000));
		assertEquals("-39.4b", Fmt.rate(-39_437_100_000L));
		assertEquals("999m", Fmt.rate(999_000_000));
	}

	@Test
	public void t2GroupedDecimals()
	{
		assertEquals("1,020.8", Fmt.qty(1020.8));
		assertEquals("12,345.25", Fmt.qty(12345.25));
		assertEquals("1,000", Fmt.qty(1000));
		assertEquals("1,000-1,020.8", Fmt.qtyRange(1000, 1020.8));
	}

	@Test
	public void t3NegativeCardRange()
	{
		final CardModel big = card(ranger(10), 1154);
		assertEquals("GP per hour", big.facts.get(4)[0]);
		assertEquals("-100m to -150m", big.facts.get(4)[1]);
		assertEquals("-100k to -150k", card(ranger(13), 1154).facts.get(4)[1]);
		assertEquals("-100k to -150k", Fmt.range(-100000, -150000));
		assertEquals("45k-55k", Fmt.range(45000, 55000));
	}

	@Test
	public void t4OneAction()
	{
		assertEquals("1 action", card(ranger(10), 4460).trip.actions);
		assertEquals("1 run", card(runner(), 4070).trip.actions);
		assertEquals("7 runs", card(runner(), 1154).trip.actions);
	}

	@Test
	public void t5NoEmptySections()
	{
		final CardModel free = card(hourly(NONE), 1154);
		assertTrue(free.uses.isEmpty());
		assertTrue(free.makes.isEmpty());
		final CardModel withItem = card(hourly(one(10)), 1154);
		final int bare = CardPainter.paint(free, id -> null).getHeight();
		final int full = CardPainter.paint(withItem, id -> null).getHeight();
		assertTrue("the heading and the item are not drawn: " + bare + " vs " + full, full - bare >= 30);
	}

	private static ListLine line(String name)
	{
		return new ListLine(null, name, "Lv 60", "1.5", "50k", "-75k", Money.SPEND, false, false);
	}

	@Test
	public void t6TwoLineNames()
	{
		final String longName = "Thieving the Ardougne knights with extra coins";
		final String name60 = (longName + " and a very long ending").substring(0, 60);
		final ListRow shortRow = new ListRow(line("Short one"));
		final ListRow longRow = new ListRow(line(name60));
		shortRow.setSize(225, 40);
		longRow.setSize(225, 40);
		assertTrue(longRow.getPreferredSize().height > shortRow.getPreferredSize().height);

		final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final FontMetrics fm = image.createGraphics().getFontMetrics(longRow.getFont());
		final List<String> parts = ListRow.splitName(name60, fm, 225 - 12 - fm.stringWidth("Lv 60") - 6, 225 - 12);
		assertEquals(2, parts.size());
		assertEquals(name60, String.join(" ", parts));
	}
}
