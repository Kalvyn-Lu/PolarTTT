package Players;

import Logic.Location;
import Logic.PolarTTT;

public class GreedyPlayer extends Player {
	
	public GreedyPlayer(int fitness_mode) {
		super(fitness_mode);
	}

	@Override
	public Location getChoice(Location[] options) {
		if (options.length == 48) {
			return options[(int)(Math.random() * 48)];
		}
		else if (options.length == 0) {
			throw new RuntimeException("No options provided!");
		}
		
		Location[] bestPlays = new Location[options.length];
		int num_best = 0, best_val = Integer.MIN_VALUE;
		
		for (int i = 0; i < options.length; i++) {
			char[][] theory = game.theoreticalMove(options[i], is_maximizer? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
			int theory_fitness = game.getFitness(theory, fitness_mode, is_maximizer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
			
			//	Winning move- make it!
			if (theory_fitness == PolarTTT.WIN_WEIGHT) {
				bestPlays[0] = options[i];
				num_best = 0;
				break;
			}
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
		String name = "";
		switch (fitness_mode) {
		case PolarTTT.DYLAN_FITNESS:
			name = "Dylan's ";
			break;
		case PolarTTT.ALEX_FITNESS:
			name = "Alex's ";
			break;
		case PolarTTT.CLASSIFIER_FITNESS:
			name = "Classifer ";
			break;
		case PolarTTT.ANN_FITNESS:
			name = "RoxANNe ";
			break;
		}
		return name + " Greedy";
	}

}
