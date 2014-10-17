package Players;

import Logic.*;

public abstract class Player {
	public PolarTTT game;
	protected boolean mousereceived;
	protected Location mouseLocation;
	/**
	 * Construct a player
	 */
	public Player() {}
	/**
	 * It's your turn; make a move
	 * @return The location to move
	 */
	public abstract Location getChoice();
	public abstract String getName();
	/**
	 * Reset the player to its defaults
	 */
	public void newGame(PolarTTT game) {
		this.game = game;
		mousereceived = false;
	}
	/**
	 * Prepare a player for a new turn
	 */
	public void newRound(){
//		mousereceived = false;
	}
	/**
	 * Accept input from the game
	 * @param radius The ring the mouse enterred
	 * @param theta The spoke the mouse clicked on
	 */
	public void receiveMouseInput(int radius, int theta) {
		synchronized(this) {
			mouseLocation = new Location(radius, theta);
			mousereceived = true;
			this.notifyAll();
		}
	}
	/**
	 * Listen for the mouse to click and then return the location closest to it.
	 * @return The location chosen
	 */
	public Location getMouseInput() {
		synchronized(this) {
			mousereceived = false;
			while (!mousereceived) {
				try {
					this.wait();
				}
				catch (InterruptedException e) {
					break;
				}
			}
		}
		return mouseLocation;
	}
}

