package com.gpperxp;

import java.awt.image.BufferedImage;
import java.util.function.IntFunction;
import java.util.function.IntToLongFunction;

/** What the healing card needs from the plugin at the moment a row is hovered. */
final class HealContext
{
	final IntToLongFunction price;
	final IntFunction<BufferedImage> icon;
	final int maxHp;
	/** The date of the bundled file; null when unknown. */
	final String dataDate;
	/** The price source in words for the footer, "guide" or "live trades". */
	final String priceSource;

	HealContext(IntToLongFunction price, IntFunction<BufferedImage> icon, int maxHp, String dataDate,
		String priceSource)
	{
		this.price = price;
		this.icon = icon;
		this.maxHp = maxHp;
		this.dataDate = dataDate;
		this.priceSource = priceSource;
	}
}
