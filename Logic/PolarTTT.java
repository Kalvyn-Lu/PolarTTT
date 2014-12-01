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
					
					if (num_games % 1000 == 0){
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
			findAvailableMoves();
			
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
				players[(turn + 1) & 1].incScore();
				return;
			}
			//if (checkWin(choice)) {
			if (win(board, getPlayerSymbol(p), choice.r, choice.t)){
				gameon = false;
				canvas.setStatus(GameCanvas.STATUS_WON, turn, p.getName() + " ( " + getPlayerSymbol(p) + " ) got 4 in a row and won the game!\n");
				fitnesses[turn - 1] = WIN_WEIGHT;
				players[(turn + 1) & 1].incScore();
				return;
			}
		}
		
		//	Cat's game!
		gameon = false;
		canvas.setStatus(GameCanvas.STATUS_TIE, turn, players[1].getName() + " ( " + PLAYER2 + " ) made the last move and tied the game!\n");
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
			fitness = dylanFitness(board);
			fitnesses[turn] = fitness;
			
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

					//	The greedy player is the third option
					case 2:
						players[i] = new GreedyPlayer();
						break;
						
					case 3:
						players[i] = new MinimaxPlayer();
						fitness_mode = DYLAN_FITNESS;
						break;
						
					case 4:
						players[i] = new MinimaxPlayer();
						fitness_mode = CLASSIFIER_FITNESS;
						break;
						
					case 5:
						players[i] = new MinimaxPlayer();
						fitness_mode = ANN_FITNESS;
						break;
					
					//	This should only happen during test stage
					default:
						System.out.println(canvas.menu_indices[i]);
						System.exit(0);
					}
				}
				
				if (canvas.menu_indices[2] == 1) {
					if (canvas.menu_indices[0] == 0 || canvas.menu_indices[1] == 0) {
						throw new RuntimeException("Humans can't play in bulk!");
					}
					
					isVisible = false;
				}
				
				//	Break the lock on the main thread which was waiting for this input
				
				synchronized(this){
					this.notifyAll();
				}
			}
			
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
	public int getFitness(char[][] board) {
		switch (fitness_mode) {
		case DYLAN_FITNESS:
			return dylanFitness(board);
		default:
			return 0;
		}
	}
	private int dylanFitness(char[][] board) {
		//	Todd's edit:
		//	Not the most efficient check but allows for AI to account for winning states
		for (int r = 0; r < 4; r++) {
			for (int t = 0; t < 12; t++) {
				if (win(board, PLAYER1, r, t)) {
					return WIN_WEIGHT;
				}
				else if (win (board, PLAYER2, r, t)) {
					return -WIN_WEIGHT;
				}
			}
		}
		
		String str, str2;
		int fitness = 0;
		int ring = 0;
		int spoke = 0;
		int diagonal = 0;
		int p1Counter = 0;
		int p2Counter = 0;
		
		for(spoke = 0; spoke<12; spoke++)//iterating through the spokes
		{
			p1Counter = 0;
			p2Counter = 0;

			str = ""+board[0][spoke]+board[1][spoke]+board[2][spoke]+board[3][spoke]+"";
			for(int a = 0; a<4; a++)
			{
				if(str.charAt(a) == PLAYER1) p1Counter++;
				if(str.charAt(a) == PLAYER2) p2Counter++;
			}
			if(p1Counter != 0 && p2Counter != 0){}//both player on this spoke, no possible win
			else if(p1Counter == 0 && p2Counter ==0){}//neither player on this spoke, trivial
			else
			{
				fitness += (p1Counter*p1Counter - p2Counter*p2Counter);//right now this applies the square of the number of marks on the spoke
			}
		}
		for(diagonal = 0; diagonal<24; diagonal++)//iterating through the diagonals
		{
			p1Counter = 0;
			p2Counter = 0;

			Location[] locations = dylanDiagonal(diagonal);
			str = ""+board[locations[0].r][locations[0].t]+
					board[locations[1].r][locations[1].t]+
					board[locations[2].r][locations[2].t]+
					board[locations[3].r][locations[3].t]+"";
			for(int a = 0; a<4; a++)
			{
				if(str.charAt(a) == PLAYER1) p1Counter++;
				if(str.charAt(a) == PLAYER2) p2Counter++;
			}
			if(p1Counter != 0 && p2Counter != 0){}//both player on this diagonal, no possible win
			else if(p1Counter == 0 && p2Counter ==0){}//neither player on this diagonal, trivial
			else
			{
				fitness += (p1Counter*p1Counter - p2Counter*p2Counter);//right now this applies the square of the number of marks on the diagonal
			}
		}
		for(ring = 0; ring<4; ring++)//iterating through the rings
		{
			str = ""+board[ring][0]+
					board[ring][1]+
					board[ring][2]+
					board[ring][3]+
					board[ring][4]+
					board[ring][5]+
					board[ring][6]+
					board[ring][7]+
					board[ring][8]+
					board[ring][9]+
					board[ring][10]+
					board[ring][11]+"";
			str2 = ""+board[ring][5]+
					board[ring][6]+
					board[ring][7]+
					board[ring][8]+
					board[ring][9]+
					board[ring][10]+
					board[ring][11]+
					board[ring][0]+
					board[ring][1]+
					board[ring][2]+
					board[ring][3]+
					board[ring][4]+""; 
			if(str.contains(".XXX.")) fitness+=100;
			if(str.contains(".OOO.")) fitness-=100;//these 2 lines account for wins that are impossible to block, it does not work for if the 5 goes over the 11-0 border
			if(str2.contains(".XXX.")) fitness+=100;
			if(str2.contains(".OOO.")) fitness-=100;//these 2 lines account for wins that are impossible to block, it does not work for if the 5 goes over the 11-0 border
			//this is the only way i could think to account for it, but im not sure how to make it apply to the rest
			for(int b = 0; b<12; b++)
			{
				p1Counter = 0;
				p2Counter = 0;
				for(int a = 0; a<4; a++)
				{
					if(str.charAt((a+b)%12) == PLAYER1) p1Counter++;
					if(str.charAt((a+b)%12) == PLAYER2) p2Counter++;
				}
				if(p1Counter != 0 && p2Counter != 0){}//both player on this ring, no possible win
				else if(p1Counter == 0 && p2Counter ==0){}//neither player on this ring, trivial
				else
				{
					fitness += (p1Counter*p1Counter - p2Counter*p2Counter);//right now this applies the square of the number of marks on the ring
				}
			}
		}
		return fitness;
	}
	private Location[] dylanDiagonal(int diagonal)//this is a lookup table to avoid calculation
	{
		Location[] locations = new Location[4];
		for(int a = 0; a<4; a++)
		{
			locations[a] = new Location(0,0);
		}
		switch (diagonal)
		{
			case 0:
				locations[0] = new Location(0,0);
				locations[1] = new Location(1,1);
				locations[2] = new Location(2,2);
				locations[3] = new Location(3,3);
				break;
			case 1:
				locations[0] = new Location(0,1);
				locations[1] = new Location(1,2);
				locations[2] = new Location(2,3);
				locations[3] = new Location(3,4);
				break;
			case 2:
				locations[0] = new Location(0,2);
				locations[1] = new Location(1,3);
				locations[2] = new Location(2,4);
				locations[3] = new Location(3,5);
				break;
			case 3:
				locations[0] = new Location(0,3);
				locations[1] = new Location(1,4);
				locations[2] = new Location(2,5);
				locations[3] = new Location(3,6);
				break;
			case 4:
				locations[0] = new Location(0,4);
				locations[1] = new Location(1,5);
				locations[2] = new Location(2,6);
				locations[3] = new Location(3,7);
				break;
			case 5:
				locations[0] = new Location(0,5);
				locations[1] = new Location(1,6);
				locations[2] = new Location(2,7);
				locations[3] = new Location(3,8);
				break;
			case 6:
				locations[0] = new Location(0,6);
				locations[1] = new Location(1,7);
				locations[2] = new Location(2,8);
				locations[3] = new Location(3,9);
				break;
			case 7:
				locations[0] = new Location(0,7);
				locations[1] = new Location(1,8);
				locations[2] = new Location(2,9);
				locations[3] = new Location(3,10);
				break;
			case 8:
				locations[0] = new Location(0,8);
				locations[1] = new Location(1,9);
				locations[2] = new Location(2,10);
				locations[3] = new Location(3,11);
				break;
			case 9:
				locations[0] = new Location(0,9);
				locations[1] = new Location(1,10);
				locations[2] = new Location(2,11);
				locations[3] = new Location(3,0);
				break;
			case 10:
				locations[0] = new Location(0,10);
				locations[1] = new Location(1,11);
				locations[2] = new Location(2,0);
				locations[3] = new Location(3,1);
				break;
			case 11:
				locations[0] = new Location(0,11);
				locations[1] = new Location(1,0);
				locations[2] = new Location(2,1);
				locations[3] = new Location(3,2);
				break;
			case 12:
				locations[0] = new Location(0,0);
				locations[1] = new Location(1,11);
				locations[2] = new Location(2,10);
				locations[3] = new Location(3,9);
				break;
			case 13:
				locations[0] = new Location(0,1);
				locations[1] = new Location(1,0);
				locations[2] = new Location(2,11);
				locations[3] = new Location(3,10);
				break;
			case 14:
				locations[0] = new Location(0,2);
				locations[1] = new Location(1,1);
				locations[2] = new Location(2,0);
				locations[3] = new Location(3,11);
				break;
			case 15:
				locations[0] = new Location(0,3);
				locations[1] = new Location(1,2);
				locations[2] = new Location(2,1);
				locations[3] = new Location(3,0);
				break;
			case 16:
				locations[0] = new Location(0,4);
				locations[1] = new Location(1,3);
				locations[2] = new Location(2,2);
				locations[3] = new Location(3,1);
				break;
			case 17:
				locations[0] = new Location(0,5);
				locations[1] = new Location(1,4);
				locations[2] = new Location(2,3);
				locations[3] = new Location(3,2);
				break;
			case 18:
				locations[0] = new Location(0,6);
				locations[1] = new Location(1,5);
				locations[2] = new Location(2,4);
				locations[3] = new Location(3,3);
				break;
			case 19:
				locations[0] = new Location(0,7);
				locations[1] = new Location(1,6);
				locations[2] = new Location(2,5);
				locations[3] = new Location(3,4);
				break;
			case 20:
				locations[0] = new Location(0,8);
				locations[1] = new Location(1,7);
				locations[2] = new Location(2,6);
				locations[3] = new Location(3,5);
				break;
			case 21:
				locations[0] = new Location(0,9);
				locations[1] = new Location(1,8);
				locations[2] = new Location(2,7);
				locations[3] = new Location(3,6);
				break;
			case 22:
				locations[0] = new Location(0,10);
				locations[1] = new Location(1,9);
				locations[2] = new Location(2,8);
				locations[3] = new Location(3,7);
				break;
			case 23:
				locations[0] = new Location(0,11);
				locations[1] = new Location(1,10);
				locations[2] = new Location(2,9);
				locations[3] = new Location(3,8);
				break;																																				
		}
		return locations;
	}
	

	/**
	 * How much a winning state matters. This mainly appears in the theoretical test
	 */
	public static final int WIN_WEIGHT = 1000000;
	
	/**
	 * How much we care about a spokes win in our fitness function
	 */
	public static final int SPOKE_WIN_WEIGHT = 7;
	
	/**
	 * How much we care about a spiral win in our fitness function
	 */
	public static final int SPIRAL_WIN_WEIGHT = 7;
	
	/**
	 * How much we care about a rub win in our fitness function
	 */
	public static final int RING_WIN_WEIGHT = 7;
	
	/**
	 * Rings can have openings that are potentially longer than 4 in a row. If such exist, how much do we care about the extras?
	 */
	public static final int RING_WIN_BEYOND_4 = 2;
	
	/**
	 * How much we care about potential wins where we already have positions owned
	 */
	public static final int ALREADY_OWNED_WEIGHT = 5;
	
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
				//e.getComponent().requestFocus();
			}
		});
		
		//	Allow the keyboard input to be run
		frame.addKeyListener(this);
			
		//	Some new arrays need to be made
		players = new Player[2];

		players[0] = players[1] = null;
		board = new char[4][12];
		available_locations = new boolean[4][12];
		history = new Location[48];
		fitnesses = new int[48];
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
	
	private boolean hasAdjacent(int r, int t) {
		return hasAdjacent(board, r, t);
	}
	
	public int gameCount() {
		return num_games;
	}
	
	public int winCount(int player) {
		return players[player & 1].getScore();
	}
	public int tieCount() {
		return num_ties;
	}
	public final static char EMPTY = '.', PLAYER1 = 'X', PLAYER2 = 'O';
	private GameCanvas canvas;
	private Frame frame;
	private char[][] board;
	private Player[] players;
	private Location[] history;
	private Location[] available_locations_l;
	private boolean[][] available_locations;
	private int fitnesses[];
	private int turn, fitness, num_games = 0, num_ties = 0;
	private boolean gameon, isVisible = true;
	public static final int
		DYLAN_FITNESS = 0,
		ANN_FITNESS = 1,
		CLASSIFIER_FITNESS = 2;
	public int fitness_mode = DYLAN_FITNESS;
}
