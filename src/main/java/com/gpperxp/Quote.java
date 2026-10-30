package com.gpperxp;

/** The price to buy an item at, the price to sell it at, and whether either came from a live trade. */
public final class Quote
{
	public final long buy;
	public final long sell;
	public final boolean live;

	public Quote(long buy, long sell, boolean live)
	{
		this.buy = buy;
		this.sell = sell;
		this.live = live;
	}
}
