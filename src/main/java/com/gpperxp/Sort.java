package com.gpperxp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/** The orders the panel can show methods in. */
public enum Sort
{
	CHEAPEST_GP_XP,
	FASTEST_XP_H,
	BEST_GP_H;

	private static final Comparator<Row> BY_LEVEL_NAME = Comparator
		.comparingInt((Row r) -> r.method.level)
		.thenComparing(r -> r.method.name);

	/** True for a row whose level is at most {@code level}. */
	public static Predicate<Row> atLevel(int level)
	{
		return r -> r.method.level <= level;
	}

	/**
	 * Priced doable rows in sort order, then unpriced doable rows, then rows {@code canDo} refuses (above the player); {@code reversed}
	 * flips the priced doable rows only (for the rate sorts, only those with a stated rate; the rest follow them).
	 */
	public static List<Row> arrange(List<Row> rows, Sort sort, Predicate<Row> canDo, boolean includeProfit,
		boolean showAbove, boolean reversed)
	{
		final List<Row> priced = new ArrayList<>();
		final List<Row> unpriced = new ArrayList<>();
		final List<Row> above = new ArrayList<>();
		for (Row r : rows)
		{
			if (!includeProfit && r.priced && r.gpPerXp < 0)
			{
				continue;
			}
			if (!canDo.test(r))
			{
				above.add(r);
			}
			else if (r.priced)
			{
				priced.add(r);
			}
			else
			{
				unpriced.add(r);
			}
		}
		// Choice: for the rate sorts, rows with no sortable rate (per run, none) follow the rated ones and never reverse with them.
		final List<Row> unrated = new ArrayList<>();
		// Choice: rows whose cost the wiki does not state trail in the two gp sorts, with the unrated ones.
		{
			for (Row r : new ArrayList<>(priced))
			{
				if ((sort != Sort.CHEAPEST_GP_XP && !Costs.rated(r.method))
					|| (sort != Sort.FASTEST_XP_H && !Costs.costStated(r.method)))
				{
					priced.remove(r);
					unrated.add(r);
				}
			}
		}
		priced.sort(primary(sort).thenComparing(BY_LEVEL_NAME));
		if (reversed)
		{
			Collections.reverse(priced);
		}
		unrated.sort(BY_LEVEL_NAME);
		priced.addAll(unrated);
		unpriced.sort(BY_LEVEL_NAME);
		above.sort(BY_LEVEL_NAME);

		final List<Row> out = new ArrayList<>(priced);
		out.addAll(unpriced);
		if (showAbove)
		{
			out.addAll(above);
		}
		return out;
	}

	private static Comparator<Row> primary(Sort sort)
	{
		switch (sort)
		{
			case FASTEST_XP_H:
				return (a, b) -> Double.compare(b.sortXpPerHour(), a.sortXpPerHour());
			case BEST_GP_H:
				return (a, b) -> Double.compare(a.sortGpPerHour(), b.sortGpPerHour());
			default:
				return (a, b) -> Double.compare(a.gpPerXp, b.gpPerXp);
		}
	}
}
