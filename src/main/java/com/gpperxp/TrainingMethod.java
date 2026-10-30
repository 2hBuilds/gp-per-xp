package com.gpperxp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One way to train a skill; {@code note} may be null. */
public class TrainingMethod
{
	public final String skill;
	public final String name;
	public final int level;
	public final double xpPerAction;
	public final List<ItemQty> inputs;
	public final List<ItemQty> outputs;
	/** Actions per hour as the wiki states it; 0 or less means no stated rate. */
	public final double actionsPerHour;
	public final String source;
	public final String note;
	/** The low end of a stated range of actions per hour; 0 when unset. */
	public final double rateLow;
	/** The high end of a range, or the figure of an "up to" rate; 0 when unset. */
	public final double rateHigh;
	/** "about", "perRun" or "none"; null when unset. */
	public final String rateKind;
	/** The level a banded rate is stated at; 0 when the method has one rate. */
	public final int rateLevel;
	/** What the method needs; never null. */
	public final List<Gate> requires;
	/** What the method gives besides priced outputs; may be null. */
	public final String outputsNote;
	/** Null when the rate is per action; "xp" when one action is one xp. */
	public final String rateUnit;
	/** True when the wiki states no cost for the method (items used or caught); its gp figures are unknown, not zero. */
	public final boolean costsNotStated;
	/** True when the skill's wiki training page recommends the method; false when unset. */
	public final boolean recommended;

	public TrainingMethod(String skill, String name, int level, double xpPerAction, List<ItemQty> inputs,
		List<ItemQty> outputs, double actionsPerHour, String source, String note)
	{
		this(skill, name, level, xpPerAction, inputs, outputs, actionsPerHour, source, note, 0, 0, null, 0,
			Collections.<Gate>emptyList(), null, null, false, false);
	}

	private TrainingMethod(String skill, String name, int level, double xpPerAction, List<ItemQty> inputs,
		List<ItemQty> outputs, double actionsPerHour, String source, String note, double rateLow, double rateHigh,
		String rateKind, int rateLevel, List<Gate> requires, String outputsNote, String rateUnit,
		boolean costsNotStated, boolean recommended)
	{
		this.recommended = recommended;
		this.rateUnit = rateUnit;
		this.costsNotStated = costsNotStated;
		this.skill = skill;
		this.name = name;
		this.level = level;
		this.xpPerAction = xpPerAction;
		this.inputs = inputs;
		this.outputs = outputs;
		this.actionsPerHour = actionsPerHour;
		this.source = source;
		this.note = note;
		this.rateLow = rateLow;
		this.rateHigh = rateHigh;
		this.rateKind = rateKind;
		this.rateLevel = rateLevel;
		this.requires = requires;
		this.outputsNote = outputsNote;
	}

	/** A copy with the version 2 fields; a null {@code requires} becomes an empty list. */
	public TrainingMethod withV2(double rateLow, double rateHigh, String rateKind, int rateLevel, List<Gate> requires,
		String outputsNote)
	{
		final List<Gate> gates = requires == null ? Collections.<Gate>emptyList()
			: Collections.unmodifiableList(new ArrayList<>(requires));
		return new TrainingMethod(skill, name, level, xpPerAction, inputs, outputs, actionsPerHour, source, note,
			rateLow, rateHigh, rateKind, rateLevel, gates, outputsNote, rateUnit, costsNotStated, recommended);
	}

	/** A copy with the rate unit ("xp" or null). */
	public TrainingMethod withRateUnit(String unit)
	{
		return new TrainingMethod(skill, name, level, xpPerAction, inputs, outputs, actionsPerHour, source, note,
			rateLow, rateHigh, rateKind, rateLevel, requires, outputsNote, unit, costsNotStated, recommended);
	}

	/** A copy at another hourly rate (actions per hour and both range ends); everything else is kept. */
	TrainingMethod withRates(double actionsPerHour, double rateLow, double rateHigh)
	{
		return new TrainingMethod(skill, name, level, xpPerAction, inputs, outputs, actionsPerHour, source, note,
			rateLow, rateHigh, rateKind, rateLevel, requires, outputsNote, rateUnit, costsNotStated, recommended);
	}

	/** A copy with the cost-not-stated flag. */
	public TrainingMethod withCostsNotStated(boolean on)
	{
		return new TrainingMethod(skill, name, level, xpPerAction, inputs, outputs, actionsPerHour, source, note,
			rateLow, rateHigh, rateKind, rateLevel, requires, outputsNote, rateUnit, on, recommended);
	}

	/** A copy with the recommended flag. */
	public TrainingMethod withRecommended(boolean on)
	{
		return new TrainingMethod(skill, name, level, xpPerAction, inputs, outputs, actionsPerHour, source, note,
			rateLow, rateHigh, rateKind, rateLevel, requires, outputsNote, rateUnit, costsNotStated, on);
	}
}
