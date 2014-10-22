package Source;
public abstract class Player {
	public PolarTTT game;
	private boolean mousereceived;
	private Location mouseLocation;
	/**
	 * Construct a player
	 */
	public Player() {}
	/**
	 * It's your turn; make a move
	 * @return The location to move
	 */
	public abstract Location getChoice();
	/**
	 * Gets the name to be displayed to the GUI of this player
	 * @return The name
	 */
	public abstract String getName();
	/**
	 * Reset the player to its defaults
	 */
	public void newGame(PolarTTT game) {
		//	Sets the game; this can't be done in constructor because of the circular logic problem
		this.game = game;
		mousereceived = false;
	}
	/**
	 * Prepare a player for a new turn. Does nothing unless overridden.
	 */
	public void newRound(){
	}
	/**
	 * Accept input from the game
	 * @param radius The ring the mouse enterred
	 * @param theta The spoke the mouse clicked on
	 */
	public void receiveMouseInput(int radius, int theta) {
		
		//	Break out of the mouse listen
		synchronized(this) {
			
			//	Set the location
			mouseLocation = new Location(radius, theta);
			
			//	Set that we received
			mousereceived = true;
			
			//	Notify- to break a listening input listener only if we're listening
			//	It won't do anything to our program if we aren't listening for the mouse.
			this.notifyAll();
		}
	}
	/**
	 * Listen for the mouse to click and then return the location closest to it.
	 * @return The location chosen
	 */
	public Location getMouseInput() {
		
		//	Wait on the mouse input
		synchronized(this) {
			
			//	Reset flag
			mousereceived = false;
			
			//	Listen for flag
			while (!mousereceived) {
				
				//	Wait for an interrupt
				try {
					this.wait();
				}
				
				//	This happens when the notify occurs
				catch (InterruptedException e) {
					break;
				}
			}
		}
		
		//	Now we can return the location of the mouse on the grid
		return mouseLocation;
	}
}

