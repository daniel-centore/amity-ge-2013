package com.solution2013.exit;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.csc2013.DungeonMaze.BoxType;
import com.solution2013.Dijkstras;
import com.solution2013.field.Space;
import com.solution2013.field.SpaceWrapper;

public class DijkstraExit
{

	private int currentKeys;		// How many keys we have right now
	private Point currentLocation;
	private HashMap<Point, Space> currentMap;

	public DijkstraExit(int keys, Point currentLocation, HashMap<Point, Space> currentMap)
	{
		this.currentKeys = keys;
		this.currentLocation = currentLocation;
		this.currentMap = currentMap;
	}

	public Stack<SpaceWrapper> toExit()
	{
		List<Path> solved = new ArrayList<>();		// List of paths that lead to an exit

		List<Path> paths = new ArrayList<>();								// List of paths we are still evaluating
		paths.add(new Path(currentMap, currentLocation, currentKeys));		// Add an initial path which we'll branch off of

		int shortest = Integer.MAX_VALUE;
		while (paths.size() > 0)
		{
			
			Iterator<Path> itr = paths.iterator();
			while (itr.hasNext())
			{
				// Find shortest path to exit. Add it to the solved list.

				Path p = itr.next();

				Dijkstras d = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap());

				Stack<SpaceWrapper> toExit = d.shortestToType(p.getLocation(), BoxType.Exit);
				if (toExit != null)
				{
					p.addToPath(toExit);		// Add going to the exit to the path
					solved.add(p);				// Add the path to the solved list
					
					if (p.getPath().size() < shortest)		// Mark the path as shortest if it is
						shortest = p.getPath().size();
				}
			}
			
			List<Path> tempPaths = new ArrayList<>();

			itr = paths.iterator();
			while (itr.hasNext())
			{
				// Get keys
				Path p = itr.next();
				
				if (p.getPath().size() > shortest)		// Prune paths that are already greater than the shortest one so far
				{
					itr.remove();
					continue;
				}

				// Get a list of all keys that we can walk to w/o going through doors
				Dijkstras k = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap());

				List<Space> keys = new ArrayList<>();
				for (Space s : p.getMap().values())
				{
					// Do not reuse the shortestToType here b/c it will be inakzhe after we've gone to a key
					if (s.getType() == BoxType.Key && k.shortestToType(p.getLocation(), s) != null)
						keys.add(s);
				}
				
				if (keys.size() == 0 && p.getKeys() == 0)		// There are no more keys to get and we're outta keys. Kill the path.
				{
					itr.remove();
					continue;
				}

				Path next = p.clone();
				
				// Find paths to go to 0,1,...,n keys. This is basically a greedy salesman algorithm repeated for multiple keys.
				// The 0 keys path is already accounted for - it's the one we're in
				for (int i = 1; i <= keys.size(); i++)
				{
					k = new Dijkstras(next.getKeys(), next.getLocation(), next.getMap());		// Load a new pathfinder with this map
					Stack<SpaceWrapper> toKey = k.shortestToType(next.getLocation(), BoxType.Key);
					
					
					if (toKey == null)		// This happens if we are already standing on the key
					{
						continue;
					}
					
					next.addToPath(toKey);
					tempPaths.add(next);
					
					next = next.clone();
				}
			}
			
			paths.addAll(tempPaths);			// To avoid conmod
			tempPaths = new ArrayList<>();

			itr = paths.iterator();
			while (itr.hasNext())
			{
				// Get doors
				Path p = itr.next();
				
				if (p.getPath().size() > shortest)		// Prune paths that are already greater than the shortest one so far
				{
					itr.remove();
					continue;
				}

				itr.remove();		// Don't need the original path anymore. We'll either give up on it or branch from it.
				if (p.getKeys() == 0)
				{
					// We can't go to a door from this path because there's no keys left
					continue;
				}

				// Get a list of all doors that we can walk to w/o going through doors
				Dijkstras k = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap());

				for (Space s : p.getMap().values())
				{
					if (s.getType() == BoxType.Door)
					{
						Stack<SpaceWrapper> toDoor = k.shortestToType(p.getLocation(), s);
						if (toDoor == null)		// No possible path to that door
							continue;

						Path next = p.clone();

						next.addToPath(toDoor);
						tempPaths.add(next);
					}
				}
			}
			
			paths.addAll(tempPaths);
			tempPaths = new ArrayList<>();
		}

		Path ideal = null;
		for (Path s : solved)
		{
			if (ideal == null || s.getPath().size() < ideal.getPath().size())
				ideal = s;
		}
		
		if (ideal == null)		// No known exits
			return null;
		
		List<SpaceWrapper> l = ideal.getPath();
		Stack<SpaceWrapper> result = new Stack<>();
		for (int i = l.size() - 1; i >= 0; i--)
		{
			result.push(l.get(i));
		}
		
		
		
		return result;
	}

}
