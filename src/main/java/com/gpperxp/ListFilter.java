package com.gpperxp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The search box's filter: keeps the lines whose name contains the text, in order. */
final class ListFilter
{
	private ListFilter()
	{
	}

	/** {@code lines} itself when {@code text} is null or blank, else the lines whose name contains it, any case. */
	static List<ListLine> apply(List<ListLine> lines, String text)
	{
		if (text == null || text.trim().isEmpty())
		{
			return lines;
		}
		final String needle = text.trim().toLowerCase(Locale.ROOT);
		final List<ListLine> out = new ArrayList<>();
		for (ListLine line : lines)
		{
			if (line.name.toLowerCase(Locale.ROOT).contains(needle))
			{
				out.add(line);
			}
		}
		return out;
	}
}
