package Players;

import Logic.*;

public class HumanPlayer extends Player {
	public HumanPlayer() {
	}
	
	@Override
	public Location getChoice() {
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