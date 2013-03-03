package com.solution2013.field;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.csc2013.DungeonMaze.BoxType;

/**
 * A single space on the board
 * 
 * @author drdanielfc
 *
 */
public class Space
{
	private BoxType type; // type of space we are

	// null means we dunno
	private Space north = null;
	private Space south = null;
	private Space east = null;
	private Space west = null;

	// x,y coords relative to 0,0 start
	private final int x;
	private final int y;

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

	public int difficulty(BoxType t, int keys)
	{
		switch (type)
		{
		case Blocked:
			return Integer.MAX_VALUE;
			
			
		case Door:
			if (t == BoxType.Door)
				return 1;	// if we are looking for a door then give it a weight of one
			else
				return 2000;		// otherwise, it is impassable

		case Key:
			return 1;
			
		case Exit:
		case Open:
			if (keys == 0)		// Prefer paths with keys if we have none
				return 5;
			if (keys == 1)
				return 2;
			
			return 1;
		}

		throw new RuntimeException("This should not be possible");
	}

	public List<Space> getSurrounding()
	{
		ArrayList<Space> result = new ArrayList<>(4);

		if (type == BoxType.Door)		// If we are a door, we have no access to the great beyond
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

	public void setType(BoxType type)
	{
		// if (type != BoxType.Open || (this.type != BoxType.Door && this.type != BoxType.Key))
		// throw new RuntimeException("We should only be setting this to empty. Keys and doors do not magically appear.");

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
