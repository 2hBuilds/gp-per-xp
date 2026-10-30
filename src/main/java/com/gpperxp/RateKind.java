package com.gpperxp;

/** The shape of a method's hourly rate. */
public enum RateKind
{
	/** One stated figure. */
	STATED,
	/** One figure the wiki calls "about". */
	ABOUT,
	/** A low and a high figure. */
	RANGE,
	/** Only a low figure ("at least"). */
	AT_LEAST,
	/** Only a high figure ("up to"). */
	UPTO,
	/** Not an hourly method: the xp and cost are per run. */
	PER_RUN,
	/** No rate at all. */
	NONE
}
