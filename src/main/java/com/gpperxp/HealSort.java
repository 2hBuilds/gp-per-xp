package com.gpperxp;

/** The orders the healing list can be shown in. */
public enum HealSort
{
	CHEAPEST_GP_HP("gp/hp", "Cheapest gp/hp", "Dearest gp/hp"),
	MOST_HP("hp", "Most hp", "Least hp"),
	CHEAPEST_PRICE("gp", "Cheapest price", "Dearest price");

	/** The sort box's word. */
	public final String word;
	/** The menu row's words in the normal and in the reversed order. */
	public final String normal;
	public final String reversed;

	HealSort(String word, String normal, String reversed)
	{
		this.word = word;
		this.normal = normal;
		this.reversed = reversed;
	}
}
