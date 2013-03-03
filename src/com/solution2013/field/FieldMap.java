package com.solution2013.field;

import java.awt.Point;
import java.util.HashMap;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Direction;
import com.csc2013.MapBox;
import com.csc2013.PlayerVision;

public class FieldMap
{
	private HashMap<Point, Space> map = new HashMap<>();

	// Player's curr loc rel to 0,0 initial pos
	private Point location = new Point(0, 0);

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

	private void fillSurrounding(MapBox box, int x, int y)
	{
		BoxType type = BoxType.Open; // Assume it's open as we only get spaces that we can walk on
		if (box.hasKey()) // If it has a key mark it as a key
			type = BoxType.Key;

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
	 * 
	 * @throws RuntimeException If the space already exists and the previous type contrasts with the new one
	 * @param x
	 * @param y
	 * @param type
	 * @return
	 */
	private Space saveSpace(int x, int y, BoxType type)
	{
		Point p = new Point(x, y);

		if (map.containsKey(p))
		{
			Space sp = map.get(p);

			if (sp.getType() != type)
				throw new RuntimeException("Detected type " + sp + " at " + p.x + "," + p.y + " but expected " + type);
			
			return sp;
		}
		else
		{
			Space sp = new Space(x, y, type);		// add space
			map.put(p, sp);
			
			// link to surroundings
			
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

	public void applyPickupKey()
	{
		map.get(location).setType(BoxType.Open);
	}

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

}
