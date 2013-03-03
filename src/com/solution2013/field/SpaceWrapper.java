package com.solution2013.field;

public class SpaceWrapper // for dijkstras
{
	private SpaceWrapper previous; // prev element in chain
	private int length; // total dist
	private Space space; // space attached to this
	private boolean removed; // removed once we visited

	public SpaceWrapper(int length, Space space)
	{
		this.length = length;
		this.space = space;
		this.removed = false;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public Space getSpace()
	{
		return space;
	}

	public boolean isRemoved()
	{
		return removed;
	}

	public void setRemoved(boolean removed)
	{
		this.removed = removed;
	}

	public SpaceWrapper getPrevious()
	{
		return previous;
	}

	public void setPrevious(SpaceWrapper previous)
	{
		this.previous = previous;
	}

	@Override
	public String toString()
	{
		return "SpaceWrapper [space=" + space + "]";
	}
}