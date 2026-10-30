package com.gpperxp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

/**
 * The plugin's settings. Flat on purpose: every item is declared here and this interface extends {@link Config}
 * alone, because RuneLite stores defaults only for the methods declared on the interface it is given. The group and
 * the keys are frozen at first release; renaming one silently discards every player's setting.
 *
 * <p>Choice: all twelve are hidden from RuneLite's settings screen, as the chat plugin hides its panel-only
 * settings; the panel's menu and boxes are where they are changed.
 */
@ConfigGroup(GpPerXpConfig.GROUP)
public interface GpPerXpConfig extends Config
{
	String GROUP = "gp-per-xp";

	@Range(min = 0, max = 99)
	@ConfigItem(
		keyName = "targetLevel",
		name = "Target level",
		description = "The level to work out the cost to reach; 0 means the next level.",
		position = 1,
		hidden = true
	)
	default int targetLevel()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "includeProfit",
		name = "Include profit methods",
		description = "Whether methods that make money are listed.",
		position = 2,
		hidden = true
	)
	default boolean includeProfit()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showAbove",
		name = "Show methods above my level",
		description = "Whether methods above your level are listed, greyed, after the others (the All levels button).",
		position = 3,
		hidden = true
	)
	default boolean showAbove()
	{
		return false;
	}

	@ConfigItem(
		keyName = "takeOffTax",
		name = "Take off the GE tax",
		description = "Whether the Grand Exchange tax is taken off what items sell for.",
		position = 4,
		hidden = true
	)
	default boolean takeOffTax()
	{
		return true;
	}

	@ConfigItem(
		keyName = "sort",
		name = "Sort",
		description = "The order of the list: cheapest gp per xp, fastest xp per hour, or best gp per hour.",
		position = 5,
		hidden = true
	)
	default Sort sort()
	{
		return Sort.CHEAPEST_GP_XP;
	}

	@ConfigItem(
		keyName = "sortReversed",
		name = "Sort reversed",
		description = "Whether the sort order is reversed.",
		position = 6,
		hidden = true
	)
	default boolean sortReversed()
	{
		return false;
	}

	@Range(min = 10, max = 100)
	@ConfigItem(
		keyName = "pace",
		name = "Pace",
		description = "The percent of the wiki's hourly rate you reach.",
		position = 8,
		hidden = true
	)
	default int pace()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "priceSource",
		name = "Price source",
		description = "Guide price, or the wiki's latest trade prices (reads the wiki's public price list; your IP address is visible to the wiki, nothing else is sent)",
		position = 9,
		hidden = true
	)
	default PriceSource priceSource()
	{
		return PriceSource.GUIDE;
	}

	@ConfigItem(
		keyName = "otherBands",
		name = "Show other level bands",
		description = "Whether a method that has a rate for several levels lists every level band.",
		position = 10,
		hidden = true
	)
	default boolean otherBands()
	{
		return false;
	}

	@ConfigItem(
		keyName = "view",
		name = "View",
		description = "Which view the panel shows: list, top (Top picks) or healing.",
		position = 11,
		hidden = true
	)
	default String view()
	{
		return "list";
	}

	@ConfigItem(
		keyName = "healSort",
		name = "Healing sort",
		description = "The order of the healing list.",
		position = 12,
		hidden = true
	)
	default HealSort healSort()
	{
		return HealSort.CHEAPEST_GP_HP;
	}

	@ConfigItem(
		keyName = "healSortReversed",
		name = "Healing sort reversed",
		description = "Whether the healing order is reversed.",
		position = 13,
		hidden = true
	)
	default boolean healSortReversed()
	{
		return false;
	}

	@ConfigItem(
		keyName = "skill",
		name = "Skill",
		description = "The skill the panel was last on.",
		position = 7,
		hidden = true
	)
	default String skill()
	{
		return "";
	}
}
