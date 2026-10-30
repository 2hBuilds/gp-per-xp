package com.gpperxp;

/** Picks the buy and sell price of one item from the guide price and the latest trade print. Pure. */
public final class TradePrices
{
	private static final long MAX_AGE_SECONDS = 86_400L;

	private TradePrices()
	{
	}

	/** The guide price is used for a side with no usable print; see the plan for the rules. */
	public static Quote choose(long guide, Print print, long nowSeconds)
	{
		if (print == null)
		{
			return new Quote(guide, guide, false);
		}
		final boolean highOk = usable(guide, print.high, print.highTime, nowSeconds);
		final boolean lowOk = usable(guide, print.low, print.lowTime, nowSeconds);
		if (highOk && lowOk && print.high < print.low)
		{
			return new Quote(guide, guide, false);
		}
		return new Quote(highOk ? print.high : guide, lowOk ? print.low : guide, highOk || lowOk);
	}

	private static boolean usable(long guide, Long price, Long time, long now)
	{
		if (price == null || time == null || now - time > MAX_AGE_SECONDS)
		{
			return false;
		}
		return guide <= 0 || (guide / 3 <= price && price <= guide * 3);
	}
}
