package Logic;

import java.util.ArrayList;

/**
 * @author Anthony
 * This class handles grid locations.
 */
public class Location {
	public int r, t;
	/**
	 * Constructs a new location given coordinates
	 * @param radius The ring of the point
	 * @param theta The spoke of the point
	 */
	public Location(int radius, int theta) {
		this.r = radius;
		this.t = theta;
	}
	/**
	 * Gets the adjacent existing locations to the current one
	 * @return The adjacent locations
	 */
	public ArrayList<Location> adjacentLocations(){
		//	Make a new arraylist
		ArrayList<Location> locations = new ArrayList<Location>((r == 0 || r == 4) ? 6 : 8);
		
		//	Add locations that are in the grid
		for (int i = r - 1; i < r + 2; i++) {
			if (-1 < i && i < 5) {
				for (int j = -1; j < 2; j++){
					locations.add(new Location(i, (j + t + 12) % 12));
				}
			}
		}
		
		//	Return that
		return locations;
	}
}
