package com.gpperxp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToLongFunction;
import java.util.function.Predicate;

/** Works the sidebar list out: the lines in order, with their texts. No Swing. */
public final class ListModel
{
	/** The level read for a player whose xp is unknown, so that nothing is greyed. */
	private static final int UNKNOWN_LEVEL = 99;

	private ListModel()
	{
	}

	/**
	 * @param playerXp the player's xp in the skill; below 0 means unknown
	 * @param reversed flips the order of the priced rows the player can do
	 * @param standing the player's levels and quests, for the gates of the methods
	 * @param otherBands true lists every level band of a banded method, false only the one that fits the player
	 */
	public static List<ListLine> build(List<TrainingMethod> methods, IntToLongFunction price, boolean tax, Sort sort,
		int playerXp, boolean includeProfit, boolean showAbove, boolean reversed, Standing standing,
		boolean otherBands)
	{
		final int playerLevel = levelOf(playerXp);
		final List<Row> rows = new ArrayList<>();
		for (TrainingMethod m : bands(methods, playerLevel, otherBands))
		{
			rows.add(Costs.row(m, price, tax));
		}
		final Predicate<Row> canDo = Sort.atLevel(playerLevel).and(r -> Gates.missing(r.method, standing).isEmpty());
		final List<ListLine> lines = new ArrayList<>();
		for (Row r : Sort.arrange(rows, sort, canDo, includeProfit, showAbove, reversed))
		{
			lines.add(line(r, canDo, playerLevel, standing));
		}
		return lines;
	}

	/** The line of one row: its texts, greyed when {@code canDo} refuses it. Shared with {@link TopPicks}. */
	static ListLine line(Row r, Predicate<Row> canDo, int playerLevel, Standing standing)
	{
		final boolean greyed = !canDo.test(r);
		final String name = r.method.rateLevel > 0 ? r.method.name + " (rate at " + r.method.rateLevel + ")"
			: r.method.name;
		final String level = levelText(r.method, playerLevel, standing);
		if (!r.priced)
		{
			return new ListLine(r, name, level, "?", "?", "?", Money.NEUTRAL, greyed, true);
		}
		if (!Costs.costStated(r.method))
		{
			return new ListLine(r, name, level, "-", xpPerHourText(r), "-", Money.NEUTRAL, greyed, false);
		}
		if (Costs.free(r.method))
		{
			return new ListLine(r, name, level, "free", xpPerHourText(r), "free", Money.NEUTRAL, greyed, false);
		}
		final String gpXp = Fmt.plus(Fmt.gpXp(-r.gpPerXp));
		return new ListLine(r, name, level, gpXp, xpPerHourText(r), gpPerHourText(r), Money.of(gpXp), greyed, false);
	}

	/** The level the model reads for a player xp; 99 when the xp is unknown. */
	static int levelOf(int playerXp)
	{
		return playerXp < 0 ? UNKNOWN_LEVEL : Xp.levelFor(playerXp);
	}

	/** The xp per hour of a row as the list shows it, in the short forms: "10-15k", "~12k", "10k+", "<20k". */
	static String xpPerHourText(Row r)
	{
		switch (Costs.kind(r.method))
		{
			case STATED:
				return Fmt.rate(r.xpPerHour);
			case ABOUT:
				return "~" + Fmt.rate(r.xpPerHour);
			case RANGE:
				return Fmt.shortRange(r.xpPerHour, r.xpPerHourHigh);
			case AT_LEAST:
				return Fmt.rate(r.xpPerHour) + "+";
			case UPTO:
				return "<" + Fmt.rate(r.xpPerHourHigh);
			case PER_RUN:
				return "per run";
			default:
				return "-";
		}
	}

	/** The gp per hour of a row as the list shows it (a cost is shown as a negative number of gp earned). */
	static String gpPerHourText(Row r)
	{
		switch (Costs.kind(r.method))
		{
			case STATED:
				return Fmt.plus(Fmt.rate(-r.gpPerHour));
			case ABOUT:
				return "~" + Fmt.plus(Fmt.rate(-r.gpPerHour));
			case RANGE:
				return Fmt.gpShortRange(-r.gpPerHour, -r.gpPerHourHigh);
			case AT_LEAST:
				return Fmt.plus(Fmt.rate(-r.gpPerHour)) + "+";
			case UPTO:
				return "<" + Fmt.plus(Fmt.rate(-r.gpPerHourHigh));
			case PER_RUN:
				return "per run";
			default:
				return "-";
		}
	}

	/** "Lv N" when this skill's level is short, else the first unmet gate: "Magic 90", or "Quest" for a quest. */
	private static String levelText(TrainingMethod m, int playerLevel, Standing standing)
	{
		if (m.level > playerLevel)
		{
			return "Lv " + m.level;
		}
		final List<String> missing = Gates.missing(m, standing);
		if (missing.isEmpty())
		{
			return "Lv " + m.level;
		}
		for (Gate g : m.requires)
		{
			if (g.quest != null && g.quest.equals(missing.get(0)))
			{
				return "Quest";
			}
		}
		return missing.get(0);
	}

	/**
	 * Choice: the rows of one skill and name with a rateLevel form a method of bands; unless {@code otherBands}, the
	 * band with the highest rateLevel at or below the player's level stays (the lowest band when there is none).
	 */
	static List<TrainingMethod> bands(List<TrainingMethod> methods, int playerLevel, boolean otherBands)
	{
		if (otherBands)
		{
			return methods;
		}
		final Map<String, TrainingMethod> chosen = new LinkedHashMap<>();
		for (TrainingMethod m : methods)
		{
			if (m.rateLevel <= 0)
			{
				continue;
			}
			final String key = m.skill + "\n" + m.name;
			final TrainingMethod best = chosen.get(key);
			if (best == null || better(m, best, playerLevel))
			{
				chosen.put(key, m);
			}
		}
		final List<TrainingMethod> out = new ArrayList<>();
		for (TrainingMethod m : methods)
		{
			if (m.rateLevel <= 0 || chosen.get(m.skill + "\n" + m.name) == m)
			{
				out.add(m);
			}
		}
		return out;
	}

	/** True when band {@code a} fits the player better than band {@code b}. */
	private static boolean better(TrainingMethod a, TrainingMethod b, int playerLevel)
	{
		final boolean aFits = a.rateLevel <= playerLevel;
		final boolean bFits = b.rateLevel <= playerLevel;
		if (aFits != bFits)
		{
			return aFits;
		}
		return aFits ? a.rateLevel > b.rateLevel : a.rateLevel < b.rateLevel;
	}
}
