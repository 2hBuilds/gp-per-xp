package com.gpperxp;

/** One requirement of a method: exactly one of a skill level, a quest, a diary or a free text is set. */
public class Gate
{
	/** The skill name of a level gate; null for the other kinds. */
	public final String skill;
	/** The level of a skill gate; 0 for the other kinds. */
	public final int level;
	public final String quest;
	public final String diary;
	public final String text;

	public Gate(String skill, int level, String quest, String diary, String text)
	{
		this.skill = skill;
		this.level = level;
		this.quest = quest;
		this.diary = diary;
		this.text = text;
	}
}
