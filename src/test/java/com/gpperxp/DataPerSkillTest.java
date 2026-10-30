package com.gpperxp;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The shipped methods file, checked one skill at a time. Each skill has its own test, so a bad row fails the test of
 * its skill, and every failure message starts with the row as {@code skill / name}.
 *
 * <p>The engine checks run every row at a flat price of 1000 gp for each item, and again at 0, through the costs, the
 * hover card and the sidebar list.
 */
public class DataPerSkillTest
{
	/** The bundled data, loaded once for the whole class. */
	private static final Methods METHODS = Methods.loadBundled(new Gson());
	/** The twenty-four skills in the data; a skill added to the data needs a test of its own in this class. */
	private static final Set<String> SKILLS = new HashSet<>(Arrays.asList(
		"Agility", "Attack", "Construction", "Cooking", "Crafting", "Defence", "Farming", "Firemaking", "Fishing", "Fletching", "Herblore",
		"Hitpoints", "Hunter", "Magic", "Mining", "Prayer", "Ranged", "Runecraft", "Sailing", "Slayer", "Smithing", "Strength",
		"Thieving", "Woodcutting"));
	private static final String WIKI_PREFIX = "https://oldschool.runescape.wiki/w/";
	private static final int MIN_ROWS = 5;
	private static final int MAX_NAME_LENGTH = 70;
	private static final int MIN_LEVEL = 1;
	private static final int MAX_LEVEL = 99;
	private static final double MIN_XP_PER_HOUR = 100;
	private static final double MAX_XP_PER_HOUR = 5_000_000;
	private static final double MAX_ACTIONS_PER_HOUR = 60_000;
	/** Every item costs 1000 gp. */
	private static final IntToLongFunction PRICED = id -> 1000L;
	/** Every item costs nothing. */
	private static final IntToLongFunction UNPRICED = id -> 0L;
	/** The player's xp for the hover card: 1154, which is level 10. */
	private static final int CARD_XP = 1154;
	private static final int CARD_TARGET_LEVEL = 0;
	/** The player's xp for the sidebar list. */
	private static final int LIST_XP = 0;

	@Test
	public void agility()
	{
		checkSkill("Agility");
	}

	@Test
	public void construction()
	{
		checkSkill("Construction");
	}

	@Test
	public void cooking()
	{
		checkSkill("Cooking");
	}

	@Test
	public void crafting()
	{
		checkSkill("Crafting");
	}

	@Test
	public void farming()
	{
		checkSkill("Farming");
	}

	@Test
	public void firemaking()
	{
		checkSkill("Firemaking");
	}

	@Test
	public void fishing()
	{
		checkSkill("Fishing");
	}

	@Test
	public void fletching()
	{
		checkSkill("Fletching");
	}

	@Test
	public void herblore()
	{
		checkSkill("Herblore");
	}

	@Test
	public void hunter()
	{
		checkSkill("Hunter");
	}

	@Test
	public void magic()
	{
		checkSkill("Magic");
	}

	@Test
	public void mining()
	{
		checkSkill("Mining");
	}

	@Test
	public void prayer()
	{
		checkSkill("Prayer");
	}

	@Test
	public void ranged()
	{
		checkSkill("Ranged");
	}

	@Test
	public void runecraft()
	{
		checkSkill("Runecraft");
	}

	@Test
	public void sailing()
	{
		checkSkill("Sailing");
	}

	@Test
	public void slayer()
	{
		checkSkill("Slayer");
	}

	@Test
	public void smithing()
	{
		checkSkill("Smithing");
	}

	@Test
	public void thieving()
	{
		checkSkill("Thieving");
	}

	@Test
	public void woodcutting()
	{
		checkSkill("Woodcutting");
	}

	@Test
	public void attack()
	{
		checkSkill("Attack");
	}

	@Test
	public void defence()
	{
		checkSkill("Defence");
	}

	@Test
	public void hitpoints()
	{
		checkSkill("Hitpoints");
	}

	@Test
	public void strength()
	{
		checkSkill("Strength");
	}

	@Test
	public void allSkillsAreCovered()
	{
		assertEquals("the skills in the data must match the skills this class tests", SKILLS,
			new HashSet<>(METHODS.skills()));
	}

	/** The whole check for one skill: its row count, each row, its note, its names, then the engine at two prices. */
	private static void checkSkill(String skill)
	{
		final List<TrainingMethod> methods = METHODS.forSkill(skill);
		assertTrue(skill + ": has " + methods.size() + " rows, expected at least " + MIN_ROWS,
			methods.size() >= MIN_ROWS);
		for (TrainingMethod m : methods)
		{
			checkRow(skill, m);
		}
		for (TrainingMethod m : methods)
		{
			checkNote(m);
		}
		checkNamesUnique(skill, methods);
		checkEngine(skill, methods, PRICED);
		checkEngine(skill, methods, UNPRICED);
	}

	/** Every field of one row is in range, and every item has an id, a name and a quantity. */
	private static void checkRow(String skill, TrainingMethod m)
	{
		assertTrue(label(m) + "skill must be " + skill + ", was " + m.skill, skill.equals(m.skill));
		assertTrue(label(m) + "name must not be blank", m.name != null && !m.name.trim().isEmpty());
		assertTrue(label(m) + "name must be at most " + MAX_NAME_LENGTH + " characters, was " + m.name.length(),
			m.name.length() <= MAX_NAME_LENGTH);
		assertTrue(label(m) + "level must be from " + MIN_LEVEL + " to " + MAX_LEVEL + ", was " + m.level,
			m.level >= MIN_LEVEL && m.level <= MAX_LEVEL);
		assertTrue(label(m) + "xpPerAction must be above 0 and finite, was " + m.xpPerAction,
			m.xpPerAction > 0 && Double.isFinite(m.xpPerAction));
		assertTrue(label(m) + "actionsPerHour must be 0 or more and finite, was " + m.actionsPerHour,
			m.actionsPerHour >= 0 && Double.isFinite(m.actionsPerHour));
		if (m.actionsPerHour > 0)
		{
			final double xpPerHour = m.xpPerAction * m.actionsPerHour;
			assertTrue(label(m) + "xpPerAction times actionsPerHour must be from 100 to 5,000,000, was " + xpPerHour,
				xpPerHour >= MIN_XP_PER_HOUR && xpPerHour <= MAX_XP_PER_HOUR);
		}
		if (!"xp".equals(m.rateUnit))
		{
			for (double rate : new double[] {m.actionsPerHour, m.rateLow, m.rateHigh})
			{
				assertTrue(label(m) + "a per-action rate must be at most 60,000, was " + rate,
					rate <= MAX_ACTIONS_PER_HOUR);
			}
		}
		assertTrue(label(m) + "inputs and outputs must not be null", m.inputs != null && m.outputs != null);
		for (List<ItemQty> items : Arrays.asList(m.inputs, m.outputs))
		{
			for (ItemQty q : items)
			{
				assertTrue(label(m) + "an item must not be null", q != null);
				assertTrue(label(m) + "item " + q.itemId + " must have an id above 0", q.itemId > 0);
				assertTrue(label(m) + "item " + q.itemId + " must have a name",
					q.item != null && !q.item.trim().isEmpty());
				assertTrue(label(m) + "item " + q.itemId + " must have a quantity above 0 and finite, was " + q.qty,
					q.qty > 0 && Double.isFinite(q.qty));
			}
		}
		assertTrue(label(m) + "source must start with " + WIKI_PREFIX + " and have no space, was " + m.source,
			m.source != null && m.source.startsWith(WIKI_PREFIX) && !m.source.contains(" "));
		assertTrue(label(m) + "note must be null or not blank, was [" + m.note + "]",
			m.note == null || !m.note.trim().isEmpty());
	}

	/**
	 * A row with no items, or a quantity that is not a whole number, says why in a note. (A row with no stated rate
	 * needs none: the list shows "-" and the card says "not stated".)
	 */
	private static void checkNote(TrainingMethod m)
	{
		if (m.inputs.isEmpty() && m.outputs.isEmpty())
		{
			assertNote(m, "has no items");
		}
		for (List<ItemQty> items : Arrays.asList(m.inputs, m.outputs))
		{
			for (ItemQty q : items)
			{
				if (q.qty != Math.floor(q.qty))
				{
					assertNote(m, "has a quantity that is not a whole number (" + q.qty + ")");
				}
			}
		}
	}

	private static void assertNote(TrainingMethod m, String reason)
	{
		assertTrue(label(m) + reason + ", so it needs a note", m.note != null && !m.note.trim().isEmpty());
	}

	/** No two rows of one skill share a name and rate level, ignoring case (the bands of one method share a name). */
	private static void checkNamesUnique(String skill, List<TrainingMethod> methods)
	{
		final Set<String> seen = new HashSet<>();
		for (TrainingMethod m : methods)
		{
			if (!seen.add(m.name.toLowerCase(Locale.ROOT) + "/" + m.rateLevel))
			{
				fail(label(m) + "name is not unique within " + skill + ", ignoring case");
			}
		}
	}

	/** The engine copes with every row at one flat price: its costs, its hover card and the sidebar list. */
	private static void checkEngine(String skill, List<TrainingMethod> methods, IntToLongFunction price)
	{
		for (TrainingMethod m : methods)
		{
			final Row row = Costs.row(m, price, false);
			assertTrue(label(m) + "gp per xp must be finite, was " + row.gpPerXp, Double.isFinite(row.gpPerXp));
			assertTrue(label(m) + "xp per hour must be finite, was " + row.xpPerHour, Double.isFinite(row.xpPerHour));
			assertTrue(label(m) + "gp per hour must be finite, was " + row.gpPerHour, Double.isFinite(row.gpPerHour));
			final CardModel card = CardModel.of(row, price, false, CARD_XP, CARD_TARGET_LEVEL);
			assertEquals(label(m) + "card title must be the name", m.name, card.title);
			assertEquals(label(m) + "card must have 5 facts", 5, card.facts.size());
		}
		final List<ListLine> lines = ListModel.build(methods, price, false, Sort.CHEAPEST_GP_XP, LIST_XP, true, true,
			false, Standing.ALL, true);
		assertEquals(skill + ": the list must have one line per row", methods.size(), lines.size());
		for (ListLine line : lines)
		{
			final TrainingMethod m = line.row.method;
			assertNotBlank(label(m) + "list name", line.name);
			assertNotBlank(label(m) + "list level", line.level);
			assertNotBlank(label(m) + "list gp per xp", line.gpXp);
			assertNotBlank(label(m) + "list xp per hour", line.xpH);
			assertNotBlank(label(m) + "list gp per hour", line.gpH);
		}
	}

	private static void assertNotBlank(String what, String text)
	{
		assertTrue(what + " must not be blank, was [" + text + "]", text != null && !text.trim().isEmpty());
	}

	/** A row's name in every failure message: "skill / name: ". */
	private static String label(TrainingMethod m)
	{
		return m.skill + " / " + m.name + ": ";
	}
}
