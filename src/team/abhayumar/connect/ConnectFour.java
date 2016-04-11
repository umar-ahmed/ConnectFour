package team.abhayumar.connect;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import team.abhayumar.connect.Game.DIAGONAL;

/**
 * Class to handle main game execution and mouse events
 * 
 * @author Abhay, Umar
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ConnectFour extends Canvas implements Runnable, MouseListener, MouseMotionListener {

	// Initialize variables
	// Application's title, width, and height
	private static String TITLE = "Connect Four";
	private static int WIDTH = 900;
	private static int HEIGHT = 800;
	
	// Game board's attributes
	private static int BOARD_PADDING = 100;
	private static int BOARD_WIDTH = WIDTH - 2*BOARD_PADDING;
	private static int BOARD_HEIGHT = HEIGHT - 2*BOARD_PADDING;
	
	// Initialize objects
	private Game game;
	private Thread thread;
	private JFrame frame;
	private Screen screen;

	// Game related variables
	private int highlightColumn = -1;
	private int winningRow;
	private int winningColumn;

	// Application booleans
	private boolean running = false;
	private boolean isVolumeOn = true;
	private boolean shouldRender = false;

	// Game outcomes
	private enum OUTCOME {
		WINNER_P1,
		WINNER_P2,
		DRAW
	}
	private OUTCOME Outcome;
	
	// Application states
	private enum STATE {
		MAIN_MENU,
		INSTRUCTION,
		GAME,
		WINNER
	};
	private STATE State;

	// Sound object for application
	private Sound gameSound;

	// Time variables for winning path line rendering delay
	private final long PERIOD = 1000L;
	private long lastTime = System.currentTimeMillis() - PERIOD;

	private ArrayList<Animation> pieces = new ArrayList<Animation>();
	
	// Graphics and images to be used
	private Art p1 = new Art("res/p1.png", 100, 100);
	private Art p2 = new Art("res/p2.png", 100, 100);
	private Art bg = new Art("res/gameboard.png", 768, 680);
	private Art highlight = new Art("res/highlight.png", 100, BOARD_HEIGHT);
	private Art turnP1 = new Art("res/turnP1.png", 100, 60);
	private Art turnP2 = new Art("res/turnP2.png", 100, 60);
	private Art newGameBtn = new Art("res/newgamebutton.png", 300, 100);
	private Art instructionsBtn = new Art("res/instructionsbutton.png", 300, 100);
	private Art instructionsText = new Art("res/instructions.png", 900, 800);
	private Art winnerp1 = new Art("res/winnerp1.png", 900, 800);
	private Art winnerp2 = new Art("res/winnerp2.png", 900, 800);
	private Art draw = new Art("res/draw.png", 900, 800);
	private Art logo = new Art("res/Logo.png", 765, 206);
	private Art volumeOn = new Art("res/volumeOn.png", 48, 48);
	private Art volumeOff = new Art("res/volumeOff.png", 48, 48);
	private Art nameCredit = new Art("res/namecredit.png", 493, 41);
	private Art home = new Art("res/home.png", 48, 48);
	private Art restart = new Art("res/restart.png", 48, 48);
	private Art verticalLine = new Art("res/vertical line.png", 100, 400);
	private Art horizontalLine = new Art("res/horizontal line.png", 400, 100);
	private Art diagonalRight = new Art("res/diagonal line right.png", 400, 400);
	private Art diagonalLeft = new Art("res/diagonal line left.png", 400, 400);

	/**
	 * Empty constructor.
	 */
	public ConnectFour() {

	}

	/**
	 * Start game thread.
	 */
	public void start() {
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Stop game thread.
	 */
	public void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run thread.
	 */
	@Override
	public void run() {
		// Initialize thread variables
		init();
		long lastTime = System.nanoTime();
		final double TARGET_FPS = 60.0;
		final double ns = 1000000000.0 / TARGET_FPS;
		double delta = 0;
		int updates = 0;
		int frames = 0;
		long timer = System.currentTimeMillis();

		// Game loop to render and update every given time increment
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;

			if (System.currentTimeMillis() - timer >= 1000) {
				timer += 1000;
				frame.setTitle(TITLE + " | " + updates + " UPS | " + frames + " FPS");
				updates = 0;
				frames = 0;
			}
		}
		stop();
	}
	
	/**
	 * Initialize application.
	 */
	public void init() {
		// Set canvas attributes
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));

		// Frame attributes
		frame = new JFrame(TITLE);
		ImageIcon img = new ImageIcon("res/icon.png");
		frame.setIconImage(img.getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
		
		// Set initial state to main menu
		State = STATE.MAIN_MENU;

		// Play background music
		gameSound = new Sound("res/test.wav", true);

		// Create new screen and game objects
		screen = new Screen(WIDTH, HEIGHT);
		game = new Game();

		// Add mouse event listeners
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * Method to execute rendering of pixels.
	 */
	public void render() {
		
		// Create new buffered strategy and graphics objects
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			requestFocus();
			return;
		}
		Graphics g = bs.getDrawGraphics();

		// Clear screen
		screen.clear();

		// Rendering for main menu
		if (State == STATE.MAIN_MENU) {

			// Volume toggle icon
			screen.fill(0xFFC107);
			if (isVolumeOn) {
				screen.render(volumeOn, 827, 20);
			} else {
				screen.render(volumeOff, 827, 20);
			}
			
			// Render main menu graphics and buttons
			screen.render(logo, (WIDTH - logo.getWidth()) / 2, 100);
			screen.render(nameCredit, (WIDTH - nameCredit.getWidth()) / 2, 700);
			screen.render(newGameBtn, (WIDTH - newGameBtn.getWidth()) / 2, 350);
			screen.render(instructionsBtn, (WIDTH - instructionsBtn.getWidth()) / 2, 475);
			
		// Rendering for instructions screen	
		} else if (State == STATE.INSTRUCTION) {
			// Fill background and render instructions
			screen.fill(0xFFC107);
			screen.render(instructionsText, 0, 0);
			
		// Rendering for winner screen
		} else if (State == STATE.WINNER) {
			// Render screen depending on who wins
			if (Outcome == OUTCOME.WINNER_P1) {
				screen.render(winnerp1, 0, 0);
			} else if (Outcome == OUTCOME.WINNER_P2) {
				screen.render(winnerp2, 0, 0);
			} else if (Outcome == OUTCOME.DRAW) {
				screen.render(draw, 0, 0);
			}
		}
		
		// Rendering for actual game screen
		else if (State == STATE.GAME) {
			// Fill background
			screen.fill(0x2196F3);

			// Draw board
			screen.render(bg, (WIDTH - bg.getWidth()) / 2, (HEIGHT - bg.getHeight()) / 2);

			// Draw player turn
			if (game.turn.getID() == 1) {
				screen.render(turnP1, 0, 0);
			} else if (game.turn.getID() == 2) {
				screen.render(turnP2, 0, 0);
			}

			// Highlight column
			if (highlightColumn != -1) {
				screen.render(highlight, highlightColumn * 100 + BOARD_PADDING, BOARD_PADDING);
			}

			// Volume toggle icon
			if (isVolumeOn) {
				screen.render(volumeOn, 827, 20);
			} else {
				screen.render(volumeOff, 827, 20);
			}

			// Home icon
			screen.render(home, 760, 20);

			// Restart icon
			screen.render(restart, 693, 20);

			// Draw game pieces
			for (int i = 0; i < pieces.size(); i++) {
				screen.render(pieces.get(i).getArt(), pieces.get(i).getX(), pieces.get(i).getY());
			}
			
			// Draw winning path line when game has a winner
			if (shouldRender) {

				// Vertical line
				if (game.hasVerticalWinner()) {
					screen.render(verticalLine, (game.winningColumn * 100), (game.winningRow * 100));
				}
				// Horizontal Line
				else if (game.hasHorizontalWinner()) {
					screen.render(horizontalLine, (game.winningColumn * 100), (game.winningRow * 100));
				}
				// Diagonal Line
				else if (game.hasDiagonalWinner()) {
					if (game.getDiagonalState() == DIAGONAL.TOP_LEFT) {
						screen.render(diagonalLeft, (game.winningColumn * 100), (game.winningRow * 100));
					} else if (game.getDiagonalState() == DIAGONAL.TOP_RIGHT) {
						screen.render(diagonalRight, (game.winningColumn * 100), (game.winningRow * 100));
					} else if (game.getDiagonalState() == DIAGONAL.BOTTOM_LEFT) {
						screen.render(diagonalRight, (game.winningColumn * 100), (game.winningRow * 100) - 300);
					} else if (game.getDiagonalState() == DIAGONAL.BOTTOM_RIGHT) {
						screen.render(diagonalLeft, (game.winningColumn * 100) - 300, (game.winningRow * 100) - 300);
					}
				}

			}
		}

		// Render collective frame
		g.drawImage(screen.image, 0, 0, WIDTH, HEIGHT, null);
		g.dispose();
		bs.show();
	}

	/**
	 * Logic for piece dropping animations.
	 */
	public void update() {
		
		// Update piece coordinates for dropping animation
		if (State == STATE.GAME) {
			for (int i = 0; i < pieces.size(); i++) {
				pieces.get(i).update();
			}
			// If winner is present, allow for winning path line to be drawn after drop animation is done
			if (shouldRenderWinLine()) {
				long thisTime = System.currentTimeMillis();
				while ((thisTime - lastTime) <= PERIOD){
					thisTime = System.currentTimeMillis();
				}
				if ((thisTime - lastTime) >= PERIOD) {
					lastTime = thisTime;
					shouldRender = true;
				}
			}
		}
	}
	

	/**
	 * Determine if winning path line should draw.
	 * 
	 * @return whether line should draw or not
	 */
	public boolean shouldRenderWinLine() {
		int counter = 0;
		
		// Check if all piece animations are done
		for (int i = 0; i < pieces.size(); i++) {
			if (pieces.get(i).isAnimationDone()) {
				counter++;
			}
			if (counter == pieces.size() && game.hasWinner()) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Mouse click event.
	 * 
	 * @Override
	 */
	public void mouseClicked(MouseEvent e) {
		
		// Get x and y coordinates of click
		int x = e.getX();
		int y = e.getY();
		
		// Set potential winning row and column
		winningRow = Math.round(y / 100);
		winningColumn = Math.round(x / 100);

		// Click logic for main menu
		if (State == STATE.MAIN_MENU) {
			
			// Toggle volume mute
			if (x > 827 && y > 20 && x < 875 && y < 68) {
				gameSound.toggleVolume();
				if (isVolumeOn) {
					isVolumeOn = false;
				} else {
					isVolumeOn = true;
				}
			}
			
			// Start new game button
			if (x > ((WIDTH - 300) / 2) && y > 350 && x < ((WIDTH + 300) / 2) && y < 350 + newGameBtn.getHeight()) {
				new Sound("res/drop.wav", false);
				game.runGame();
				State = STATE.GAME;
			} 
			
			// Go to instructions screen button 
			else if (x > ((WIDTH - 300) / 2) && y > 475 && x < ((WIDTH + 300) / 2)
					&& y < 475 + instructionsBtn.getHeight()) {
				new Sound("res/drop.wav", false);
				State = STATE.INSTRUCTION;
			}

		// Click logic for instructions screen
		} else if (State == STATE.INSTRUCTION) {
			
			// Go back to main menu button
			if (x > 35 && y > 45 && x < 140 && y < 85) {
				new Sound("res/drop.wav", false);
				State = STATE.MAIN_MENU;
				
			// Start new game button
			} else if (x > 625 && y > 45 && x < 865 && y < 85) {
				new Sound("res/drop.wav", false);
				game.runGame();
				State = STATE.GAME;
			}

		// Click logic for instructions screen
		} else if (State == STATE.GAME) {

			// Volume mute toggle
			if (x > 827 && y > 20 && x < 875 && y < 68) {
				gameSound.toggleVolume();
				if (isVolumeOn) {
					isVolumeOn = false;
				} else {
					isVolumeOn = true;
				}
			}
			
			// Main menu link
			if (x > 760 && y > 20 && x < 808 && y < 68) {
				new Sound("res/drop.wav", false);
				int option = JOptionPane.showConfirmDialog(null, new JLabel("Are you sure?"), "Quit Game",
						JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) {
					game.reset();
					pieces.clear();
					State = STATE.MAIN_MENU;
				}
			}

			// Restart game link
			if (x > 693 && y > 20 && x < 741 && y < 68) {
				new Sound("res/drop.wav", false);
				int option = JOptionPane.showConfirmDialog(null, new JLabel("Are you sure?"), "Restart Game",
						JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) {
					game.reset();
					pieces.clear();
					shouldRender = false;
				}
			}

			// Within board bounds
			if (x >= BOARD_PADDING && x <= BOARD_WIDTH+BOARD_PADDING && y >= BOARD_PADDING && y <= BOARD_HEIGHT+BOARD_PADDING) {
				
				// Set variables for game mechanics
				int column = Math.round((x - BOARD_PADDING) / 100);
				int player = game.turn.getID();
				int status = game.updateBoard(column);

				// If cell is unoccupied
				if (status != -1) {
					
					// Play piece drop sound
					new Sound("res/drop.wav", false);
					
					// Add player piece
					if (player == 1) {
						pieces.add(new Animation(p1, column * 100 + BOARD_PADDING, 100, column * 100 + BOARD_PADDING, status * 100 + BOARD_PADDING));
					} else if (player == 2) {
						pieces.add(new Animation(p2, column * 100 + BOARD_PADDING, 100, column * 100 + BOARD_PADDING, status * 100 + BOARD_PADDING));
					}
					
					// If game is not finished, proceed to next turn
					if (!game.hasWinner() && !game.hasDraw()) {
						game.nextTurn();
					}
				}

				// If game has a winner
				if (game.hasWinner()) {
					
					// Pause game
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					// Change state depending on winner
					State = STATE.WINNER;

					if (game.turn.getID() == 1) {
						Outcome = OUTCOME.WINNER_P1;
					} else if (game.turn.getID() == 2) {
						Outcome = OUTCOME.WINNER_P2;
					}

					// Reset game
					game.reset();
					pieces.clear();
					shouldRender = false;

				// If game has a draw
				} else if (game.hasDraw()) {
					// Reset game board
					State = STATE.WINNER;
					Outcome = OUTCOME.DRAW;
					game.reset();
					pieces.clear();
					shouldRender = false;
				}
			}
		}
		
		// Click logic for winner screen
		else if (State == STATE.WINNER) {
			
			// Main menu button
			if (x > 110 && y > 560 && x < 300 && y < 680) {
				new Sound("res/drop.wav", false);
				State = STATE.MAIN_MENU;
			
			// Rematch button to reset game board
			} else if (x > 500 && y > 600 && x < 790 && y < 650) {
				new Sound("res/drop.wav", false);
				State = STATE.GAME;
				game.reset();
				pieces.clear();
				shouldRender = false;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {}

	/**
	 * Method for mouse movement.
	 * 
	 * @Override
	 */
	public void mouseMoved(MouseEvent e) {
		// Get x and y coordinates of mouse
		int x = e.getX();
		int y = e.getY();

		// Hovering logic for column highlight
		if (State == STATE.GAME) {
			if (x >= BOARD_PADDING && x < BOARD_WIDTH+BOARD_PADDING && y >= BOARD_PADDING && y < BOARD_HEIGHT+BOARD_PADDING) {
				int column = Math.round((x - BOARD_PADDING) / 100);
				highlightColumn = column;
			} else {
				highlightColumn = -1;
			}
		}
	}
}