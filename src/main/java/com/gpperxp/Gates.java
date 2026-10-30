package com.gpperxp;

import java.util.ArrayList;
import java.util.List;

/** Checks and words the gates of a method. */
public final class Gates
{
	private Gates()
	{
	}

	/**
	 * The unmet skill and quest gates in order: "Magic 90" when the known level is short, a quest's name when it is not
	 * done. Diary and text gates are never counted.
	 */
	public static List<String> missing(TrainingMethod m, Standing s)
	{
		final List<String> out = new ArrayList<>();
		for (Gate g : m.requires)
		{
			if (g.skill != null)
			{
				final int have = s.level(g.skill);
				if (have >= 0 && have < g.level)
				{
					out.add(g.skill + " " + g.level);
				}
			}
			else if (g.quest != null && !s.questDone(g.quest))
			{
				out.add(g.quest);
			}
		}
		return out;
	}

	/** Every gate in order, joined by ", ": "Magic 90", a quest's name, "Ardougne hard diary", the text. */
	public static String needs(TrainingMethod m)
	{
		final List<String> parts = new ArrayList<>();
		for (Gate g : m.requires)
		{
			if (g.skill != null)
			{
				parts.add(g.skill + " " + g.level);
			}
			else if (g.quest != null)
			{
				parts.add(g.quest);
			}
			else if (g.diary != null)
			{
				parts.add(g.diary + " diary");
			}
			else if (g.text != null)
			{
				parts.add(g.text);
			}
		}
		return String.join(", ", parts);
	}
}
