package com.solution2013.exit;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.csc2013.DungeonMaze.BoxType;
import com.solution2013.Dijkstras;
import com.solution2013.field.Space;
import com.solution2013.field.SpaceWrapper;

/**
 * Represents a path we are following
 * 
 * @author Daniel Centore
 *
 */
public class Path
{
	private HashMap<Point, Space> map;
	private int keys;
	private ArrayList<SpaceWrapper> path;		// First element is first thing to perform

	public Path(HashMap<Point, Space> newMap, Point location, int keys)
	{
		this.keys = keys;

		map = new HashMap<>();
		load(newMap);

		path = new ArrayList<>();
		path.add(new SpaceWrapper(0, map.get(new Point(location))));	// Put our current location on the move stack
	}
	
	private Path(HashMap<Point, Space> newMap, int keys, ArrayList<SpaceWrapper> path)
	{
		this.keys = keys;

		map = new HashMap<>();
		load(newMap);

		this.path = new ArrayList<>();
		this.path = (ArrayList<SpaceWrapper>) path.clone();
	}

	public Path clone()
	{
		return new Path(this.map, this.keys, this.path);
	}

	public void addToPath(Stack<SpaceWrapper> proceed)
	{
		if (proceed.peek().getSpace().equals(path.get(path.size() - 1).getSpace()))		// Don't put on the first element if it matches the last element of our current list
			proceed.pop();

		while (!proceed.isEmpty())
		{
			SpaceWrapper next = proceed.pop();
			BoxType type = next.getSpace().getType();
			Point p = next.getSpace().getPoint();

			switch (type)
			{
			case Door:
				// Pretend any keys inside an area are nonexistant after we've opened a door.
				// This provides a pretty good approximation although certainly not a perfect one.
				// TODO: If we can make DijkstraExit efficient enough, then remove this
				pruneKeys();
				
				keys--;
				map.get(next.getSpace().getPoint()).setType(BoxType.Open);	// We open the door
				break;
			case Key:
				keys++;
				map.get(next.getSpace().getPoint()).setType(BoxType.Open);	// We pick up the key
				break;
			}
			
			path.add(new SpaceWrapper(1, new Space(p.x, p.y, type)));
		}
	}

	private void pruneKeys()
	{
		Dijkstras k = new Dijkstras(this.getKeys(), this.getLocation(), this.getMap(), -1);

		for (Space s : this.getMap().values())
		{
			if (s.getType() == BoxType.Key)
			{
				Stack<SpaceWrapper> toKey = k.shortestToType(this.getLocation(), s);
				if (toKey == null)
					continue;

				s.setType(BoxType.Open);
			}
		}
		
	}

	/**
	 * Clones the newMap
	 * @param newMap
	 */
	private void load(HashMap<Point, Space> newMap)
	{
		for (Space me : newMap.values())
		{
			Point p = me.getPoint();

			int x = me.getX();
			int y = me.getY();

			Space sp = new Space(me.getX(), me.getY(), me.getType());		// add space
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
		}
	}

	public int getKeys()
	{
		return keys;
	}

	public void setKeys(int keys)
	{
		this.keys = keys;
	}

	public HashMap<Point, Space> getMap()
	{
		return map;
	}

	public Point getLocation()
	{
		return new Point(path.get(path.size() - 1).getSpace().getPoint());
	}

	public List<SpaceWrapper> getPath()
	{
		return path;
	}

	@Override
	public String toString()
	{
		return "Path [path=" + path + "]";
	}
}
