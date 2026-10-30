package com.gpperxp;

/** Lets at most one panel refresh wait at a time, so a burst of xp drops makes one redraw. */
final class RefreshGate
{
	private boolean pending;

	/** True when no refresh was waiting (one now is); false when one already is. */
	synchronized boolean request()
	{
		if (pending)
		{
			return false;
		}
		pending = true;
		return true;
	}

	/** The waiting refresh has run (or is dropped). */
	synchronized void done()
	{
		pending = false;
	}
}
