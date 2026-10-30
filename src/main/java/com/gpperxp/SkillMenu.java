package com.gpperxp;

import net.runelite.api.Skill;
import net.runelite.client.util.Text;

/** Reads the Skills tab's own menu option ("View Prayer guide") back into a skill. */
public final class SkillMenu
{
	private static final String PREFIX = "View ";
	private static final String SUFFIX = " guide";

	private SkillMenu()
	{
	}

	/** The skill named by a "View <name> guide" option, or null for anything else and for Overall. */
	static Skill skillOf(String option)
	{
		if (option == null)
		{
			return null;
		}
		final String text = Text.removeTags(option);
		if (!text.startsWith(PREFIX) || !text.endsWith(SUFFIX) || text.length() <= PREFIX.length() + SUFFIX.length())
		{
			return null;
		}
		final String name = text.substring(PREFIX.length(), text.length() - SUFFIX.length());
		for (Skill skill : Skill.values())
		{
			if (skill != Skill.OVERALL && skill.getName().equalsIgnoreCase(name))
			{
				return skill;
			}
		}
		return null;
	}
}
