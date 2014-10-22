package Logic;

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
	public Location[] adjacentLocations(){
		//	Make a new arraylist
		Location[] locations = new Location[((r == 0 || r == 3) ? 5 : 8)];
		int count = 0;
		
		//	Add locations that are in the grid
		for (int i = r - 1; i < r + 2; i++) {
			if (-1 < i && i < 4){
				for (int j = -1; j < 2; j++){
					
					//	Don't add the same as this one
					if (i != r || j != 0) {
						System.out.println(i + ", " + (j + t + 12) % 12);
						locations[count++] = new Location(i, (j + t + 12) % 12);
					}
				}
			}
		}
		
		//	This shouldn't happen
		if (count != locations.length){
			throw new RuntimeException("Neighbors not found!");
		}
		
		//	Return the new list
		return locations;
	}
}
