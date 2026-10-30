package com.gpperxp;

import java.util.function.IntToLongFunction;

/** Item prices with two sides: {@code applyAsLong} is what you pay to buy, {@link #sell} what you get when you sell. */
public interface SidedPrices extends IntToLongFunction
{
	long sell(int id);
}
