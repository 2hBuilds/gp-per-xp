package com.gpperxp;

/** A method with its costs worked out at the current prices. */
public class Row
{
	public final TrainingMethod method;
	public final double gpPerXp;
	/** At the low end of the rate (the point for a single figure). */
	public final double xpPerHour;
	/** At the high end of the rate (the point for a single figure); 0 when there is no rate. */
	public final double xpPerHourHigh;
	public final double gpPerHour;
	public final double gpPerHourHigh;
	public final boolean priced;

	public Row(TrainingMethod method, double gpPerXp, double xpPerHour, double xpPerHourHigh, double gpPerHour,
		double gpPerHourHigh, boolean priced)
	{
		this.method = method;
		this.gpPerXp = gpPerXp;
		this.xpPerHour = xpPerHour;
		this.xpPerHourHigh = xpPerHourHigh;
		this.gpPerHour = gpPerHour;
		this.gpPerHourHigh = gpPerHourHigh;
		this.priced = priced;
	}

	/** The xp per hour a rate sort uses: the high end for an "up to" row, the low end for every other rate. */
	public double sortXpPerHour()
	{
		return Costs.kind(method) == RateKind.UPTO ? xpPerHourHigh : xpPerHour;
	}

	/** The gp per hour a rate sort uses: the high end for an "up to" row, the low end for every other rate. */
	public double sortGpPerHour()
	{
		return Costs.kind(method) == RateKind.UPTO ? gpPerHourHigh : gpPerHour;
	}
}
