package com.solution2013.field;

/**
 * Wraps a {@link Space} in a node for use in Dijkstra's algorithm.
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

	/**
	 * Gets the distance of this node from the root node
	 * @return The distance
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Sets the distance of this node from the root one
	 * @param length The distance to set it to
	 */
	public void setLength(int length)
	{
		this.length = length;
	}

	/**
	 * Gets the space we are wrapping
	 * @return The space we are wrapping
	 */
	public Space getSpace()
	{
		return space;
	}

	/**
	 * Checks if this node has been removed from the graph
	 * @return True if it has been removed; False otherwise
	 */
	public boolean isRemoved()
	{
		return removed;
	}

	/**
	 * Set whether or not this node has been removed from the graph
	 * @param removed Set to true if it has; False otherwise
	 */
	public void setRemoved(boolean removed)
	{
		this.removed = removed;
	}

	/**
	 * Gets the node before this one on the path to the end
	 * @return The {@link SpaceWrapper} before this one
	 */
	public SpaceWrapper getPrevious()
	{
		return previous;
	}

	/**
	 * Sets the node before this one on the path to the end
	 * @param previous The {@link SpaceWrapper} before this one
	 */
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