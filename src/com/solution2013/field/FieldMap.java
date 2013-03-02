package com.solution2013.field;

import java.awt.Point;
import java.util.HashMap;

import com.csc2013.MapBox;
import com.csc2013.PlayerVision;
import com.csc2013.DungeonMaze.BoxType;

public class FieldMap
{
	private HashMap<Point, Space> map = new HashMap<>();

	// Player's curr loc rel to 0,0 initial pos
	private Point location = new Point(0, 0);

	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;

	public FieldMap()
	{

	}

	public void applyMove(int direction)
	{
		switch (direction)
		{
		case NORTH:
			location.y++;
			break;

		case SOUTH:
			location.y--;
			break;

		case EAST:
			location.x++;
			break;

		case WEST:
			location.x--;
			break;

		default:
			throw new RuntimeException("Must be a direction!");
		}
	}

	/**
	 * Fill in any new map information 
	 * @param vision
	 */
	public void fillIn(PlayerVision vision)
	{
		// Fill in current point

		fillPoint(vision.CurrentPoint, location.x, location.y, (vision.CurrentPoint.hasKey() ? Space.KEY : Space.EMPTY));

		// Now let's go through the others

		// North
		for (int i = 0; i < vision.mNorth; i++)
		{
			MapBox mb = vision.North[i];

			int x = location.x;
			int y = location.y + 1;

			fillPoint(mb, x, y, -1); // We can ignore the type because the it should have already been filled in
		}

		// South
		for (int i = 0; i < vision.mSouth; i++)
		{
			MapBox mb = vision.South[i];

			int x = location.x;
			int y = location.y - 1;

			fillPoint(mb, x, y, -1); // We can ignore the type because the it should have already been filled in
		}

		// East
		for (int i = 0; i < vision.mEast; i++)
		{
			MapBox mb = vision.East[i];

			int x = location.x + 1;
			int y = location.y;

			fillPoint(mb, x, y, -1); // We can ignore the type because the it should have already been filled in
		}

		// West
		for (int i = 0; i < vision.mWest; i++)
		{
			MapBox mb = vision.West[i];

			int x = location.x - 1;
			int y = location.y;

			fillPoint(mb, x, y, -1); // We can ignore the type because the it should have already been filled in
		}
	}

	private void fillPoint(MapBox box, int x, int y, int type)
	{
		Space space = fillPoint(x, y, type);

		fillSpot(NORTH, box.North, space);
		fillSpot(SOUTH, box.South, space);
		fillSpot(EAST, box.East, space);
		fillSpot(WEST, box.West, space);
	}

	/**
	 * 
	 * @param direction Direction to fill in
	 * @param boxType BoxType of the object to fill in
	 * @param space The space from which it was derived (so we took it from space.North, South,...)
	 */
	private void fillSpot(int direction, BoxType boxType, Space space)
	{
		if (boxType == BoxType.Blocked) // Wall
		{
			if (direction == NORTH)
				space.setNorth(new Wall());
			else if (direction == SOUTH)
				space.setSouth(new Wall());
			else if (direction == EAST)
				space.setEast(new Wall());
			else if (direction == WEST)
				space.setWest(new Wall());
		}
		else		// Not a wall
		{
			int x = space.getX();
			int y = space.getY();

			if (direction == NORTH)
				y++;
			if (direction == SOUTH)
				y--;
			if (direction == EAST)
				x++;
			if (direction == WEST)
				x--;

			int type = -1;

			if (boxType == boxType.Open)
				type = Space.EMPTY;
			else if (boxType == boxType.Door)
				type = Space.DOOR;
			else if (boxType == boxType.Key)
				type = Space.KEY;
			else if (boxType == boxType.Exit)
				type = Space.EXIT;

			Space newSpace = fillPoint(x, y, type);
			
			if (direction == NORTH)
			{
				space.setNorth(newSpace);
				newSpace.setSouth(space);
			}
			if (direction == SOUTH)
			{
				space.setSouth(newSpace);
				newSpace.setNorth(space);
			}
			if (direction == EAST)
			{
				space.setEast(newSpace);
				newSpace.setWest(space);
			}
			if (direction == WEST)
			{
				space.setWest(newSpace);
				newSpace.setEast(space);
			}
		}
	}

	// For filling in points we don't have a MapBox of (ie they were derived from the MapBox
	private Space fillPoint(int x, int y, int type)
	{
		Point p = new Point(x, y);

		Space space;
		if (map.containsKey(p)) // already know about this point
			space = map.get(p);
		else
		{
			if (type < 0)
				throw new RuntimeException("Invalid type: " + type);

			space = new Space(x, y, type);
			map.put(p, space);
		}

		return space;
	}
}
