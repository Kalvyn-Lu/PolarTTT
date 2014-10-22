package Source;
import java.util.Random;

/**
 * Makes a random available move.
 */
public class RandomPlayer extends Player {
	public RandomPlayer() {
	}
	
	@Override
	public Location getChoice() {
		
		//	See the moves we have available
		Location[] locs = game.allAvailableLocations();
		
		//	Check off-by-one; force the move if only one is allowed
		if (locs.length == 1) {
			return locs[0];
		}

		//	Generate our random seed
		Random r = new Random();
		
		//	Decide the random place to go
		return locs[r.nextInt(locs.length)];
	}
	
	@Override
	public String getName() {
		return "Random Player";
	}
	
}