package Players;

import Logic.Location;
import Logic.PolarTTT;

public class GreedyPlayer extends Player {
	
	/**
	 * Makes a greedy player
	 * @param fitness_mode The fitness type to run on
	 */
	public GreedyPlayer(int fitness_mode) {
		super(fitness_mode);
	}

	@Override
	public Location getChoice(Location[] options) {
		//	First turn- pick anything
		if (options.length == 48) {
			return options[(int)(Math.random() * 48)];
		}
		
		//	Track the list of best plays, not just one of the bst
		Location[] bestPlays = new Location[options.length];
		int num_best = 0, best_val = Integer.MIN_VALUE;
		
		//	Check all of the options
		for (int i = 0; i < options.length; i++) {
			
			//	Propose a move
			char[][] theory = game.theoreticalMove(options[i], is_maximizer? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
			
			//	How good was that move?
			int theory_fitness = game.getFitness(theory, fitness_mode, is_maximizer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
			
			//	Flip minimizer to maximizer for easy max
			if (!is_maximizer) {
				theory_fitness *= -1;
			}
			
			//	Winning move- make it!
			if (theory_fitness >= PolarTTT.WIN_WEIGHT / 2) {
				bestPlays[0] = options[i];
				num_best = 0;
				break;
			}
			
			//	If the move sucks, don't do anything
			if (theory_fitness < best_val) {
				continue;
			}
			
			//	New best found!
			if (best_val < theory_fitness) {
				best_val = theory_fitness;
				num_best = 0;
			}
			
			//	Add this move to the list of best moves
			bestPlays[num_best++] = options[i];
		}
		
		//	Pick randomly between the best moves
		return bestPlays[(int)(Math.random() * num_best)];
	}

	public String getName() {
		String name = "";
		
		//	Pick the one running this player
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
		
		//	Return the name
		return name + " Greedy";
	}

}
