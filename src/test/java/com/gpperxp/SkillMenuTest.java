package com.gpperxp;

import net.runelite.api.Skill;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SkillMenuTest
{
	@Test
	public void skillOfKnown()
	{
		assertEquals(Skill.PRAYER, SkillMenu.skillOf("View Prayer guide"));
		assertEquals(Skill.HERBLORE, SkillMenu.skillOf("View Herblore guide"));
		assertEquals(Skill.CONSTRUCTION, SkillMenu.skillOf("View Construction guide"));
		assertEquals(Skill.CRAFTING, SkillMenu.skillOf("View Crafting guide"));
		assertEquals(Skill.SMITHING, SkillMenu.skillOf("View Smithing guide"));
		assertEquals(Skill.RUNECRAFT, SkillMenu.skillOf("View Runecraft guide"));
		assertEquals(Skill.SAILING, SkillMenu.skillOf("View Sailing guide"));
	}

	@Test
	public void skillOfStripsTags()
	{
		assertEquals(Skill.PRAYER, SkillMenu.skillOf("<col=ff9040>View Prayer guide</col>"));
	}

	@Test
	public void skillOfNameIgnoresCase()
	{
		assertEquals(Skill.PRAYER, SkillMenu.skillOf("View prayer guide"));
	}

	@Test
	public void skillOfRejects()
	{
		assertNull(SkillMenu.skillOf(null));
		assertNull(SkillMenu.skillOf(""));
		assertNull(SkillMenu.skillOf("Examine"));
		assertNull(SkillMenu.skillOf("Cancel"));
		assertNull(SkillMenu.skillOf("View Overall guide"));
		assertNull(SkillMenu.skillOf("View Nothing guide"));
		assertNull(SkillMenu.skillOf("view Prayer guide"));
		assertNull(SkillMenu.skillOf("View Prayer"));
	}
}
