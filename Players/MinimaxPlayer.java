package Players;

import Logic.Location;
import Logic.PolarTTT;

import java.awt.*;
import java.awt.event.*;

public class MinimaxPlayer extends Player {


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
		System.out.println("Number of plies: " + num_plies);
		System.out.println("Using Alpha-Beta: " + use_alpha_beta);
	}
	
	@Override
	public Location getChoice(Location[] options) {
		if (options.length == 48) {
			return options[(int)(Math.random() * 48)];
		}
		return null;
	}

	@Override
	public String getName() {
		return "Minimax " + num_plies + "p" + (use_alpha_beta ? "+AB" : "");
	}
	
	private Frame frame;
	private MinimaxCanvas canvas;
	public int num_plies = 1;
	public boolean use_alpha_beta, setup = false;
	public String[] menues = {"Ply count", "Use Alpha-Beta pruning?"};
	public int current_menu = 0;
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