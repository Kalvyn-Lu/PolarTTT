package Players;

import Logic.*;

public class MinimaxPlayer extends Player {
	

	public MinimaxPlayer(int fitness_mode) {
		super(fitness_mode);
	}
	
	@Override
	public Location getChoice(Location[] options) {
		Main.sout("Options", options.length);
		return makeMove(game.theoreticalMove(options[0], '.'));
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
	public int current_menu = 0;
	
	public int minimax(char[][]state, int depth, boolean is_maxer) {
		Location[] childs;
		if (depth == 0 || (childs = findAvailableMoves(state)).length == 0) {
			return game.getFitness(state, fitness_mode, is_maxer ? PolarTTT.PLAYER1 : PolarTTT.PLAYER2);
		}
		int bestVal = 0;
		if (is_maxer) {
			bestVal = Integer.MIN_VALUE;
			for (Location child : childs) {
				// do it
				domove(state, child, is_maxer);
				
				int val = minimax(state, depth - 1, !is_maxer);
				
				// undo it
				undomove(state, child);
				
				if (bestVal < val) {
					bestVal = val;
				}
			}
		}
		else {
			bestVal = Integer.MAX_VALUE;
			for (Location child : childs) {
				//	do it
				domove(state, child, is_maxer);
				
				int val = minimax(state, depth - 1, !is_maxer);
				
				//undo it
				undomove(state, child);
				
				if (bestVal > val) {
					bestVal = val;
				}
			}
		}
		
		return bestVal;
	}
		
	public Location makeMove(char[][]state) {
		Location[] childs = findAvailableMoves(state);
		if (childs.length == 0) {
			return null;
		}
		int bestChild = 0;
		
		//	do it
		domove(state, childs[0], true);
		
		int bestVal = minimax(state, num_plies, true);
		
		// undo it
		undomove(state, childs[0]);
		
		for (int i = 1; i < childs.length; i++) {
			
			//	do it
			domove(state, childs[i], true);
			
			int childval = minimax(state, num_plies, true);
			
			// undo it
			undomove(state, childs[i]);
			
			if (childval > bestVal) {
				Main.sout("New max", bestVal);
				bestChild = i;
				bestVal = childval;
			}
		}
		
		return childs[bestChild];
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

}
