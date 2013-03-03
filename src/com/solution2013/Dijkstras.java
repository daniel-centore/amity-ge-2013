package com.solution2013;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Direction;
import com.solution2013.field.FieldMap;
import com.solution2013.field.Space;

public class Dijkstras
{
	private int keys;
	private FieldMap map;

	public Dijkstras(int keys, FieldMap map)
	{
		this.keys = keys;
		this.map = map;

		// shortest to exit
		// shortest to unexplored
		// shortest to door
	}

	public DijResult shortestToType(Point start, BoxType type)
	{
		HashMap<Space, SpaceWrapper> vertices = wrap(map.getUnblockedSpaces(), start);

		while (true)
		{
			// Is the end still in the graph?
			for (SpaceWrapper sw : vertices.values())
			{
				Space space = sw.getSpace();

				if ((space != null && space.getType() == type) || (space == null && type == null)) // this is what we were looking for
				{
					if (sw.isRemoved())
					{
						SpaceWrapper path = sw;
						do
						{
							System.out.println(path.space);
							path = path.previous;

						} while (path != null);

						return null;
					}
				}
			}

			// Choose the vertex with the least distance
			SpaceWrapper min = min(vertices.values());
			if (min == null) // no path to an exit
				return null;

			// Remove it from the graph

			min.setRemoved(true);

			if (min.getSpace() != null)
			{
				// Calculate distances between it and neighbors still in the graph
				for (Space sp : min.getSpace().getSurrounding())
				{
					if (sp == null && type != null) // ignore null spaces. not looking for those here.
						continue;

					SpaceWrapper wrap = vertices.get(sp);

					if (wrap.isRemoved()) // don't care about these
						continue;

					int length = min.length + (sp == null ? 1 : sp.difficulty());		// default difficulty for unknown space is 1

					if (length < wrap.getLength())
					{
						wrap.setLength(length);
						wrap.setPrevious(min);
					}
				}
			}
		}
	}

	private SpaceWrapper min(Collection<SpaceWrapper> collection)
	{
		SpaceWrapper shortest = null;
		Iterator<SpaceWrapper> itr = collection.iterator();

		while (itr.hasNext())
		{
			SpaceWrapper next = itr.next();

			if (!next.isRemoved())
			{
				if (shortest == null || shortest.length > next.length)
					shortest = next;
			}
		}

		return shortest;
	}

	public List<DijResult> toKeyThenDoor(Point start)
	{
		return null;
	}

	public HashMap<Space, SpaceWrapper> wrap(List<Space> spaces, Point start)
	{
		HashMap<Space, SpaceWrapper> result = new HashMap<>();

		// Add the null value (indicates unknown region)
		result.put(null, new SpaceWrapper(Integer.MAX_VALUE, null));

		// Add all the other values
		for (Space sp : spaces)
		{
			int dist = Integer.MAX_VALUE;
			if (sp.getPoint().equals(start))
				dist = 0;

			result.put(sp, new SpaceWrapper(dist, sp));
		}

		return result;
	}

	class SpaceWrapper // for dijkstras
	{
		private SpaceWrapper previous; // prev element in chain
		private int length; // total dist
		private Space space; // space attached to this
		private boolean removed; // removed once we visited

		public SpaceWrapper(int length, Space space)
		{
			this.length = length;
			this.space = space;
			this.removed = false;
		}

		public int getLength()
		{
			return length;
		}

		public void setLength(int length)
		{
			this.length = length;
		}

		public Space getSpace()
		{
			return space;
		}

		public boolean isRemoved()
		{
			return removed;
		}

		public void setRemoved(boolean removed)
		{
			this.removed = removed;
		}

		public SpaceWrapper getPrevious()
		{
			return previous;
		}

		public void setPrevious(SpaceWrapper previous)
		{
			this.previous = previous;
		}
	}

	class DijResult // result of an algorithm
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