package com.solution2013.field;

/**
 * A single space on the board
 * 
 * @author drdanielfc
 *
 */
public class Space extends FieldEntity
{
	public static final int EMPTY = 0;
	public static final int KEY = 1;
	public static final int DOOR = 2;
	public static final int EXIT = 3;

	private int type;	// type of space we are
	
	// null means we dunno
	private FieldEntity north = null;
	@Override
	public String toString()
	{
		return "Space [x=" + x + ", y=" + y + ", type=" + type + "]";
	}

	private FieldEntity south = null;
	private FieldEntity east = null;
	private FieldEntity west = null;
	
	// x,y coords relative to 0,0 start
	private final int x;
	private final int y;

	public Space(int x, int y, int type)
	{
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		if (type != EMPTY)
			throw new RuntimeException("We should only be setting this to empty. Keys and doors do not magically appear.");
		
		this.type = type;
	}

	public FieldEntity getNorth()
	{
		return north;
	}

	public void setNorth(FieldEntity north)
	{
		this.north = north;
	}

	public FieldEntity getSouth()
	{
		return south;
	}

	public void setSouth(FieldEntity south)
	{
		this.south = south;
	}

	public FieldEntity getEast()
	{
		return east;
	}

	public void setEast(FieldEntity east)
	{
		this.east = east;
	}

	public FieldEntity getWest()
	{
		return west;
	}

	public void setWest(FieldEntity west)
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

}
