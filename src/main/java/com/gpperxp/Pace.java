package com.gpperxp;

import java.util.ArrayList;
import java.util.List;

/** The pace setting: a percentage of the wiki's hourly rate. Pure. */
public final class Pace
{
	private static final int MIN = 10;
	private static final int MAX = 100;

	private Pace()
	{
	}

	/** The method at {@code percent} of its rate; the same instance at 100 or when it has no rate to scale (a figure, a range end or an "up to"). */
	public static TrainingMethod apply(TrainingMethod m, int percent)
	{
		if (percent >= MAX || (m.actionsPerHour <= 0 && m.rateLow <= 0 && m.rateHigh <= 0))
		{
			return m;
		}
		return m.withRates(m.actionsPerHour * percent / 100.0, m.rateLow * percent / 100.0,
			m.rateHigh * percent / 100.0);
	}

	public static List<TrainingMethod> applyAll(List<TrainingMethod> methods, int percent)
	{
		final List<TrainingMethod> out = new ArrayList<>(methods.size());
		for (TrainingMethod m : methods)
		{
			out.add(apply(m, percent));
		}
		return out;
	}

	/** Held between 10 and 100. */
	public static int clamp(int percent)
	{
		return Math.max(MIN, Math.min(MAX, percent));
	}
}
