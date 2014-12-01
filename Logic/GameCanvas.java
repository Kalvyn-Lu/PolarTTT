package Logic;

import java.awt.*;
import java.awt.event.*;

import Players.Player;

/**
 * The Polar TTT canvas that handles both the menu and the game.
 * @author Anthony
 *
 */
public class GameCanvas extends Canvas {
	
	Point[][] points;
	
	/**
	 * Constructs the game canvas
	 * @param width The width of the canvas in pixels
	 * @param height The height of the canvas in pixels
	 */
	public GameCanvas(PolarTTT game, int width, int height) {
		this.game = game;
		
		//	Initialize the menu data
		menu_selected = 0;
		menu_indices = new int[menu.length];
		for (int i = 0; i < menu_indices.length; i++) {
			menu_indices[i]= 0;
		}
		
		//	Set up the center of the circle
		origin_x = (int)RADIUS_UNIT * 5;
		origin_y = height/2;
		
		points = new Point[4][12];
		for (int r = 0; r < 4; r++) {
			for (int t = 0; t < 12; t++) {
				points[r][t] = new Point(
					getXPixelFromLocation(r, t),
					getYPixelFromLocation(r, t)
				);
			}
		}
		
		//	Set up the canvas for display
		setBackground(BACKGROUND_COLOR);
		setSize(width, height);
		
		
		//	Allow mouse input
		this.addMouseListener(new MouseAdapter() {
			
			//	The goal is to find the closest grid location and send it into the game to handle
			public void mouseClicked(MouseEvent e) {
				
				//	Don't allow mouse events if the game hasn't started
				if (mode != MODE_GAME) {
					return;
				}
				
				//	The XY coordinates relative to the game window's top left corner
				Point p = e.getPoint();
				
				//	Track the best
				int dist_squared = Integer.MAX_VALUE;
				
				//	Check them all
				for (int r = 0; r < 4; r++) {
					for (int t = 0; t < 12; t++) {
						
						//	Distance formula
						int dist = (points[r][t].x - p.x) * (points[r][t].x - p.x) + 
								(points[r][t].y - p.y) * (points[r][t].y - p.y);
						
						//	Since we only care about the values of radius and theta at the end
						//	we can simply overwrite when a new best is found.
						if (dist < dist_squared) {
							dist_squared = dist;
							mouse_radius = r;
							mouse_theta = t;
						}
					}
				}
				
				//	Signal mouse input to the game
				receiveMouseInput();
			}
		});
	}
	
	/**
	 * Passes mouse input to the game. This nightmare is because Eclipse wants game
	 * to be final if used inside of the listener.
	 */
	private void receiveMouseInput(){
		game.receiveMouseInput(mouse_radius, mouse_theta);
	}
	
	/**
	 * Handles the down arrow
	 */
	public void movedown(){
		
		//	If we can move down, move down
		if (menu_selected < menu_indices.length - 1) {
			menu_selected++;
			repaint();
		}
	}
	
	/**
	 * Handles the up arrow
	 */
	public void moveup() {
		
		//	If we can move up, move up
		if (0 < menu_selected){
			menu_selected--;
			repaint();
		}
	}
	
	/**
	 * Handles right arrow
	 */
	public void moveright() {
		//	If we can move right, move right
		if (menu_indices[menu_selected] < menu[menu_selected].length - 1) {
			menu_indices[menu_selected]++;
			repaint();
		}
	}
	
	/**
	 * Handles the left arrow
	 */
	public void moveleft(){
		//	If we can move left, move left
		if (0 < menu_indices[menu_selected]) {
			menu_indices[menu_selected]--;
			repaint();
		}
	}
	
	@Override
	public void paint(Graphics g){
		
		//	We're painting in 3D
		Graphics2D g2d = (Graphics2D)g;
		
		//	Pick runtime which to draw
		switch (mode){
		case MODE_MENU:
			paintMenu(g2d);
			break;
		case MODE_GAME:
			paintGame(g2d);
			break;
		case MODE_INVISIBLE:
			paintInvisible(g2d);
		}
	}
	
	/**
	 * Paints the menu
	 * @param g The Graphics
	 */
	private void paintMenu(Graphics2D g) {
		//	Draw the highlights first
		g.setColor(Color.BLUE);
		for (int i = 0; i < menu_indices.length; i++) {
			g.fillRect(20 + 75 * menu_indices[i], 100 * (i + 1) - 16, 75, 24);
		}
		
		//	Draw the current highlight
		g.setColor(Color.GREEN);
		g.fillRect(20 + 75 * menu_indices[menu_selected], 100 * (menu_selected + 1) - 16, 75, 24);
		
		//	Draw the options
		g.setColor(Color.WHITE);
		g.drawString("Player 1 Type", 50, 75);
		for (int i = 0; i < menu[0].length; i++) {
			g.drawString(menu[0][i], 25 + 75 * i, 100);
		}
		g.drawString("Player 2 Type", 50, 175);
		for (int i = 0; i < menu[1].length; i++) {
			g.drawString(menu[1][i], 25 + 75 * i, 200);
		}
		g.drawString("Training Menu", 50, 275);
		for (int i = 0; i < menu[2].length; i++) {
			g.drawString(menu[2][i], 25 + 75 * i, 300);
		}
	}
	
	/**
	 * Paints the game
	 * @param g
	 */
	private void paintGame(Graphics2D g){ 
		//	Prepare to draw
		g.setColor(FOREGROUND_COLOR);
		
		//	Draw the game board's circles
		g.fillOval(origin_x - 2, origin_y - 2, 4, 4);
		for (int r = 0; r < 4; r++) {
			int offset = (1 + r) * (int)RADIUS_UNIT;
			g.drawOval(origin_x - offset, origin_y - offset , 2 * offset, 2 * offset);
		}
		
		//	Draw the game board's spokes
		g.drawLine(points[3][0].x, points[3][0].y, points[3][6].x, points[3][6].y);
		g.drawLine(points[3][1].x, points[3][1].y, points[3][7].x, points[3][7].y);
		g.drawLine(points[3][2].x, points[3][2].y, points[3][8].x, points[3][8].y);
		g.drawLine(points[3][3].x, points[3][3].y, points[3][9].x, points[3][9].y);
		g.drawLine(points[3][4].x, points[3][4].y, points[3][10].x, points[3][10].y);
		g.drawLine(points[3][5].x, points[3][5].y, points[3][11].x, points[3][11].y);
		
		//	Draw the plays
		g.setFont(new Font("Arial", Font.BOLD, 24));
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 12; j++) {
				char at = game.peek(i, j);
				if (at == PolarTTT.EMPTY) {
					if (game.moveIsAvailable(new Location(i, j))){
						g.setColor(NEUTRAL_COLOR);
						g.drawOval(points[i][j].x - SYMBOL_WIDTH/2, points[i][j].y - SYMBOL_WIDTH/2, SYMBOL_WIDTH, SYMBOL_HEIGHT);
					}
				}
				else {
					g.setColor(at == PolarTTT.PLAYER1 ? P1_COLOR : P2_COLOR);
					g.drawString("" + at, points[i][j].x - SYMBOL_WIDTH/2, points[i][j].y + SYMBOL_WIDTH/2);
				}
			}
		}
		
		//	Prepare to draw the history
		g.setFont(new Font("Arial", Font.PLAIN, 12));
		boolean p1 = true;
		int xloc1 = (int)RADIUS_UNIT * 10,
			xloc2 = xloc1 + 36,
			xloc3 = xloc1 + 68;
		
		g.setColor(FOREGROUND_COLOR);
		g.drawString("Move History", xloc1 + 24, 18);

		g.setColor(P1_COLOR);
		g.drawString("P1", xloc1, 34);
		g.setColor(P2_COLOR);
		g.drawString("P2", xloc2, 34);
		g.setColor(FOREGROUND_COLOR);
		g.drawString("Fitness", xloc3, 34);
		int i;
		
		//	Draw the history
		for (i = 0; i < 48; i++, p1 = !p1) {
			Location l = game.getNthMoveMade(i);
			
			//	Don't print if no move is made
			if (l == null) {
				break;
			}
			
			g.setColor(p1 ? P1_COLOR: P2_COLOR);
			g.drawString(l.toString(), p1 ? xloc1 : xloc2, 10 * i + 46);
			
			int f = game.getNthFitness(i);
			g.setColor(f == 0 ? FOREGROUND_COLOR : f < 0 ? P2_COLOR : P1_COLOR);
			g.drawString(" " + f, xloc3 + (p1 ? 0 : 30), 10 * i + 46);
		}
		
		//	Draw the stats
		g.setColor(FOREGROUND_COLOR);
		g.drawString("Game Information", 650, 18);
		g.drawString("Player 1 Name:", 675, 66);
		g.drawString("Player 2 Name:", 675, 114);
		
		String p1name = game.getPlayerName(PolarTTT.PLAYER1),
			p2name = game.getPlayerName(PolarTTT.PLAYER2);

		//	Draw the status with our foreground
		g.setColor(FOREGROUND_COLOR);
		switch (status) {
		case STATUS_IN_PROGRESS:
			g.setColor(FOREGROUND_COLOR);
			g.drawString("Turn Number: " + (1 + i), 675, 194);
			g.setColor(i%2 == 0 ? P1_COLOR : P2_COLOR);
			g.drawString((i%2 == 0 ? p1name : p2name) + " to play.", 650, 226);
			break;
		case STATUS_WON:case STATUS_TIE:
			g.drawString("Results:", 200, 540);
			g.drawString("Enter to restart. Escape to quit.", 270, 530);
			g.setColor(status_color);
			g.drawString(information, 250, 540);
			break;
		}
		
		g.setColor(P1_COLOR);
		g.drawString(p1name, 660, 82);
		g.setColor(P2_COLOR);
		g.drawString(p2name, 660, 130);
		
	}
	
	private void paintInvisible(Graphics2D g){
		int games = game.gameCount();
		if (games == 0) {
			g.setColor(FOREGROUND_COLOR);
			g.drawString("Running first batch of games... ", 200, 200);
			return;
		}
		
		int p1wins = p1.getScore();
		int p2wins = p2.getScore();
		int ties = game.tieCount();
		g.setColor(FOREGROUND_COLOR);
		g.drawString("Number of games: ", 200, 200);
		g.drawString("" + games, 350, 200);
		g.drawString("Number of ties: ", 200, 320);
		g.drawString("" + ties, 350, 320);
		g.drawString((100 * ties / games) + "%", 400, 320);
		g.setColor(P1_COLOR);
		g.drawString(p1.getName() + " wins: ", 200, 240);
		g.drawString("" + p1wins, 350, 240);
		g.drawString((100 * p1wins / games) + "%", 400, 240);
		g.setColor(P2_COLOR);
		g.drawString(p2.getName() + " wins: ", 200, 280);
		g.drawString("" + p2wins, 350, 280);
		g.drawString((100 * p2wins / games) + "%", 400, 280);
	}
	
	/**
	 * Finds the pixel's x location on the screen
	 * @param radius
	 * @param theta
	 * @return The x-coordinate of the pixel
	 */
	private int getXPixelFromLocation(int radius, int theta) {
		return origin_x + (int)(Math.floor(((1 + radius) * RADIUS_UNIT) * Math.cos(THETACONVERSION[theta])));
	}
	
	/**
	 * Finds the pixel's y location on the screen
	 * @param radius
	 * @param theta
	 * @return The y-coordinate of the pixel
	 */
	private int getYPixelFromLocation(int radius, int theta) {
		return origin_y - (int)(Math.floor(((1 + radius) * RADIUS_UNIT) * Math.sin(THETACONVERSION[theta])));
	}

	/**
	 * Set the game to the play board
	 */
	public void gameon() {
		mode = MODE_GAME;
		repaint();
	}

	/**
	 * Set the game to the menu
	 */
	public void gameoff() {
		mode = MODE_MENU;
		status = STATUS_IN_PROGRESS;
		repaint();
	}
	
	public void setInvisible(Player p1, Player p2) {
		this.p1 = p1;
		this.p2 = p2;
		mode = MODE_INVISIBLE;
		repaint();
	}
	private Player p1, p2;
	
	/**
	 * Get the current mode
	 * @return The mode
	 */
	public int getMode() {
		return mode;
	}
	/**
	 * Sets the status of the game (often to end it).
	 * @param status The mode to set this by
	 * @param turn The turn in which the status was set
	 * @param message The message to display
	 */
	public void setStatus(int status, int turn, String message) {
		if (status != STATUS_IN_PROGRESS && status != STATUS_WON && status != STATUS_TIE){
			throw new RuntimeException("Invalid status- must be a STATUS_ mode");
		}
		
		//	Save the main data
		this.status = status;
		information = message;
		
		//	Fix an off-by-one error that occurs mid-move.
		turn--;
		
		//	The color is the player's turn it happened- not necessarily affiliated.
		this.status_color = turn % 2 == 0 ? P1_COLOR : P2_COLOR;
	}
	
	//	Because Eclipse said to make this
	private static final long serialVersionUID = 1L;
	
	//	The angles at which a spoke will be pointing
	private final double[] THETACONVERSION = {0., 0.524, 1.047, 1.571, 2.094, 2.618, 3.142, 3.665, 4.189, 4.712, 5.236, 5.760};
	
	//	The difference in radius between the rings
	private final double RADIUS_UNIT = 50.0;
		
	//	The size of the graphic to be used to mark the spot
	private final int SYMBOL_WIDTH = 16, SYMBOL_HEIGHT = 16;
	
	//	The color presets
	private final Color BACKGROUND_COLOR = Color.BLACK,	//	Black
		FOREGROUND_COLOR = new Color(200, 200, 200),	//	White
		P1_COLOR = new Color(255, 31, 0),				//	Red
		P2_COLOR = new Color(31, 128, 255),				//	Blue
		NEUTRAL_COLOR = new Color(6, 176, 80);			//	Green
	
	//	Integers to be used in calculations
	private int origin_x, origin_y, mouse_radius = -1, mouse_theta = -1;
	
	//	Menu handlers
	private int menu_selected;
	public int[] menu_indices;
	
	//	The game this gui interfaces with
	private PolarTTT game;
	
	//	Menues
	private final String[] PlayerTypes = {"Human", "Random", "Greedy", "Minimax", "Classifier", "ANN"},
			FastRun = {"One Game", "Bulk Training"};
	private String[][] menu = {PlayerTypes, PlayerTypes, FastRun};
	
	//	Modes determine which game is to be played
	private int mode;
	public static final int MODE_MENU = 0;
	public static final int MODE_GAME = 1;
	public static final int MODE_INVISIBLE = 2;
	
	//	Use this information to display end conditions
	private int status;
	private String information;
	private Color status_color;
	public static final int STATUS_IN_PROGRESS = 0;
	public static final int STATUS_WON = 1;
	public static final int STATUS_TIE = 2;
}