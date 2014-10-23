package Logic;

import java.awt.*;
import java.awt.event.*;

import Players.*;

/**
 * @author Anthony
 * The PolarTTT class 
 */
public class PolarTTT extends KeyAdapter{
	
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
			
		//	Some new arrays need to be made
		players = new Player[2];
		board = new char[4][12];
		available_locations = new boolean[4][12];
		history = new Location[48];
		turn = 0;
		gameon = true;
	}
	
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
		this.players[turn % 2].receiveMouseInput(radius, theta);
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
	 * Peaks at the marking at a location
	 * @param location
	 * @return The marking
	 */
	public char peak(Location location) {
		return board[location.r][location.t];
	}
	
	/**
	 * Peaks at the marking at a location
	 * @param radius The ring to check
	 * @param theta The spoke to check
	 * @return The marking
	 */
	public char peak(int radius, int theta) {
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
	 * Determines updates the available_locations and available_locations_l arrays to match the game state
	 */
	private void findAvailableMoves(){
		
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
			available_locations_l = null;
		}
		else {
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
	 * Gets all available locations.
	 * @return An array of all locations that are legal moves
	 */
	public Location[] allAvailableLocations(){
		
		//	It's fine to do this because the boolean array is used for logic;
		//	if the player tampers with this array, it won't affect the game.
		return available_locations_l;
	}
	
	/**
	 * Determines if a location is adjacent to a space that a player picked.
	 * @param r The ring
	 * @param t The spoke
	 * @return Whether there is any adjacent taken location
	 */
	private boolean hasAdjacent(int r, int t){
		
		//	Get all neighbors
		Location[] neighbors = new Location(r, t).adjacentLocations();
		 
		//	Check all neighbors
		for (Location location : neighbors){
			if (peak(location) != EMPTY){
				return true;
			}
		}
		
		//	None adjacent
		return false;
	}
	
	/**
	 * Starts the game from scratch
	 */
	public void begin() {
		synchronized (frame) {
			
			//	Game loop!
			while (true) {
				
				//	Reset everything
				turn = 0;
				players[0] = players[1] = null;
				for(int i = 0; i < 4; i++) {
					for (int j = 0; j < 12; j++){
						board[i][j] = EMPTY;
					}
				}
				for (int i = 0; i < 48; i++) {
					history[i] = null;
				}
				
				//	Switch to menu mode
				canvas.gameoff();
				frame.setVisible(true);
				
				//	Wait until the second player is set
				//	(the first player is always set before the second)
				synchronized (this) {
					while (players[1] == null){
						try {
							this.wait();
						}
						catch (InterruptedException e) {
						}
					}
				}
				//	Make the new players
				players[0].newGame(this);
				players[1].newGame(this);
				
				//	Set the game mode so the players aren't overwritten later
				canvas.gameon();
				
				//	Ask player 1 to make a move
				invokePlayerMove();
				
				//	Keep restarting the game until exit
				try {
					frame.wait();
				} catch (InterruptedException e) {
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
			findAvailableMoves();
			
			//	This shouldn't happen.
			if (available_locations_l == null){
				throw new RuntimeException("Out of moves prematurely!");
			}
			
			//	Start a player's new round
			Player p = players[turn % 2];
			p.newRound();
			
			/*
			//	Print out the list of every location (helpful when debugging)
			ArrayList<Location> locs = allAvailableLocations();
			
			for (Location l : locs) {
				System.out.print("(" + l.r + "," + l.t + "), ");
			}
			System.out.println();
 			*/
			
			//	Get the player's move
			choice = p.getChoice();
			if (!choose(choice)){
				gameon = false;
				canvas.setStatus(GameCanvas.STATUS_WON, turn, p.getName() + " ( " + getPlayerSymbol(p) + " ) made an illegal move and lost the game!\n");
				return;
			}
			if (checkWin(choice)) {
				gameon = false;
				canvas.setStatus(GameCanvas.STATUS_WON, turn, p.getName() + " ( " + getPlayerSymbol(p) + " ) got 4 in a row and won the game!\n");
				return;
			}
		}
		
		//	Cat's game!
		gameon = false;
		canvas.setStatus(GameCanvas.STATUS_TIE, turn, players[1].getName() + " ( " + PLAYER2 + " ) made the last move and tied the game!\n");
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
			board[location.r][location.t] = (0 == turn % 2 ? PLAYER1 : PLAYER2);
			history[turn] = location;
			
			//	Rotate turn count and thus give other player a turn
			turn++;
			
			//	Redraw the board
			canvas.repaint();
			
			//	Pass that the move was successful
			return true;
		}
		
		return false;
	}
	
	private boolean win(char player){
		if (player != PLAYER1 && player != PLAYER2) {
			throw new RuntimeException ("Only players 0 and 1 exist");
		}
		
/*
win(player) -> Exists (ring, spoke) such that:
At(player, Location(ring,spoke)) ^ (
(Is(ring, 0) ^ (
At(player, Location(ring + 1, spoke)) ^ At(player, Location(ring + 2, spoke)) ^ At(player, Location(ring + 3, spoke))
v At(player, Location(ring + 1, spoke + 1)) ^ At(player, Location(ring + 2, spoke + 2)) ^ At(player, Location(ring + 3, spoke + 3))
v At(player, Location(ring + 1, spoke - 1)) ^ At(player, Location(ring + 2, spoke - 2)) ^ At(player, Location(ring + 3, spoke - 3))
)
v (At(player, Location(ring, spoke + 1) ^ At(player, Location(ring, spoke + 2)) ^ At(player, Location(ring, spoke + 1))))

Unification string is { player/?, ring/?, spoke/? } where each ? is decided at runtime.
*/

		//	This function will override checkWin but we don't want to break anything [yet]
		//	so it'll just wait until the next commit so we still have a working one.
		return false;
	}
	
	/**
	 * Determines if the current state is a winning one
	 * @param location The location of the last play
	 * @return Whether the game has ended
	 */
	private boolean checkWin(Location location) {
		
		//	It's impossible to win in the first 6 turns
		if (turn < 6) {
			return false;
		}
		
		//	See which move was made
		char player = peak(location);
		
		//	Check all the win conditions
		return checkSpokesWin(player, location) || checkRingWin(player, location)
			|| checkClockwiseWin(player, location) || checkCounterClockwiseWin(player, location);
	}
	
	/**
	 * Checks to see if a single player has at least four in a row along a spoke
	 * @param player The player to test with
	 * @param location The location of the last move
	 * @return Whether the game was won
	 */
	private boolean checkSpokesWin(char player, Location location) {
		int furthest = location.r;
		int count = 0;
		
		//	Find the furthest-out ring in the line
		while (furthest < 3 && board[furthest + 1][location.t] == player) {
			furthest++;
		}
		
		//	It's impossible to win if the fourth ring is not reached
		if (furthest < 3) {
			return false;
		}
		
		//	Count how many in a row exist
		while (-1 < furthest && board[furthest][location.t] == player){
			furthest--;
			count++;
		}
		
		//	Show if it is at least 4 in a row
		return 3 < count;
	}
	
	/**
	 * Checks to see if a single player has at least four in a row along a ring
	 * @param player
	 * @param location
	 * @return Whether the player has won
	 */
	private boolean checkRingWin(char player, Location location) {
		int furthest = location.t;
		int count = 0;
		
		//	Find the furthest out player along the ring
		//	Due to the size of the board, no ring can be full that
		//		can cause this to become an infinite loop
		while (board[location.r][((furthest + 1) + 12) % 12] == player) {
			furthest = (furthest + 13) % 12;	//	Wrapping increment
		}
		
		//	Count how many around this ring this player owns
		while (board[location.r][furthest] == player) {
			furthest = (furthest + 11) % 12;	//	Wrapping decrement
			count++;
		}
		
		//	Show if it is at least 4 in a row
		return 3 < count;
	}
	
	/**
	 * Checks to see if a single player has at least four in a row around the ring
	 * @param player
	 * @param location
	 * @return
	 */
	private boolean checkClockwiseWin(char player, Location location) {
		//	Make a copy- don't edit the original
		Location furthest = new Location(location.r, location.t);
		int count = 0;
		
		//	Find the closest in point in the spiral
		while (furthest.r > 0 && board[furthest.r - 1][(furthest.t + 13) % 12] == player){
			furthest.r--;
			furthest.t = (furthest.t + 13) % 12;	//	Wrapping increment
		}
		
		//	Find the number in a row spirally
		while (furthest.r < 4 && peak(furthest) == player) {
			count++;
			furthest.r++;
			furthest.t = (furthest.t + 11) % 12;	//	Wrapping decrement
		}
		
		//	Show if it is at least 4 in a row
		return 3 < count;
	}
	
	/**
	 * Checks to see if a single player has at least four in a row around the ring
	 * @param player
	 * @param location
	 * @return
	 */
	private boolean checkCounterClockwiseWin(char player, Location location) {
		//	Make a copy- don't edit the original
		Location furthest = new Location(location.r, location.t);
		int count = 0;
		
		//	Find the closest in point in the spiral
		while (furthest.r > 0 && board[furthest.r - 1][(furthest.t + 11) % 12] == player){
			furthest.r--;
			furthest.t = (furthest.t + 11) % 12;	//	Wrapping decrement
		}
		
		//	Find the number in a row spirally
		while (furthest.r < 4 && peak(furthest) == player) {
			count++;
			furthest.r++;
			furthest.t = (furthest.t + 13) % 12;	//	Wrapping increment
		}
		
		//	Show if it is at least 4 in a row
		return 3 < count;
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
										
				//	Assign each player from the provided menu selections
				for (int i = 0; i < 2; i++) {
					switch (canvas.menu_indices[i]){

					//	The human player is the first option
					case 0:
						players[i] = new HumanPlayer();
						break;
					
					//	The random player is the second option
					case 1:
						players[i] = new RandomPlayer();
						break;
						
					//	This should only happen during test stage
					default:
						System.out.println(canvas.menu_indices[i]);
						System.exit(0);
					}
				}
				
				//	Break the lock on the main thread which was waiting for this input
				synchronized(this){
					this.notifyAll();
				}
			}
			
			else if (!gameon) {
				synchronized(frame) {
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

	public final static char EMPTY = '.', PLAYER1 = 'X', PLAYER2 = 'O';
	private GameCanvas canvas;
	private Frame frame;
	private char[][] board;
	private Player[] players;
	private Location[] history;
	private Location[] available_locations_l;
	private boolean[][] available_locations;
	private int turn;
	private boolean gameon;
}
