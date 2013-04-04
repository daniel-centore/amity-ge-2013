package com.solution2013;

import java.awt.Point;
import java.util.Stack;

import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Direction;
import com.csc2013.PlayerVision;
import com.solution2013.Dijkstras.GetKeyException;
import com.solution2013.field.FieldMap;
import com.solution2013.field.SpaceWrapper;

/**
 * 
 * [To be completed by students]
 * 
 * @author Amity Region 5
 *
 */
public class SchoolPlayer
{
	private static LearningTracker LEARNING_TRACKER = new LearningTracker();

	public FieldMap map = new FieldMap(LEARNING_TRACKER);		// The map for the current game
	private Stack<SpaceWrapper> currentStack = null;			// The current stack of moves we're following
	private int moves = 0;										// The number of moves we've taken so far

	public SchoolPlayer() throws SlickException
	{
		// complete
	}

	/** 
	 * To properly implement this class you simply must return an Action in the function nextMove below.
	 * You are allowed to define any helper variables or methods as you see fit
	 * For a full explanation of the variables please reference the instruction manual provided
	 * 
	 * @param vision
	 * @param keyCount
	 * @param lastAction
	 * @return Action
	 */
	public Action nextMove(final PlayerVision vision, final int keyCount, final boolean lastAction)
	{
		Action action = null;
		try
		{
			action = amityNextMove(vision, keyCount);		// Request our next move from the program
		} catch (Exception e)
		{
			// In case something goes horribly wrong, this is better than getting disqualified.
			e.printStackTrace();
			return Action.South;
		}

		switch (action)			// Apply the action we are about to take to the map
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
	 * Figures out the next move to take
	 * @param vision The current {@link PlayerVision}
	 * @param keyCount The number of keys we have
	 * @return An {@link Action} to take
	 */
	public Action amityNextMove(PlayerVision vision, int keyCount)
	{
		int oldMapSize = map.getMap().size();	// The amount of data we knew before applying our new vision
		map.fillVision(vision);					// Fill in what we know

		// Recalculate the best path if:
		if (oldMapSize < map.getMap().size()		// The map changed, or
				|| currentStack == null				// The last iteration requested a recalculation, or
				|| currentStack.size() < 2)			// We have no moves left!
		{
			try
			{
				currentStack = new Dijkstras(keyCount, map).getNext();
			} catch (GetKeyException e)
			{
				// Force a recalculation next time. We do need to do this as parts of the algorithm assume a recalculation between key pickups.
				currentStack = null;
				return Action.Pickup;		// Pickup the key
			}
		}

		// Pickup key if we are on top of it
		if (map.getMap().get(map.getLocation()).getType() == BoxType.Key)
		{
			currentStack = null;		// Force a recalculation next time
			return Action.Pickup;
		}

		moves++;
		if (currentStack.get(currentStack.size() - 2).getSpace().getType() == BoxType.Exit)		// About to hit the exit. Save our best case so far.
		{
			LEARNING_TRACKER.setBestCase(moves);
		}

		Action act = toAction(currentStack);		// Takes the next 2 positions and finds out what action is appropriate to take next
		if (act != Action.Use)
			currentStack.pop(); 		// Pop off our last movement
		else
			currentStack = null;		// Force a recalculation next time. We just opened a door.

		return act;
	}

	/**
	 * Finds the direction between the first two moves on the stack (ie from point a to b)
	 * and then the appropriate action to take based on this
	 * @param toExit The move list
	 * @throws RuntimeException If the stack is bad (ie The two moves are not consecutive)
	 * @return The action appropriate to take (either moving in a direction or opening a door)
	 */
	private Action toAction(Stack<SpaceWrapper> toExit)
	{
		if (toExit.get(toExit.size() - 2).getSpace().getType() == BoxType.Door)		// If next space is a door, open it
			return Action.Use;

		Point a = toExit.get(toExit.size() - 1).getSpace().getPoint();
		Point b = toExit.get(toExit.size() - 2).getSpace().getPoint();

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