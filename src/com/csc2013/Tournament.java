package com.csc2013;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.PlayerType;

/**
 * 
 * This class starts a tournament consisting of Dungeon Maze games
 * 
 * @author GE
 *
 */
public class Tournament {
	/*
	 * This variable controls how many steps a player can take before starting the next game
	 */
	public final static int maxSteps = 20000;//200;
	/*
	 * Possible maps to use are:
	 * map01.tmx
	 * map02.tmx
	 * map03.tmx
	 */
	public final static String maps[] = {/*"map01.tmx", */"map02.tmx", "map03.tmx"};
	/*
	 * This is how many spaces ahead players can see.  Acceptable values 
	 * are 1 through 15.	
	 */
	public final static int vision[] = {15};
	/*
	 * Possible players are:
	 * PlayerType.AI
	 * PlayerType.School
	 * PlayerType.Human
	 */
	public final static PlayerType players[] = {PlayerType.Human};
	
	public static void main(String[] argv) {
		DungeonMaze dm = new DungeonMaze();
		AppGameContainer container;
		try {
			container = new AppGameContainer(dm, 640, 480, false);
			container.start();
		} catch (SlickException e) {
			System.out.println("Internal code with running Dungeon maze.  Please send the following error code to CSC.CaseStudy@ge.com.");
			e.printStackTrace();
		}
	}
}