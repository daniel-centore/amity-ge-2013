package com.csc2013;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import com.csc2013.Dijkstras.GetKeyException;
import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Direction;
import com.csc2013.DungeonMaze.MoveType;

/**
 * 
 * The Amity Regional High School Case Study Solution
 * 
 * Algorithm Outline:
 * - If we have never found a solution to the current map
 * 		- Look for the closest area in the map we have never been to
 * 		- Go to it
 * - If we have solved it before
 * 		- Use a mixture of brute force and dijkstra's algorithm to find the bestish solution based on all our knowledge
 * 
 * @author Daniel Centore
 *
 */
public class SchoolPlayer
{
	// Print out debug data?
	public static final boolean VERBOSE = true;

	// The data that we save across all runs
	private static final LearningTracker LEARNING_TRACKER = new LearningTracker();

	public FieldMap map = new FieldMap(LEARNING_TRACKER);		// The map for the current game
	private Stack<Space> currentStack = null;					// The current stack of moves we're following
	private int moves = 0;										// The number of moves we've taken so far
	private boolean giveUp = false;								// True if we have issued any bad moves and want to give up

	/** 
	 * Called by the GE code.
	 * This just finds the action we want to take, applies it to our current map, and returns it
	 * 
	 * @param vision Our current {@link PlayerVision} data
	 * @param keyCount 
	 * @param lastAction
	 * @return Action
	 */
	public Action nextMove(final PlayerVision vision, final int keyCount, final boolean lastAction)
	{
		if (!lastAction || giveUp)		// We failed, so let's just give up.
		{
			if (VERBOSE)
				System.out.println("FAILED. JUST DOING RANDOM MOVES.");

			giveUp = true;

			return getSimpleAction(vision);
		}

		// Request our next move from the program
		Action action = null;
		try
		{
			action = amityNextMove(vision, keyCount);
		} catch (Throwable t)
		{
			// In case something goes horribly wrong, this is better than getting disqualified.
			if (VERBOSE)
				t.printStackTrace();

			giveUp = true;

			return getSimpleAction(vision);
		}

		// Apply the action we are about to take to our own map
		switch (action)
		{
		case North:
			map.applyMove(Direction.North);
			break;

		case South:
			map.applyMove(Direction.South);
			break;

		case East:
			map.applyMove(Direction.East);
			break;

		case West:
			map.applyMove(Direction.West);
			break;

		case Pickup:
			map.applyPickupKey();
			break;

		case Use:
			map.applyOpenDoor();
			break;
		}

		return action;
	}

	/**
	 * Comes up with a legal move. This is the "everything else failed" plan.
	 * @param vision The current vision
	 * @return A legal {@link Action}
	 */
	private Action getSimpleAction(PlayerVision vision)
	{
		if (vision.CurrentPoint.NorthMove != MoveType.Blocked)
			return Action.North;

		if (vision.CurrentPoint.SouthMove != MoveType.Blocked)
			return Action.South;

		if (vision.CurrentPoint.EastMove != MoveType.Blocked)
			return Action.East;

		if (vision.CurrentPoint.WestMove != MoveType.Blocked)
			return Action.West;

		// This should never happen (then again, this method shouldn't be running at all either)
		return Action.Use;
	}

	/**
	 * Figures out the next move to take
	 * 
	 * @param vision The current {@link PlayerVision}
	 * @param keyCount The number of keys we have
	 * @return An {@link Action} to take
	 */
	public Action amityNextMove(PlayerVision vision, int keyCount)
	{
		int oldMapSize = map.getMap().size();	// Calculate the amount of data we knew before applying our new vision
		map.fillVision(vision);					// Fill in any new data we learned from the vision

		// Recalculate the best path if:
		if (oldMapSize < map.getMap().size()		// The map changed, or
				|| currentStack == null				// The last iteration requested a recalculation, or
				|| currentStack.size() < 2)			// We have no moves left!
		{
			try
			{
				currentStack = new Dijkstras(keyCount, map).getNext();		// Request new move list

			} catch (GetKeyException e)		// The algorithm requested that we pick up a key
			{
				currentStack = null;		// Force a recalculation next time.
				return Action.Pickup;		// Pickup the key
			}
		}

		// Pickup a key if we are on top of it
		if (map.getMap().get(map.getLocation()).getType() == BoxType.Key)
		{
			return Action.Pickup;			// Pickup the key
		}

		moves++;		// Keep track of how many moves we've taken

		// About to hit the exit. Save our best solved exit case time so far to improve future algorithm runtimes.
		if (currentStack.get(currentStack.size() - 2).getType() == BoxType.Exit)
		{
			LEARNING_TRACKER.setBestCase(moves);
		}

		Action act = toAction(currentStack);		// Takes the next 2 positions and finds out what action is appropriate to take next
		if (act != Action.Use)
			currentStack.pop(); 												// Pop off our last movement
		else
			currentStack.get(currentStack.size() - 2).setType(BoxType.Open);	// The door is now open. Mark it as such and we'll walk to it next move.

		return act;
	}

	/**
	 * Finds the direction between the next two moves on the stack and then the appropriate action to take based on this
	 * 
	 * @param toExit The move list
	 * @throws RuntimeException If the stack is bad (ie The two moves are not consecutive)
	 * @return The action appropriate to take (either moving in a direction or opening a door)
	 */
	private Action toAction(Stack<Space> toExit)
	{
		if (toExit.get(toExit.size() - 2).getType() == BoxType.Door)		// If next space is a door, open it
			return Action.Use;

		Point a = toExit.get(toExit.size() - 1).getPoint();		// The two points
		Point b = toExit.get(toExit.size() - 2).getPoint();

		// Find the direction between them
		if (b.y - 1 == a.y)
			return Action.North;
		if (b.y + 1 == a.y)
			return Action.South;
		if (b.x - 1 == a.x)
			return Action.East;
		if (b.x + 1 == a.x)
			return Action.West;

		throw new RuntimeException("Bad stack: " + toExit.toString());
	}
}

/**
 * The map of the current game.
 * Handles parsing vision data as well
 * 
 * @author Daniel Centore 
 *
 */
class FieldMap
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

	// The best case we have encountered for this map so far (or Integer.MAX_VALUE if it has never been solved)
	private int bestCase;

	/**
	 * Instantiates the {@link FieldMap}
	 * @param lt The {@link LearningTracker} which keeps track of how much we know about the map already
	 */
	public FieldMap(LearningTracker lt)
	{
		originalMap = lt.nextMap();
		bestCase = lt.getBestCase();

		updateData(originalMap);		// Deep copies how much we know about the map already
	}

	/**
	 * Inserts all the data from 'data' into this.map without referencing any of the original objects.
	 * Performs a deep copy.
	 * @param data A map of the field
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
		BoxType type = BoxType.Open; 	// Assume it's open as we only get spaces that we can walk on

		if (box.hasKey()) 			// If it has a key mark it as a key
			type = BoxType.Key;

		// Save the original space
		saveSpace(x, y, type);

		// Now save the surroundings
		saveSpace(x, y + 1, box.North);
		saveSpace(x, y - 1, box.South);
		saveSpace(x + 1, y, box.East);
		saveSpace(x - 1, y, box.West);
	}

	/**
	 * If a space already exists, verify that it is correct.
	 * If it doesn't, add it
	 * 
	 * @throws RuntimeException If the space already exists and the previous type contrasts with the new one
	 * 
	 * @param x X coordinate of the {@link Space}
	 * @param y Y coordinate of the {@link Space}
	 * @param type The type of space it is
	 * 
	 * @return The {@link Space} which either already existed in the map or which we added.
	 */
	private Space saveSpace(int x, int y, BoxType type)
	{
		Point p = new Point(x, y);

		if (map.containsKey(p))		// If the point already exists
		{
			Space sp = map.get(p);

			if (sp.getType() != type)	// The new one we want to add contrasts with the already existing one
				throw new RuntimeException("Expected type " + sp + " at " + p.x + "," + p.y + " but asked to save " + type);

			return sp;
		}
		else
		{
			if (!originalMap.containsKey(p))		// add the space as it existed in the original map to the learned map
				originalMap.put(p, new Space(x, y, type));

			Space sp = new Space(x, y, type);		// add the new space
			map.put(p, sp);

			return sp;
		}
	}

	/**
	 * Lets the map know that we moved in a direction and updates the location accordingly 
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
	 * Lets the map know we just picked up a key on the space we're on.
	 */
	public void applyPickupKey()
	{
		map.get(location).setType(BoxType.Open);
	}

	/**
	 * Lets the map know we just opened a door.
	 */
	public void applyOpenDoor()
	{
		Point p = new Point();
		Space sp;

		// Look around us and open any doors (there should theoretically only be 1)

		// North
		p.x = location.x;
		p.y = location.y + 1;
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);

		// South
		p.x = location.x;
		p.y = location.y - 1;
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);

		// East
		p.x = location.x + 1;
		p.y = location.y;
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);

		// West
		p.x = location.x - 1;
		p.y = location.y;
		if (map.containsKey(p) && (sp = map.get(p)).getType() == BoxType.Door)
			sp.setType(BoxType.Open);
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

	/**
	 * Get's the best number of moves we've encountered so far for the map.
	 * Be careful because since this can be Integer.MAX_VALUE you need to watch out for overflow
	 * @return The number of moves (or Integer.MAX_VALUE if it has never been solved)
	 */
	public int getBestCase()
	{
		return bestCase;
	}

}

/**
 * Represents a single space on the board
 * 
 * @author Daniel Centore
 *
 */
class Space
{
	private BoxType type; // type of space we are

	// The (x,y) coordinate of this space
	private final int x;
	private final int y;

	// Just a Point representation of (x,y) for quick access
	private final Point point;

	// True if this Space is representing all spaces we haven't been to yet 
	private boolean unexplored = false;

	// == Dijkstra's algorithm Variables == //
	private int length;						// The distance of this Space from root
	private boolean removed = false;		// Whether or not this space has been removed
	private Space previous = null;			// The previous space on the chain back to root

	/**
	 * Creates a space
	 * @param x X coordinate of the space
	 * @param y Y coordinate of the space
	 * @param type The {@link BoxType} of the space.
	 */
	public Space(int x, int y, BoxType type)
	{
		this.x = x;
		this.y = y;
		this.type = type;

		point = new Point(x, y);
	}

	/**
	 * Gets the current {@link BoxType} of this space
	 * @return The type of space we are representing
	 */
	public BoxType getType()
	{
		return type;
	}

	/**
	 * Sets the type of this space.
	 * You should really only be setting this to empty after opening a door or picking up a key.
	 * @param type The {@link BoxType} to set it to.
	 */
	public void setType(BoxType type)
	{
		this.type = type;
	}

	/**
	 * Gets the X coordinate of the space
	 * @return The X coordinate
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * Gets the Y coordinate of the space
	 * @return The Y coordinate
	 */
	public int getY()
	{
		return y;
	}

	/**
	 * Gets the position of the point as a {@link Point}
	 * @return The position
	 */
	public Point getPoint()
	{
		return point;
	}

	@Override
	public String toString()
	{
		return "Space [type=" + type + ", x=" + x + ", y=" + y + ", removed=" + removed + "]";
	}

	/**
	 * Override the hashcode and equals so that two Spaces are considered equal whenever they are located in the same position
	 * This is to make things easier when referencing them in HashMaps and the like
	 */

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Space other = (Space) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	/**
	 * Finds the current distance of the space from root
	 * @return  The length
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Sets the current distance of the space from root
	 * @param length The length
	 */
	public void setLength(int length)
	{
		this.length = length;
	}

	/**
	 * Checks if the space has been visited/removed from the graph
	 * @return True if it has been removed; False otherwise
	 */
	public boolean isRemoved()
	{
		return removed;
	}

	/**
	 * Sets if the space has been visited/removed from the graph
	 * @param removed True if it has been removed; False otherwise
	 */
	public void setRemoved(boolean removed)
	{
		this.removed = removed;
	}

	/**
	 * Gets the previous space on the chain back to the root node
	 * @return The previous space or null if this is root
	 */
	public Space getPrevious()
	{
		return previous;
	}

	/**
	 * Sets the previous space on the chain back to root
	 * @param previous The space to set it to
	 */
	public void setPrevious(Space previous)
	{
		this.previous = previous;
	}

	/**
	 * Is this space a marker for unexplored territory?
	 * @return True if it is; False otherwise
	 */
	public boolean isUnexplored()
	{
		return unexplored;
	}

	/**
	 * Sets whether or not this space is a marker for unexplored territory
	 * @param unexplored True if it is; False otherwise
	 */
	public void setUnexplored(boolean unexplored)
	{
		this.unexplored = unexplored;
	}
}

/**
 * Uses Dijkstra's pathfinding algorithm to find the shortest route between 2 nodes.
 * This class also handles finding the next path to take.
 * 
 * @author Daniel Centore
 *
 */
class Dijkstras
{
	/**
	 * This exception is used to tell the {@link SchoolPlayer} that we want to pick up a key rather than
	 * follow a path.
	 */
	class GetKeyException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	private int keys;							// How many keys we have right now
	private Point location;						// The player's current location
	private HashMap<Point, Space> map;			// The player's current map
	private int bestCase;						// The best case the player has encountered in this map

	// The space that represents somewhere we haven't been to yet
	private static final Space UNEXP;
	static
	{
		UNEXP = new Space(Integer.MAX_VALUE, Integer.MAX_VALUE, null);
		UNEXP.setUnexplored(true);
		UNEXP.setLength(Integer.MAX_VALUE);
	}
	// The location to go along with that space
	private static final Point INFI = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

	/**
	 * Creates an instance of the Dijkstra's algorithm solver
	 * @param keys Number of keys the player has
	 * @param map The current {@link FieldMap} from which we extract other data
	 */
	public Dijkstras(int keys, FieldMap map)
	{
		this(keys, map.getLocation(), map.getMap(), map.getBestCase());
	}

	/**
	 * Creates an instance of the Dijkstra's algorithm solver
	 * @param keys Number of keys the player has
	 * @param location The player's current location
	 * @param map The player's current map
	 * @param bestCase The best case the player has encountered in this map
	 */
	public Dijkstras(int keys, Point location, HashMap<Point, Space> map, int bestCase)
	{
		this.keys = keys;
		this.location = location;
		this.map = map;
		this.bestCase = bestCase;
	}

	/**
	 * Figures out which path to take next.
	 * @return A {@link Stack} which gives you the moves you should take in order.
	 * 			The first and last elements are where you are and where you want to be, respectively. 
	 * @throws GetKeyException If we want you to pick up a key instead of following a path
	 */
	public Stack<Space> getNext() throws GetKeyException
	{
		// Find shortest path to an exit. Take it if it exists.
		// This uses a brute force to try to find the very most ideal path
		try
		{
			// Check to see if we even know about the existence of an exit
			boolean hasExit = false;
			for (Space s : map.values())
			{
				if (s.getType() == BoxType.Exit)
				{
					hasExit = true;
					break;
				}
			}

			// If we know about an exit, try to find the best path to it
			if (hasExit)
			{

				Stack<Space> toExit = new BruteForcePathfinder(keys, location, map, bestCase).toType(BoxType.Exit);
				if (toExit != null)
					return toExit;
			}
		} catch (Throwable e)
		{
			// If the brute force algorithm fails (unexpectedly) then fall back on this algorithm
			if (SchoolPlayer.VERBOSE)
				e.printStackTrace();
		}

		// If standing on key, grab it
		if (map.get(location).getType() == BoxType.Key)
			throw new GetKeyException();

		// Grab keys if we need them and they are nearby
		Stack<Space> toCloseKey = shortestToType(location, BoxType.Key);
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
		Stack<Space> toUnknown = new BruteForcePathfinder(keys, location, map, Integer.MAX_VALUE).toType(null);

		if (toUnknown == null)
		{
			String s = "This map is seemingly impossible to solve in the current state.\n" +
					"We've explored all unexplored areas and used all available key+door combinations.\n" +
					"This usually means the map has some sort of flaw in it which permits one to use\n" +
					"all of the map's keys but still have some doors locked.";

			throw new RuntimeException(s);
		}

		return toUnknown;		// Return the shortest path
	}

	/**
	 * Returns the shortest path to a type
	 * Special Case: If the start point IS of type type, then it returns NULL!
	 * @param start The initial point
	 * @param type The type of space we want to go to
	 * @return The shortest path to the closest space of the requested type
	 */
	public Stack<Space> shortestToType(Point start, BoxType type)
	{
		return shortestToType(start, type, null);
	}

	/**
	 * Returns the shortest path to a space
	 * @param start The initial point
	 * @param goal The space we want to go to
	 * @return The shortest path to the requested space
	 */
	public Stack<Space> shortestToType(Point start, Space goal)
	{
		Stack<Space> stack = shortestToType(goal.getPoint(), null, map.get(start));
		if (stack == null)
			return null;

		Stack<Space> result = new Stack<>();		// checks in reverse order (fails faster

		while (!stack.isEmpty())
			result.add(stack.pop());

		return result;
	}

	/**
	 * Finds the shortest path from a {@link Point} to a goal using Dijkstra's algorithm.
	 * The goal can be either a certain type of space (like unexplored, door, key) or to a specific space (like 2,5)
	 * 
	 * @param start The starting {@link Point}
	 * @param type The {@link BoxType} we are looking for. Set to null if you want unexplored or to use the {@link Space} goal instead.
	 * @param goal The {@link Space} goal we want to go to. Set to null to use the {@link BoxType} goal.
	 * 
	 * @return The {@link Stack} of moves to follow. The first element will be the {@link Space} on {@link Point} and the last element is the goal.
	 * 			This can be null if there is no possible path.
	 */
	private Stack<Space> shortestToType(Point start, BoxType type, Space goal)
	{
		if (goal != null)
		{

		}
		if (!map.containsKey(INFI))		// Add unknown as a possible goal
			map.put(INFI, UNEXP);

		List<Space> unremoved = new ArrayList<>();		// All spaces that have not been visited by Dijkstra's
		List<Space> removed = new ArrayList<>();		// All spaces that have been visited

		// Add all non-blocked spaces to the algorithm list
		for (Space k : map.values())
		{
			if (k.getType() != BoxType.Blocked)
				unremoved.add(k);
		}

		// Clear out the pathfinding data in those nodes
		reset(start, unremoved);

		while (true)
		{
			// Is the goal still in the graph?
			for (Space sw : removed)
			{
				if ((goal != null && !sw.isUnexplored() && sw.equals(goal))				// If we found the Space goal, or
						|| (goal == null && !sw.isUnexplored() && sw.getType() == type)	// If we found the type goal,  or
						|| (goal == null && sw.isUnexplored() && type == null)			// If we found the type goal (for unexplored)
				)
				{
					// Generate a stack of the path and return it
					// This is based on the backward linking of one node in the path to the next
					Stack<Space> fullPath = new Stack<>();

					Space path = sw;
					do
					{
						fullPath.push(path);
						path = path.getPrevious();

					} while (path != null);

					if (fullPath.size() <= 1) // Need to be at least 2 elements to be a path, otherwise we've got a dud.
						return null;

					return fullPath;
				}
			}

			// Choose the vertex with the least distance
			Space min = min(unremoved);

			if (min == null) 	// No possible path to our goal
				return null;

			// Remove it from the graph (or "mark it as visited")
			min.setRemoved(true);
			removed.add(min);
			unremoved.remove(min);

			if (min != null)
			{
				// Calculate distances between the vertex with the smallest distance and neighbors still in the graph
				for (Space sp : MapUtils.findSurroundingSpaces(map, min, UNEXP))
				{
					if (sp.isRemoved())			// Ignore the item if we've already visited it
						continue;

					// Ignore null spaces unless we are actually looking for unexplored areas
					if ((sp.isUnexplored()) && (type != null || goal != null))
						continue;

					// Don't include doors if we are not looking for a door
					if (sp.getType() == BoxType.Door && type != BoxType.Door && !sp.equals(goal))
						continue;

					int length = min.getLength() + 1; 		// Difficulty for getting anywhere is 1

					// If this is the shortest path to the node so far, label it as such.
					if (length < sp.getLength())
					{
						sp.setLength(length);
						sp.setPrevious(min);
					}
				}
			}

			// Time for another iteration of the while loop....
		}
	}

	/**
	 * Find the element in the collection with the shortest length.
	 * 
	 * @param collection The {@link Collection} of nodes
	 * @return The {@link Space} with the smallest value.
	 */
	private Space min(Collection<Space> collection)
	{
		Space shortest = null;
		Iterator<Space> itr = collection.iterator();

		while (itr.hasNext())
		{
			Space next = itr.next();

			if (shortest == null || shortest.getLength() > next.getLength())
				shortest = next;
			else if (shortest.getLength() == next.getLength())
				shortest = next;
		}

		if (shortest == null || shortest.getLength() == Integer.MAX_VALUE)		// The 'shortest' path is impossible to get to.
		{
			return null;
		}

		return shortest;
	}

	/**
	 * Resets all the {@link Space}s in terms of the temporary pathfinding variables
	 * @param start The point which will have an initial length of 0 for this algorithm run
	 * @param parts The {@link List} to go through
	 */
	private void reset(Point start, List<Space> parts)
	{
		for (Space s : parts)
		{
			if (s == null)
				continue;

			int dist = Integer.MAX_VALUE;
			if (s.getPoint().equals(start))
				dist = 0;

			s.setLength(dist);
			s.setRemoved(false);
			s.setPrevious(null);
		}
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

/**
 * Looks for the shortest possible path to a type.
 * This uses a combination of brute forcing and dijkstras algorithm to find the ideal path including door+key combinations.
 * 
 * Uses the following algorithm:
 *  1. Look for all possible paths to the type. Add them to the 'solved' list.
 *  2. Branch for all possible paths to keys.
 *  3. Branch for all possible paths to doors.
 *  4. Repeat until all possible key+door combinations are exhausted.
 * 
 * The algorithm uses some approximations in this "brute force" so it doesn't take days to run:
 *  - Once a door has been opened, all keys before that door are marked as nonexistant for future iterations along the path
 *  - Instead of finding *all* key combinations, it instead finds the shortest route to 0,1,2,...,n keys and uses each of these as a branch.
 *  	This is further described within the algorithm
 * 
 * @author Daniel Centore
 *
 */
class BruteForcePathfinder
{
	// The algorithm is first run cutting off all paths that get longer than 100 moves.
	// If it failed to solve the map it is increased to 200, then 300, and so on.
	// This is so we don't end up with massive amounts of time going into solving very long paths.
	private static final int SMALLEST_DELTA = 100;

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
	public BruteForcePathfinder(int keys, Point currentLocation, HashMap<Point, Space> currentMap, int bestCase)
	{
		this.currentKeys = keys;
		this.currentLocation = currentLocation;
		this.currentMap = currentMap;
		this.bestCase = bestCase;
	}

	/**
	 * Finds the shortest path to a {@link BoxType} using the algorithm outlined in the class javadoc
	 * @param type The {@link BoxType} to look for (you can also use null to indicate an unexplored area)
	 * @return The stack of moves to follow
	 */
	public Stack<Space> toType(BoxType type)
	{
		int input = SMALLEST_DELTA;		// Initial cutoff value for path length
		int shortest = (bestCase == Integer.MAX_VALUE ? Tournament.maxSteps : bestCase);		// Absolute maximum cutoff value
		boolean lastTry = false;		// If this is the last iteration we can perform before giving up

		// Once we are using the maximum cutoff value as out cutoff, label this as the last iteration
		if (input > shortest)
		{
			input = shortest;
			lastTry = true;
		}

		while (true)
		{
			Stack<Space> result = toTypeSub(type, input);		// Try getting a result for the current cutoff value

			if (result != null)
			{
				return result;
			}

			input += SMALLEST_DELTA;	// Increase the cutoff value

			if (lastTry)				// Time to give up
				break;

			if (input > shortest)		// Once we are using the maximum cutoff value as out cutoff, label this as the last iteration
			{
				input = shortest;
				lastTry = true;
			}
		}

		return null;

	}

	/**
	 * Actually does the calculation trying to find the shortest path to a type
	 * @param type The {@link BoxType} we are trying to get to
	 * @param shortest The maximum path length
	 * @return The stack of moves to follow
	 */
	private Stack<Space> toTypeSub(BoxType type, int shortest)
	{
		if (SchoolPlayer.VERBOSE)
			System.out.println("Goal: " + type);

		int iteration = 1;
		List<Path> solved = new ArrayList<>();		// List of paths that lead to an exit
		List<Path> paths = new ArrayList<>();		// List of paths we are still evaluating

		paths.add(new Path(currentMap, currentLocation, currentKeys));		// Add an initial path which we'll branch off of

		// While there are still paths to evaluate, evaluate them!
		while (paths.size() > 0)
		{
			if (SchoolPlayer.VERBOSE)
			{
				System.out.println();
				System.out.println("=== STARTING ITERATION " + (iteration++) + " ===");
				System.out.println("Checking for duplicates...");
			}

			// Check for duplicates
			List<Path> dups = findDups(paths);
			int initialSize = paths.size();

			paths.removeAll(dups);		// Remove all the duplicates

			if (SchoolPlayer.VERBOSE)
				System.out.println("Finished Checking. Removed [" + (initialSize - paths.size()) + "]");

			dups = null;		// Clean up our references

			// PART 1: For each path, see if there is a way to get to the goal without going through doors
			// If there is, add the path to the solved list.
			if (SchoolPlayer.VERBOSE)
				System.out.println("Looking for paths to goal [" + type + "]...");

			Iterator<Path> itr = paths.iterator();
			while (itr.hasNext())		// Go through all the paths
			{
				Path p = itr.next();

				Dijkstras d = new Dijkstras(p.getKeys(), p.getLocation(), p.getMap(), -1);

				Stack<Space> toExit = d.shortestToType(p.getLocation(), type);		// Find shortest path to an exit
				if (toExit != null)			// There is such a path
				{
					p = p.clone();
					p.addToPath(toExit);		// Add going to the exit to the path
					solved.add(p);				// Add the path to the solved list

					if (p.getPathSize() < shortest)		// Mark the path as shortest if it is
						shortest = p.getPathSize();
				}
			}

			if (SchoolPlayer.VERBOSE)
			{
				System.out.println("Solved Paths:  " + solved.size());
				System.out.println("Shortest Path: " + shortest);
				System.out.println("Total Paths:   " + paths.size());
				System.out.println();
			}

			// PART 2: For each path, find all possible reasonable paths to keys

			if (SchoolPlayer.VERBOSE)
				System.out.println("Looking for paths to keys...");

			List<Path> tempPaths = toKeys(paths, shortest);
			paths.addAll(tempPaths);

			if (SchoolPlayer.VERBOSE)
			{
				System.out.println("Added key paths. Total paths: " + paths.size());
				System.out.println("Looking for paths to doors....");
			}

			// PART 3: For all paths, find all possible paths to doors
			tempPaths = toDoors(paths, shortest);
			paths.addAll(tempPaths);

			tempPaths = new ArrayList<>();

			if (SchoolPlayer.VERBOSE)
				System.out.println("Found paths to doors. Total paths: " + paths.size());
		}

		// END: Find the shortest path in our solved list

		Path ideal = null;
		for (Path s : solved)
		{
			if (ideal == null || s.getPathSize() < ideal.getPathSize())
				ideal = s;
		}

		if (ideal == null)		// No known path exits
		{
			return null;
		}

		// A path does exist - Put it on a stack in the format we like
		List<Space> l = ideal.getPath();
		Stack<Space> result = new Stack<>();
		for (int i = l.size() - 1; i >= 0; i--)
		{
			result.push(l.get(i));
		}

		return result;
	}

	/**
	 * Finds all reasonable paths to doors (helper method)
	 * @param paths The list of paths to iterate through
	 * @param shortest The cutoff distance for paths
	 * @return The new list of paths
	 */
	private List<Path> toDoors(List<Path> paths, int shortest)
	{
		List<Path> tempPaths = new ArrayList<>();

		Iterator<Path> itr = paths.iterator();
		while (itr.hasNext())		// Go through all the paths
		{
			Path p = itr.next();

			itr.remove();		// Don't need the original path anymore. We'll either give up on it or branch from it.

			if (p.getKeys() == 0)
			{
				// We can't go to a door from this path because there are no keys left
				continue;
			}
			Point pLoc = p.getLocation();

			// Get a list of all doors that we can walk to without going through other doors
			Dijkstras k = new Dijkstras(p.getKeys(), pLoc, p.getMap(), -1);

			for (Space s : p.getMap().values())
			{
				if (s.getType() == BoxType.Door)
				{
					Stack<Space> toDoor = k.shortestToType(pLoc, s);

					if (toDoor == null)			// No possible path to that door
						continue;

					// The path will be too long. Don't add it.
					if (toDoor.size() + p.getPathSize() > shortest)
						continue;

					Path next = p.clone();		// Clone the original path

					next.addToPath(toDoor);		// Add the path to the door to it
					tempPaths.add(next);		// Add it to the list of paths

				}
			}
		}

		return tempPaths;
	}

	/**
	 * Finds all reasonable paths to keys (helper method)
	 * @param paths The list of paths to iterate through
	 * @param shortest The cutoff distance for paths
	 * @return The new list of paths
	 */
	private List<Path> toKeys(List<Path> paths, int shortest)
	{
		List<Path> tempPaths = new ArrayList<>();

		Iterator<Path> itr = paths.iterator();
		while (itr.hasNext())
		{
			Path p = itr.next();

			if (p.getPathSize() > shortest)		// Prune paths that are already greater than the shortest one so far
			{
				itr.remove();
				continue;
			}

			// Get a list of all keys that we can walk to without going through doors
			Queue<Space> keys = new LinkedList<>();		// The list of keys
			Path temp = p.clone();

			Dijkstras k = new Dijkstras(temp.getKeys(), temp.getLocation(), temp.getMap(), -1);
			while (true)
			{
				k = new Dijkstras(temp.getKeys(), temp.getLocation(), temp.getMap(), -1);

				Stack<Space> toKey = k.shortestToType(temp.getLocation(), BoxType.Key);
				if (toKey == null)
					break;
				else
				{
					temp.addToPath(toKey);
					keys.offer(p.getMap().get(temp.getLocation()));		// Use p's map so we use original value
				}
			}

			// There are no more keys to get and we're out of keys. Kill the potential path.
			if (keys.size() == 0 && p.getKeys() == 0)
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

			// Generate a list of keys in each direction
			// This generates a list of lists such that the first list contains all keys in order of increasing distance in one
			// direction, the second list contains all keys in another direction, and so on and so forth.
			List<List<Space>> keyLists = new ArrayList<>();

			while (!keys.isEmpty())
			{
				List<Space> curr = new ArrayList<>();
				temp = p.clone();
				k = new Dijkstras(temp.getKeys(), temp.getLocation(), temp.getMap(), -1);		// create a new path

				Space key = keys.poll();	// Get the next key

				temp.addToPath(k.shortestToType(temp.getLocation(), key));		// Add it to the path

				curr.add(key);		// Add it to the current direction list

				Iterator<Space> spi = keys.iterator();		// iterate through the remaining keys
				while (spi.hasNext())
				{
					key = spi.next();

					Stack<Space> original = k.shortestToType(p.getLocation(), key);
					Stack<Space> after = k.shortestToType(temp.getLocation(), key);

					if (after.size() < original.size())		// If we got closer to the key by taking the path
					{
						curr.add(key);		// Add it to the current direction list
						spi.remove();		// Remove it from future iterations
					}
				}

				keyLists.add(curr);		// Add the current direction to the list of directions
			}

			keys = null;

			// Iterates through each of the directions and then finds the possible paths in each of those directions
			for (List<Space> list : keyLists)
			{
				Path next = p.clone();

				// Find paths to go to 0,1,...,n keys.
				// Example: If there are 3 keys I can get to, then the paths are:
				//  1. The original path (which is already accounted for)
				//  2. Going to the closest key
				//  3. Going to the closest key and then the next closest key
				//  4. Going to the closest key, then the next closest, then the next closest after that
				// Doesn't look for more keys than there are doors
				int i = 0;
				for (Space s : list)		// Go through all spaces in the current direction list
				{
					i++;
					if (i > doors - next.getKeys())		// If we are on more keys than there are doors, scrap the paths
						break;

					k = new Dijkstras(next.getKeys(), next.getLocation(), next.getMap(), -1);

					Stack<Space> toKey = k.shortestToType(next.getLocation(), s);	// Find the path to the key

					if (toKey == null)		// This happens if we are already standing on the key
					{
						continue;
					}

					if (next.getPathSize() + toKey.size() > shortest)		// The path will be too long. Don't add it.
						continue;

					next.addToPath(toKey);		// Add the part to the path
					tempPaths.add(next);		// Add the path to the list of paths

					next = next.clone();		// Clone the Path for the next key iteration
				}
			}
		}

		return tempPaths;
	}

	/**
	 * Finds duplicates in a list of paths
	 * @param paths The list of paths to go through
	 * @return The elements to remove
	 */
	private List<Path> findDups(List<Path> paths)
	{
		List<Path> dups = new ArrayList<>();

		for (int i = 0; i < paths.size(); i++)
		{
			Path p = paths.get(i);
			List<Space> pPath = p.getPath();

			// Only looks a maximum of 10000 spaces ahead in the list.
			// This approximation works for most cases and really speeds things up
			for (int j = i + 1; j < Math.min(10000, paths.size()); j++)
			{
				Path q = paths.get(j);

				if (q.getPathSize() != p.getPathSize())
					continue;

				List<Space> qPath = q.getPath();

				if (Path.pathsEqual(qPath, pPath))
				{
					dups.add(q);
				}
			}
		}

		return dups;
	}

}

/**
 * Represents a path we are simulating in a brute force
 * We use these in a chain fashion so when one gets cloned, the clone links to the original to save memory
 * You can thus only add to a Path and then the difference between the paths is stored
 * 
 * @author Daniel Centore
 *
 */
class Path
{
	// The current state of the map in this path.
	// Only stores values that are different than the previous map on the chain
	private HashMap<Point, Space> map;

	private int keys;							// The number of keys the player has
	private ArrayList<Space> path;				// The path so far. The first element is first thing to perform
	private int pathSize = 0;					// The path size

	private Path previous;						// The previous path in the chain

	/**
	 * Creates a new Path from scratch
	 * @param newMap The map to load (we do a shallow clone of it but deep clone any objects before changing them)
	 * @param location The player's current location
	 * @param keys The number of keys the player has
	 */
	public Path(HashMap<Point, Space> newMap, Point location, int keys)
	{
		this.keys = keys;

		map = new HashMap<>();
		load2(newMap);		// Clone the map

		path = new ArrayList<>();		// Add our current location to the path
		path.add(map.get(location));
	}

	/**
	 * Creates a new path (for use by cloning)
	 * @param newMap The map to clone
	 * @param keys Number of keys the player has
	 * @param path The path so far
	 * @param previous The previous node on the chain of paths
	 */
	private Path(HashMap<Point, Space> newMap, int keys, ArrayList<Space> path, Path previous)
	{
		this.keys = keys;

		map = new HashMap<>();

		this.path = new ArrayList<>();

		this.previous = previous;
		this.pathSize = previous.pathSize;
	}

	/**
	 * Shallow copies the map
	 * @param newMap The map to shallow copy
	 */
	private void load2(HashMap<Point, Space> newMap)
	{
		for (Space sp : newMap.values())
			map.put(sp.getPoint(), sp);
	}

	@Override
	public Path clone()
	{
		return new Path(this.map, this.keys, this.path, this);
	}

	/**
	 * Concatenates another path onto this one
	 * @param proceed The path to add onto it
	 */
	@SuppressWarnings("unchecked")
	public long addToPath(Stack<Space> proceed)
	{
		proceed = (Stack<Space>) proceed.clone();

		// Don't put on the first element of the path as it matches the last element of our current list
		proceed.pop();

		long time = 0;

		while (!proceed.isEmpty())
		{
			Space next = proceed.pop();

			if (next.isUnexplored())		// Do not include walking to unknown in the path
			{
				break;
			}

			// Set the type and location.
			BoxType type = next.getType();

			// Handle key usage along the path
			switch (type)
			{
			case Door:
				// Pretend any keys inside an area are nonexistant after we've opened a door.
				// This is a pretty good approximation although not a perfect one.
				// Without this pruning, the number of brute force paths quickly gets out of hand
				long k = System.currentTimeMillis();
//				pruneKeys();
				time += System.currentTimeMillis() - k;

				keys--;
				cloneSpaceToMap(next).setType(BoxType.Open);		// We open the door
				break;

			case Key:
				keys++;
				cloneSpaceToMap(next).setType(BoxType.Open);		// We pick up the key
				break;

			default:
				break;
			}

			// Add the path element
			path.add(next);
			pathSize++;
		}

		return time;
	}

	/**
	 * Prunes out any keys that are currently reachable
	 */
	private void pruneKeys()
	{
		Dijkstras dijkstras = new Dijkstras(this.getKeys(), this.getLocation(), this.getMap(), -1);

		for (Space s : this.getMap().values())
		{
			if (s.getType() == BoxType.Key)
			{
				Stack<Space> toKey = dijkstras.shortestToType(this.getLocation(), s);
				if (toKey == null)		// If we cannot get to the key, continue
					continue;

				cloneSpaceToMap(s).setType(BoxType.Open);		// Mark it as open (even though it's not!)
			}
		}

	}

	/**
	 * Clones a space that's in the map (which is also likely referenced elsewhere) and puts the clone in the map
	 * @param me The {@link Space} to clone
	 * @return The cloned spaces (so you can then make changes to it)
	 */
	private Space cloneSpaceToMap(Space me)
	{
		Point p = me.getPoint();

		Space sp = new Space(me.getX(), me.getY(), me.getType());		// add space
		map.put(p, sp);

		return sp;
	}

	/**
	 * Gets the number of keys that the path has
	 * @return The number of keys
	 */
	public int getKeys()
	{
		return keys;
	}

	/**
	 * Gets the current state of the map for this path
	 * This is calculated on the fly based on the previous paths on the chain
	 * @return The map
	 */
	public HashMap<Point, Space> getMap()
	{
		HashMap<Point, Space> temp = new HashMap<>();

		Path p = this;
		// Go through the chain
		do
		{
			for (Space s : p.map.values())		// Add all the spaces to the map
			{
				if (!temp.containsKey(s.getPoint()))
					temp.put(s.getPoint(), s);
			}

			p = p.previous;
		} while (p != null);

		return temp;
	}

	/**
	 * Gets the current simulation's location
	 * WARNING: This is slow so save the value and reuse it if possible
	 * @return The player's current location
	 */
	public Point getLocation()
	{
		List<Space> temp = getPath();

		return temp.get(temp.size() - 1).getPoint();
	}

	/**
	 * Gets the current simulation path
	 * This is calculated on the fly based on the previous paths on the chain
	 * @return The current path
	 */
	public List<Space> getPath()
	{
		Stack<Path> backward = new Stack<>();		// Fills up the path backward on a stack

		Path p = this;

		do
		{
			backward.add(p);

			p = p.previous;
		} while (p != null);

		List<Space> result = new ArrayList<>();

		while (!backward.isEmpty())
			// Pop them back off in the right order
			result.addAll(backward.pop().path);

		return result;
	}

	@Override
	public String toString()
	{
		return "Path [path=" + path + "]";
	}

	/**
	 * Gets the path's current size
	 * This is saved so it is much faster that getPath().size()
	 * @return The size of the path
	 */
	public int getPathSize()
	{
		return pathSize;
	}

	/**
	 * Checks if two paths are equal
	 * This is optimized to be very quick for comparisons when we check for duplicates in the brute force
	 * @param qPath The first path
	 * @param pPath The second path
	 * @return True if they are equal; false otherwise
	 */
	public static boolean pathsEqual(List<Space> qPath, List<Space> pPath)
	{
		for (int i = qPath.size() - 1; i >= 0; i--)
		{
			if (!qPath.get(i).equals(pPath.get(i)))
				return false;
		}

		return true;
	}
}

/**
 * This class keeps track of maps as we learn them.
 * This way, we can take data we've learned in the past to help us make better decision in the future.
 * 
 * @author Daniel Centore
 *
 */
class LearningTracker
{
	private int currentMap = -1;										// The current map we are playing
	private List<HashMap<Point, Space>> maps = new ArrayList<>();		// A list of the known map for each game 
	private List<Integer> bestCase = new ArrayList<>();					// The best move case we have encountered for each map

	/**
	 * Gets the next map to use for learning.
	 * This will be copied for 'map' in {@link FieldMap}.
	 * We will use a pointer to this for 'originalMap' in {@link FieldMap}. That way, we can just update the map seamlessly.
	 * @return
	 */
	public HashMap<Point, Space> nextMap()
	{
		currentMap++;
		if (currentMap >= Tournament.maps.length)
		{
			currentMap = 0;
		}

		if (maps.size() >= currentMap)
			maps.add(new HashMap<Point, Space>());

		return maps.get(currentMap);
	}

	/**
	 * Gets the best encountered case for the current map
	 * @return The best encountered number of moves or Integer.MAX_VALUE if we have not yet solved it
	 */
	public int getBestCase()
	{
		if (bestCase.size() <= currentMap)
			bestCase.add(Integer.MAX_VALUE);

		return bestCase.get(currentMap);
	}

	/**
	 * Sets the best encountered case for the current map
	 * @param i The number of moves to set it to
	 */
	public void setBestCase(int i)
	{
		bestCase.set(currentMap, i);
	}
}

/**
 * Tools for processing maps which aren't part of {@link FieldMap} (ie those within paths)
 * 
 * @author Daniel Centore
 *
 */
class MapUtils
{
	/**
	 * Finds all unblocked spaces surrounding a space
	 * @param map The map to look in
	 * @param sp The space to look around
	 * @param unexp What to represent "unknown" with
	 * @return
	 */
	public static List<Space> findSurroundingSpaces(HashMap<Point, Space> map, Space sp, Space unexp)
	{
		List<Space> result = new ArrayList<>();

		Point p = sp.getPoint();

		// The spaces in each of the directions
		Point n = new Point(p.x, p.y + 1);
		Point s = new Point(p.x, p.y - 1);
		Point e = new Point(p.x + 1, p.y);
		Point w = new Point(p.x - 1, p.y);

		Point[] points = { n, s, e, w };

		boolean u = false;

		for (Point g : points)
		{
			if (map.containsKey(g))
			{
				Space k = map.get(g);
				if (k.getType() != BoxType.Blocked)
					result.add(k);
			}
			else if (!u)		// If the space is not known and we have not yet added the unknown space, then add it
			{
				result.add(unexp);
				u = true;
			}
		}

		return result;
	}
}