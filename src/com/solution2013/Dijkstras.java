package com.solution2013;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.csc2013.DungeonMaze.Direction;
import com.solution2013.field.FieldMap;

public class Dijkstras
{
	private int keys;
	private FieldMap map;
	
	
	public Dijkstras(int keys, FieldMap map)
	{
		this.keys = keys;
		this.map = map;
	}

	public DijResult shortestToUnexplored(Point start)
	{
		return null;
	}
	
	public DijResult shortestToDoor(Point start)
	{
		return null;
	}
	
	public List<DijResult> toKeyThenDoor(Point start)
	{
		return null;
	}
	
	class DijResult		// result of an algorithm
	{
		private Direction dir;
		private int distance;
		
		public DijResult(Direction dir, int distance)
		{
			this.dir = dir;
			this.distance = distance;
		}
		
		public Direction getDir()
		{
			return dir;
		}
		
		public int getDistance()
		{
			return distance;
		}
	}
	
	

}
