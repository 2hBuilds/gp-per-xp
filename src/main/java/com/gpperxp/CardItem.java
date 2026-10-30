package com.gpperxp;

/** One item line of the card: the picture's id, the "<qty> x <name>" text and the price shown at the right. */
public final class CardItem
{
	public final int itemId;
	public final String text;
	public final String price;

	public CardItem(int itemId, String text, String price)
	{
		this.itemId = itemId;
		this.text = text;
		this.price = price;
	}
}
