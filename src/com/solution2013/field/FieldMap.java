package com.solution2013.field;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Direction;
import com.csc2013.MapBox;
import com.csc2013.PlayerVision;
import com.solution2013.LearningTracker;

public class FieldMap
{
	// Map of how much we know of the maze
	// This map is a running map of the *current* game
	private HashMap<Point, Space> map = new HashMap<>();
	
	// Map of the original maze
	// As we collect data about the maze we add it here
	// However, if we pick up a key or open a door, this new knowledge is not added
	// This is so we can reuse the map in the future.
	private HashMap<Point, Space> originalMap = null;

	// Player's current location
	private Point location = new Point(0, 0);
	
	public FieldMap(LearningTracker lt)
	{
		originalMap = lt.nextMap();
		updateData(originalMap);
	}
	
	/**
	 * Inserts all the data from 'data' into this.map without referencing any of the original objects.
	 * @param data
	 */
	private void updateData(HashMap<Point, Space> data)
	{
		for (Space s : data.values())
		{
			saveSpace(s.getX(), s.getY(), s.getType());
		}
	}

	/**
	 * Fills in our map with as much information as can be derived from the {@link PlayerVision}
	 * @param vision The {@link PlayerVision} we are pulling info from
	 */
	public void fillVision(PlayerVision vision)
	{
		// Current square
		fillSurrounding(vision.CurrentPoint, location.x, location.y);

		// North
		for (int i = 0; i < vision.mNorth; i++)
			fillSurrounding(vision.North[i], location.x, location.y + i + 1);

		// South
		for (int i = 0; i < vision.mSouth; i++)
			fillSurrounding(vision.South[i], location.x, location.y - i - 1);

		// East
		for (int i = 0; i < vision.mEast; i++)
			fillSurrounding(vision.East[i], location.x + i + 1, location.y);

		// West
		for (int i = 0; i < vision.mWest; i++)
			fillSurrounding(vision.West[i], location.x - i - 1, location.y);
	}

	/**
	 * Fills in information of all the squares surrounding a {@link MapBox}
	 * @param box The {@link MapBox} to extract the info from. Must be either Open or a Key.
	 * @param x X coordinate of the square
	 * @param y Y coordinate of the square
	 */
	private void fillSurrounding(MapBox box, int x, int y)
	{
		BoxType type = BoxType.Open; // Assume it's open as we only get spaces that we can walk on
		if (box.hasKey()) // If it has a key mark it as a key
			type = BoxType.Key;

		//		System.out.println(location+" "+x+" "+y);
		saveSpace(x, y, type);

		// Now grab the surroundings
		saveSpace(x, y + 1, box.North);
		saveSpace(x, y - 1, box.South);
		saveSpace(x + 1, y, box.East);
		saveSpace(x - 1, y, box.West);
	}

	/**
	 * If a space already exists, verify that it is correct.
	 * If it doesn't, add it and link it to surrounding nodes.
	 * @throws RuntimeException If the space already exists and the previous type contrasts with the new one
	 * @param x X coordinate of the {@link Space}
	 * @param y Y coordinate of the {@link Space}
	 * @param type The type of space it is
	 * @return The {@link Space} which either already existed in the map or which we added.
	 */
	private Space saveSpace(int x, int y, BoxType type)
	{
		Point p = new Point(x, y);

		if (map.containsKey(p))
		{
			Space sp = map.get(p);

			if (sp.getType() != type)
				throw new RuntimeException("Expected type " + sp + " at " + p.x + "," + p.y + " but asked to save " + type);

			return sp;
		}
		else
		{
			if (!originalMap.containsKey(p))
				originalMap.put(p, new Space(x, y, type));		// add the space as it existed in the original map to the learned map
			
			Space sp = new Space(x, y, type);		// add space
			map.put(p, sp);
			
			// link space to surroundings

			Point n = new Point(x, y + 1);
			Point s = new Point(x, y - 1);
			Point e = new Point(x + 1, y);
			Point w = new Point(x - 1, y);

			if (map.containsKey(n))
			{
				Space temp = map.get(n);
				sp.setNorth(temp);
				temp.setSouth(sp);
			}

			if (map.containsKey(s))
			{
				Space temp = map.get(s);
				sp.setSouth(temp);
				temp.setNorth(sp);
			}

			if (map.containsKey(e))
			{
				Space temp = map.get(e);
				sp.setEast(temp);
				temp.setWest(sp);
			}

			if (map.containsKey(w))
			{
				Space temp = map.get(w);
				sp.setWest(temp);
				temp.setEast(sp);
			}

			return sp;
		}
	}

	/**
	 * Let's the map know that we moved in a direction
	 * @param dir The {@link Direction} we moved in
	 */
	public void applyMove(Direction dir)
	{
		switch (dir)
		{
		case North:
			location = new Point(location.x, location.y + 1);
			break;

		case South:
			location = new Point(location.x, location.y - 1);
			break;

		case East:
			location = new Point(location.x + 1, location.y);
			break;

		case West:
			location = new Point(location.x - 1, location.y);
			break;
		}
	}

	/**
	 * Let's the map know we just picked up a key on the space we're on.
	 */
	public void applyPickupKey()
	{
		map.get(location).setType(BoxType.Open);
	}

	/**
	 * Let's the map know we just opened a door.
	 */
	public void applyOpenDoor()
	{
		Point p;
		Space sp;

		p = new Point(location.x, location.y + 1);
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);

		p = new Point(location.x, location.y - 1);
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);

		p = new Point(location.x + 1, location.y);
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);

		p = new Point(location.x - 1, location.y);
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);
	}

	/**
	 * Collects a {@link List} of all the {@link Space}s in our map which are not blocked.
	 * This function includes doors.
	 * @return The list
	 */
	public List<Space> getUnblockedSpaces()
	{
		ArrayList<Space> result = new ArrayList<>();

		for (Space sp : map.values())
		{
			if (sp.getType() != BoxType.Blocked)
				result.add(sp);
		}

		return result;
	}

	/**
	 * Gets the entire map. The key is the {@link Point} the {@link Space} is located on relative to (0,0) being the initial position.
	 * North, South, East, and West are +y,-y,+x,-x respectively. Please do not edit the map.
	 * @return The map.
	 */
	public HashMap<Point, Space> getMap()
	{
		return map;
	}

	/**
	 * Our player's current location relative to (0,0) being their initial position.
	 * @return Their location
	 */
	public Point getLocation()
	{
		return location;
	}

}
