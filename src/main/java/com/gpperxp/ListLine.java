package com.gpperxp;

/** One line of the sidebar list: a row and the texts it shows. */
public final class ListLine
{
	public final Row row;
	public final String name;
	public final String level;
	public final String gpXp;
	public final String xpH;
	public final String gpH;
	public final Money money;
	public final boolean greyed;
	public final boolean unpriced;

	public ListLine(Row row, String name, String level, String gpXp, String xpH, String gpH, Money money,
		boolean greyed, boolean unpriced)
	{
		this.row = row;
		this.name = name;
		this.level = level;
		this.gpXp = gpXp;
		this.xpH = xpH;
		this.gpH = gpH;
		this.money = money;
		this.greyed = greyed;
		this.unpriced = unpriced;
	}
}
