package com.csc2013;

import java.awt.Point;

import com.csc2013.DungeonMaze.MoveType;

public class PlayerVision
{
	public MapBox CurrentPoint;

	public MapBox[] North;
	public MapBox[] South;
	public MapBox[] East;
	public MapBox[] West;

	public int mNorth;
	public int mSouth;
	public int mEast;
	public int mWest;
	public static int distanceToView = 1;

	public PlayerVision(Map map, Point playerLoc)
	{
		mNorth = 0;
		mSouth = 0;
		mEast = 0;
		mWest = 0;

		CurrentPoint = map.getMapBox(playerLoc);

		/* West */
		MapBox curPoint = map.getMapBox(playerLoc);
		while (curPoint.WestMove == MoveType.Open && mWest < distanceToView)
		{
			mWest++;
			curPoint = map.getMapBox(new Point(playerLoc.x - mWest, playerLoc.y));
		}

		West = new MapBox[mWest];
		for (int i = 0; i < mWest; i++)
		{
			curPoint = map.getMapBox(new Point(playerLoc.x - i - 1, playerLoc.y));
			West[i] = curPoint;
		}

		/* East */
		curPoint = map.getMapBox(playerLoc);
		while (curPoint.EastMove == MoveType.Open && mEast < distanceToView)
		{
			mEast++;
			curPoint = map.getMapBox(new Point(playerLoc.x + mEast, playerLoc.y));
		}

		East = new MapBox[mEast];
		for (int i = 0; i < mEast; i++)
		{
			curPoint = map.getMapBox(new Point(playerLoc.x + i + 1, playerLoc.y));
			East[i] = curPoint;
		}

		/* North */
		curPoint = map.getMapBox(playerLoc);
		while (curPoint.NorthMove == MoveType.Open && mNorth < distanceToView)
		{
			mNorth++;
			curPoint = map.getMapBox(new Point(playerLoc.x, playerLoc.y - mNorth));
		}

		North = new MapBox[mNorth];
		for (int i = 0; i < mNorth; i++)
		{
			curPoint = map.getMapBox(new Point(playerLoc.x, playerLoc.y - i - 1));
			North[i] = curPoint;
		}

		/* South */
		curPoint = map.getMapBox(playerLoc);
		while (curPoint.SouthMove == MoveType.Open && mSouth < distanceToView)
		{
			mSouth++;
			curPoint = map.getMapBox(new Point(playerLoc.x, playerLoc.y + mSouth));
		}

		South = new MapBox[mSouth];
		for (int i = 0; i < mSouth; i++)
		{
			curPoint = map.getMapBox(new Point(playerLoc.x, playerLoc.y + i + 1));
			South[i] = curPoint;
		}
	}
}
