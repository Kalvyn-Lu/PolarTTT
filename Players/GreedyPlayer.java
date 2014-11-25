package Players;

import Logic.Location;
import Logic.PolarTTT;

public class GreedyPlayer extends Player {

	@Override
	public Location getChoice(Location[] options) {
		if (options.length == 48) {
			return options[(int)(Math.random() * 48)];
		}
		
		Location[] bestPlays = new Location[options.length];
		int num_best = 0, best_val = Integer.MIN_VALUE;
		
		for (int i = 0; i < options.length; i++) {
			char[][] theory = game.theoreticalMove(options[i], is_maximizer? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
			int theory_fitness = game.dylanFitness(theory);
			if (!is_maximizer) {
				theory_fitness *= -1;
			}
			
			if (theory_fitness < best_val) {
				continue;
			}
			if (best_val < theory_fitness) {
				best_val = theory_fitness;
				num_best = 0;
			}
			bestPlays[num_best++] = options[i];
		}
		
		return bestPlays[(int)(Math.random() * num_best)];
	}

	@Override
	public String getName() {
		return "Greedy Player";
	}

}
