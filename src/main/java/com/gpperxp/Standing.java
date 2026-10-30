package com.gpperxp;

/** What the gates of a method are checked against: the player's levels and finished quests. */
public interface Standing
{
	/** Everything unknown: no level known, every quest done, so no gate ever blocks. */
	Standing ALL = new Standing()
	{
		@Override
		public int level(String skill)
		{
			return -1;
		}

		@Override
		public boolean questDone(String quest)
		{
			return true;
		}
	};

	/** The player's level in the skill, or -1 when unknown. */
	int level(String skill);

	/** True when the quest is finished, or when that is not known. */
	boolean questDone(String quest);
}
