package com.gpperxp;

/** Which way a gp figure points: money earned (green, "+"), money spent (red, "-"), or neither (grey or plain). */
public enum Money
{
	EARN,
	SPEND,
	NEUTRAL;

	/** Choice: read from the text itself: a leading "+" earns, a leading "-" followed by a figure spends, the rest is neutral. */
	static Money of(String text)
	{
		if (text.startsWith("+"))
		{
			return EARN;
		}
		return text.length() > 1 && text.startsWith("-") ? SPEND : NEUTRAL;
	}
}
