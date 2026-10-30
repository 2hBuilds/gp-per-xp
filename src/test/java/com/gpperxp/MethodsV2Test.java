package com.gpperxp;

import com.google.gson.Gson;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Quest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** The version 2 fields as {@link Methods} reads them, and the bundled data's rules for them. */
public class MethodsV2Test
{
	private static final Set<String> SKILLS = new HashSet<>(Arrays.asList(
		"Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting",
		"Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving",
		"Slayer", "Farming", "Runecraft", "Hunter", "Construction", "Sailing"));

	private static final String RANGE_ROW = "{\"updated\":\"2026-10-30\",\"methods\":[{"
		+ "\"skill\":\"Test\",\"name\":\"Range row\",\"level\":70,\"xpPerAction\":10.0,"
		+ "\"inputs\":[],\"outputs\":[],\"actionsPerHour\":0,\"source\":\"https://oldschool.runescape.wiki/x\","
		+ "\"note\":null,\"rateLow\":1800,\"rateHigh\":2200,\"rateKind\":\"range\",\"rateLevel\":70,"
		+ "\"requires\":[{\"skill\":\"Magic\",\"level\":90},{\"quest\":\"Dragon Slayer II\"},"
		+ "{\"diary\":\"Ardougne hard\"},{\"text\":\"Smiths' Uniform\"}],"
		+ "\"outputsNote\":\"supply crates\"}]}";

	private static final String V1_ROW = "{\"updated\":\"2026-10-30\",\"methods\":[{"
		+ "\"skill\":\"Test\",\"name\":\"Old row\",\"level\":5,\"xpPerAction\":10.0,"
		+ "\"inputs\":[],\"outputs\":[],\"actionsPerHour\":1000,\"source\":\"https://oldschool.runescape.wiki/x\","
		+ "\"note\":null}]}";

	private static String label(TrainingMethod m)
	{
		return m.skill + " / " + m.name + ": ";
	}

	@Test
	public void v2RowIsReadInFull()
	{
		final TrainingMethod m = Methods.load(new Gson(), new StringReader(RANGE_ROW)).all().get(0);
		assertEquals("Test", m.skill);
		assertEquals("Range row", m.name);
		assertEquals(70, m.level);
		assertEquals(10.0, m.xpPerAction, 1e-9);
		assertEquals(0.0, m.actionsPerHour, 1e-9);
		assertEquals("https://oldschool.runescape.wiki/x", m.source);
		assertNull(m.note);
		assertEquals(1800.0, m.rateLow, 1e-9);
		assertEquals(2200.0, m.rateHigh, 1e-9);
		assertEquals("range", m.rateKind);
		assertEquals(70, m.rateLevel);
		assertEquals("supply crates", m.outputsNote);
		assertEquals(RateKind.RANGE, Costs.kind(m));

		final List<Gate> requires = m.requires;
		assertEquals(4, requires.size());
		assertEquals("Magic", requires.get(0).skill);
		assertEquals(90, requires.get(0).level);
		assertNull(requires.get(0).quest);
		assertEquals("Dragon Slayer II", requires.get(1).quest);
		assertEquals("Ardougne hard", requires.get(2).diary);
		assertEquals("Smiths' Uniform", requires.get(3).text);
	}

	@Test
	public void v1RowReadsDefaults()
	{
		final TrainingMethod m = Methods.load(new Gson(), new StringReader(V1_ROW)).all().get(0);
		assertEquals(0.0, m.rateLow, 1e-9);
		assertEquals(0.0, m.rateHigh, 1e-9);
		assertNull(m.rateKind);
		assertEquals(0, m.rateLevel);
		assertTrue(m.requires != null && m.requires.isEmpty());
		assertNull(m.outputsNote);
	}

	@Test
	public void bundledRowsHaveAtMostOneRateShape()
	{
		for (TrainingMethod m : Methods.loadBundled(new Gson()).all())
		{
			int shapes = 0;
			shapes += m.actionsPerHour > 0 ? 1 : 0;
			shapes += m.rateLow > 0 && m.rateHigh > 0 ? 1 : 0;
			shapes += m.rateHigh > 0 && m.rateLow <= 0 ? 1 : 0;
			shapes += m.rateLow > 0 && m.rateHigh <= 0 ? 1 : 0;
			shapes += "perRun".equals(m.rateKind) ? 1 : 0;
			shapes += "none".equals(m.rateKind) ? 1 : 0;
			assertTrue(label(m) + "must have one rate shape at most, found " + shapes, shapes <= 1);
		}
	}

	@Test
	public void bundledRateUnitIsNullOrXpWithOneXpPerAction()
	{
		for (TrainingMethod m : Methods.loadBundled(new Gson()).all())
		{
			assertTrue(label(m) + "rateUnit must be null or xp, was " + m.rateUnit,
				m.rateUnit == null || "xp".equals(m.rateUnit));
			if ("xp".equals(m.rateUnit))
			{
				assertEquals(label(m) + "an xp-unit row has xpPerAction 1.0", 1.0, m.xpPerAction, 1e-9);
			}
		}
	}

	@Test
	public void bundledCostNotStatedRowsHaveNoItemsAndExplainThemselves()
	{
		for (TrainingMethod m : Methods.loadBundled(new Gson()).all())
		{
			if (!m.costsNotStated)
			{
				continue;
			}
			assertTrue(label(m) + "a cost-not-stated row has no inputs", m.inputs.isEmpty());
			assertTrue(label(m) + "a cost-not-stated row has no outputs", m.outputs.isEmpty());
			final boolean noted = (m.note != null && !m.note.trim().isEmpty())
				|| (m.outputsNote != null && !m.outputsNote.trim().isEmpty());
			assertTrue(label(m) + "a cost-not-stated row needs a note or an outputsNote", noted);
		}
	}

	@Test
	public void bundledRateLevelIsZeroOrAFromLevelTo99()
	{
		for (TrainingMethod m : Methods.loadBundled(new Gson()).all())
		{
			assertTrue(label(m) + "rateLevel must be 0 or from level to 99, was " + m.rateLevel,
				m.rateLevel == 0 || (m.rateLevel >= m.level && m.rateLevel <= 99));
		}
	}

	@Test
	public void bundledGatesNameExactlyOneThing()
	{
		final Set<String> quests = new HashSet<>();
		for (Quest q : Quest.values())
		{
			quests.add(q.getName());
		}
		for (TrainingMethod m : Methods.loadBundled(new Gson()).all())
		{
			for (Gate g : m.requires)
			{
				int named = (g.skill != null ? 1 : 0) + (g.quest != null ? 1 : 0)
					+ (g.diary != null ? 1 : 0) + (g.text != null ? 1 : 0);
				assertEquals(label(m) + "a gate names exactly one of skill, quest, diary, text", 1, named);
				if (g.skill != null)
				{
					assertTrue(label(m) + "gate skill must be one of the 24 skills, was " + g.skill,
						SKILLS.contains(g.skill));
					assertTrue(label(m) + "gate skill must not be the row's own skill", !g.skill.equals(m.skill));
					assertTrue(label(m) + "gate level must be 1 to 99, was " + g.level,
						g.level >= 1 && g.level <= 99);
				}
				if (g.quest != null)
				{
					assertTrue(label(m) + "gate quest must be a RuneLite quest name, was " + g.quest,
						quests.contains(g.quest));
				}
			}
		}
	}

	@Test
	public void bundledOutputsNoteIsNullOrNotBlank()
	{
		for (TrainingMethod m : Methods.loadBundled(new Gson()).all())
		{
			assertTrue(label(m) + "outputsNote must be null or not blank, was [" + m.outputsNote + "]",
				m.outputsNote == null || !m.outputsNote.trim().isEmpty());
		}
	}

	@Test
	public void recommendedReadsTrueWhenMarked()
	{
		final String row = V1_ROW.replace("}]}", ",\"recommended\":true}]}");
		assertTrue(Methods.load(new Gson(), new StringReader(row)).all().get(0).recommended);
	}

	@Test
	public void recommendedReadsFalseWhenAbsent()
	{
		assertFalse(Methods.load(new Gson(), new StringReader(V1_ROW)).all().get(0).recommended);
	}
}
