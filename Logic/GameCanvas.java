package Logic;

import java.awt.*;
import java.awt.event.*;

/**
 * The Polar TTT canvas that handles both the menu and the game.
 * @author Anthony
 *
 */
public class GameCanvas extends Canvas{
	
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
		
		//	Set up the canvas for display
		setBackground(BACKGROUND_COLOR);
		setSize(width, height);
		
		//	Set up the center of the circle
		origin_x = (int)RADIUS_UNIT * 5;
		origin_y = height/2;
		
		//	Allow mouse input
		this.addMouseListener(new MouseAdapter() {
			
			//	The goal is to find the closest grid location and send it into the game to handle
			public void mouseClicked(MouseEvent e) {
				
				//	Don't allow mouse events if the game hasn't started
				if (mode == MODE_MENU) {
					return;
				}
				
				//	The XY coordinates relative to the game window's top left corner
				Point p = e.getPoint();
				
				//	Adjust to the origin
				int x = p.x - origin_x, y = p.y - origin_y;
				
				//	Get the radius in pixels
				double r = Math.sqrt(x * x + y * y);
				
				//	Assume we're on the farthest out loop first
				mouse_radius = 3;
				
				//	Start from the inside and go out
				for (int i = 0; i < 3; i++) {
					
					//	If it is within the circle, it's in!
					//	If it's in a ring, it is in this circle but not the previous
					//		Note 1.5- this is because we adjust by 1 (can't pick 0th ring)
					//		and .5 (to give margins to the rings)
					if (r < RADIUS_UNIT * ((double)i + 1.5)){
						mouse_radius = i;
						break;
					}
				}
				
				//	Grab the angle
				double theta = Math.atan(-(double)y/(double)x);
				
				//	Save where to start the tracker
				int start = 0;
				
				//	If we're in an odd quadrant
				if (0 < x * y) {
					
					//	then offset by 3 spokes
					start = 3;
					
					//	and then adjust the angle back to positive
					theta += Math.PI/2.;
				}
				
				//	Now see if we're below the X axis
				if (0 < y) {
					
					//	Offset 6 more spokes if so
					start += 6;
				}
				
				//	Do the same trick with the rings only this time for spokes
				for (int i = 0; i < 4; i++) {
					
					//	Unlike before, 0th spoke is allowed, so 0.5 is fine.
					if (theta < THETA_UNIT * ((double)i + 0.5)) {
						mouse_theta = (i + start) % 12;	//	Keep it wrapped aroud
						break;
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
		}
	}
	
	/**
	 * Paints the menu
	 * @param g2d The Graphics
	 */
	private void paintMenu(Graphics2D g2d) {
		//	Draw the highlights first
		g2d.setColor(Color.BLUE);
		for (int i = 0; i < menu_indices.length; i++) {
			g2d.fillRect(20 + 75 * menu_indices[i], 100 * (i + 1) - 16, 75, 24);
		}
		
		//	Draw the current highlight
		g2d.setColor(Color.GREEN);
		g2d.fillRect(20 + 75 * menu_indices[menu_selected], 100 * (menu_selected + 1) - 16, 75, 24);
		
		//	Draw the options
		g2d.setColor(Color.WHITE);
		g2d.drawString("Player 1 Type", 50, 75);
		for (int i = 0; i < menu[0].length; i++) {
			g2d.drawString(menu[0][i], 25 + 75 * i, 100);
		}
		g2d.drawString("Player 2 Type", 50, 175);
		for (int i = 0; i < menu[1].length; i++) {
			g2d.drawString(menu[1][i], 25 + 75 * i, 200);
		}
	}
	
	/**
	 * Paints the game
	 * @param g2d
	 */
	private void paintGame(Graphics2D g2d){ 
		//	Prepare to draw
		g2d.setColor(FOREGROUND_COLOR);
		
		//	Draw the game board's circles
		g2d.fillOval(origin_x - 2, origin_y - 2, 4, 4);
		for (int r = 0; r < 4; r++) {
			int offset = (1 + r) * (int)RADIUS_UNIT;
			g2d.drawOval(origin_x - offset, origin_y - offset , 2 * offset, 2 * offset);
		}
		
		//	Draw the game board's spokes
		g2d.drawLine(getXPixelFromLocation(3, 0) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 0) + SYMBOL_HEIGHT/2,getXPixelFromLocation(3, 6) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 6) + SYMBOL_HEIGHT/2);
		g2d.drawLine(getXPixelFromLocation(3, 1) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 1) + SYMBOL_HEIGHT/2,getXPixelFromLocation(3, 7) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 7) + SYMBOL_HEIGHT/2);
		g2d.drawLine(getXPixelFromLocation(3, 2) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 2) + SYMBOL_HEIGHT/2,getXPixelFromLocation(3, 8) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 8) + SYMBOL_HEIGHT/2);
		g2d.drawLine(getXPixelFromLocation(3, 3) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 3) + SYMBOL_HEIGHT/2,getXPixelFromLocation(3, 9) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 9) + SYMBOL_HEIGHT/2);
		g2d.drawLine(getXPixelFromLocation(3, 4) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 4) + SYMBOL_HEIGHT/2,getXPixelFromLocation(3, 10) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 10) + SYMBOL_HEIGHT/2);
		g2d.drawLine(getXPixelFromLocation(3, 5) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 5) + SYMBOL_HEIGHT/2,getXPixelFromLocation(3, 11) + SYMBOL_WIDTH/2, getYPixelFromLocation(3, 11) + SYMBOL_HEIGHT/2);
		
		//	Draw the plays
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 12; j++) {
				char at = game.peak(i, j);
				if (at == PolarTTT.EMPTY) {
					if (game.moveIsAvailable(new Location(i, j))){
						g2d.setColor(NEUTRAL_COLOR);
						g2d.drawOval(getXPixelFromLocation(i, j), getYPixelFromLocation(i, j), SYMBOL_WIDTH, SYMBOL_HEIGHT);
					}
				}
				else {
					g2d.setColor(at == PolarTTT.PLAYER1 ? P1_COLOR : P2_COLOR);
					g2d.fillOval(getXPixelFromLocation(i, j), getYPixelFromLocation(i, j), SYMBOL_WIDTH, SYMBOL_HEIGHT);
				}
			}
		}
		
		//	Prepare to draw the history
		boolean p1 = true;
		int xloc1 = (int)RADIUS_UNIT * 10,
			xloc2 = xloc1 + 64;
		
		g2d.setColor(FOREGROUND_COLOR);
		g2d.drawString("Move History", xloc1 + 24, 48);

		g2d.setColor(P1_COLOR);
		g2d.drawString("P1 History", xloc1, 64);
		g2d.setColor(P2_COLOR);
		g2d.drawString("P2 History", xloc2, 64);
		int i;
		
		//	Draw the history
		for (i = 0; i < 48; i++, p1 = !p1) {
			Location l = game.getNthMoveMade(i);
			
			//	Don't print if no move is made
			if (l == null) {
				break;
			}
			
			g2d.setColor(p1 ? P1_COLOR: P2_COLOR);
			g2d.drawString("(" + l.r +  ", " + l.t + ")", p1 ? xloc1 : xloc2, 16 * (i/2) + 96);
		}
		
		//	Draw the stats
		g2d.setColor(FOREGROUND_COLOR);
		g2d.drawString("Game Information", 650, 48);
		g2d.drawString("Player 1 Name:", 675, 96);
		g2d.drawString("Player 2 Name:", 675, 144);
		
		String p1name = game.getPlayerName(0),
			p2name = game.getPlayerName(1);
		
		//	Draw the status since we're still in White font
		switch (status) {
		case STATUS_IN_PROGRESS:
			g2d.drawString("Turn Number: " + (1 + i), 675, 224);
			g2d.setColor(i%2 == 0 ? P1_COLOR : P2_COLOR);
			g2d.drawString((i%2 == 0 ? p1name : p2name) + " to play.", 650, 256);
			break;
		case STATUS_WON:case STATUS_TIE:
			g2d.drawString("Results:", 200, 540);
			g2d.drawString("Enter to restart. Escape to quit.", 270, 560);
			g2d.setColor(status_color);
			g2d.drawString(information, 250, 540);
			break;
		}
		
		g2d.setColor(P1_COLOR);
		g2d.drawString(p1name, 660, 112);
		g2d.setColor(P2_COLOR);
		g2d.drawString(p2name, 660, 160);
		
	}
	
	/**
	 * Finds the pixel's x location on the screen
	 * @param radius
	 * @param theta
	 * @return The x-coordinate of the pixel
	 */
	private int getXPixelFromLocation(int radius, int theta) {
		return origin_x - SYMBOL_WIDTH/2 + (int)(Math.floor(((1 + radius) * RADIUS_UNIT) * Math.cos(THETACONVERSION[theta])));
	}
	
	/**
	 * Finds the pixel's y location on the screen
	 * @param radius
	 * @param theta
	 * @return The y-coordinate of the pixel
	 */
	private int getYPixelFromLocation(int radius, int theta) {
		return origin_y - SYMBOL_HEIGHT/2 - (int)(Math.floor(((1 + radius) * RADIUS_UNIT) * Math.sin(THETACONVERSION[theta])));
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
	
	//	The difference in angle between spokes
	private final static double THETA_UNIT = 0.5253;
	
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
	private final String[]
			Player1Types = {"Human", "Random", "C"},
			Player2Types = {"Human", "Random", "C"};
	private String[][] menu = {Player1Types, Player2Types};
	
	//	Modes determine which game is to be played
	private int mode;
	public static final int MODE_MENU = 0;
	public static final int MODE_GAME = 1;
	
	//	Use this information to display end conditions
	private int status;
	private String information;
	private Color status_color;
	public static final int STATUS_IN_PROGRESS = 0;
	public static final int STATUS_WON = 1;
	public static final int STATUS_TIE = 2;
}