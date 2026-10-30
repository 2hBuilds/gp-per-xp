package com.gpperxp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntToLongFunction;
import java.util.function.Predicate;

/**
 * The few best methods of one skill, in four lists of at most three lines: the fastest xp, the best gp per hour, the
 * cheapest per xp, and the ones the wiki's training page recommends. No Swing.
 *
 * <p>Choice: only priced rows the player can do now are picked, in the level band that fits the player; ties go by name.
 */
public final class TopPicks
{
	/** How many lines a list holds at most. */
	static final int MAX = 3;

	/** Highest xp per hour first (the low end of a range, the stated figure of an "up to"). */
	public final List<ListLine> fastest;
	/** Least cost, or most profit, per hour first. */
	public final List<ListLine> bestGp;
	/** Lowest gp per xp first. */
	public final List<ListLine> cheapest;
	/** The rows marked recommended, the highest level first. */
	public final List<ListLine> suggested;

	private TopPicks(List<ListLine> fastest, List<ListLine> bestGp, List<ListLine> cheapest, List<ListLine> suggested)
	{
		this.fastest = fastest;
		this.bestGp = bestGp;
		this.cheapest = cheapest;
		this.suggested = suggested;
	}

	/**
	 * @param playerXp the player's xp in the skill; below 0 means unknown
	 * @param standing the player's levels and quests, for the gates of the methods
	 * @param includeProfit false drops the rows that earn gp from all four lists
	 */
	public static TopPicks build(List<TrainingMethod> methods, IntToLongFunction price, boolean tax, int playerXp,
		Standing standing, boolean includeProfit)
	{
		final int playerLevel = ListModel.levelOf(playerXp);
		final Predicate<Row> canDo = Sort.atLevel(playerLevel).and(r -> Gates.missing(r.method, standing).isEmpty());
		final List<Row> rows = new ArrayList<>();
		for (TrainingMethod m : ListModel.bands(methods, playerLevel, false))
		{
			final Row r = Costs.row(m, price, tax);
			if (r.priced && canDo.test(r) && (includeProfit || r.gpPerXp >= 0))
			{
				rows.add(r);
			}
		}
		final Comparator<Row> byName = Comparator.comparing((Row r) -> r.method.name);
		return new TopPicks(
			pick(rows, r -> Costs.rated(r.method),
				Comparator.comparingDouble((Row r) -> r.sortXpPerHour()).reversed().thenComparing(byName), canDo, playerLevel, standing),
			pick(rows, r -> Costs.rated(r.method) && Costs.costStated(r.method),
				Comparator.comparingDouble((Row r) -> r.sortGpPerHour()).thenComparing(byName), canDo, playerLevel, standing),
			pick(rows, r -> Costs.costStated(r.method),
				Comparator.comparingDouble((Row r) -> r.gpPerXp).thenComparing(byName), canDo, playerLevel, standing),
			pick(rows, r -> r.method.recommended,
				Comparator.comparingInt((Row r) -> r.method.level).reversed().thenComparing(byName), canDo, playerLevel,
				standing));
	}

	private static List<ListLine> pick(List<Row> rows, Predicate<Row> keep, Comparator<Row> order, Predicate<Row> canDo,
		int playerLevel, Standing standing)
	{
		final List<Row> kept = new ArrayList<>();
		for (Row r : rows)
		{
			if (keep.test(r))
			{
				kept.add(r);
			}
		}
		Collections.sort(kept, order);
		final List<ListLine> out = new ArrayList<>();
		for (Row r : kept.subList(0, Math.min(MAX, kept.size())))
		{
			out.add(ListModel.line(r, canDo, playerLevel, standing));
		}
		return Collections.unmodifiableList(out);
	}
}
