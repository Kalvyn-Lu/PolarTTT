package Players;

import Logic.*;

public class HumanPlayer extends Player {
	/**
	 * Constructs a human player
	 */
	public HumanPlayer() {
	}
	
	@Override
	public Location getChoice(Location[] options) {
		
		//	Only accept user input that is a viable move
		Location l = null;
		do {
			l = getMouseInput();
		}
		while (!game.moveIsAvailable(l));
		return l;
	}
	
	@Override
	public String getName() {
		return "Human Player";
	}
	
}