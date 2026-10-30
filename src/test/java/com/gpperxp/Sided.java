package com.gpperxp;

import java.util.Map;

/**
 * A test price source with a buy side and a sell side, each read from its own map; an id with no entry is 0. Its
 * {@code applyAsLong} is the buy side and {@code sell} the sell side, as {@link SidedPrices} says.
 */
public final class Sided implements SidedPrices
{
	private final Map<Integer, Long> buy;
	private final Map<Integer, Long> sell;

	public Sided(Map<Integer, Long> buy, Map<Integer, Long> sell)
	{
		this.buy = buy;
		this.sell = sell;
	}

	@Override
	public long applyAsLong(int id)
	{
		return buy.getOrDefault(id, 0L);
	}

	@Override
	public long sell(int id)
	{
		return sell.getOrDefault(id, 0L);
	}
}
