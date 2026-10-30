package com.gpperxp;

import java.util.function.IntToLongFunction;

/** Cost, xp and time maths for a method; a positive cost is gp spent, a negative one is profit. */
public final class Costs
{
	private static final long TAX_CAP = 5_000_000L;

	private Costs()
	{
	}

	/** The gp you get for selling one item, after the 2 % GE tax (capped at 5 m) when tax is on. */
	public static long sellPrice(long price, boolean tax)
	{
		if (!tax)
		{
			return price;
		}
		return price - Math.min(price / 50, TAX_CAP);
	}

	/** The prices to sell at: the sell side of sided prices, else the function itself. */
	public static IntToLongFunction sellSide(IntToLongFunction price)
	{
		return price instanceof SidedPrices ? ((SidedPrices) price)::sell : price;
	}

	public static double costPerAction(TrainingMethod m, IntToLongFunction price, boolean tax)
	{
		double cost = 0;
		for (ItemQty in : m.inputs)
		{
			cost += in.qty * price.applyAsLong(in.itemId);
		}
		final IntToLongFunction sell = sellSide(price);
		for (ItemQty out : m.outputs)
		{
			cost -= out.qty * sellPrice(sell.applyAsLong(out.itemId), tax);
		}
		return cost;
	}

	public static double gpPerXp(TrainingMethod m, IntToLongFunction price, boolean tax)
	{
		return costPerAction(m, price, tax) / m.xpPerAction;
	}

	/** The shape of the method's rate. */
	public static RateKind kind(TrainingMethod m)
	{
		if ("perRun".equals(m.rateKind))
		{
			return RateKind.PER_RUN;
		}
		if ("none".equals(m.rateKind))
		{
			return RateKind.NONE;
		}
		if (m.rateLow > 0 && m.rateHigh > 0)
		{
			return RateKind.RANGE;
		}
		if (m.rateLow > 0 && m.rateHigh <= 0)
		{
			return RateKind.AT_LEAST;
		}
		if (m.rateHigh > 0)
		{
			return RateKind.UPTO;
		}
		if (m.actionsPerHour > 0)
		{
			return "about".equals(m.rateKind) ? RateKind.ABOUT : RateKind.STATED;
		}
		return RateKind.NONE;
	}

	/** The slowest actions per hour: the point, the low end of a range, 0 otherwise. */
	private static double rateFloor(TrainingMethod m)
	{
		switch (kind(m))
		{
			case STATED:
			case ABOUT:
				return m.actionsPerHour;
			case RANGE:
			case AT_LEAST:
				return m.rateLow;
			default:
				return 0;
		}
	}

	/** The fastest actions per hour: the point, the high end of a range or "up to", 0 otherwise. */
	private static double rateCeiling(TrainingMethod m)
	{
		switch (kind(m))
		{
			case STATED:
			case ABOUT:
				return m.actionsPerHour;
			case RANGE:
			case UPTO:
				return m.rateHigh;
			default:
				return 0;
		}
	}

	/** Xp per hour at the low end of the rate. */
	public static double xpPerHour(TrainingMethod m)
	{
		return m.xpPerAction * rateFloor(m);
	}

	/** Xp per hour at the high end of the rate. */
	public static double xpPerHourHigh(TrainingMethod m)
	{
		return m.xpPerAction * rateCeiling(m);
	}

	/** A cost per hour at the low end of the rate, the same sign as gp per xp; the best is the lowest. */
	public static double gpPerHour(TrainingMethod m, IntToLongFunction price, boolean tax)
	{
		return costPerAction(m, price, tax) * rateFloor(m);
	}

	/** A cost per hour at the high end of the rate. */
	public static double gpPerHourHigh(TrainingMethod m, IntToLongFunction price, boolean tax)
	{
		return costPerAction(m, price, tax) * rateCeiling(m);
	}

	/** True when every input and output has a price above 0. */
	public static boolean priced(TrainingMethod m, IntToLongFunction price)
	{
		for (ItemQty in : m.inputs)
		{
			if (price.applyAsLong(in.itemId) <= 0)
			{
				return false;
			}
		}
		final IntToLongFunction sell = sellSide(price);
		for (ItemQty out : m.outputs)
		{
			if (sell.applyAsLong(out.itemId) <= 0)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * True when the method has a rate that sorts: a stated figure, an "about" figure, a range, an "at least" floor or an
	 * "up to" figure. Choice: an "up to" row sorts by its stated figure, the high end (see {@link Row#sortXpPerHour()}).
	 */
	public static boolean rated(TrainingMethod m)
	{
		final RateKind kind = kind(m);
		return kind == RateKind.STATED || kind == RateKind.ABOUT || kind == RateKind.RANGE
			|| kind == RateKind.AT_LEAST || kind == RateKind.UPTO;
	}

	/** True when one action of the method is one xp (its rate is stated in xp per hour). */
	public static boolean perXp(TrainingMethod m)
	{
		return "xp".equals(m.rateUnit);
	}

	/** True when the method has no ingredients and no products, so it costs nothing at any price. */
	public static boolean free(TrainingMethod m)
	{
		return m.inputs.isEmpty() && m.outputs.isEmpty();
	}

	/** False when the wiki states no cost for the method, so its gp figures are unknown. */
	public static boolean costStated(TrainingMethod m)
	{
		return !m.costsNotStated;
	}

	public static Row row(TrainingMethod m, IntToLongFunction price, boolean tax)
	{
		return new Row(m, gpPerXp(m, price, tax), xpPerHour(m), xpPerHourHigh(m), gpPerHour(m, price, tax),
			gpPerHourHigh(m, price, tax), priced(m, price));
	}

	public static Trip trip(TrainingMethod m, IntToLongFunction price, boolean tax, int currentXp, int targetXp)
	{
		final int needed = targetXp - currentXp;
		if (needed <= 0)
		{
			return new Trip(0, 0.0, 0.0, 0.0);
		}
		final long actions = (long) Math.ceil(needed / m.xpPerAction);
		final double gp = actions * costPerAction(m, price, tax);
		final double fast = rateCeiling(m);
		final double slow = rateFloor(m);
		return new Trip(actions, gp, fast <= 0 ? 0.0 : actions / fast, slow <= 0 ? 0.0 : actions / slow);
	}
}
