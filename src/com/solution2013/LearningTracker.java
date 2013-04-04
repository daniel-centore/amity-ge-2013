package com.solution2013;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.csc2013.Tournament;
import com.solution2013.field.FieldMap;
import com.solution2013.field.Space;


/**
 * This class keeps track of maps as we learn them.
 * This way, we can take data we've learned in the past to help us make better decision in the future.
 * 
 * @author Daniel Centore
 *
 */
public class LearningTracker
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
