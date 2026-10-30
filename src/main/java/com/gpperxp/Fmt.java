package com.gpperxp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** The short texts of the list: rates with k and m, and gp per xp with one decimal under 1000. */
public final class Fmt
{
	private Fmt()
	{
	}

	/** 0, 999, 1k, 62.5k, 313k, 1m, 1.3m, 1b, 39.4b; a minus sign for negatives, never "-0". */
	public static String rate(double v)
	{
		final double a = Math.abs(v);
		final String text;
		if (a < 999.5)
		{
			text = Long.toString(Math.round(a));
		}
		else if (a < 99_950)
		{
			text = trim(a / 1000) + "k";
		}
		else if (a < 999_500)
		{
			text = Math.round(a / 1000) + "k";
		}
		else if (a < 999_950_000)
		{
			text = trim(a / 1_000_000) + "m";
		}
		else
		{
			text = trim(a / 1_000_000_000) + "b";
		}
		return v < 0 && !text.equals("0") ? "-" + text : text;
	}

	/**
	 * Two rates as one range, the smaller size first: "45k-55k"; one text when they read the same; when both are
	 * negative each end keeps its sign and "to" joins them: "-100k to -150k".
	 */
	public static String range(double a, double b)
	{
		final String first = rate(a);
		final String second = rate(b);
		if (first.equals(second))
		{
			return first;
		}
		if (a < 0 && b < 0)
		{
			return a >= b ? first + " to " + second : second + " to " + first;
		}
		return a <= b ? first + "-" + second : second + "-" + first;
	}

	/**
	 * Two rates as one short range for the list: "10-15k", "-100-150k", "800-1.2k", "500k-1.2m". The unit letter is
	 * written once when both rates end in the same one; equal texts give one text.
	 */
	public static String shortRange(double a, double b)
	{
		if (a < 0 && b < 0)
		{
			final String inner = shortRange(-a, -b);
			return "-" + inner;
		}
		final String first = rate(Math.min(a, b));
		final String second = rate(Math.max(a, b));
		if (first.equals(second))
		{
			return first;
		}
		final char unit = second.charAt(second.length() - 1);
		if ((unit == 'k' || unit == 'm') && first.charAt(first.length() - 1) == unit)
		{
			return first.substring(0, first.length() - 1) + "-" + second;
		}
		return first + "-" + second;
	}

	/** Two quantities as one range, the smaller first: "1,800-2,200"; one text when equal. */
	public static String qtyRange(double a, double b)
	{
		final String first = qty(Math.min(a, b));
		final String second = qty(Math.max(a, b));
		return first.equals(second) ? first : first + "-" + second;
	}

	/** The fast and the slow time of a trip: "13 min to 20 min"; one time when they read the same. */
	public static String timeRange(double fastHours, double slowHours)
	{
		final String fast = time(fastHours);
		final String slow = time(slowHours);
		return fast.equals(slow) ? fast : fast + " to " + slow;
	}

	/** {@code text} with a "+" in front unless it is a negative figure or a zero ("0", "0.0"): the mark of gp earned. */
	public static String plus(String text)
	{
		return text.startsWith("-") || text.equals("0") || text.equals("0.0") ? text : "+" + text;
	}

	/** A gp range for the card: "+100k to +150k" when both ends earn; both spending keeps {@link #range}. */
	public static String gpRange(double a, double b)
	{
		if (a <= 0 && b <= 0)
		{
			return range(a, b);
		}
		final String first = plus(rate(Math.min(a, b)));
		final String second = plus(rate(Math.max(a, b)));
		return first.equals(second) ? first : first + " to " + second;
	}

	/** A gp range for the list: "+100-150k" when both ends earn, "-100-150k" when both spend. */
	public static String gpShortRange(double a, double b)
	{
		if (a > 0 && b > 0)
		{
			return "+" + shortRange(a, b);
		}
		return shortRange(a, b);
	}

	/** One decimal under 1000 ("12.5", "0.0"), the rate text from there up. */
	public static String gpXp(double v)
	{
		final double a = Math.abs(v);
		if (a >= 999.95)
		{
			return rate(v);
		}
		final String text = String.format(Locale.ROOT, "%.1f", a);
		return v < 0 && !text.equals("0.0") ? "-" + text : text;
	}

	/** One decimal, a trailing ".0" dropped. */
	private static String trim(double v)
	{
		final String text = String.format(Locale.ROOT, "%.1f", v);
		return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
	}

	/** Thousands commas, a minus sign for negatives. */
	static String grouped(long v)
	{
		return String.format(Locale.ROOT, "%,d", v);
	}

	/** A whole number grouped; otherwise up to 4 decimals with the trailing zeros dropped. */
	static String qty(double v)
	{
		if (v == Math.rint(v))
		{
			return grouped((long) v);
		}
		final String text = new BigDecimal(Math.abs(v)).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros()
			.toPlainString();
		if (text.equals("0"))
		{
			return "0";
		}
		final int dot = text.indexOf('.');
		final String whole = grouped(Long.parseLong(dot < 0 ? text : text.substring(0, dot)));
		return (v < 0 ? "-" : "") + whole + (dot < 0 ? "" : text.substring(dot));
	}

	/** "<1 min", "30 min", "1 h", "1 h 30 min". */
	static String time(double hours)
	{
		if (hours * 60 < 1)
		{
			return "<1 min";
		}
		final long total = Math.round(hours * 60);
		final long h = total / 60;
		final long m = total % 60;
		if (h == 0)
		{
			return m + " min";
		}
		return m == 0 ? h + " h" : h + " h " + m + " min";
	}
}
