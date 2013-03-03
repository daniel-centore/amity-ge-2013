package com.solution2013.field;

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
	private BoxType type;	// type of space we are
	
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
	
	public List<Space> getSurrounding()
	{
		ArrayList<Space> result = new ArrayList<>(4);
		
		if (north != null)
			result.add(north);
		
		if (south != null)
			result.add(south);
		
		if (east != null)
			result.add(east);
		
		if (west != null)
			result.add(west);
		
		return result;
	}

	public void setType(BoxType type)
	{
		//if (type != BoxType.Open || (this.type != BoxType.Door && this.type != BoxType.Key))
			//throw new RuntimeException("We should only be setting this to empty. Keys and doors do not magically appear.");
		
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

	@Override
	public String toString()
	{
		return "Space [type=" + type + ", x=" + x + ", y=" + y + "]";
	}

}
