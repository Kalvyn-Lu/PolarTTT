package Players;

import Logic.*;

public class MinimaxPlayer extends Player {
	

	public MinimaxPlayer(int fitness_mode) {
		super(fitness_mode);
	}
	
	@Override
	public Location getChoice(Location[] options) {
		
		//	Any move is good on the first turn
		//	Actually this might be not a very good assumption
		if (options.length == 48) {
			return options[(int)(Math.random() * 48)];
		}
		
		//	Track the best moves
		Location[] bestPlays = new Location[options.length];
		int num_best = 0, best_val = Integer.MIN_VALUE;
		
		//	Check everymove
		for (int i = 0; i < options.length; i++) {
			
			//	Propose that move
			char[][] theory = game.theoreticalMove(options[i], is_maximizer? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
			
			//	How good was it?
			int theory_fitness = 0;
			if (use_pruning) {
				
				//	Prune- plan for the worst
				theory_fitness = minimax_with_pruning(theory, num_plies, !is_maximizer, Integer.MIN_VALUE, Integer.MAX_VALUE);
			}
			else {
				
				//	Don't prune, just minimax
				theory_fitness = minimax(theory, num_plies, !is_maximizer);
			}
			
			//	Flip the fitness for maximizing
			if (!is_maximizer) {
				theory_fitness *= -1;
			}
			
			//	Winning move- make it!
			if (theory_fitness >= PolarTTT.WIN_WEIGHT / 2) {
				bestPlays[0] = options[i];
				num_best = 0;
				break;
			}
			
			//	The move sucks. Ignore it
			if (theory_fitness < best_val) {
				continue;
			}
			
			//	Found a new best!
			if (best_val < theory_fitness) {
				best_val = theory_fitness;
				num_best = 0;
			}
			
			//	Add this move to the list of bests
			bestPlays[num_best++] = options[i];
		}
		
		System.out.println("\nMinimax found options: ");
		for (Location play : bestPlays){
			try {
				System.out.print(play.toString() + ", ");
			}
			catch (NullPointerException e) {
				//	Don't print
			}
		}
		
		
		if (use_pruning) {

			//	Track the best moves
			bestPlays = new Location[options.length];
			num_best = 0; best_val = Integer.MIN_VALUE;
			
			//	Check everymove
			for (int i = 0; i < options.length; i++) {
				
				//	Propose that move
				char[][] theory = game.theoreticalMove(options[i], is_maximizer? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
				
				//	How good was it?
				int theory_fitness = 0;
				theory_fitness = minimax(theory, num_plies, !is_maximizer);
				
				//	Flip the fitness for maximizing
				if (!is_maximizer) {
					theory_fitness *= -1;
				}
				
				//	Winning move- make it!
				if (theory_fitness >= PolarTTT.WIN_WEIGHT / 2) {
					bestPlays[0] = options[i];
					num_best = 0;
					break;
				}
				
				//	The move sucks. Ignore it
				if (theory_fitness < best_val) {
					continue;
				}
				
				//	Found a new best!
				if (best_val < theory_fitness) {
					best_val = theory_fitness;
					num_best = 0;
				}
				
				//	Add this move to the list of bests
				bestPlays[num_best++] = options[i];
			}
			System.out.println("\nMinimax without pruning found options: ");
			for (Location play : bestPlays){
				try {
					System.out.print(play.toString() + ", ");
				}
				catch (NullPointerException e) {
					//	Don't print
				}
			}
		}
		System.out.println();
		//	Pick between the best
		return bestPlays[(int)(Math.random() * num_best)];
	}

	/**
	 * Finds the best outcome from a certain depth
	 * @param board The board to analyze
	 * @param ply The number of plies left before the search stops
	 * @param is_maximizer Whether the player is maximizing
	 * @return The best value
	 */
	private int minimax(char[][] board, int ply, boolean is_maximizer){
		
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
				
				//	Track a new best
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
				
				// Track a new best
				if (best > fitness) {
					best = fitness;
				}
			}
			return best;
		}
	}

	/**
	 * Finds the best outcome from a certain depth
	 * @param board The board to analyze
	 * @param ply The number of plies left before the search stops
	 * @param is_maximizer Whether the player is maximizing
	 * @param alpha The best ancestor value from an odd ply
	 * @param beta The worst ancestor value from an even ply
	 * @return The best value
	 */
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
	
	//	Performs a move on the theoretical board
	private void domove(char[][] state, Location l, boolean is_maxer) {
		state[l.r][l.t] = is_maxer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2;
	}
	
	//	Undoes a move on the theoretical board
	private void undomove(char[][] state, Location l) {
		state[l.r][l.t] = PolarTTT.EMPTY;
	}
	
	/**
	 * Find all available moves on a theoretical board
	 * @param board
	 * @return The list of available moves
	 */
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
		
		//	Copy the locations from coordinates to Location objects
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
	
	/**
	 * Determines of a location has an adjacent move on a theoretical board
	 * @param board
	 * @param r
	 * @param t
	 * @return
	 */
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
		case PolarTTT.ALTERNATE_FITNESS:
			name = "Alt's ";
			break;
		case PolarTTT.CLASSIFIER_FITNESS:
			name = "Classifer ";
			break;
		case PolarTTT.ANN_FITNESS:
			name = "RoxANNe ";
			break;
		}
		
		//	Provide stats this player
		return name + num_plies + "p" + (use_pruning ? "+AB" : "");
	}
	int turn = -1;
	public int num_plies = 1;
	public boolean use_pruning, setup = false;
}

