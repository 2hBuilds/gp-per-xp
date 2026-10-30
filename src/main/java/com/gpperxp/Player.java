package com.gpperxp;

/** What the panel knows of the player. */
public interface Player extends Standing
{
	/** The player's xp in the skill, or -1 when unknown (not logged in). */
	int xp(String skillName);

	/** The level worked out from the xp; -1 when the xp is unknown. */
	@Override
	default int level(String skill)
	{
		final int xp = xp(skill);
		return xp < 0 ? -1 : Xp.levelFor(xp);
	}

	/** Choice: unknown quests count as done, so that nothing is greyed on a guess. */
	@Override
	default boolean questDone(String quest)
	{
		return true;
	}
}
