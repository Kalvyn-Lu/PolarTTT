package Players;

import java.util.ArrayList;
import java.util.Random;

import Logic.*;

/**
 * Makes a random available move.
 */
public class RandomPlayer extends Player {
	public RandomPlayer() {
	}
	
	@Override
	public Location getChoice() {
		
		//	See the moves we have available
		ArrayList<Location> locs = game.allAvailableLocations();
		
		//	Check off-by-one; force the move if only one is allowed
		if (locs.size() == 1) {
			return locs.get(0);
		}

		//	Generate our random seed
		Random r = new Random();
		
		//	Decide the random place to go
		return locs.get(r.nextInt(locs.size() - 1));
	}
	
	@Override
	public String getName() {
		return "Random Player";
	}
	
}