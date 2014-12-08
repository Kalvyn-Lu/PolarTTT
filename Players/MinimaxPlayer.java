package Players;

import Logic.*;

public class MinimaxPlayer extends Player {
	

	public MinimaxPlayer(int fitness_mode) {
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
		
		//	Prepare for the worst
		for (int i = 0; i < options.length; i++) {
			char[][] theory = game.theoreticalMove(options[i], is_maximizer? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
			int theory_fitness = 0;
			if (use_pruning) {
				theory_fitness = minimax_with_pruning(theory, num_plies, !is_maximizer, Integer.MIN_VALUE, Integer.MAX_VALUE);
			}
			else {
				theory_fitness = minimax(theory, num_plies, !is_maximizer);
			}
			
			if (!is_maximizer) {
				theory_fitness *= -1;
			}
			
			//	Winning move- make it!
			if (theory_fitness >= PolarTTT.WIN_WEIGHT) {
				bestPlays[0] = options[i];
				num_best = 0;
				break;
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

	public int minimax(char[][] board, int ply, boolean is_maximizer){
		
		//	Base case: out of plies!
		if (ply == 0) {
			return game.getFitness(board, fitness_mode, is_maximizer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
		}
		
		//	Find available moves
		Location[] options = findAvailableMoves(board);
		
		//	Base case: Out of moves!
		if (options.length == 0) {
			return 0;
		}
		
		if (is_maximizer) {
			int best = Integer.MIN_VALUE;
			for (Location choice : options) {
				//	Preview a move
				domove(board, choice, is_maximizer);
				
				int fitness = minimax(board, ply - 1, !is_maximizer);
				
				//	Undo the preview
				undomove(board, choice);
				
				if (best < fitness) {
					best = fitness;
				}
			}
			return best;
		}
		else {
			int best = Integer.MAX_VALUE;
			for (Location choice : options) {
				//	Preview a move
				domove(board, choice, is_maximizer);
				
				int fitness = minimax(board, ply - 1, !is_maximizer);
				
				//	Undo the preview
				undomove(board, choice);
				
				if (best > fitness) {
					best = fitness;
				}
			}
			return best;
		}
	}
	
	private int minimax_with_pruning(char[][] board, int ply, boolean is_maximizer, int alpha, int beta){
		
		//	Base case: out of plies!
		if (ply == 0) {
			return game.getFitness(board, fitness_mode, is_maximizer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
		}
		
		//	Find available moves
		Location[] options = findAvailableMoves(board);
		
		//	Base case: Out of moves!
		if (options.length == 0) {
			return 0;
		}
		
		//	I have no idea why but this negation which should not be there makes it work
		if (is_maximizer) {
			int best = Integer.MIN_VALUE;
			for (Location choice : options) {
				//	Preview a move
				domove(board, choice, is_maximizer);
				
				int fitness = minimax(board, ply - 1, !is_maximizer);
				
				
				//	Undo the preview
				undomove(board, choice);
				
				alpha = Math.max(fitness, alpha);
				if (beta <= alpha) {
					return beta;
				}
				if (best < fitness) {
					best = fitness;
				}
				
			}
			return best;
		}
		else {
			int best = Integer.MAX_VALUE;
			for (Location choice : options) {
				//	Preview a move
				domove(board, choice, is_maximizer);
				
				int fitness = minimax(board, ply - 1, !is_maximizer);
				
				//	Undo the preview
				undomove(board, choice);
				
				beta = Math.min(fitness, beta);
				
				if (beta <= alpha) {
					return alpha;
				}
				
				if (best > fitness) {
					best = fitness;
				}
			}
			return best;
		}
	}
	
	public void domove(char[][] state, Location l, boolean is_maxer) {
		state[l.r][l.t] = is_maxer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2;
	}
	public void undomove(char[][] state, Location l) {
		state[l.r][l.t] = PolarTTT.EMPTY;
	}
	
	public Location[] findAvailableMoves(char[][] board){
		Location[] available_locations_l;
		boolean[][] available_locations = new boolean[4][12];
		
		//	Track how many are available
		int count = 0;
		
		//	Check every spot
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 12; j++){

				//	Anywhere is legal on first turn
				if (turn == 0) {
					available_locations[i][j] = true;
				}
				
				//	If the spot is taken then it's not available
				else if (board[i][j] != '.'){
					available_locations[i][j] = false;
				}
				
				
				//	Spot is available if it has an adjacent taken
				else {
					available_locations[i][j] = hasAdjacent(board, i, j);
				}
				
				//	Keep count
				if (available_locations[i][j]) {
					count++;
				}
			}
		}
		
	
		available_locations_l = new Location[count];
		if (0 < count) {
			for (int r = 0; r < 4; r++){
				for (int t = 0; t < 12; t++){
					if (available_locations[r][t]){
						available_locations_l[--count] = new Location(r, t);
					}
				}
			}
		}
		return available_locations_l;
	}
	
	private boolean hasAdjacent(char[][] board, int r, int t){
		
		//	Get all neighbors
		Location[] neighbors = new Location(r, t).adjacentLocations();
		 
		//	Check all neighbors
		for (Location location : neighbors){
			if (board[location.r][location.t] != PolarTTT.EMPTY){
				return true;
			}
		}
		
		//	None adjacent
		return false;
	}
	

	@Override
	public String getName() {
		String name = "Minimax ";
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
		return name + num_plies + "p" + (use_pruning ? "+AB" : "");
	}
	int turn = -1;
	public int num_plies = 1;
	public boolean use_pruning, setup = false;
	public String[] menues = {"Ply count", "Use Alpha-Beta pruning?"};
}

