package com.solution2013;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.csc2013.DungeonMaze.BoxType;
import com.solution2013.field.FieldMap;
import com.solution2013.field.Space;
import com.solution2013.field.SpaceWrapper;

public class Dijkstras
{
	class GetKeyException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	private int keys;
	private FieldMap map;

	public Dijkstras(int keys, FieldMap map)
	{
		this.keys = keys;
		this.map = map;
	}

	public Stack<SpaceWrapper> getNext() throws GetKeyException
	{
		// Find shortest path to an exit
		Stack<SpaceWrapper> toExit = shortestToType(map.getLocation(), BoxType.Exit);
		if (toExit != null)
			return toExit;

		// If standing on key, grab it
		if (map.getMap().get(map.getLocation()).getType() == BoxType.Key)
			throw new GetKeyException();

		// Grab keys if we need them and they are nearby
		Stack<SpaceWrapper> toCloseKey = shortestToType(map.getLocation(), BoxType.Key);
		if (toCloseKey != null)
		{
			int dist = toCloseKey.size();
			
			dist -= 1;		// don't include the space we're on
			
			if (keys == 0 && dist <= 5 )
				return toCloseKey;
			else if (keys == 1 & dist <= 2)
				return toCloseKey;
			else if (keys == 2 && dist == 1)
				return toCloseKey;
		}
		
		// Find shortest path to unknown region
		List<Stack<SpaceWrapper>> possiblePaths = new ArrayList<>();

		Stack<SpaceWrapper> toUnknown = shortestToType(map.getLocation(), null, null);

		if (toUnknown != null)
			possiblePaths.add(toUnknown); // To unknown region not including through doors

		// To unknown region through door
		if (keys > 0) // Just go straight to the door. Already have a key.
		{
			Stack<SpaceWrapper> toDoor = shortestToType(map.getLocation(), BoxType.Door);
			if (toDoor != null)
				possiblePaths.add(toDoor);
		}
		else
		// No key - find paths to all keys then to door
		{
			for (Space sp : map.getUnblockedSpaces())
			{
				if (sp.getType() == BoxType.Key)
				{
					// find path to the key
					Stack<SpaceWrapper> toKey = shortestToType(map.getLocation(), sp);

					// find path to closest door from the key
					Stack<SpaceWrapper> toDoor = shortestToType(toKey.firstElement().getSpace().getPoint(), BoxType.Door);

					if (toDoor == null) // No paths to doors - no point in looking for these
						break;

					// combine them
					toDoor.pop(); // pop the first off the door so we don't repeat it

					Stack<SpaceWrapper> temp = new Stack<>(); // put on a temp stack

					while (!toKey.isEmpty())
						temp.push(toKey.pop());

					while (!toDoor.isEmpty())
						temp.push(toDoor.pop());

					Stack<SpaceWrapper> toKeyToDoor = new Stack<>(); // final stack

					while (!temp.isEmpty())
						// reverse the elements b/c they're backwards now
						toKeyToDoor.push(temp.pop());

					possiblePaths.add(toKeyToDoor);
				}
			}
		}

		// Find the shortest path to the unknown region now

		Stack<SpaceWrapper> min;
		Iterator<Stack<SpaceWrapper>> itr = possiblePaths.iterator();
		if (!itr.hasNext())		// It is impossible to do anything
			throw new RuntimeException("This map is impossible to solve in the current state.\nWe've explored all unexplored areas and used all key+door combinations.");
		
		min = itr.next();
		while (itr.hasNext())
		{
			Stack<SpaceWrapper> next = itr.next();

			if (next.size() < min.size())
				min = next;
		}

		return min;
	}

	public Stack<SpaceWrapper> shortestToType(Point start, BoxType type)
	{
		return shortestToType(start, type, null);
	}

	public Stack<SpaceWrapper> shortestToType(Point start, Space goal)
	{
		return shortestToType(start, null, goal);
	}

	public Stack<SpaceWrapper> shortestToType(Point start, BoxType type, Space goal)
	{
		HashMap<Space, SpaceWrapper> vertices = wrap(map.getUnblockedSpaces(), start);

		while (true)
		{
			// Is the end still in the graph?
			for (SpaceWrapper sw : vertices.values())
			{
				Space space = sw.getSpace();

				if ((goal != null && space != null && space.equals(goal)) || (goal == null && space != null && space.getType() == type) || (goal == null && space == null && type == null)) // this is what we were looking for
				{
					// Generate a stack of the path and return it
					if (sw.isRemoved())
					{
						Stack<SpaceWrapper> fullPath = new Stack<>();

						SpaceWrapper path = sw;
						do
						{
							fullPath.push(path);
							path = path.getPrevious();

						} while (path != null);

						if (fullPath.size() <= 1) // Needs to be at least 2 to be a path
							return null;

						return fullPath;
					}
				}
			}

			// Choose the vertex with the least distance
			SpaceWrapper min = min(vertices.values());
			if (min == null) // no path to our goal
				return null;

			// Remove it from the graph

			min.setRemoved(true);

			if (min.getSpace() != null)
			{
				// Calculate distances between it and neighbors still in the graph
				for (Space sp : min.getSpace().getSurrounding())
				{
					if (sp == null && (type != null || goal != null)) // ignore null spaces. not looking for those here.
						continue;

					SpaceWrapper wrap = vertices.get(sp);

					if (wrap.isRemoved()) // don't care about these
						continue;

					int length = min.getLength() + (sp == null ? 1 : sp.difficulty(type, keys)); // default difficulty for unknown space is 1

					if (length < wrap.getLength())
					{
						wrap.setLength(length);
						wrap.setPrevious(min);
					}
				}
			}
		}
	}

	private SpaceWrapper min(Collection<SpaceWrapper> collection)
	{
		SpaceWrapper shortest = null;
		Iterator<SpaceWrapper> itr = collection.iterator();

		while (itr.hasNext())
		{
			SpaceWrapper next = itr.next();

			if (!next.isRemoved())
			{
				if (shortest == null || shortest.getLength() > next.getLength())
					shortest = next;
			}
		}

		return shortest;
	}

	public HashMap<Space, SpaceWrapper> wrap(List<Space> spaces, Point start)
	{
		HashMap<Space, SpaceWrapper> result = new HashMap<>();

		// Add the null value (indicates unknown region)
		result.put(null, new SpaceWrapper(Integer.MAX_VALUE, null));

		// Add all the other values
		for (Space sp : spaces)
		{
			int dist = Integer.MAX_VALUE;
			if (sp.getPoint().equals(start))
				dist = 0;

			result.put(sp, new SpaceWrapper(dist, sp));
		}

		return result;
	}

}
