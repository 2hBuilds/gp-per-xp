package com.gpperxp;

import java.awt.image.BufferedImage;
import java.util.function.IntFunction;
import java.util.function.IntToLongFunction;

/** What the card needs from the plugin at the moment a row is hovered. */
final class CardContext
{
	final IntToLongFunction price;
	final boolean tax;
	final int currentXp;
	final int targetLevel;
	final IntFunction<BufferedImage> icon;
	/** The pace setting, 10 to 100. */
	final int pace;
	/** The date of the bundled table; null when unknown. */
	final String dataDate;
	/** The price source in words for the footer, "guide" or "live trades". */
	final String priceSource;

	CardContext(IntToLongFunction price, boolean tax, int currentXp, int targetLevel,
		IntFunction<BufferedImage> icon, int pace, String dataDate, String priceSource)
	{
		this.priceSource = priceSource;
		this.pace = pace;
		this.dataDate = dataDate;
		this.price = price;
		this.tax = tax;
		this.currentXp = currentXp;
		this.targetLevel = targetLevel;
		this.icon = icon;
	}
}
