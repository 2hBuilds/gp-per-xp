package com.gpperxp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToLongFunction;

/** The texts of a healing hover card. Pure: no Swing. */
public final class HealCard
{
	private static final String UNKNOWN = "?";

	public final String title;
	/** Each entry is {label, value}. */
	public final List<String[]> facts;
	/** The item's note, trimmed; "" for none. */
	public final String note;
	public final int itemId;

	private HealCard(String title, List<String[]> facts, String note, int itemId)
	{
		this.title = title;
		this.facts = facts;
		this.note = note;
		this.itemId = itemId;
	}

	public static HealCard of(HealingItem item, IntToLongFunction price, int maxHp)
	{
		final List<String[]> facts = new ArrayList<>();
		facts.add(new String[] {"Heals per portion", perPortion(item, maxHp)});
		facts.add(new String[] {"Portions", Integer.toString(item.portionCount())});
		facts.add(new String[] {"Heals per item", HealModel.hpText(item, maxHp)});
		final long cost = price.applyAsLong(item.itemId);
		final int hp = item.healsPerItem(maxHp);
		facts.add(new String[] {"Price", cost <= 0 ? UNKNOWN : Fmt.grouped(cost)});
		facts.add(new String[] {"GP per HP", cost <= 0 || hp <= 0 ? UNKNOWN : Fmt.gpXp((double) cost / hp)});
		return new HealCard(item.name, facts, item.note == null ? "" : item.note.trim(), item.itemId);
	}

	private static String perPortion(HealingItem item, int maxHp)
	{
		final int low = item.healsPerPortion(maxHp);
		final int high = item.healsPerPortionHigh(maxHp);
		if (low != high)
		{
			return low + "-" + high;
		}
		if (item.isPercent())
		{
			final String how = (item.healsFlat > 0 ? item.healsFlat + " + " : "") + item.healsPercent + "% of max HP";
			return low + " (" + how + ")";
		}
		return Integer.toString(low);
	}

	/** The card as the painter draws it: the facts, the item's picture, the note, the footer. */
	public CardModel toModel(String footer)
	{
		return CardModel.healing(title, facts, itemId, note, footer);
	}
}
