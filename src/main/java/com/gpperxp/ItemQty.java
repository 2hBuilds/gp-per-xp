package com.gpperxp;

/** An item id, its name and how many of it. */
public class ItemQty
{
	public final int itemId;
	/** The item name; null when not given. */
	public final String item;
	public final double qty;

	public ItemQty(int itemId, double qty)
	{
		this(itemId, null, qty);
	}

	public ItemQty(int itemId, String item, double qty)
	{
		this.itemId = itemId;
		this.item = item;
		this.qty = qty;
	}
}
