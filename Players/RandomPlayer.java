package Players;

import java.util.Random;

import Logic.*;

/**
 * Makes a random available move.
 */
public class RandomPlayer extends Player {
	public RandomPlayer() {
	}
	
	@Override
	public Location getChoice(Location[] options) {
				
		//	Check off-by-one; force the move if only one is allowed
		if (options.length == 1) {
			return options[0];
		}

		//	Generate our random seed
		Random r = new Random();
		
		//	Decide the random place to go
		return options[r.nextInt(options.length)];
	}
	
	@Override
	public String getName() {
		return "Random Player";
	}
	
}