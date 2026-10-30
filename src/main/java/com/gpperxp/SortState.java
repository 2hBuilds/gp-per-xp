package com.gpperxp;

/** The sort and whether it is reversed; picking the same sort again flips it. */
public final class SortState
{
	public final Sort sort;
	public final boolean reversed;

	public SortState(Sort sort, boolean reversed)
	{
		this.sort = sort;
		this.reversed = reversed;
	}

	/** The same sort flips the order; another sort starts in its normal order. */
	public SortState pick(Sort picked)
	{
		return new SortState(picked, picked == sort && !reversed);
	}

	/** The words for this state. */
	public String label()
	{
		if (!reversed)
		{
			return normalLabel(sort);
		}
		switch (sort)
		{
			case FASTEST_XP_H:
				return "Slowest xp/h";
			case BEST_GP_H:
				return "Worst gp/h";
			default:
				return "Dearest gp/xp";
		}
	}

	/** The words for a sort in its normal order. */
	public static String normalLabel(Sort sort)
	{
		switch (sort)
		{
			case FASTEST_XP_H:
				return "Fastest xp/h";
			case BEST_GP_H:
				return "Best gp/h";
			default:
				return "Cheapest gp/xp";
		}
	}
}
