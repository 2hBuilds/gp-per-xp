package com.gpperxp;

/** The settings the panel shows and writes; the plugin backs it with the config, a test with fields. */
public interface PanelSettings
{
	int targetLevel();

	void setTargetLevel(int level);

	boolean includeProfit();

	void setIncludeProfit(boolean on);

	boolean showAbove();

	void setShowAbove(boolean on);

	/** Whether a method with several level bands lists every band rather than the one that fits the player. */
	boolean otherBands();

	void setOtherBands(boolean on);

	boolean takeOffTax();

	void setTakeOffTax(boolean on);

	Sort sort();

	void setSort(Sort sort);

	boolean sortReversed();

	void setSortReversed(boolean on);

	/** The percent of the wiki's hourly rate the player reaches, 10 to 100. */
	int pace();

	void setPace(int percent);

	/** Where prices come from: the guide price or the wiki's live trade prices. */
	PriceSource priceSource();

	void setPriceSource(PriceSource source);

	/** Which view the list shows: "list", "top" (Top picks) or "healing" (Hitpoints only). */
	String view();

	void setView(String view);

	HealSort healSort();

	void setHealSort(HealSort sort);

	boolean healSortReversed();

	void setHealSortReversed(boolean on);

	/** The skill the picker was last on, "" when none. */
	String skill();

	void setSkill(String skill);
}
