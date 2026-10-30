package com.gpperxp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.IntToLongFunction;

/** The healing list's lines, worked out and sorted. Pure: no Swing. */
public final class HealModel
{
	private static final String UNKNOWN = "?";

	private HealModel()
	{
	}

	/**
	 * Priced lines in sort order (ties by name), then unpriced lines by name; {@code reversed} flips the priced part only.
	 * A row with an hpMin/hpMax band is kept only when {@code maxHp} lies in it.
	 * Choice: an item that heals nothing at {@code maxHp} counts as unpriced, as there is no gp per hp to show.
	 */
	public static List<HealLine> build(List<HealingItem> items, IntToLongFunction price, int maxHp, HealSort sort,
		boolean reversed)
	{
		final List<Entry> priced = new ArrayList<>();
		final List<HealLine> unpriced = new ArrayList<>();
		for (HealingItem item : items)
		{
			if (!item.appliesAt(maxHp))
			{
				continue;
			}
			final long cost = price.applyAsLong(item.itemId);
			final int hp = item.healsPerItem(maxHp);
			final String hpText = hpText(item, maxHp);
			if (cost <= 0 || hp <= 0)
			{
				unpriced.add(new HealLine(item, item.name, UNKNOWN, hpText, cost <= 0 ? UNKNOWN : Fmt.rate(cost), true));
			}
			else
			{
				final double gpHp = (double) cost / hp;
				priced.add(new Entry(new HealLine(item, item.name, Fmt.gpXp(gpHp), hpText, Fmt.rate(cost), false),
					gpHp, hp, cost));
			}
		}
		final Comparator<Entry> byName = Comparator.comparing((Entry e) -> e.line.name);
		final Comparator<Entry> primary;
		switch (sort)
		{
			case MOST_HP:
				primary = (a, b) -> Integer.compare(b.hp, a.hp);
				break;
			case CHEAPEST_PRICE:
				primary = (a, b) -> Long.compare(a.cost, b.cost);
				break;
			default:
				primary = (a, b) -> Double.compare(a.gpHp, b.gpHp);
				break;
		}
		priced.sort(primary.thenComparing(byName));
		if (reversed)
		{
			Collections.reverse(priced);
		}
		unpriced.sort(Comparator.comparing((HealLine l) -> l.name));
		final List<HealLine> out = new ArrayList<>();
		for (Entry e : priced)
		{
			out.add(e.line);
		}
		out.addAll(unpriced);
		return out;
	}

	/** The heals per item as text: "64", or "10-22" for a range. */
	static String hpText(HealingItem item, int maxHp)
	{
		final int low = item.healsPerItem(maxHp);
		final int high = item.healsPerItemHigh(maxHp);
		return low == high ? Integer.toString(low) : low + "-" + high;
	}

	/** The lines whose name contains the text, any case; the lines themselves when it is blank. */
	static List<HealLine> filter(List<HealLine> lines, String text)
	{
		if (text == null || text.trim().isEmpty())
		{
			return lines;
		}
		final String needle = text.trim().toLowerCase(Locale.ROOT);
		final List<HealLine> out = new ArrayList<>();
		for (HealLine line : lines)
		{
			if (line.name.toLowerCase(Locale.ROOT).contains(needle))
			{
				out.add(line);
			}
		}
		return out;
	}

	private static final class Entry
	{
		final HealLine line;
		final double gpHp;
		final int hp;
		final long cost;

		Entry(HealLine line, double gpHp, int hp, long cost)
		{
			this.line = line;
			this.gpHp = gpHp;
			this.hp = hp;
			this.cost = cost;
		}
	}
}
