package com.solution2013.field;

/**
 * Wraps a {@link Space} in a node for use in DIjkstra's algorithm.
 * This allows us to run multiple distance calculations on the same {@link Space}s without worrying about corruption
 * 	between them because each calculation uses new {@link SpaceWrapper}s
 * 
 * @author Daniel Centore
 *
 */
public class SpaceWrapper
{
	// The previous element in the chain (or null if this is root or hasn't been set yet)
	private SpaceWrapper previous;
	
	private int length;			// Total distance of this node from the root one
	private Space space;		// The space we are actually referencing
	private boolean removed;	// Set to true to signify "removal" from the graph (sometimes called "visited" in Dijkstra's)

	/**
	 * Creates a new wrapper.
	 * @param length Initial distance. Usually 0 for root and Integer.MAX_VALUE (ie "infinity") for all others
	 * @param space The {@link Space} we are wrapping
	 */
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