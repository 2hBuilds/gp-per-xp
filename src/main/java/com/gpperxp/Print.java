package com.gpperxp;

/** The wiki's latest trade print of one item; any field may be null; times are Unix seconds. */
public final class Print
{
	public final Long high;
	public final Long highTime;
	public final Long low;
	public final Long lowTime;

	public Print(Long high, Long highTime, Long low, Long lowTime)
	{
		this.high = high;
		this.highTime = highTime;
		this.low = low;
		this.lowTime = lowTime;
	}
}
