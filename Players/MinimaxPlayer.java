package Players;

import Logic.Location;
import Logic.PolarTTT;

import java.awt.*;
import java.awt.event.*;

public class MinimaxPlayer extends Player {
	
	@Override
	public Location getChoice(Location[] options) {
		turn++;
		
		char[][] board = game.theoreticalMove(options[0], '.');

		MMNode best = bestnode(board, new MMNode(is_maximizer), 0);
		
		if (best == null ) {
			System.out.println("well, fuck");
			System.exit(0);
			return null;
		}
		
		return best.move;
	}
	
	private MMNode bestnode(char[][] board, MMNode node, int ply) {
		if (ply == num_plies) {
			node.fitness = game.getFitness(board);
			return node;
		}
		
		Location[] moves = findAvailableMoves(board);
		
		if (moves.length == 0) {
			return node;
		}
		
		MMNode[] children = new MMNode[moves.length];
		for (int i = 0; i < moves.length; i++) {
			children[i] = new MMNode(moves[i], !node.is_maxer);
		}
		
		MMNode best = null;
		int best_fit = (node.is_maxer ? Integer.MIN_VALUE : Integer.MAX_VALUE);
		
		for (int i = 0; i < children.length; i++) {
			board[moves[i].r][moves[i].t] = (node.is_maxer ? PolarTTT.PLAYER2 : PolarTTT.PLAYER1);
			
			MMNode test = bestnode(board, children[i], ply + 1);
			
			if (node.is_maxer ? best_fit < test.fitness : test.fitness < best_fit) {
				best_fit = test.fitness;
				best = test;
			}
		}
		
		return best;
	}
	
	
	private Location[] findAvailableMoves(char[][] board){
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
		
		Location[] available_locations_l = new Location[count];
		for (int r = 0; r < 4; r++){
			for (int t = 0; t < 12; t++){
				if (available_locations[r][t]){
					available_locations_l[--count] = new Location(r, t);
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
		return "Minimax " + num_plies + "p" + (use_alpha_beta ? "+AB" : "");
	}
	
	public void newGame(PolarTTT game, boolean isMaximizer) {
		super.newGame(game, isMaximizer);
		frame = new Frame("Minimax Menu");
		frame.setSize(400, 300);
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		frame.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e ) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
					current_menu = (current_menu + 1) & 1;
					break;
				case KeyEvent.VK_LEFT:
					if (current_menu == 0) {
						if (1 < num_plies) {
							num_plies--;
						}
					}
					else {
						use_alpha_beta = !use_alpha_beta;
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (current_menu == 0) {
						if (num_plies < 48) {
							num_plies++;
						}
					}
					else {
						use_alpha_beta = !use_alpha_beta;
					}
					break;
				case KeyEvent.VK_ENTER:
					setup = true;
					synchronized(frame) {
						frame.notifyAll();
					}
					break;
				}
				canvas.repaint();
			}
		});
		
		canvas = new MinimaxCanvas(this);
		frame.add(canvas);
		frame.setVisible(true);
		frame.toFront();
		synchronized(frame) {
			while (!setup) {
				try {
					frame.wait();
				} catch (InterruptedException e) {
					//	Finally done
				}
			}
			setup = true;
		}
		frame.setVisible(false);
	}

	int turn = -1;
	private Frame frame;
	private MinimaxCanvas canvas;
	public int num_plies = 1;
	public boolean use_alpha_beta, setup = false;
	public String[] menues = {"Ply count", "Use Alpha-Beta pruning?"};
	public int current_menu = 0;
}

class MMNode {
	boolean is_maxer;
	int fitness;
	Location move;
	MMNode(boolean is_maxer) {
		this.is_maxer = is_maxer;
		fitness = 0;		
	}
	
	MMNode(Location move, boolean is_maxer) {
		this.is_maxer = is_maxer;
		this.move = move;
		fitness = 0;
	}
}

class MinimaxCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	MinimaxPlayer p;
	MinimaxCanvas (MinimaxPlayer p ) {
		this.p = p;
		setBackground(Color.WHITE);
	}
	
	public void paint(Graphics g) {
		g.drawString(p.menues[0], 100, 50);
		g.drawString("<- " + p.num_plies + " ->", 100, 100);
		g.drawString(p.menues[1], 100, 150);
		g.drawString(p.use_alpha_beta ? "Yes Yes Yes!" : "No No No!", 100, 200);
		
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(98, p.current_menu == 0 ? 100 : 200, 50, 3);
	}
	
}