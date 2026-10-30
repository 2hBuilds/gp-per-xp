package com.gpperxp;

/** The card trip block: the texts for getting from the player xp to the target. */
public final class CardTrip
{
	public final String heading;
	public final String xp;
	public final String actions;
	public final String cost;
	public final String time;
	public final Money money;

	public CardTrip(String heading, String xp, String actions, String cost, String time, Money money)
	{
		this.heading = heading;
		this.xp = xp;
		this.actions = actions;
		this.cost = cost;
		this.time = time;
		this.money = money;
	}
}
