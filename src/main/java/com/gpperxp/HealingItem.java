package com.gpperxp;

/**
 * One food or potion from the healing file: how much it heals per bite or dose, and how many bites or doses an item has.
 * Gson fills the fields; a heal is a plain figure ({@code heals}), a flat part plus a percent of the player's max HP
 * ({@code healsFlat}, {@code healsPercent}), or a range the wiki states ({@code healsLow} to {@code healsHigh}).
 */
public final class HealingItem
{
	public String name;
	public int itemId;
	public String item;
	public int heals;
	public int healsFlat;
	public int healsPercent;
	public int healsLow;
	public int healsHigh;
	/** The Hitpoints levels this row holds for; 0 means unset (no limit). */
	public int hpMin;
	public int hpMax;
	public int portions = 1;
	public String source;
	public String note;

	/** Whether the row applies to a player with this max HP; a row with no band always does. */
	public boolean appliesAt(int maxHp)
	{
		return (hpMin <= 0 || maxHp >= hpMin) && (hpMax <= 0 || maxHp <= hpMax);
	}

	/** True when the wiki states a range. */
	public boolean isRange()
	{
		return healsLow > 0 || healsHigh > 0;
	}

	/** True when the heal is a flat part plus a percent of max HP. */
	public boolean isPercent()
	{
		return healsPercent > 0;
	}

	/** The bites or doses an item has, at least 1. */
	public int portionCount()
	{
		return Math.max(1, portions);
	}

	/** Choice: for a range this is the low end, so that gp per hp is the dearest case. */
	public int healsPerPortion(int maxHp)
	{
		if (isRange())
		{
			return healsLow;
		}
		if (isPercent() || healsFlat > 0)
		{
			return healsFlat + healsPercent * Math.max(0, maxHp) / 100;
		}
		return heals;
	}

	/** The high end per portion; the same as {@link #healsPerPortion} unless the wiki states a range. */
	public int healsPerPortionHigh(int maxHp)
	{
		return isRange() ? Math.max(healsLow, healsHigh) : healsPerPortion(maxHp);
	}

	public int healsPerItem(int maxHp)
	{
		return healsPerPortion(maxHp) * portionCount();
	}

	public int healsPerItemHigh(int maxHp)
	{
		return healsPerPortionHigh(maxHp) * portionCount();
	}
}
