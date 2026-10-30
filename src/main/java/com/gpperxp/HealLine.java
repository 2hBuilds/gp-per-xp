package com.gpperxp;

/** One line of the healing list: the item and the texts it shows. */
public final class HealLine
{
	public final HealingItem item;
	public final String name;
	public final String gpHp;
	public final String hp;
	public final String price;
	public final boolean unpriced;

	HealLine(HealingItem item, String name, String gpHp, String hp, String price, boolean unpriced)
	{
		this.item = item;
		this.name = name;
		this.gpHp = gpHp;
		this.hp = hp;
		this.price = price;
		this.unpriced = unpriced;
	}
}
