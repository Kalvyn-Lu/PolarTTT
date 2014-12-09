package Players;

import Logic.*;

public class HumanPlayer extends Player {
	/**
	 * Constructs a human player
	 */
	public HumanPlayer(int fitness_mode) {
		super(fitness_mode);
	}
	
	@Override
	public Location getChoice(Location[] options) {
		
		Location move = null;
		do {
			
			//	Get the human's move
			move = getMouseInput();
		}
		
		//	Only accept user input that is a viable move
		while (!game.moveIsAvailable(move));
		
		//	Pass on that good move
		return move;
	}
	
	@Override
	public String getName() {
		return "Human Player";
	}
	
}