package com.gpperxp;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class MethodsDataTest
{
	private static final Set<String> SKILLS = new HashSet<>(Arrays.asList(
		"Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting",
		"Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving",
		"Slayer", "Farming", "Runecraft", "Hunter", "Construction", "Sailing"));
	/** The skills that must keep at least 10 rows. */
	private static final Set<String> BIG_SKILLS = new HashSet<>(Arrays.asList(
		"Prayer", "Herblore", "Construction", "Crafting", "Smithing"));
	private static final String SOURCE_PREFIX = "https://oldschool.runescape.wiki/";
	private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

	private static List<TrainingMethod> rows()
	{
		return Methods.loadBundled(new Gson()).all();
	}

	private static String label(TrainingMethod m)
	{
		return m.skill + " / " + m.name + ": ";
	}

	@Test
	public void skillIsOneOfTheFiveSkills()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "skill must be one of " + SKILLS + ", was " + m.skill,
				SKILLS.contains(m.skill));
		}
	}

	@Test
	public void nameIsNotNullOrBlank()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "name must not be null or blank",
				m.name != null && !m.name.trim().isEmpty());
		}
	}

	@Test
	public void levelIsBetweenOneAndNinetyNine()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "level must be between 1 and 99, was " + m.level,
				m.level >= 1 && m.level <= 99);
		}
	}

	@Test
	public void xpPerActionIsGreaterThanZero()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "xpPerAction must be greater than 0, was " + m.xpPerAction,
				m.xpPerAction > 0);
		}
	}

	@Test
	public void exactlyOneRateShapePerRow()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "actionsPerHour must not be negative, was " + m.actionsPerHour,
				m.actionsPerHour >= 0);
			int shapes = 0;
			shapes += m.actionsPerHour > 0 ? 1 : 0;
			shapes += m.rateLow > 0 && m.rateHigh > 0 ? 1 : 0;
			shapes += m.rateHigh > 0 && m.rateLow <= 0 ? 1 : 0;
			shapes += m.rateLow > 0 && m.rateHigh <= 0 ? 1 : 0;
			shapes += "perRun".equals(m.rateKind) ? 1 : 0;
			shapes += "none".equals(m.rateKind) ? 1 : 0;
			assertTrue(label(m) + "must have at most one rate shape, found " + shapes, shapes <= 1);
		}
	}

	@Test
	public void rowWithNoItemsHasANote()
	{
		for (TrainingMethod m : rows())
		{
			if (m.inputs.isEmpty() && m.outputs.isEmpty())
			{
				assertTrue(label(m) + "has no inputs and no outputs, so it needs a note",
					(m.note != null && !m.note.trim().isEmpty())
						|| (m.outputsNote != null && !m.outputsNote.trim().isEmpty()));
			}
		}
	}

	@Test
	public void everyItemQtyHasPositiveIdAndQty()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "outputs must not be null", m.outputs != null);
			assertTrue(label(m) + "inputs must not be null", m.inputs != null);
			for (List<ItemQty> list : Arrays.asList(m.inputs, m.outputs))
			{
				for (ItemQty q : list)
				{
					assertTrue(label(m) + "ItemQty must not be null", q != null);
					assertTrue(label(m) + "ItemQty needs itemId > 0 and qty > 0, was itemId "
						+ q.itemId + " qty " + q.qty,
						q.itemId > 0 && q.qty > 0);
				}
			}
		}
	}

	@Test
	public void sourceStartsWithWikiUrl()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "source must start with " + SOURCE_PREFIX + ", was " + m.source,
				m.source != null && m.source.startsWith(SOURCE_PREFIX));
		}
	}

	@Test
	public void noteIsNullOrNotBlank()
	{
		for (TrainingMethod m : rows())
		{
			assertTrue(label(m) + "note must be null or not blank, was [" + m.note + "]",
				m.note == null || !m.note.trim().isEmpty());
		}
	}

	@Test
	public void noTwoRowsShareSkillAndName()
	{
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (TrainingMethod m : rows())
		{
			counts.merge(m.skill + " / " + m.name + " / " + m.rateLevel, 1, Integer::sum);
		}
		List<String> duplicates = new ArrayList<>();
		for (Map.Entry<String, Integer> e : counts.entrySet())
		{
			if (e.getValue() > 1)
			{
				duplicates.add(e.getKey() + " (x" + e.getValue() + ")");
			}
		}
		assertTrue("duplicate rows with the same skill and name: " + duplicates,
			duplicates.isEmpty());
	}

	@Test
	public void everySkillHasEnoughRows()
	{
		Methods methods = Methods.loadBundled(new Gson());
		for (String skill : BIG_SKILLS)
		{
			int count = methods.forSkill(skill).size();
			assertTrue(skill + " has " + count + " rows, expected at least 10", count >= 10);
		}
	}

	@Test
	public void updatedIsADate()
	{
		String updated = Methods.loadBundled(new Gson()).updated();
		assertTrue("updated must be null or match yyyy-mm-dd, was " + updated,
			updated == null || DATE.matcher(updated).matches());
	}

	@Test
	public void everyItemHasAName()
	{
		for (TrainingMethod m : rows())
		{
			for (ItemQty q : m.inputs)
			{
				assertTrue(label(m) + "input " + q.itemId + " has no item name", q.item != null && !q.item.trim().isEmpty());
			}
			for (ItemQty q : m.outputs)
			{
				assertTrue(label(m) + "output " + q.itemId + " has no item name", q.item != null && !q.item.trim().isEmpty());
			}
		}
	}
}
