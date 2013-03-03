package com.solution2013;

import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.Direction;
import com.csc2013.PlayerVision;
import com.solution2013.field.FieldMap;

/**
 * 
 * [To be completed by students]
 * 
 * @author Amity Region 5
 *
 */
public class SchoolPlayer
{
	
	public FieldMap map = new FieldMap();

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
		
		switch (action)		// Apply the action we are about to take to the map
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
	
	public Action amityNextMove(PlayerVision vision, int keyCount)
	{
		return null;
	}
}