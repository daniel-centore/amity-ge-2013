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
	private int currentMap = -1;
	private List<HashMap<Point, Space>> maps = new ArrayList<>();
	
	
	public LearningTracker()
	{
		
	}
	
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
		
		System.out.println("Map: "+currentMap);
		
		if (maps.size() >= currentMap)
			maps.add(new HashMap<Point, Space>());
		
		return maps.get(currentMap);
	}
}
