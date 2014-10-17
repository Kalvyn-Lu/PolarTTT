package Players;

import java.util.ArrayList;
import java.util.Random;

import Logic.*;

public class RandomPlayer extends Player {
	public RandomPlayer() {
	}
	
	@Override
	public Location getChoice() {
		Random r = new Random();
		ArrayList<Location> locs = game.allAvailableLocations();
		if (locs.size() == 1) {
			return locs.get(0);
		}
		return locs.get(r.nextInt(locs.size() - 1));
	}
	
	@Override
	public String getName() {
		return "Random Player";
	}
	
}