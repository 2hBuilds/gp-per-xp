package com.gpperxp;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** {@link Methods}: reading the methods file from a string, from empty or broken input, and from the bundled resource. */
public class MethodsTest
{
	private static final String ATTACK_POTION = "{\"skill\":\"Herblore\",\"name\":\"Attack potion\",\"level\":3,"
		+ "\"xpPerAction\":25,\"inputs\":[{\"itemId\":91,\"qty\":1},{\"itemId\":221,\"qty\":1}],"
		+ "\"outputs\":[{\"itemId\":121,\"qty\":1}],\"actionsPerHour\":2000,"
		+ "\"source\":\"https://oldschool.runescape.wiki/w/Pay-to-play_Herblore_training\",\"note\":\"typical\"}";

	private static final String BONES = "{\"skill\":\"Prayer\",\"name\":\"Bones\",\"level\":1,\"xpPerAction\":4.5,"
		+ "\"inputs\":[{\"itemId\":526,\"qty\":1}],\"actionsPerHour\":5000,\"source\":\"src\"}";

	private static final String GUAM_POTION = "{\"skill\":\"Herblore\",\"name\":\"Guam potion (unf)\",\"level\":1,"
		+ "\"xpPerAction\":1,\"inputs\":[{\"itemId\":91,\"qty\":1}],\"actionsPerHour\":2000,\"source\":\"src\"}";

	private static String fixtureJson()
	{
		return "{\"updated\":\"2026-10-29\",\"methods\":[" + ATTACK_POTION + "," + BONES + "," + GUAM_POTION + "]}";
	}

	private static List<String> names(List<TrainingMethod> methods)
	{
		final List<String> names = new ArrayList<>();
		for (TrainingMethod m : methods)
		{
			names.add(m.name);
		}
		return names;
	}

	@Test
	public void loadsRows()
	{
		final Methods methods = Methods.load(new Gson(), new StringReader(fixtureJson()));
		assertEquals(3, methods.all().size());
		assertEquals("2026-10-29", methods.updated());
		assertEquals(Arrays.asList("Herblore", "Prayer"), methods.skills());
		assertEquals(Arrays.asList("Attack potion", "Guam potion (unf)"), names(methods.forSkill("Herblore")));
		assertTrue(methods.forSkill("Cooking").isEmpty());
	}

	@Test
	public void rowFieldsRead()
	{
		final Methods methods = Methods.load(new Gson(), new StringReader(fixtureJson()));
		final TrainingMethod row = methods.all().get(0);
		assertEquals(3, row.level);
		assertEquals(25.0, row.xpPerAction, 1e-6);
		assertEquals(2000.0, row.actionsPerHour, 1e-6);
		assertEquals("typical", row.note);
		assertEquals(2, row.inputs.size());
		assertEquals(91, row.inputs.get(0).itemId);
		assertEquals(1.0, row.inputs.get(0).qty, 1e-6);
		assertEquals(121, row.outputs.get(0).itemId);
	}

	@Test
	public void missingListsAreEmpty()
	{
		final Methods methods = Methods.load(new Gson(), new StringReader(fixtureJson()));
		final TrainingMethod bones = methods.all().get(1);
		assertNotNull(bones.outputs);
		assertTrue(bones.outputs.isEmpty());
		assertNull(bones.note);
	}

	@Test
	public void emptyAndBroken()
	{
		final Gson gson = new Gson();

		final Methods empty = Methods.load(gson, new StringReader("{}"));
		assertTrue(empty.all().isEmpty());
		assertNull(empty.updated());

		final Methods blank = Methods.load(gson, new StringReader(""));
		assertTrue(blank.all().isEmpty());
		assertNull(blank.updated());

		try
		{
			Methods.load(gson, new StringReader("not json{"));
			fail("a broken file should throw JsonParseException");
		}
		catch (JsonParseException expected)
		{
			// the outcome the test wants
		}
	}

	@Test
	public void bundledLoads()
	{
		final Methods bundled = Methods.loadBundled(new Gson());
		assertNotNull(bundled);
		assertNotNull(bundled.all());
	}
}
