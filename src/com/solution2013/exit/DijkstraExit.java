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

/**
 * Looks for the shortest possible path to an exit.
 * 
 * Uses the following algorithm:
 *  1. Look for all possible paths to exits. Add them to the 'solved' list.
 *  2. Look for all possible paths to keys.
 *  3. Look for all possible paths to doors.
 *  4. Repeat until all possible key+door combinations are exhausted.
 * 
 * The algorithm uses some approximations in this "brute force" so it doesn't take days to run:
 *  - Once a door has been opened, all keys before that door are marked as nonexistant for future iterations along the path
 *  - Instead of finding all key combinations, it instead finds the shortest route to 0,1,2,...,n keys and uses each of these as a branch.
 * 
 * @author Daniel Centore
 *
 */
public class DijkstraExit
{

	private int currentKeys;					// How many keys we have right now
	private Point currentLocation;				// Our actual current location
	private HashMap<Point, Space> currentMap;	// Our actual current map
	private int bestCase;						// The best exit case we have encountered so far (or Integer.MAX_VALUE if it has not yet been solved) 

	/**
	 * Instantiates the class
	 * @param keys The number of keys we have right now
	 * @param currentLocation The player's current location
	 * @param currentMap The player's current map
	 * @param bestCase The best case we have encountered on the map so far (or Integer.MAX_VALUE if it has never been solved)
	 */
	public DijkstraExit(int keys, Point currentLocation, HashMap<Point, Space> currentMap, int bestCase)
	{
		this.currentKeys = keys;
		this.currentLocation = currentLocation;
		this.currentMap = currentMap;
		this.bestCase = bestCase;
	}

	/**
	 * Finds the shortest path to an exit
	 * @return The path, or null if none could be found
	 */
	public Stack<SpaceWrapper> toExit()
	{
		List<Path> solved = new ArrayList<>();		// List of paths that lead to an exit

		List<Path> paths = new ArrayList<>();								// List of paths we are still evaluating

		paths.add(new Path(currentMap, currentLocation, currentKeys));		// Add an initial path which we'll branch off of

		int shortest = bestCase;

		long time = System.currentTimeMillis();

		// While there are still paths to evaluate, evaluate them!
		while (paths.size() > 0)
		{
			if (System.currentTimeMillis() - time > 240000)		// Timeout after 4 minutes
				break;

			// PART 1: For each path, see if there is a way to get to an exit without going through doors
			// If there is, add the path to the solved list.
			Iterator<Path> itr = paths.iterator();
			while (itr.hasNext())
			{
				Path p = itr.next();

				Dijkstras d = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap(), -1);

				Stack<SpaceWrapper> toExit = d.shortestToType(p.getLocation(), BoxType.Exit);		// Find shortest path to an exit
				if (toExit != null)			// There is such a path
				{
					p.addToPath(toExit);		// Add going to the exit to the path
					solved.add(p);				// Add the path to the solved list

					if (p.getPath().size() < shortest)		// Mark the path as shortest if it is
						shortest = p.getPath().size();
				}
			}

			// PART 2: For each path, find all possible paths to keys

			List<Path> tempPaths = new ArrayList<>();

			itr = paths.iterator();
			while (itr.hasNext())
			{
				Path p = itr.next();

				if (p.getPath().size() > shortest)		// Prune paths that are already greater than the shortest one so far
				{
					itr.remove();
					continue;
				}

				// Get a list of all keys that we can walk to without going through doors
				Dijkstras k = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap(), -1);

				List<Space> keys = new ArrayList<>();		// The list of keys
				for (Space s : p.getMap().values())
				{
					// If it is a keys that we can get to, add it
					if (s.getType() == BoxType.Key && k.shortestToType(p.getLocation(), s) != null)
						keys.add(s);
				}

				if (keys.size() == 0 && p.getKeys() == 0)		// There are no more keys to get and we're out of keys. Kill the potential path.
				{
					itr.remove();
					continue;
				}

				// Count the number of doors left on the board
				int doors = 0;
				for (Space s : p.getMap().values())
				{
					if (s.getType() == BoxType.Door)
						doors++;
				}

				Path next = p.clone();

				// Find paths to go to 0,1,...,n keys.
				// Example: If there are 3 keys I can get to, then the paths are:
				//  1. The original path (which is already accounted for)
				//  2. Going to the closest key
				//  3. Going to the closest key and then the next closest key
				//  4. Going to the closest key, then the next closest, then the next closest after that
				// Doesn't look for more keys than there are doors
				for (int i = 1; i <= Math.min(keys.size(), doors); i++)
				{
					k = new Dijkstras(next.getKeys(), next.getLocation(), next.getMap(), -1);
					// Find the shortest path to the list
					Stack<SpaceWrapper> toKey = k.shortestToType(next.getLocation(), BoxType.Key);

					if (toKey == null)		// This happens if we are already standing on the key
					{
						continue;
					}

					next.addToPath(toKey);		// Add the part to the path
					tempPaths.add(next);		// Add the path to the list of paths

					next = next.clone();		// Clone the new path so that it's cumulative
				}
			}

			paths.addAll(tempPaths);			// To avoid ConcurrentModificationException
			
			// PART 3: For all paths, find all possible paths to doors
			tempPaths = new ArrayList<>();

			itr = paths.iterator();
			while (itr.hasNext())
			{
				Path p = itr.next();

				if (p.getPath().size() > shortest)		// Prune paths that are already greater than the shortest one so far
				{
					itr.remove();
					continue;
				}

				itr.remove();		// Don't need the original path anymore. We'll either give up on it or branch from it.
				
				if (p.getKeys() == 0)
				{
					// We can't go to a door from this path because there are no keys left
					continue;
				}

				// Get a list of all doors that we can walk to without going through other doors
				Dijkstras k = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap(), -1);

				for (Space s : p.getMap().values())
				{
					if (s.getType() == BoxType.Door)
					{
						Stack<SpaceWrapper> toDoor = k.shortestToType(p.getLocation(), s);
						if (toDoor == null)		// No possible path to that door
							continue;

						Path next = p.clone();		// Clone the original path

						next.addToPath(toDoor);		// Add the path to the door to it
						tempPaths.add(next);		// Add it to the list of paths
					}
				}
			}

			paths.addAll(tempPaths);		// To avoid ConcurrentModificationException
			
			tempPaths = new ArrayList<>();
		}
		
		
		// END: Find the shortest path so far

		Path ideal = null;
		for (Path s : solved)
		{
			if (ideal == null || s.getPath().size() < ideal.getPath().size())
				ideal = s;
		}

		if (ideal == null)		// No known path exits
			return null;

		// A path does exist - Put it on a stack in the format we like
		List<SpaceWrapper> l = ideal.getPath();
		Stack<SpaceWrapper> result = new Stack<>();
		for (int i = l.size() - 1; i >= 0; i--)
		{
			result.push(l.get(i));
		}

		return result;
	}

}
