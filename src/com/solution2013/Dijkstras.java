package com.solution2013;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import com.csc2013.DungeonMaze.BoxType;
import com.solution2013.exit.DijkstraExit;
import com.solution2013.field.FieldMap;
import com.solution2013.field.Space;
import com.solution2013.field.SpaceWrapper;

/**
 * Uses Dijkstra's pathfinding algorithm as a basis for finding an ideal path based on our current situation
 * 
 * TODO: Detail algorithm
 * 
 * @author Daniel Centore
 *
 */
public class Dijkstras
{
	/**
	 * This exception is used to tell the {@link SchoolPlayer} that we want to pick up a key rather than
	 * follow a path.
	 */
	class GetKeyException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	private int keys;		// How many keys we have right now
	private Point location;
	private HashMap<Point, Space> map;

	public Dijkstras(int keys, FieldMap map)
	{
		this.keys = keys;
		this.location = map.getLocation();
		this.map = map.getMap();
		
//		new DijkstraExit(keys, map).toExit();
	}
	
	public Dijkstras(int keys, Point location, HashMap<Point, Space> map)
	{
		this.keys = keys;
		this.location = location;
		this.map = map;
	}

	/**
	 * Figures out which path to take next. This algorithm is really the "guts" of this program.
	 * @return A {@link Stack} which gives you the moves you should take in order.
	 * 			The first and last elements are where you are and where you want to be, respectively. 
	 * @throws GetKeyException If we want you to pick up a key instead of following a path
	 */
	public Stack<SpaceWrapper> getNext() throws GetKeyException
	{
		// Find shortest path to an exit. Take it if it exists.
		Stack<SpaceWrapper> toExit = new DijkstraExit(keys, location, map).toExit();//shortestToType(location, BoxType.Exit);
		if (toExit != null)
			return toExit;

		// If standing on key, grab it
		if (map.get(location).getType() == BoxType.Key)
			throw new GetKeyException();

		// Grab keys if we need them and they are nearby
		Stack<SpaceWrapper> toCloseKey = shortestToType(location, BoxType.Key);
		if (toCloseKey != null)
		{
			int dist = toCloseKey.size();

			dist -= 1;		// don't include the space we're on

			if (keys == 0 && dist <= 7)
				return toCloseKey;
			else if (keys == 1 & dist <= 3)
				return toCloseKey;
		}

		// == Find shortest path to an unexplored area ==
		List<Stack<SpaceWrapper>> possiblePaths = new ArrayList<>();

		// Shortest path to unexplored not including doors
		Stack<SpaceWrapper> toUnknown = shortestToType(location, null, null);

		if (toUnknown != null)
			possiblePaths.add(toUnknown);

		// Shortest path to unexplored through a door
		if (keys > 0) 	// Just go straight to the door. We already have a key.
		{
			Stack<SpaceWrapper> toDoor = shortestToType(location, BoxType.Door);
			if (toDoor != null)
				possiblePaths.add(toDoor);
		}
		else
		// We have no keys - find all key+door paths.
		{
			for (Space sp : this.getUnblockedSpaces())		// Check the map for all keys
			{
				if (sp.getType() == BoxType.Key)
				{
					// Find a path to the key
					Stack<SpaceWrapper> toKey = shortestToType(location, sp);
					
					if (toKey == null) 			// This key is impossible to get to right now. Try another key.
						continue;

					// Find a path from the key to the closest door
					Stack<SpaceWrapper> toDoor = shortestToType(toKey.firstElement().getSpace().getPoint(), BoxType.Door);

					if (toDoor == null) 		// There are no known doors. Just quit the whole key+door search.
						break;

					// Combine the key+door stacks

					toDoor.pop(); // Pop the first item off the door stack because otherwise it will be repeated

					// Put them on a temporary stack
					Stack<SpaceWrapper> temp = new Stack<>();

					while (!toKey.isEmpty())
						temp.push(toKey.pop());

					while (!toDoor.isEmpty())
						temp.push(toDoor.pop());

					Stack<SpaceWrapper> toKeyToDoor = new Stack<>();

					// Reverse the elements because they're backward right now
					while (!temp.isEmpty())
						toKeyToDoor.push(temp.pop());

					possiblePaths.add(toKeyToDoor);
				}
			}
		}

		// Find the shortest path to the unexplored areas now

		Stack<SpaceWrapper> min;
		Iterator<Stack<SpaceWrapper>> itr = possiblePaths.iterator();
		if (!itr.hasNext())
		{
			String s = "This map is impossible to solve in the current state.\n" +
					"We've explored all unexplored areas and used all available key+door combinations.\n" +
					"This usually means the map has some sort of flaw in it which permits one to use\n" +
					"all of the map's keys but still have some doors locked.";

			throw new RuntimeException(s);
		}

		min = itr.next();
		while (itr.hasNext())
		{
			Stack<SpaceWrapper> next = itr.next();

			if (next.size() < min.size())
				min = next;
		}

		return min;		// Return the shortest path
	}
	
	/**
	 * Returns the shortest path to a type
	 * Special Case: If the start point IS of type type, then it returns NULL!
	 * @param start
	 * @param type
	 * @return
	 */
	public Stack<SpaceWrapper> shortestToType(Point start, BoxType type)
	{
		return shortestToType(start, type, null);
	}

	public Stack<SpaceWrapper> shortestToType(Point start, Space goal)
	{
		return shortestToType(start, null, goal);
	}

	/**
	 * Finds the shortest path from a {@link Point} to a goal.
	 * The goal can be either a certain type of space (like unexplored, door, key) or to a specific space (like 2,5)
	 * @param start The starting {@link Point}
	 * @param type The {@link BoxType} we are looking for. Set to null if you want unexplored or to use the {@link Space} goal instead.
	 * @param goal The {@link Space} goal we want to go to. Set to null to use the {@link BoxType} goal.
	 * @return The {@link Stack} of moves to follow. The first element will be the {@link Space} on {@link Point} and the last element is the goal.
	 * 			This can be null if there is no possible path.
	 */
	public Stack<SpaceWrapper> shortestToType(Point start, BoxType type, Space goal)
	{
		HashMap<Space, SpaceWrapper> vertices = wrap(this.getUnblockedSpaces(), start);

		while (true)
		{
			// Is the goal still in the graph?
			for (SpaceWrapper sw : vertices.values())
			{
				Space space = sw.getSpace();

				if ((goal != null && space != null && space.equals(goal))				// If we found the Space goal
						|| (goal == null && space != null && space.getType() == type)	// If we found the type goal
						|| (goal == null && space == null && type == null)				// If we found the type goal (for unexplored)
				)
				{
					if (sw.isRemoved())		// And we've found the shortest possible path to it
					{
						// Generate a stack of the path and return it
						Stack<SpaceWrapper> fullPath = new Stack<>();

						SpaceWrapper path = sw;
						do
						{
							fullPath.push(path);
							path = path.getPrevious();

						} while (path != null);

						if (fullPath.size() <= 1) // Need to be at least 2 to be a path. Otherwise we've got a dud.
							return null;

						return fullPath;
					}
				}
			}

			// Choose the vertex with the least distance
			SpaceWrapper min = min(vertices.values());
			
			if (min == null) // No possible path to our goal
				return null;
			
			min.setRemoved(true);			// Remove it from the graph (or "mark it as visited")

			if (min.getSpace() != null)
			{
				// Calculate distances between the vertex with the smallest distance and neighbors still in the graph
				for (Space sp : min.getSpace().getSurrounding())
				{
					if (sp == null && (type != null || goal != null)) // Ignore null spaces unless we are actually looking for unexplored areas
						continue;

					SpaceWrapper wrap = vertices.get(sp);

					if (wrap.isRemoved())			// Ignore the item if we've already visited it
						continue;

					if (sp != null && sp.getType() == BoxType.Door && type != BoxType.Door && !sp.equals(goal))		// Don't include doors if we are not looking for a door
						continue;

					int length = min.getLength() + (sp == null ? 1 : sp.difficulty(type, goal, keys)); // Difficulty for getting to an unexplored area is 1

					// If this is the shortest path to the node so far, label it as such.
					if (length < wrap.getLength())
					{
						wrap.setLength(length);
						wrap.setPrevious(min);
					}
				}
			}
			// Time for another iteration of the while loop....
		}
	}
	
	Random rand = new Random();

	/**
	 * Find the element in the {@link SpaceWrapper} with the shortest length.
	 * Only includes nodes still in the graph (ie those we haven't visited yet)
	 * @param collection The {@link Collection} of {@link SpaceWrapper}s
	 * @return The {@link SpaceWrapper} with the smallest value.
	 */
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
				else if (shortest.getLength() == next.getLength() && rand.nextBoolean())		// If they are equal randomly pick one
					shortest = next;
			}
		}
		
		if (shortest.getLength() == Integer.MAX_VALUE)		// The 'shortest' path is impossible to get to.
		{
			return null;
		}
		
		return shortest;
	}

	/**
	 * Wraps a {@link List} of {@link Space}s in {@link SpaceWrapper}s and spits them out as a {@link HashMap}
	 * 	where the key is the {@link Space} and the value the {@link SpaceWrapper}.
	 * This {@link HashMap} includes one Key,Value combination which are null,SpaceWrapper(space=null) to symbolize
	 * 	an unexplored area
	 * Sets the length of each of these to infinity (Ineteger.MAX_VALUE) except for the start node which is 0.
	 * @param spaces The {@link List} of {@link Space}s
	 * @param start	The start node whose distance we set to 0.
	 * @return The {@link HashMap} of {@link Space},{@link SpaceWrapper}
	 */
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


}
