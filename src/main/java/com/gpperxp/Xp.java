package com.gpperxp;

/** The OSRS experience table. */
public final class Xp
{
	private static final int MAX_LEVEL = 99;
	private static final int[] TABLE = build();

	private Xp()
	{
	}

	private static int[] build()
	{
		final int[] table = new int[MAX_LEVEL + 1];
		double sum = 0;
		for (int level = 1; level < MAX_LEVEL; level++)
		{
			sum += Math.floor(level + 300 * Math.pow(2, level / 7.0));
			table[level + 1] = (int) Math.floor(sum) / 4;
		}
		return table;
	}

	/** Xp needed for a level, the level clamped to 1..99. */
	public static int forLevel(int level)
	{
		return TABLE[Math.max(1, Math.min(MAX_LEVEL, level))];
	}

	/** The highest level (1..99) whose xp is at most {@code xp}. */
	public static int levelFor(int xp)
	{
		int level = 1;
		while (level < MAX_LEVEL && TABLE[level + 1] <= xp)
		{
			level++;
		}
		return level;
	}

	/** Target 0 means the next level; 1..99 that level; above 99 is 99; below 1 is 1. */
	public static int targetXp(int currentXp, int targetLevel)
	{
		if (targetLevel == 0)
		{
			return forLevel(levelFor(currentXp) + 1);
		}
		return forLevel(targetLevel);
	}
}
