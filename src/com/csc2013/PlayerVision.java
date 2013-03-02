package com.csc2013;

import java.awt.Point;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.MoveType;

public class PlayerVision {
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
    
    
    public PlayerVision(Map map, Point playerLoc) {
        mNorth = 0;
        mSouth = 0;
        mEast = 0;
        mWest = 0;
        
        CurrentPoint = map.getMapBox(playerLoc);

        /* West */
        MapBox curPoint = map.getMapBox(playerLoc);
        while(curPoint.West == BoxType.Open && mWest < distanceToView) {
            mWest++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()-mWest,(int)playerLoc.getY()));
        }

        West = new MapBox[mWest];
        //System.out.println(mWest);
        curPoint = map.getMapBox(playerLoc);
        for(int i = 0; i < mWest; i++) {
            West[i] = curPoint;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()-mWest,(int)playerLoc.getY()));
        }
        
        /* East */
        curPoint = map.getMapBox(playerLoc);
        while(curPoint.East == BoxType.Open  && mEast < distanceToView) {
            mEast++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()+mEast,(int)playerLoc.getY()));
        }

        East = new MapBox[mEast];
        curPoint = map.getMapBox(playerLoc);
        for(int i = 0; i < mEast; i++) {
            East[i] = curPoint;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()+mEast,(int)playerLoc.getY()));
        }
        
        
        
        
        /* North */
        curPoint = map.getMapBox(playerLoc);
        while(curPoint.North == BoxType.Open && mNorth < distanceToView) {
            mNorth++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()-mNorth));
        }

        North = new MapBox[mNorth];
        curPoint = map.getMapBox(playerLoc);
        for(int i = 0; i < mNorth; i++) {
            North[i] = curPoint;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()-mNorth));
        }
        
        
        
        
        /* South */
        curPoint = map.getMapBox(playerLoc);
        while(curPoint.South == BoxType.Open && mSouth < distanceToView) {
            mSouth++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()+mSouth));
        }
        
        South = new MapBox[mSouth];
        curPoint = map.getMapBox(playerLoc);
        for(int i = 0; i < mSouth; i++) {
            South[i] = curPoint;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()+mSouth));
        }
    }
}
