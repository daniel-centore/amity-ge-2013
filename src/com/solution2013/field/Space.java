package com.solution2013.field;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.csc2013.DungeonMaze.BoxType;

/**
 * A single space on the board
 * 
 * @author Daniel Centore
 *
 */
public class Space
{
	private BoxType type; // type of space we are

	// What space lies in each of the 4 cardinal directions. Null indicates unknown.
	private Space north = null;
	private Space south = null;
	private Space east = null;
	private Space west = null;

	// The (x,y) coordinate of this space
	private final int x;
	private final int y;

	/**
	 * Creates a space
	 * @param x X coordinate of the space
	 * @param y Y coordinate of the space
	 * @param type The {@link BoxType} of the space.
	 */
	public Space(int x, int y, BoxType type)
	{
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public BoxType getType()
	{
		return type;
	}

	/**
	 * Finds the difficulty of traversing this node
	 * @param t Our end goal when using this in Dijkstra's algorithm.
	 * 			Just for error checking - you can safely set this to null if this space is not a door.
	 * @param keys Number of keys we have.
	 * @throws RuntimeException If this space is a door and t is not a door (ie we are not looking for one)
	 * @return The difficulty of traversing the node.
	 */
	public int difficulty(BoxType t, Space goal, int keys)
	{
		switch (type)
		{
		case Blocked:
			return Integer.MAX_VALUE;

		case Door:
			if (t == BoxType.Door || (goal != null && goal.getType() == BoxType.Door))
				return 1; // if we are looking for a door then give it a weight of one
			else
				throw new RuntimeException("We shouldn't be asking for the difficulty of a door if we are not searching for one.");

		case Key:
			return 1;

		case Exit:
		case Open:
			if (keys == 0) // Prefer paths with keys if we have none
				return 5;
			if (keys == 1)
				return 2;

			return 1;
		}

		throw new RuntimeException("This should not be possible");
	}

	/**
	 * Gets a list of surrounding nodes.
	 * This includes null (unknown) spaces as long as this {@link Space} is not a door.
	 * This does NOT include walls.
	 * This is because a Door does not have direct access to the unknown areas so we don't want to include that
	 * 	in our calculations.
	 * @return A {@link List} of surrounding spaces.
	 */
	public List<Space> getSurrounding()
	{
		ArrayList<Space> result = new ArrayList<>(4);

		if (type == BoxType.Door) // If we are a door, we do not have direct access to the unknown (null) areas.
		{
			if (north != null && north.type != BoxType.Blocked)
				result.add(north);

			if (south != null && south.type != BoxType.Blocked)
				result.add(south);

			if (east != null && east.type != BoxType.Blocked)
				result.add(east);

			if (west != null && west.type != BoxType.Blocked)
				result.add(west);
		}
		else
		{
			if (north == null || north.type != BoxType.Blocked)
				result.add(north);

			if (south == null || south.type != BoxType.Blocked)
				result.add(south);

			if (east == null || east.type != BoxType.Blocked)
				result.add(east);

			if (west == null || west.type != BoxType.Blocked)
				result.add(west);
		}

		return result;
	}

	/**
	 * Sets the type of this space.
	 * You should really only be setting this to empty after opening a door or picking up a key.
	 * @param type The {@link BoxType} to set it to.
	 */
	public void setType(BoxType type)
	{
		this.type = type;
	}

	public Space getNorth()
	{
		return north;
	}

	public void setNorth(Space north)
	{
		this.north = north;
	}

	public Space getSouth()
	{
		return south;
	}

	public void setSouth(Space south)
	{
		this.south = south;
	}

	public Space getEast()
	{
		return east;
	}

	public void setEast(Space east)
	{
		this.east = east;
	}

	public Space getWest()
	{
		return west;
	}

	public void setWest(Space west)
	{
		this.west = west;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Point getPoint()
	{
		return new Point(x, y);
	}

	@Override
	public String toString()
	{
		return "Space [type=" + type + ", x=" + x + ", y=" + y + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Space other = (Space) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

}
