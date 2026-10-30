package com.gpperxp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToLongFunction;

/** The texts of a method hover card. Pure: no Swing. */
public final class CardModel
{
	private static final String UNKNOWN = "?";
	private static final String NOT_STATED = "not stated";
	private static final String FREE = "free";
	private static final String USES_HEADING = "Uses (per action)";

	public final String title;
	public final String subtitle;
	/** Each entry is {label, value}. */
	public final List<String[]> facts;
	public final List<CardItem> uses;
	public final List<CardItem> makes;
	public final String makesNote;
	/** Null when the player xp is unknown or the target is not above it. */
	public final CardTrip trip;
	/** The whole trip's ingredients and products; empty when {@link #trip} is null. */
	public final List<CardItem> buy;
	public final List<CardItem> get;
	/** The method's note, trimmed; "" for none. */
	public final String note;
	/** The method's requirements as one line ("Magic 90, Dragon Slayer II"); "" for none. */
	public final String needs;
	/** What the method gives besides the priced products, trimmed; "" for none. */
	public final String youGet;
	/** A small grey line at the bottom; "" for none. */
	public final String footer;
	/** The heading over {@link #uses}. */
	public final String usesHeading;
	/** The method's rate shape, for the pace line. */
	private final RateKind kind;
	/** The method's actions per hour (the point), the range's low end and the high end, as shown. */
	private final double actionsPerHour;
	private final double rateLow;
	private final double rateHigh;
	/** True when one action is one xp, so the per-action figures are not stated. */
	private final boolean perXp;

	private CardModel(String title, String subtitle, List<String[]> facts, List<CardItem> uses, List<CardItem> makes,
		String makesNote, CardTrip trip, List<CardItem> buy, List<CardItem> get, String note, String needs,
		String youGet, String footer, RateKind kind, double actionsPerHour, double rateLow, double rateHigh, boolean perXp,
		String usesHeading)
	{
		this.usesHeading = usesHeading;
		this.perXp = perXp;
		this.needs = needs;
		this.youGet = youGet;
		this.kind = kind;
		this.rateLow = rateLow;
		this.rateHigh = rateHigh;
		this.title = title;
		this.subtitle = subtitle;
		this.facts = facts;
		this.uses = uses;
		this.makes = makes;
		this.makesNote = makesNote;
		this.trip = trip;
		this.buy = buy;
		this.get = get;
		this.note = note;
		this.footer = footer;
		this.actionsPerHour = actionsPerHour;
	}

	/**
	 * The card of a food or potion: the facts, the item's picture under them (a line with no price), the note.
	 * Choice: the subtitle says "Healing", and the picture rides alone (no name: the title has it) in the uses list under the heading "Item".
	 */
	public static CardModel healing(String title, List<String[]> facts, int itemId, String note, String footer)
	{
		final List<CardItem> item = new ArrayList<>();
		item.add(new CardItem(itemId, "", ""));
		return new CardModel(title, "Healing", facts, item, new ArrayList<>(), "", null, new ArrayList<>(),
			new ArrayList<>(), note, "", "", footer, RateKind.NONE, 0, 0, 0, true, "Item");
	}

	/** A copy with the footer line. */
	public CardModel withFooter(String text)
	{
		return new CardModel(title, subtitle, facts, uses, makes, makesNote, trip, buy, get, note, needs, youGet, text,
			kind, actionsPerHour, rateLow, rateHigh, perXp, usesHeading);
	}

	/** A copy whose "Actions per hour" says the pace; the same instance at 100 or with no rate to scale. */
	public CardModel withPace(int percent)
	{
		if (perXp)
		{
			return this;
		}
		final String pace;
		switch (kind)
		{
			case STATED:
				pace = Fmt.qty(actionsPerHour) + " (" + percent + "% of typical)";
				break;
			case ABOUT:
				pace = "about " + Fmt.qty(actionsPerHour) + " (" + percent + "% of typical)";
				break;
			case RANGE:
				pace = Fmt.qtyRange(rateLow, rateHigh) + " (" + percent + "% of wiki range)";
				break;
			case AT_LEAST:
				pace = "at least " + Fmt.qty(rateLow) + " (" + percent + "% of wiki figure)";
				break;
			case UPTO:
				pace = "up to " + Fmt.qty(rateHigh) + " (" + percent + "% of wiki figure)";
				break;
			default:
				return this;
		}
		if (percent >= 100)
		{
			return this;
		}
		final List<String[]> paced = new ArrayList<>(facts);
		paced.set(1, new String[] {facts.get(1)[0], pace});
		return new CardModel(title, subtitle, paced, uses, makes, makesNote, trip, buy, get, note, needs, youGet,
			footer, kind, actionsPerHour, rateLow, rateHigh, perXp, usesHeading);
	}

	/** @param currentXp the player xp in the skill; below 0 means unknown */
	public static CardModel of(Row row, IntToLongFunction price, boolean tax, int currentXp, int targetLevel)
	{
		final TrainingMethod m = row.method;
		final List<String[]> facts = new ArrayList<>();
		final RateKind kind = Costs.kind(m);
		final boolean perRun = kind == RateKind.PER_RUN;
		final boolean perXp = Costs.perXp(m);
		facts.add(new String[] {perRun ? "XP per run" : "XP per action", perXp ? NOT_STATED : Fmt.qty(m.xpPerAction)});
		final boolean rated = Costs.rated(m);
		final boolean free = Costs.free(m);
		facts.add(new String[] {"Actions per hour", perXp ? NOT_STATED : actionsText(m, kind)});
		facts.add(new String[] {"XP per hour", kind == RateKind.PER_RUN ? "-" : xpPerHourLong(row)});
		facts.add(new String[] {"GP per XP", !Costs.costStated(m) ? NOT_STATED : free ? FREE : row.priced ? Fmt.plus(Fmt.gpXp(-row.gpPerXp)) : UNKNOWN});
		facts.add(new String[] {"GP per hour",
			!Costs.costStated(m) ? NOT_STATED : free ? (rated ? FREE : "-") : !row.priced ? UNKNOWN : perRun ? "-" : gpPerHourLong(row)});

		final List<CardItem> uses = new ArrayList<>();
		for (ItemQty in : m.inputs)
		{
			final long p = price.applyAsLong(in.itemId);
			uses.add(new CardItem(in.itemId, text(in), p <= 0 ? UNKNOWN : Fmt.grouped(p)));
		}
		final IntToLongFunction sell = Costs.sellSide(price);
		final List<CardItem> makes = new ArrayList<>();
		for (ItemQty out : m.outputs)
		{
			final long p = sell.applyAsLong(out.itemId);
			makes.add(new CardItem(out.itemId, text(out), p <= 0 ? UNKNOWN : Fmt.grouped(Costs.sellPrice(p, tax))));
		}
		final String makesNote = tax && !makes.isEmpty() ? "after 2% GE tax" : "";
		final CardTrip trip = trip(row, price, tax, currentXp, targetLevel);
		final List<CardItem> buy = new ArrayList<>();
		final List<CardItem> get = new ArrayList<>();
		if (trip != null)
		{
			final long actions = Costs.trip(m, price, tax, currentXp, Xp.targetXp(currentXp, targetLevel)).actions;
			for (ItemQty in : m.inputs)
			{
				final long p = price.applyAsLong(in.itemId);
				buy.add(total(in, actions, p, p));
			}
			for (ItemQty out : m.outputs)
			{
				final long p = sell.applyAsLong(out.itemId);
				get.add(total(out, actions, p, Costs.sellPrice(p, tax)));
			}
		}
		return new CardModel(m.name, m.skill + ", level " + m.level, facts, uses, makes, makesNote, trip, buy, get,
			m.note == null ? "" : m.note.trim(), Gates.needs(m), m.outputsNote == null ? "" : m.outputsNote.trim(), "",
			kind, m.actionsPerHour, m.rateLow, m.rateHigh, perXp, USES_HEADING);
	}

	/** The "Actions per hour" value for the method's rate shape. */
	private static String actionsText(TrainingMethod m, RateKind kind)
	{
		switch (kind)
		{
			case STATED:
				return Fmt.qty(m.actionsPerHour) + " (typical)";
			case ABOUT:
				return "about " + Fmt.qty(m.actionsPerHour);
			case RANGE:
				return Fmt.qtyRange(m.rateLow, m.rateHigh) + " (wiki range)";
			case AT_LEAST:
				return "at least " + Fmt.qty(m.rateLow);
			case UPTO:
				return "up to " + Fmt.qty(m.rateHigh);
			case PER_RUN:
				return "per run";
			default:
				return NOT_STATED;
		}
	}

	/** "1 action", "7 actions", "1 run", "7 runs". */
	private static String actionsText(long count, boolean perRun)
	{
		return Fmt.grouped(count) + (perRun ? (count == 1 ? " run" : " runs") : (count == 1 ? " action" : " actions"));
	}

	/** One shopping line: the quantity for the whole trip and its total at the unit price {@code each}. */
	private static CardItem total(ItemQty item, long actions, long listed, long each)
	{
		final double qty = item.qty * actions;
		final ItemQty all = new ItemQty(item.itemId, item.item, qty);
		return new CardItem(item.itemId, text(all), listed <= 0 ? UNKNOWN : Fmt.rate(qty * each));
	}

	private static String text(ItemQty item)
	{
		return Fmt.qty(item.qty) + " x " + (item.item == null ? Integer.toString(item.itemId) : item.item);
	}

	private static CardTrip trip(Row row, IntToLongFunction price, boolean tax, int currentXp, int targetLevel)
	{
		if (currentXp < 0)
		{
			return null;
		}
		final int targetXp = Xp.targetXp(currentXp, targetLevel);
		if (targetXp <= currentXp)
		{
			return null;
		}
		final Trip t = Costs.trip(row.method, price, tax, currentXp, targetXp);
		final RateKind kind = Costs.kind(row.method);
		final String time;
		switch (kind)
		{
			case STATED:
				time = Fmt.time(t.hours);
				break;
			case ABOUT:
				time = "about " + Fmt.time(t.hours);
				break;
			case RANGE:
				time = Fmt.timeRange(t.hours, t.hoursSlow);
				break;
			case AT_LEAST:
				time = "at most " + Fmt.time(t.hoursSlow);
				break;
			case UPTO:
				time = "at least " + Fmt.time(t.hours);
				break;
			default:
				time = NOT_STATED;
				break;
		}
		return new CardTrip("To level " + Xp.levelFor(targetXp), Fmt.grouped(targetXp - currentXp) + " xp",
			Costs.perXp(row.method) ? NOT_STATED : actionsText(t.actions, kind == RateKind.PER_RUN),
			!Costs.costStated(row.method) ? NOT_STATED : Costs.free(row.method) ? FREE : row.priced ? Fmt.plus(Fmt.rate(-t.gp)) + " gp" : UNKNOWN, time,
			Costs.costStated(row.method) && row.priced ? (t.gp < 0 ? Money.EARN : t.gp > 0 ? Money.SPEND : Money.NEUTRAL)
				: Money.NEUTRAL);
	}

	/** The xp per hour of a row in the long forms of the card. */
	private static String xpPerHourLong(Row r)
	{
		switch (Costs.kind(r.method))
		{
			case STATED:
				return Fmt.rate(r.xpPerHour);
			case ABOUT:
				return "about " + Fmt.rate(r.xpPerHour);
			case RANGE:
				return Fmt.range(r.xpPerHour, r.xpPerHourHigh);
			case AT_LEAST:
				return "at least " + Fmt.rate(r.xpPerHour);
			case UPTO:
				return "up to " + Fmt.rate(r.xpPerHourHigh);
			case PER_RUN:
				return "per run";
			default:
				return "-";
		}
	}

	/** The gp per hour of a row in the long forms of the card (a cost is shown as a negative number of gp earned). */
	private static String gpPerHourLong(Row r)
	{
		switch (Costs.kind(r.method))
		{
			case STATED:
				return Fmt.plus(Fmt.rate(-r.gpPerHour));
			case ABOUT:
				return "about " + Fmt.plus(Fmt.rate(-r.gpPerHour));
			case RANGE:
				return Fmt.gpRange(-r.gpPerHour, -r.gpPerHourHigh);
			case AT_LEAST:
				return "at least " + Fmt.plus(Fmt.rate(-r.gpPerHour));
			case UPTO:
				return "up to " + Fmt.plus(Fmt.rate(-r.gpPerHourHigh));
			case PER_RUN:
				return "per run";
			default:
				return "-";
		}
	}
}
