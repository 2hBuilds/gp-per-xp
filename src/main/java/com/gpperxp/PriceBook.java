package com.gpperxp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntToLongFunction;

/**
 * The item prices the panel and the card read. RuneLite's item manager answers only on the client thread, so that thread
 * fills this book ({@link #refresh}) and every other thread reads it ({@link #price}), which never calls the source.
 */
final class PriceBook implements SidedPrices
{
	private final IntToLongFunction source;
	private final Map<Integer, Long> prices = new ConcurrentHashMap<>();
	private volatile boolean useTrades;
	private volatile Map<Integer, Print> prints = new HashMap<>();

	/** @param source where prices come from; it is called only inside {@link #refresh} */
	PriceBook(IntToLongFunction source)
	{
		this.source = source;
	}

	/** Every item id the methods use, inputs and outputs. */
	static Set<Integer> idsOf(Methods methods)
	{
		final Set<Integer> ids = new HashSet<>();
		for (TrainingMethod m : methods.all())
		{
			for (ItemQty in : m.inputs)
			{
				ids.add(in.itemId);
			}
			for (ItemQty out : m.outputs)
			{
				ids.add(out.itemId);
			}
		}
		return ids;
	}

	/** Reads the source for each id; call it on the thread the source needs. Returns whether any price changed. */
	boolean refresh(Collection<Integer> ids)
	{
		boolean changed = false;
		for (int id : ids)
		{
			final long price = source.applyAsLong(id);
			final Long before = prices.put(id, price);
			if (before == null || before != price)
			{
				changed = true;
			}
		}
		return changed;
	}

	/** Whether prices come from the wiki's trade prints (true) or are the guide price (false). */
	void setUseTrades(boolean on)
	{
		useTrades = on;
	}

	/** The latest prints, copied. */
	void setPrints(Map<Integer, Print> latest)
	{
		prints = new HashMap<>(latest);
	}

	/** The buy price of the item, or 0 when none has been read; safe on any thread. */
	long price(int id)
	{
		return quote(id, true);
	}

	@Override
	public long applyAsLong(int id)
	{
		return price(id);
	}

	/** The sell price of the item, or 0 when none has been read; safe on any thread. */
	@Override
	public long sell(int id)
	{
		return quote(id, false);
	}

	private long quote(int id, boolean buy)
	{
		final Long guide = prices.get(id);
		final long g = guide == null ? 0L : guide;
		if (!useTrades)
		{
			return g;
		}
		final Quote q = TradePrices.choose(g, prints.get(id), System.currentTimeMillis() / 1000L);
		return buy ? q.buy : q.sell;
	}
}
