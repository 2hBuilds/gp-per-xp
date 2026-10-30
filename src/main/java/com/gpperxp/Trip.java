package com.gpperxp;

/** What it takes to get from one xp total to another. */
public class Trip
{
	public final long actions;
	public final double gp;
	/** The time at the fastest stated rate; 0 when there is none (per run, no rate). */
	public final double hours;
	/** The time at the slowest stated rate; 0 when there is none or the rate is only an "up to" figure. */
	public final double hoursSlow;

	public Trip(long actions, double gp, double hours, double hoursSlow)
	{
		this.actions = actions;
		this.gp = gp;
		this.hours = hours;
		this.hoursSlow = hoursSlow;
	}
}
