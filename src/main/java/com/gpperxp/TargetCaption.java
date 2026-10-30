package com.gpperxp;

/** The grey note beside the target box: what the box does for this player. */
final class TargetCaption
{
	private static final int MAX_LEVEL = 99;

	private TargetCaption()
	{
	}

	/** @param xp the player's xp in the skill, below 0 when not logged in */
	static String of(int xp, int targetLevel)
	{
		if (xp < 0)
		{
			return "0 = next level";
		}
		final int level = Xp.levelFor(xp);
		if (targetLevel <= 0)
		{
			return level >= MAX_LEVEL ? "level " + MAX_LEVEL + " reached" : "to level " + (level + 1);
		}
		return targetLevel <= level ? "level " + targetLevel + " reached" : "to level " + targetLevel;
	}
}
