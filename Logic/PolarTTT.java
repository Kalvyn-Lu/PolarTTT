package Logic;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

import TDNeuralNet.*;

import Players.*;
import RBFClassifier.RBFClassifier;

/**
 * @author Anthony
 * The PolarTTT class 
 */
public class PolarTTT extends KeyAdapter{
		
	/**
	 * Starts the game from scratch
	 */
	public void begin() {
		synchronized (frame) {
			
			//	Game loop!
			while (true) {
				
				//	Reset everything
				turn = 0;
				for(int i = 0; i < 4; i++) {
					for (int j = 0; j < 12; j++){
						board[i][j] = EMPTY;
					}
				}
				for (int i = 0; i < 48; i++) {
					history[i] = null;
					fitnesses[i] = 0;
				}
				
				if (isVisible) {
					players[0] = players[1] = null;
					//	Switch to menu mode
					canvas.gameoff();
					frame.setVisible(true);
				}
				
				else {
					//	swap players to share the first-move bias
					Player temp = players[0];
					players[0] = players[1];
					players[1] = temp;
					
					if (num_games % 25 == 0){
						canvas.setInvisible(players[0], players[1]);
						canvas.repaint();
					}
				}
				
				//	Wait until the second player is set
				//	(the first player is always set before the second)
				synchronized (this) {
					while (players[1] == null) {
						try {
							this.wait();
						}
						catch (InterruptedException e) {
						}
					}
				}
				if (isVisible) {
					canvas.gameon();
				}
				else if (num_games == 0){
					canvas.setInvisible(players[0], players[1]);
				}
				
				//	Make the new players
				players[0].newGame(this, true);
				players[1].newGame(this, false);
				
				
				//	Ask players to make moves
				invokePlayerMove();
				
				num_games++;

				players[0].endGame(board, history, fitnesses);
				players[1].endGame(board, history, fitnesses);
				
				
				if (isVisible) {
					//	Keep restarting the game until exit
					try {
						frame.wait();
					} catch (InterruptedException e) {
						canvas.gameoff();
					}
				}
			}
		}

	}
	/**
	 * Requests the players' movements in sequence.
	 */
	private void invokePlayerMove() {
		//	Instantiate outside of the while scope
		Location choice;
		while (turn < 48) {
			
			//	Reset available moves
			assignAvailableMoves();
			
			//	This shouldn't happen.
			if (available_locations_l == null){
				throw new RuntimeException("Out of moves prematurely!");
			}
			
			//	Start a player's new round
			Player p = players[turn & 1];
			p.newRound();
			
		
			//	Get the player's move
			choice = p.getChoice(available_locations_l);
			
			
			if (choice == null || !choose(choice)){
				gameon = false;
				canvas.setStatus(GameCanvas.STATUS_WON, turn, p.getName() + " ( " + getPlayerSymbol(p) + " ) made an illegal move and lost the game!\n");
				Main.sout("Illegal Choice", choice);
				players[(turn + 1) & 1].incScore();
				save_data(getPlayerSymbol(players[(turn + 1) & 1]));
				return;
			}
			
//			if (turn % 5 == 3) {
				save_board(board);
	//		}
			
			//if (checkWin(choice)) {
			if (win(board, getPlayerSymbol(p), choice.r, choice.t)){
				gameon = false;
				canvas.setStatus(GameCanvas.STATUS_WON, turn, p.getName() + " ( " + getPlayerSymbol(p) + " ) got 4 in a row and won the game!\n");
				fitnesses[turn - 1] = WIN_WEIGHT;
				players[(turn + 1) & 1].incScore();
//				save_board(board);
				save_data(getPlayerSymbol(players[(turn + 1) & 1]));
				return;
			}
		}
		
		//	Cat's game!
		gameon = false;
		canvas.setStatus(GameCanvas.STATUS_TIE, turn, players[1].getName() + " ( " + PLAYER2 + " ) made the last move and tied the game!\n");
		save_data(EMPTY);
		num_ties++;
	}
	
	/**
	 * Makes a move
	 * @param location The location to move
	 * @return Whether the move was allowed
	 */
	private boolean choose (Location location) {
		
		//	If we're allowed to play here, play here
		if (moveIsAvailable(location)) {
			
			//	Store the player's move
			board[location.r][location.t] = (0 == (turn & 1) ? PLAYER1 : PLAYER2);
			history[turn] = location;
			
			//	Evaluate the players' fitnesses;
			char player = (0 == (turn & 1) ? PLAYER1 : PLAYER2 );
			fitness = getFitness(board, DYLAN_FITNESS, player);
			fitnesses[turn] = fitness;
			if (isVisible) {
				Main.sout("Dylan's fitness", fitness);
				Main.sout("Alex's fitness", getFitness(board, ALEX_FITNESS, player));
				Main.sout("Classifier's judgment", getFitness(board, CLASSIFIER_FITNESS, player));
				Main.sout("Neural Network", getFitness(board, ANN_FITNESS, player));
				System.out.println();
			}
			
			//	Rotate turn count and thus give other player a turn
			turn++;
			
			if (isVisible) {
				//	Redraw the board
				canvas.repaint();
			}
			
			//	Pass that the move was successful
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if the player has won the game given the location of a move
	 * @param player The player to check with. This is unnecessary in theory.
	 * @param ring The ring on which the last play was made
	 * @param spoke The spoke on which the last play was made
	 * @return
	 */
	private boolean win(char[][] board, char player, int ring, int spoke) {
		return Is(player, At(ring, spoke)) && (
			(	//	One Spoke win
				Is(player, Neighbor(0, 0, spoke, 0))
				&& Is(player, Neighbor(1, 0, spoke, 0))
				&& Is(player, Neighbor(2, 0, spoke, 0))
				&& Is(player, Neighbor(3, 0, spoke, 0))
			) || (	//	One counter-clockwise win
				Is(player, Neighbor(0, 0, spoke, -ring))
				&& Is(player, Neighbor(1, 0, spoke, -ring + 1))
				&& Is(player, Neighbor(2, 0, spoke, -ring + 2))
				&& Is(player, Neighbor(3, 0, spoke, -ring + 3))
			) || (	//	One clockwise win
				Is(player, Neighbor(0, 0, spoke, ring))
				&& Is(player, Neighbor(1, 0, spoke, ring - 1))
				&& Is(player, Neighbor(2, 0, spoke, ring - 2))
				&& Is(player, Neighbor(3, 0, spoke, ring - 3))
			) || (	//	Ring win *-X-X-X
				Is(player, Neighbor(ring, 0, spoke, 1))
				&& Is(player, Neighbor(ring, 0, spoke, 2))
				&& Is(player, Neighbor(ring, 0, spoke, 3))
			) || (	//	Ring win X-*-X-X
				Is(player, Neighbor(ring, 0, spoke, -1))
				&& Is(player, Neighbor(ring, 0, spoke, 1))
				&& Is(player, Neighbor(ring, 0, spoke, 2))
			) || (	//	Ring win X-X-*-X
				Is(player, Neighbor(ring, 0, spoke, -2))
				&& Is(player, Neighbor(ring, 0, spoke, -1))
				&& Is(player, Neighbor(ring, 0, spoke, 1))
			) || (	//	Ring win X-X-X-*
				Is(player, Neighbor(ring, 0, spoke, -3))
				&& Is(player, Neighbor(ring, 0, spoke, -2))
				&& Is(player, Neighbor(ring, 0, spoke, -1))
			)
		);
	}
	private char At(int ring, int spoke) {
		return peek(ring, spoke);
	}
	private char Neighbor(int ring, int ring_offset, int spoke, int spoke_offset){
		return peek(ring + ring_offset, (spoke + spoke_offset + 12) % 12);
	}
	private boolean Is(char x, char y) {
		return x == y;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()){
		
		//	Move up
		case KeyEvent.VK_UP:
			canvas.moveup();
			break;
			
		//	Move down
		case KeyEvent.VK_DOWN:
			canvas.movedown();
			break;
			
		//	Move left
		case KeyEvent.VK_LEFT:
			canvas.moveleft();
			break;
		
		//	Move right
		case KeyEvent.VK_RIGHT:
			canvas.moveright();
			break;
			
		//	Enter
		case KeyEvent.VK_ENTER:
			
			//	This overwrites players so make sure the mode is right
			if (canvas.getMode() == GameCanvas.MODE_MENU){
				for (int i = 0; i < 2; i++) {
					switch (canvas.menu_indices[i + 1]){
	
						case GameCanvas.HUMAN:
							players[i] = new HumanPlayer(NONE);
							break;
						
						case GameCanvas.RANDOM:
							players[i] = new RandomPlayer(NONE);
							break;
	
						case GameCanvas.ALEX:
							players[i] = new MinimaxPlayer(ALEX_FITNESS);
							break;
							
						case GameCanvas.DYLAN:
							players[i] = new MinimaxPlayer(DYLAN_FITNESS);
							break;
							
						case GameCanvas.CLASSIFIER:
							players[i] = new MinimaxPlayer(CLASSIFIER_FITNESS);
							break;
							
						case GameCanvas.ANN:
							players[i] = new MinimaxPlayer(ANN_FITNESS);
							break;
						
						//	This should only happen during test stage
						default:
							System.out.println(canvas.menu_indices[1]);
							System.exit(0);
					}
				}
				
				//	Minimax players require more setup
				if (players[0] instanceof MinimaxPlayer) {
					
					int plies = canvas.menu_indices[GameCanvas.P1NUMPLIES];
					
					//	Greedy player plays with one ply
					if (plies == 1) {
						players[0] = new GreedyPlayer(players[0].getFitnessMode());
					}
					else {
						((MinimaxPlayer)players[0]).num_plies = plies;
						((MinimaxPlayer)players[0]).use_pruning = canvas.menu_indices[GameCanvas.P1PRUNE] == 0;
					}
				}
				if (players[1] instanceof MinimaxPlayer) {
					int plies = canvas.menu_indices[GameCanvas.P2NUMPLIES];
					
					//	Greedy player plays with one ply
					if (plies == 1) {
						players[1] = new GreedyPlayer(players[1].getFitnessMode());
					}
					else {
						((MinimaxPlayer)players[1]).num_plies = plies;
						((MinimaxPlayer)players[1]).use_pruning = canvas.menu_indices[GameCanvas.P2PRUNE] == 0;
					}
				}
				
				if (canvas.menu_indices[GameCanvas.NUM_GAMES] == 1) {
					if (players[0] instanceof HumanPlayer || players[1] instanceof HumanPlayer) {
						//	TODO probably don't have it kill the program when this happens
						throw new RuntimeException("Humans can't play in bulk!");
					}
					
					isVisible = false;
				}
				
				//	Break the lock on the main thread which was waiting for this input
				
				synchronized(this){
					this.notifyAll();
				}
			}
			
			//	 what the fuck
			else if (!gameon) {

				synchronized(frame){
					frame.notifyAll();
				}
			}
			
			break;
		
		//	Allow keyboard shortcut to close
		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;
		}
	}
	
	/**
	 * From the current state, provides a theoretical view of what the board might look like after a potential move is made
	 * @param loc The location of the theoretical move
	 * @param player The player who is making this move
	 * @return The state of the board if that particular move was made
	 */
	public char[][] theoreticalMove(Location loc, char player) {
		return theoreticalMove(board, loc, player);
	}
	
	/**
	 * From a provided state, gives a theoretical view of what the board might look like after a potential move is made
	 * @param move
	 * @param loc The location of the theoretical move
	 * @param player The player who is making this move
	 * @return The state of the board if that particular move was made. A '!' will be placed in the board of the theory is illegal
	 */
	public char[][] theoreticalMove(char[][] board, Location loc, char player) {
		
		//	Make a new theory
		char[][] theory_with_move = new char[board.length][board[0].length];
		
		int num_plays = 0;
		//	Populated it with the current state
		theory_with_move = new char[board.length][board[0].length];
		for (int r = 0; r < 4; r++) {
			for (int t = 0; t < 12; t++) {
				theory_with_move[r][t] = board[r][t];
				if (board[r][t] != EMPTY ){
					num_plays++;
				}
			}
		}
		
		//	Mark an invalid move
		if (0 < num_plays && (
				theory_with_move[loc.r][loc.t] != EMPTY
				|| !hasAdjacent(theory_with_move, loc.r, loc.t))) {
			theory_with_move[loc.r][loc.t] = '!';
		}
		else {
		
			//	Make the move
			theory_with_move[loc.r][loc.t] = player;
		}
		//	Give the board up
		return theory_with_move;
	}
	
	public int fitness(char player) {
		return fitness;
	}
	public int getFitness(char[][] board, int fitness_mode, char player) {
		int f = 0;
		switch (fitness_mode) {
		case DYLAN_FITNESS:
			f= dylanFitness(board, player);
			break;
		case ALEX_FITNESS:
			f=alexFitness(board, player);
			break;
		case CLASSIFIER_FITNESS:
			f=classifyFitness(board);
			break;
		case ANN_FITNESS:
			f=neuralFitness(board, player);
//                        f = player == PLAYER1 ? f : -f;
			break;
		case NONE:
			return 0;
		default:
			System.out.println("Using undefined fitness mode!");
			System.exit(0);
			return 0;
		}
		return f;
	}
	
	private int alexFitness(char[][] state, char player) {
		return (PLAYER2 == player ? -1 : 1) * heuristic(state, player);
	}
	
	public static int expand(char[][] state, char playingPlayer, int x, int y, int d) {
        int mx = state.length;
        int my = state[0].length;
        if (state[x][y] != EMPTY) { //do nothing if the cell is not empty
            return 0;
        }
        //simple switch to test the 4 directions to test
        //0 is n-s, 1 is sw-ne, 2 is e-w, 3 is nw-se
        int dx, dy;
        switch (d) {
            case 0:
                dx = 0;
                dy = 1;
                break;
            case 1:
                dx = -1;
                dy = 1;
                break;
            case 2:
                dx = 1;
                dy = 0;
                break;
            default:
                dx = 1;
                dy = 1;
        }
        int hfp1 = 0; //counter for How Far 
        int tx = posMod(x + dx, mx);
        int ty = posMod(y + dy, my);
        char type1 = state[tx][ty];
        boolean hb1 = false;
        if (type1 != EMPTY) {
            hfp1++;
            //scans along direction until reaching a different type of cell
            //counts length
            //stops if it reaches starting point
            while (state[tx][ty] == type1 && !(tx == x && ty == y)) {
                tx = posMod(tx + dx, mx);
                ty = posMod(ty + dy, my);
                hfp1++;
            }
            //test if this chain is blocked at the end
            if (state[tx][ty] != EMPTY) {
                hb1 = true;
            }
        }
        //check the opposite direction
        int hfp2 = 0; //counter for How Far 2
        int tx2 = posMod(x - dx, mx);
        int ty2 = posMod(y - dy, my);
        char type2 = state[tx2][ty2];
        boolean hb2 = false;
        if (type2 != EMPTY && !hb1 && !(tx2 == tx && ty2 == ty)) {
            hfp2++;
            while (state[tx2][ty2] == type1 && !(tx2 == x && ty2 == y)) {
                tx2 = posMod(tx2 - dx, mx);
                ty2 = posMod(ty2 - dy, my);
                hfp2++;
            }
            //test if this chain is blocked at the end
            if (state[tx2][ty2] != EMPTY) {
                hb2 = true;
            }
        }
 
        if (type1 == type2) {
            if (hfp1 + hfp2 >= 4) { //if the chain is >= win condition, return val
                return ((type1 == playingPlayer) ? 1 : -1) * (hfp1 + hfp2);
            } else if (hb2 && hb1) { //if both sids of the chain are blocked
                return 0; //bad chain
            } else { //means that it is unblocked, verify that I can make whole chain
                return ((type1 == playingPlayer) ? 1 : -1) * (hfp1 + hfp2); //this is where it potentially gets tricked
            }
        } else { //if they are different types, 
            if (hfp1 > hfp2) {
                return ((type1 == playingPlayer) ? 1 : -1) * hfp1;
            } else {
                return ((type2 == playingPlayer) ? 1 : -1) * hfp2;
            }
        }
    }
 
    public static int heuristic(char[][] state, char playingPlayer) {
        int count = 0; //longest potential chain
        for (int i = 0; i < state.length; i++) { //each row
            for (int j = 0; j < state[0].length; j++) { //each col
                for (int k = 0; k < 4; k++) { //each of 4 directions
                    int tcount = expand(state, playingPlayer, i, j, k); //find potential chain length in this direction
                    if (Math.abs(tcount) == Math.abs(count)) { //prefer offensive
                        count = (tcount > count) ? tcount : count;
                    } else if (Math.abs(tcount) > Math.abs(count)) { //take chain of higher magnitude
                        count = tcount;
                    }
                }
            }
        }
        return count;
    }
	 
    /**
     * Find the modulo of a by b, requiring the range to be [0,b)
     * @param a The value to modulo
     * @param b The modulo
     * @return The positive modulo
     */
    public static int posMod(int a, int b) {
        return (a % b + b) % b;
    }
    
	
	/**
	 * Evaluates the state of the board as a zero-sum game
	 * @param board The state to evaluate
	 * @return The fitness of the board
	 */
	private int dylanFitness(char[][] board, char player) {
		
		//	Track as we go
		int fitness = 0;
		
		//iterating through the spokes
		for(int spoke = 0; spoke < 12; spoke++) {
			
			//	Check every value on this spoke
			char[] vals = {
				board[0][spoke],
				board[1][spoke],
				board[2][spoke],
				board[3][spoke]
			};
			
			//	See how good it is
			fitness += dylan_helper(vals, player);

			//	Found a win!
			if (fitness <= -WIN_WEIGHT || WIN_WEIGHT <= fitness) {
				return fitness;
			}
		}
		
		//iterating through the diagonals
		for (int diagonal = 0; diagonal < 12; diagonal++) {
			
			//	Counterclockwise diagonals
			char[] vals = {
				board[0][posMod(diagonal    , 12)],
				board[1][posMod(diagonal + 1, 12)],
				board[2][posMod(diagonal + 2, 12)],
				board[3][posMod(diagonal + 3, 12)],
			};
			
			//	Check them
			fitness += dylan_helper(vals, player);
			
			//	Clockwise diagonals
			vals[0] = board[0][posMod(diagonal    , 12)];
			vals[1] = board[1][posMod(diagonal - 1, 12)];
			vals[2] = board[2][posMod(diagonal - 2, 12)];
			vals[3] = board[3][posMod(diagonal - 3, 12)];
			
			//	Check them
			fitness += dylan_helper(vals, player);

			//	Found a win!
			if (fitness <= -WIN_WEIGHT || WIN_WEIGHT <= fitness) {
				return fitness;
			}
		}
		
		//iterating through the rings
		for(int ring = 0; ring< 4 ; ring++) {
			
			//	Check the whole string
			String str = new String(board[ring]);
			
			//	Build the wrapping
			str += str.substring(0, 6);
			
			//	With proper play, it's impossible to lose if you have the two-sided win case
			if (str.contains("XXXX") || str.contains(".XXX.")) {
				return WIN_WEIGHT;
			}
			
			//	Same check but with minimizer
			if (str.contains("OOOO") || str.contains(".OOO.")) {
				return -WIN_WEIGHT;
			}
			
			//this accounts for wins and wins that are impossible to block, str1 does not work if the string goes over the 11-0 border which is why str2 is needed
			for(int b = 0; b < 12; b++)
			{
				
				//	Build the string yet again
				fitness += dylan_helper(str.substring(b, b + 4).toCharArray(), player);
				
				//	Found a win!
				if (fitness <= -WIN_WEIGHT || WIN_WEIGHT <= fitness) {
					return fitness;
				}
			}
		}
		return fitness;
	}
	
	private int dylan_helper(char[] vals, char player) {
		
		//	Start with no finds
		int p1Counter = 0, p2Counter = 0;
		for(int i = 0; i< 4 ; i++) {
			char check_spot = vals[i];
			if (check_spot == PLAYER1) {
				
				//	If both players are found, neither can win
				if (p2Counter != 0) {
					return 0;
				}
				
				//	Otherwise tally
				p1Counter++;
			}
			else if(check_spot == PLAYER2) {
				
				//	If both players are found, neither can win
				if (p1Counter != 0) {
					return 0;
				}
				
				//	Otherwise tally
				p2Counter++;
			}
		}
		
/*
		//	Found a win!
		if (p1Counter == 4) {
			return WIN_WEIGHT;
		}
		else if (p2Counter == 4) {
			return -WIN_WEIGHT;
		}
		//	It's my turn and I can win!
		else if (p1Counter == 3 && player == PLAYER1) {
			return WIN_WEIGHT;
		}
		//	It's my turn and I can win!
		else if (p2Counter == 3 && player == PLAYER2) {
			return -WIN_WEIGHT;
		}
	*/	
		//	returns the square of the result
		return (p1Counter * p1Counter - p2Counter * p2Counter);
	}
	
	
	//	Kalvyn should do this
	private int neuralFitness(char[][]board, char player) {
            float[]input=new float[48];
            int counter = 0;
            for(int i = 0; i < board.length;i++){
                for(int j = 0; j < board[i].length;j++){
                    switch (board[i][j]){
                        case PLAYER1:
                            input[counter++] = 1;
                            break;
                        case PLAYER2:
                            input[counter++] = -1;
                            break;
                        case EMPTY:
                            input[counter++] = 0;
                            break;
                    }
                }
            }
		return (int)(net.output(input)[0] * 52.6f);
	}

	

	/**
	 * How much a winning state matters. This mainly appears in the theoretical test
	 */
	public static final int WIN_WEIGHT = 1000000;
	
	//////////////////////////////
	//		PRIVATE HELPERS		//
	//////////////////////////////
	

	//	We're doing this
	private int classifyFitness(char[][] board) {
		float[] input = new float[48];
		int i = 0;
		for (int r = 0; r < board.length; r++) {
			for (int t = 0; t < board[0].length; t++) {
				switch(board[r][t]) {
				case PLAYER1:
					input[i++] = 1;
					break;
				case PLAYER2:
					input[i++] = -1;
					break;
				case EMPTY:
					input[i++] = 0;
					break;
				}
			}
		}
		
		int output = classifier.classify(input, 48);
		
		switch (output) {
		case 0:
			return 1;
		case 1:
			return -1;
		case 2:
			return 0;
		}
		
		return 0;
	}
	
	/**
	 * Determines from any board if a location is adjacent to a desired space
	 * @param board The board to check
	 * @param r The ring
	 * @param t The spoke
	 * @return Whether there is any adjacent taken location
	 */
	private boolean hasAdjacent(char[][] board, int r, int t){
		
		//	Get all neighbors
		Location[] neighbors = new Location(r, t).adjacentLocations();
		 
		//	Check all neighbors
		for (Location location : neighbors){
			if (peek(location) != EMPTY){
				return true;
			}
		}
		
		//	None adjacent
		return false;
	}
	

	/**
	 * Determines if a location is adjacent to a desired space.
	 * @param r The ring
	 * @param t The spoke
	 * @return Whether there is any adjacent taken location
	 */
	private boolean hasAdjacent(int r, int t) {
		return hasAdjacent(board, r, t);
	}
	
	//////////////////////////////
	//		PUBLIC HELPERS		//
	//////////////////////////////
	

	/**
	 * Gets the turn count of the current play.
	 * @return The current turn
	 */
	public int getTurn() {
		return turn;
	}
	
	/**
	 * Signals to the current player that a mouse input has been enterred and the Location of that click
	 * @param radius The loop from the center closest to the mouse on click
	 * @param theta The spoke on the loop closest to the mouse location on click
	 */
	public void receiveMouseInput(int radius, int theta) {
		this.players[turn & 1].receiveMouseInput(radius, theta);
	}
	
	/**
	 * Get the move made at the nth point in history
	 * @param n The move number
	 * @return The location of that move or null if it hasn't been played yet
	 */
	public Location getNthMoveMade(int n){
		//	Make sure the move exists
		if (-1 < n && n < turn){ 
			return history[n];
		}
		
		//	Null indicates no move
		return null;
	}
	
	/**
	 * Get the fitness of the board at the given state
	 * @param n The move number
	 * @return The fitness during that turn
	 */
	public int getNthFitness(int n) {
		//	Only allowed to get the fitness of a turn that there was a move made
		if (-1 < n && n < turn) {
			return fitnesses[n];
		}
		return 0;
	}
	
	/**
	 * Peaks at the marking at a location
	 * @param location
	 * @return The marking
	 */
	public char peek(Location location) {
		return board[location.r][location.t];
	}
	
	/**
	 * Peaks at the marking at a location
	 * @param radius The ring to check
	 * @param theta The spoke to check
	 * @return The marking
	 */
	public char peek(int radius, int theta) {
		return board[radius][theta];
	}
	
	/**
	 * Query the player for its name
	 * @param player The player number to check
	 * @return The name of that player
	 */ 
	public String getPlayerName(char player) {
		try {
			return players[getPlayerIndex(player)].getName();
		}
		catch (ArrayIndexOutOfBoundsException e){
			//	It's not worth killing the game
			return "Unknown Player";
		}
	}
	
	/**
	 * Gets the index in the players array of a player
	 * @param player The player to check
	 * @return The index of that player in the players array
	 */
	public int getPlayerIndex(char player) {
		return player == PLAYER1 ? 0 : player == PLAYER2 ? 1 : -1;
	}
	/**
	 * Get the symbol associated with the Player
	 * @param player The Player object to test with
	 * @return The player's symbol or PolarTTT.EMPTY if the player isn't in the game
	 */
	public char getPlayerSymbol(Player player) {
		return players[0] == player ? PLAYER1 : players[1] == player ? PLAYER2 : EMPTY;
	}
	
	/**
	 * Determines if a move is available to play
	 * @param location The location to test
	 * @return Whether to allow that play
	 */
	public boolean moveIsAvailable(Location location) {
		return available_locations[location.r][location.t];
	}
	
	/**
	 * Save the state of the board into a list for output later
	 * @param board The board
	 */
	private void save_board(char[][] board) {
		int[] list = new int[49];
		int i = 0;
		for (int r = 0; r < board.length; r++) {
			for (int t = 0; t < board[0].length; t++) {
				switch(board[r][t]) {
				case PLAYER1:
					list[i++] = 1;
					break;
				case PLAYER2:
					list[i++] = -1;
					break;
				case EMPTY:
					list[i++] = 0;
					break;
				}
			}
		}
		data.add(list);
	}
	
	/**
	 * Saves the results of all of the turns this game into a file
	 * @param winner Which player won or EMPTY
	 */
	private void save_data(char winner) {
		//	Default to a tie
		int res = 2;
		
		switch (winner) {
		case PLAYER1:
			res = 0;
			break;
		case PLAYER2:
			res = 1;
		}
		data.get(data.size() - 1)[48] = res;
		for (int [] list : data) {
			if (list[48] != res) {
				list[48] = -1;
			}
			float[] datainner = new float[48];
			for (int i = 0; i < 48; i++) {
				datainner[i] = (float)list[i];
			}
				
			net.learn(datainner, list[48]);
		}
		
		for (int[] list : data) {
			list[48] = res;
			classifier.learn(list, 48);
		}

        
		
		int[][] complete = new int[data.size()][49];
		data.toArray(complete);
        
		//Main.int_to_csv("data/test.csv", complete, true);
		classifier.save_weights("data/classifier_weights.csv");
		//net.printWeights();
		
		//	Clear the list!
		data.clear();
	}
	
	/**
	 * Updates the available_locations and available_locations_l arrays to match the game state
	 */
	private void assignAvailableMoves(){
		
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
					available_locations[i][j] = hasAdjacent(i, j);
				}
				
				//	Keep count
				if (available_locations[i][j]) {
					count++;
				}
			}
		}
		
		if (count == 0) {
			//	This will throw an error- which we want to see rather than just having 0 possible moves.
			available_locations_l = null;
		}
		else {
			//	Copy the locations into a list of Locations from the double-array
			available_locations_l = new Location[count];
			for (int r = 0; r < 4; r++){
				for (int t = 0; t < 12; t++){
					if (available_locations[r][t]){
						available_locations_l[--count] = new Location(r, t);
					}
				}
			}
		}
	}
	
	/**
	 * Gets all available locations in the current board state
	 * @return An array of all locations that are legal moves
	 */
	public Location[] allAvailableLocations(){
		
		//	It's fine to do this because the boolean array is used for logic;
		//	if the player tampers with this array, it won't affect the game.
		return available_locations_l;
	}
	
	/**
	 * Checks how many games were played this session
	 * @return the number of games
	 */	
	public int gameCount() {
		return num_games;
	}
	
	/**
	 * Checks how many games a player won this session
	 * @param player
	 * @return the number of games
	 */
	public int winCount(int player) {
		return players[player & 1].getScore();
	}

	/**
	 * Checks how many games a neither player won this session
	 * @return the number of games
	 */
	public int tieCount() {
		return num_ties;
	}

	//////////////////////////////////
	//		PUBLIC VARIABLES		//
	//////////////////////////////////

	//	The player characters are these- and can change at will
	public final static char
		EMPTY = '.',
		PLAYER1 = 'X',
		PLAYER2 = 'O';
	
	//	The fitness modes that are supported by the game so far are indexed this way
	public static final int
		NONE = 0,
		DYLAN_FITNESS = 3,
		ALEX_FITNESS = 4,
		ANN_FITNESS = 1,
		CLASSIFIER_FITNESS = 2;
	
	//////////////////////////////////////////////////
	//		CONSTRUCTOR AND PRIVATE VARIABLES		//
	//////////////////////////////////////////////////
	
	/**
	 * Constructs a new Polar Tic-Tac-Toe game.
	 */
	public PolarTTT () {
		
		//	Set up the GUI first.
		frame = new Frame("Polar Tic-Tac-Toe");
		frame.setSize(800, 600);		//	Standard game window size
		frame.setResizable(false);		//	Don't let them screw up our GUI
		canvas = new GameCanvas(this, frame.getWidth(), frame.getHeight());
		frame.add(canvas);				//	Put the canvas into the GUI
		
		//	Make sure the window can close
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		
		/*
		 * There is a bug in Java where clicking outside of the window does not cause
		 * the focusLost event to fire until you click inside.
		 * 
		 * This solution fixes the focus issue but causes Windows users to get a flashy
		 * window. But hey- a flashy window is better than a window that doesn't work.
		 */
		frame.addFocusListener(new FocusListener(){
			
			//	Do nothing
			public void focusGained(FocusEvent e){}
			
			//	Request focus
			public void focusLost(FocusEvent e){
				e.getComponent().requestFocus();
			}
		});
		
		
		//	Allow the keyboard input to be run
		frame.addKeyListener(this);
		
		String learnset = "data/learnset.csv";
		
		//	Learing agents
		classifier = new RBFClassifier(48, 400, 3, .1f, .725f, learnset);
		
		//	@Kalvyn You might want to make this into a function
		//	You also shold probably comment this.
        int layer[] = {48,10,1};
        net = new NeuralNetwork();
        float[][] data = Main.csv_to_float(learnset);
        int j = 0;
        for(float[] line : data ){

            try {
            float[] boardArr = new float[48];
            System.arraycopy(line, 0, boardArr, 0, 48);
            net.learn(boardArr, line[48]);
            }
            catch (Exception e) {
            	//Main.sout("Problem processing", Arrays.toString(line));
            }
        }
        net.printWeights();
		
		//	Some new arrays need to be made
		players = new Player[2];
		players[0] = players[1] = null;
		board = new char[4][12];
		available_locations = new boolean[4][12];
		history = new Location[48];
		fitnesses = new int[48];
		
		//	Finally start the game
		turn = 0;
		gameon = true;
	}
	
	
	//	Private variables
	private GameCanvas canvas;
	private RBFClassifier classifier;
        private NeuralNetwork net;
	private Frame frame;
	private char[][] board;
	private Player[] players;
	private Location[] history;
	private Location[] available_locations_l;
	private boolean[][] available_locations;
	private int fitnesses[];
	private int turn, fitness, num_games = 0, num_ties = 0;
	private boolean gameon, isVisible = true;
	private ArrayList<int[]> data = new ArrayList<int[]>();
}
