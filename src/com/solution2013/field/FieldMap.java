package com.solution2013.field;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FieldMap
{
	private HashMap<Point, Space> map = new HashMap<>();

	// Player's curr loc rel to 0,0 initial pos
	private Point location = new Point(0, 0);

}
