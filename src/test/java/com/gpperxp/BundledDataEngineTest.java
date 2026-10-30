package com.gpperxp;

import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.function.IntToLongFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * The real engine run over every row of the shipped methods file, so a bad row (a zero rate, a row with no
 * inputs, a number that is not finite) fails here with its name in the message.
 *
 * <p>Every failure message starts with the row as {@code skill / name}. Prices are flat: each item costs 1000 gp,
 * or nothing for the unpriced test.
 */
public class BundledDataEngineTest
{
	private static final String[] SKILLS = {"Prayer", "Herblore", "Construction", "Crafting", "Smithing"};
	private static final Methods METHODS = Methods.loadBundled(new Gson());
	private static final List<TrainingMethod> ALL = METHODS.all();
	private static final IntToLongFunction PRICED = id -> 1000L;
	private static final IntToLongFunction UNPRICED = id -> 0L;
	/** The player the list tests use: 1154 xp, which is level 10. */
	private static final int PLAYER_XP = 1154;
	private static final int[] CURRENT_XP = {-1, 0, 1154, 13034431};
	private static final int[] TARGET_LEVELS = {0, 1, 50, 92, 99};
	/** Slack when comparing two costs, in gp. */
	private static final double COST_SLACK = 1e-9;
	/**
	 * Slack when checking a trip's xp, in xp. The action count is a ceiling of a double quotient, and a quotient
	 * that lands a hair under a whole number would fail a check that holds in real arithmetic.
	 */
	private static final double XP_SLACK = 1e-6;

	@Test
	public void everyRowCostsAreFinite()
	{
		for (boolean tax : new boolean[] {false, true})
		{
			for (TrainingMethod m : ALL)
			{
				final Row row = Costs.row(m, PRICED, tax);
				assertTrue(who(m) + ": gp per xp is " + row.gpPerXp + " (tax " + tax + ")",
					Double.isFinite(row.gpPerXp));
				assertTrue(who(m) + ": xp per hour is " + row.xpPerHour + " (tax " + tax + ")",
					Double.isFinite(row.xpPerHour));
				assertTrue(who(m) + ": gp per hour is " + row.gpPerHour + " (tax " + tax + ")",
					Double.isFinite(row.gpPerHour));
				assertTrue(who(m) + ": not priced although every price is 1000 (tax " + tax + ")", row.priced);
				assertTrue(who(m) + ": xp per hour is " + row.xpPerHour + ", not above 0 (tax " + tax + ")",
					m.actionsPerHour <= 0 || row.xpPerHour > 0);
			}
		}
	}

	@Test
	public void noPricesMeansUnpriced()
	{
		for (TrainingMethod m : ALL)
		{
			// A free row has no items to price, so it is always priced; every other row needs its prices.
			assertEquals(who(m) + ": priced state with every price 0", Costs.free(m),
				Costs.row(m, UNPRICED, false).priced);
		}
	}

	@Test
	public void taxNeverLowersCost()
	{
		for (TrainingMethod m : ALL)
		{
			final double untaxed = Costs.costPerAction(m, PRICED, false);
			final double taxed = Costs.costPerAction(m, PRICED, true);
			assertTrue(who(m) + ": cost with tax " + taxed + " is below the cost without it, " + untaxed,
				taxed >= untaxed - COST_SLACK);
		}
	}

	@Test
	public void listModelKeepsEveryRow()
	{
		for (String skill : SKILLS)
		{
			final List<TrainingMethod> methods = METHODS.forSkill(skill);
			for (Sort sort : Sort.values())
			{
				for (boolean reversed : new boolean[] {false, true})
				{
					final List<ListLine> lines = ListModel.build(methods, PRICED, false, sort, PLAYER_XP, true, true,
						reversed, Standing.ALL, true);
					assertEquals(skill + " / " + sort + ": line count", methods.size(), lines.size());
					for (TrainingMethod m : methods)
					{
						assertEquals(who(m) + ": listed other than once under " + sort + " reversed " + reversed, 1,
							countOfMethod(lines, m));
					}
				}
			}
		}
	}

	/** How many lines belong to exactly this method (a banded row's list name carries its band). */
	private static int countOfMethod(List<ListLine> lines, TrainingMethod m)
	{
		int n = 0;
		for (ListLine line : lines)
		{
			if (line.row.method == m)
			{
				n++;
			}
		}
		return n;
	}

	@Test
	public void listModelFilters()
	{
		final int playerXp = Xp.forLevel(50);
		for (String skill : SKILLS)
		{
			final List<TrainingMethod> methods = METHODS.forSkill(skill);
			for (Sort sort : Sort.values())
			{
				for (ListLine line : ListModel.build(methods, PRICED, false, sort, playerXp, true, false, false, Standing.ALL, true))
				{
					assertTrue(who(line.row.method) + ": level " + line.row.method.level
						+ " is listed for a player at level 50 with showAbove off", line.row.method.level <= 50);
				}
				for (ListLine line : ListModel.build(methods, PRICED, false, sort, playerXp, false, false, false, Standing.ALL, true))
				{
					assertNotEquals(who(line.row.method) + ": a profit row is listed with includeProfit off",
						Money.EARN, line.money);
				}
			}
		}
	}

	private static List<String> namesOf(List<ListLine> lines)
	{
		final List<String> names = new java.util.ArrayList<>();
		for (ListLine line : lines)
		{
			names.add(line.row.method.name);
		}
		return names;
	}

	@Test
	public void listModelReverseKeepsTheSameRows()
	{
		for (String skill : SKILLS)
		{
			final List<TrainingMethod> methods = METHODS.forSkill(skill);
			for (Sort sort : Sort.values())
			{
				final List<String> normal = namesOf(ListModel.build(methods, PRICED, false, sort, PLAYER_XP, true, true,
					false, Standing.ALL, true));
				final List<String> reversed = namesOf(ListModel.build(methods, PRICED, false, sort, PLAYER_XP, true, true,
					true, Standing.ALL, true));
				assertEquals(skill + " / " + sort + ": reversing must keep the same rows", new HashSet<>(normal),
					new HashSet<>(reversed));
			}
		}
	}

	@Test
	public void cardModelForEveryRow()
	{
		for (TrainingMethod m : ALL)
		{
			final Row row = Costs.row(m, PRICED, false);
			for (int currentXp : CURRENT_XP)
			{
				for (int targetLevel : TARGET_LEVELS)
				{
					final String where = who(m) + " (xp " + currentXp + ", target " + targetLevel + ")";
					final CardModel card = CardModel.of(row, PRICED, false, currentXp, targetLevel);
					assertEquals(where + ": title", m.name, card.title);
					assertEquals(where + ": fact count", 5, card.facts.size());
					for (CardItem item : card.uses)
					{
						assertFalse(where + ": empty price on " + item.text, item.price.isEmpty());
					}
					for (CardItem item : card.makes)
					{
						assertFalse(where + ": empty price on " + item.text, item.price.isEmpty());
					}
					if (currentXp < 0)
					{
						assertNull(where + ": a trip with xp unknown", card.trip);
					}
					else if (Xp.targetXp(currentXp, targetLevel) <= currentXp)
					{
						assertNull(where + ": a trip to a target already passed", card.trip);
					}
					else
					{
						assertNotNull(where + ": no trip", card.trip);
					}
				}
			}
		}
	}

	@Test
	public void tripToNinetyTwo()
	{
		final int target = Xp.forLevel(92);
		for (TrainingMethod m : ALL)
		{
			final CardModel card = CardModel.of(Costs.row(m, PRICED, false), PRICED, false, 0, 92);
			assertNotNull(who(m) + ": no trip from 0 xp to 92", card.trip);
			assertEquals(who(m) + ": heading", "To level 92", card.trip.heading);
			assertEquals(who(m) + ": xp text", Fmt.grouped(target) + " xp", card.trip.xp);

			final Trip trip = Costs.trip(m, PRICED, false, 0, target);
			assertTrue(who(m) + ": " + trip.actions + " actions do not reach 92",
				trip.actions * m.xpPerAction + XP_SLACK >= target);
			assertTrue(who(m) + ": " + trip.actions + " actions is one more than needed",
				(trip.actions - 1) * m.xpPerAction < target);
		}
	}

	@Test
	public void everyCardPaints()
	{
		for (TrainingMethod m : ALL)
		{
			final CardModel card = CardModel.of(Costs.row(m, PRICED, false), PRICED, false, 1154, 0);
			final BufferedImage image = CardPainter.paint(card, id -> null);
			assertEquals(who(m) + ": card width", 230, image.getWidth());
			assertTrue(who(m) + ": card is " + image.getHeight() + " px tall, under 100",
				image.getHeight() >= 100);
		}
	}

	@Test
	public void noRowIsFree()
	{
		// A row with no items at all would look free at any price, so it must say why in a note.
		for (TrainingMethod m : ALL)
		{
			assertTrue(who(m) + ": has no inputs, no outputs and no note",
				!m.inputs.isEmpty() || !m.outputs.isEmpty() || (m.note != null && !m.note.trim().isEmpty()));
		}
	}

	/** A row's name in every failure message: "skill / name". */
	private static String who(TrainingMethod m)
	{
		return m.skill + " / " + m.name;
	}
}
