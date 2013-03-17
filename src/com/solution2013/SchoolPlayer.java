package com.solution2013;

import java.awt.Point;
import java.util.Stack;

import javax.management.RuntimeErrorException;

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
	
	public FieldMap map = new FieldMap(LEARNING_TRACKER);

	public SchoolPlayer() throws SlickException
	{
		// complete
	}

	/** 
	 * To properly implement this class you simply must return an Action in the function nextMove below.
	 * 
	 * You are allowed to define any helper variables or methods as you see fit
	 * 
	 * For a full explanation of the variables please reference the instruction manual provided
	 * 
	 * @param vision
	 * @param keyCount
	 * @param lastAction
	 * @return Action
	 */
	public Action nextMove(final PlayerVision vision, final int keyCount, final boolean lastAction)
	{
		Action action = amityNextMove(vision, keyCount);

		switch (action)
		// Apply the action we are about to take to the map
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

	private Stack<SpaceWrapper> currentStack = null;

	public Action amityNextMove(PlayerVision vision, int keyCount)
	{
		int oldMapSize = map.getMap().size();
		map.fillVision(vision);

		if (oldMapSize < map.getMap().size() || currentStack == null || currentStack.size() < 2) // If the map changed, recalculate the best path
		{
			try
			{
				currentStack = new Dijkstras(keyCount, map).getNext();
			} catch (GetKeyException e)
			{
				currentStack = null; // force a recalculation next time
				return Action.Pickup;
			}
		}
		
		// Pickup key if we are on top of it and we are not on the way to an exit already
		if (map.getMap().get(map.getLocation()).getType() == BoxType.Key && currentStack.lastElement().getSpace().getType() != BoxType.Exit)
		{
			currentStack = null;
			return Action.Pickup;
		}
		
//		System.out.println(currentStack);
		
		Action act = toAction(currentStack);
		if (act != Action.Use)
			currentStack.pop(); // pop off our last movement
		else
			currentStack = null; // force a recalculation next time. We just opened a door.
		
		return act;
	}

	/**
	 * Finds the direction between the first two moves on the stack (ie from point a to b)
	 * @param toExit
	 * @return
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