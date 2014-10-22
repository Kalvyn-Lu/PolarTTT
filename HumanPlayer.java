public class HumanPlayer extends Player {
	public HumanPlayer() {
	}
	
	@Override
	public Location getChoice() {
		
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