package Logic;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

import Players.*;
import RBFClassifier.RBFClassifier;

/**
 * @author Anthony
 * The PolarTTT class 
 */
public class PolarTTT extends KeyAdapter{
	
	
	//	Kalvyn should do this
	private int neuralFitness(char[][]board) {
		return 0;
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
			
			if (turn % 5 == 3) {
				save_board(board);
			}
			
			//if (checkWin(choice)) {
			if (win(board, getPlayerSymbol(p), choice.r, choice.t)){
				gameon = false;
				canvas.setStatus(GameCanvas.STATUS_WON, turn, p.getName() + " ( " + getPlayerSymbol(p) + " ) got 4 in a row and won the game!\n");
				fitnesses[turn - 1] = WIN_WEIGHT;
				players[(turn + 1) & 1].incScore();
				save_board(board);
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
			fitness = getFitness(board, DYLAN_FITNESS, (0 == (turn & 1) ? PLAYER1 : PLAYER2 ));
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
			f= dylanFitness(board);
			break;
		case ALEX_FITNESS:
			f=alexFitness(board, player);
			break;
		case CLASSIFIER_FITNESS:
			f=classifyFitness(board);
			break;
		case ANN_FITNESS:
			f=neuralFitness(board);
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
		return heuristic(state, player);
	}
	
	public int expand(char[][] state, char playingPlayer, int x, int y, int d) {
        int mx = state.length;
        int my = state[0].length;
        if (state[x][y] != EMPTY) {
            return 0;
        }
        //0 is up-down, 1 is sw-ne, 2 is left-right, 3 is nw-se
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
        int hfp1 = 0;
        int tx = posMod(x + dx, mx);
        int ty = posMod(y + dy, my);
        char type1 = state[tx][ty];
        boolean hb1 = false;
        if (type1 != EMPTY) {
            hfp1++;
            while (state[tx][ty] == type1 && !(tx == x && ty == y)) {
                tx = posMod(tx + dx, mx);
                ty = posMod(ty + dy, my);
                hfp1++;
            }
            if (state[tx][ty] != EMPTY) hb1 = true;
        }
        int hfp2 = 0;
        tx = posMod(x - dx, mx);
        ty = posMod(y - dy, my);
        char type2 = state[tx][ty];
        boolean hb2 = false;
        if (type2 != EMPTY) {
            hfp2++;
            while (state[tx][ty] == type1 && !(tx == x && ty == y)) {
                tx = posMod(tx - dx, mx);
                ty = posMod(ty - dy, my);
                hfp2++;
            }
            if (state[tx][ty] != EMPTY) hb2 = true;
        }
        if (type1==type2) {
            if (hfp1+hfp2 >= 4) return ((type1==playingPlayer)?1:-1) * Integer.MAX_VALUE;
            else if (hb2 && hb1) return 0; //same type, but can't hit total length 4. Return 0, bad spot.
            else return hfp1+hfp2; //else return sum
        } else {
            if (hfp1 >= 4 && type1 == playingPlayer) return ((type1==playingPlayer)?1:-1) * Integer.MAX_VALUE;
            if (hfp2 >= 4 && type2 == playingPlayer) return ((type2==playingPlayer)?1:-1) * Integer.MAX_VALUE;
             
            if (hfp1 > hfp2) {
                return ((type1==playingPlayer)?1:-1) * hfp1;
            } else {
                return ((type2==playingPlayer)?1:-1) * hfp2;
            }
        }
    }
     
    public int heuristic(char[][] state, char playingPlayer) {
        int count = 0;
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[0].length; j++) {
                for (int k = 0; k < 4; k++) {
                    int tcount = expand(state, playingPlayer, i, j, k);
                    if (Math.abs(tcount) == Math.abs(count)) { //prefer offensive
                        count = (tcount>count)?tcount:count;
                    } else if (Math.abs(tcount) > Math.abs(count)) {
                        count = tcount;
                    }
                }
            }
        }
        return count;
    }
 
    public static int posMod(int a, int b) {
        return (a % b + b) % b;
    }
    
	
	
	private int dylanFitness(char[][] board) {
		//	Todd's edit:
		//	Not the most efficient check but allows for AI to account for winning states
		/*for (int r = 0; r < 4; r++) {
			for (int t = 0; t < 12; t++) {
				if (win(board, PLAYER1, r, t)) {
					return WIN_WEIGHT;
				}
				else if (win (board, PLAYER2, r, t)) {
					return -WIN_WEIGHT;
				}
			}
		}*/
		
		String str, str2;
		int fitness = 0;
		int ring,spoke,diagonal,p1Counter,p2Counter;
		
		for(spoke = 0; spoke<12; spoke++)//iterating through the spokes
		{
			p1Counter = 0;
			p2Counter = 0;

			str = ""+board[0][spoke]+board[1][spoke]+board[2][spoke]+board[3][spoke]+"";
                        if(str.equals("XXXX")){return WIN_WEIGHT;}
                        if(str.equals("OOOO")){return -WIN_WEIGHT;}
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
                        if(str.equals("XXXX")){return WIN_WEIGHT;}
                        if(str.equals("OOOO")){return -WIN_WEIGHT;}
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
			if(     str.contains("XXXX")||
                                str.contains(".XXX.")||
                                str2.contains(".XXX.")) return WIN_WEIGHT;
			if(     str.contains("OOOO")||
                                str.contains(".OOO.")||
                                str2.contains(".OOO.")) return -WIN_WEIGHT;
			//this accounts for wins and wins that are impossible to block, str1 does not work if the string goes over the 11-0 border which is why str2 is needed
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
		
		int output = classifier.classify(input);
		
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
		
		for (int[] list : data) {
			list[48] = res;
		}
		
		int[][] complete = new int[data.size()][49];
		data.toArray(complete);
		Main.int_to_csv("data/test.csv", complete, true);
		
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
		
		//	Learing agents
		//classifier = new RBFClassifier(48, 00, 3, 0.1f, 0.15f, "data/test.csv");
		
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
