package engine;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;

import java.io.IOException;

import javax.swing.JFrame;

import resource.bank.AudioBank;
import resource.bank.FontBank;
import resource.bank.ImageBank;
import state.State;
import state.TitleState;

/**
 * Singleton class that handles all system-level jobs. Creates the native UI
 * instance (JFrame) and creates and holds game thread wrappers. Also holds a
 * state manager, which determines what the engine does.
 */
public class Engine {

	// singleton instance
	private static Engine engine;

	// width and height of the internal game display
	public static final int SCREEN_WIDTH = 480;
	public static final int SCREEN_HEIGHT = 416;
	// the scale of the game display in the JFrame
	private int displayScale;

	// target time for updates/sec; cap for max frames to render/second
	public static final double TICK_TIME = 1_000_000_000 / 45.0;
	public static final double RENDER_CAP = 1_000_000_000 / 60.0;
	
	// JFrame title
	private String title = "Picnix";
	// JFrame and Canvas component for drawing
	private JFrame frame;
	public Canvas canvas;

	// instance of state manager, holding a stack of game states
	private StateManager stateManager;

	// whether the Engine should run updates
	private boolean running;
	
	/**
	 * Private constructor to prevent outside initialization.
	 */
	private Engine() {}
	
	/**
	 * @return Returns the Engine singleton instance.
	 */
	public static Engine getEngine() {
		return engine;
	}
	
	/**
	 * Mutator for the JFrame title. Calls setTitle with the
	 * given String.
	 * @param newTitle The new title for the JFrame.
	 */
	public void changeFrameTitle(String newTitle) {
		title = newTitle;
		frame.setTitle(title);
	}
	
	/**
	 * Returns the scale factor of the engine's display.
	 * This is the ratio of the display size to the
	 * original game's screen size.
	 * @return
	 */
	public int getDisplayScale() {
		return displayScale;
	}
	
	/**
	 * Accessor for the display width.
	 * @return The width of this engine's display.
	 */
	public int getDisplayWidth() {
		return SCREEN_WIDTH * displayScale;
	}
	
	/**
	 * Accessor for the display height.
	 * @return The height of this engine's display.
	 */
	public int getDisplayHeight() {
		return SCREEN_HEIGHT * displayScale;
	}

	/**
	 * Opens a new state by requesting the
	 * {@link StateManager} to add this state to its stack.
	 * @param state The new state to open.
	 */
	public void openState(State state) {
		stateManager.openState(state);
	}
	
	/**
	 * Closes the current state by requesting the
	 * {@link StateManager} to pop this state off its stack.
	 */
	public void exitTopState() {
		stateManager.exitTopState();
	}

	/**
	 * Provides the currently active state.
	 * @return The current state of the engine's {@link StateManager}.
	 */
	public State getActiveState() {
		return stateManager.getTopState();
	}
	
	/**
	 * Sets the size of the Canvas component by calling
	 * setMinimumSize, setMaximumSize, and setPreferredSize.
	 */
	public void setCanvasSize(int width, int height) {
		Dimension size = new Dimension(width, height);
		canvas.setMinimumSize(size);
		canvas.setPreferredSize(size);
		canvas.setMaximumSize(size);
	}
	
	/**
	 * Maximizes the display scale to be as large as possible
	 * while fitting in the current display device.
	 */
	public void maximizeCanvas() {
		// get user display size
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration config = gd.getDefaultConfiguration();
		Rectangle bounds = config.getBounds();
		// increase frame size as big as possible while fitting in display bounds
		displayScale = 1;
		while (getDisplayWidth() <= bounds.getWidth()
				&& getDisplayHeight() <= bounds.getHeight()) {
			displayScale++;
		}
		// fit to the last valid scale
		displayScale--;
		// set size plus insets, so window content size = width and height
		setCanvasSize(getDisplayWidth(), getDisplayHeight());
		frame.pack();
	}

	/**
	 * Creates and configures the native window (JFrame). Also
	 * creates the Canvas, maximizing it to fit the user's display
	 * and packing it into the JFrame. Finally, adds various listeners
	 * to the frame for input and window events.
	 */
	private void frameInit() {
		// create JFrame
		frame = new JFrame(title);
		// set up Canvas component
		canvas = new Canvas();
		frame.add(canvas);
		// maximize the canvas scale
		maximizeCanvas();
		// frame settings
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setAutoRequestFocus(true);
		// add input listeners
		Input input = Input.getInstance();
		canvas.addKeyListener(input);
		canvas.addMouseListener(input);
		canvas.addMouseMotionListener(input);
		canvas.addMouseWheelListener(input);
		// add window listener
		frame.addWindowListener(new EngineWindowListener());
		// make the window appear
		frame.setVisible(true);
	}
	
	/*
	/**
	 * Draws a splash screen and makes the window visible.
	 * @throws IOException If the splash image cannot be loaded.
	 /
	private void splash() throws IOException {
		Insets insets = frame.getInsets();
		frame.setSize(SCREEN_WIDTH + insets.left + insets.right,
				SCREEN_HEIGHT + insets.top + insets.bottom);
		frame.setLocationRelativeTo(null);
		// draw splash image on the screen
		InputStream splashRes = Engine.class.getResourceAsStream("/splash.png");
		if (splashRes == null)
			throw new IOException("No splash image to load");
		BufferedImage splash = ImageIO.read(splashRes);
		Graphics g = frame.getGraphics();
		g.drawImage(splash, insets.left, insets.top, null);
		g.dispose();
		// artificial splash waiting time
		final int WAIT_TIME = 1000;
		try {
			Thread.sleep(WAIT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		frame.setVisible(false);
	}
	*/
	
	/**
	 * Initializes and configures the engine's default fields and necessary objects.
	 */
	private void init() {
		// creates the JFrame
		frameInit();
		// load global resources
		try {
			ImageBank.loadGlobalResources();
			FontBank.loadGlobalResources();
			AudioBank.loadGlobalResources();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// create state manager
		//TODO THIS IS AN EXPLICIT TESTING PLUG-IN
		//stateManager = new StateManager(new PuzzleState(new Puzzle(Puzzle.genPuzzle(5, 5))));
		stateManager = new StateManager(new TitleState());
		// enter game loop
		running = true;
		loop();
	}
	
	/**
	 * The main game loop, which handles both logic
	 * updates and game rendering.
	 * 
	 * Updates (ticks) are performed as needed to be
	 * consistent with the target update/second rate.
	 * Rendering is done when possible,
	 * between batches of ticks.
	 */
	private void loop() {
		final int SLEEP = 4; //(int) Math.floor((RENDER_CAP / 1_000_000) / 2);
		// game loop stuff
		long lastTime = System.nanoTime();
		double queuedTicks = 0;
		
		while (running) {
			// calculate unprocessed ticks
			long now = System.nanoTime();
			queuedTicks += (now - lastTime) / TICK_TIME;
			// do ticks
			while (queuedTicks >= 1) {
				getActiveState().tick();
				queuedTicks -= 1;
			}
			// render setup
			BufferStrategy bs = canvas.getBufferStrategy();
			if (bs == null) {
				canvas.createBufferStrategy(2);
				continue;
			}
	        VolatileImage image = canvas.createVolatileImage(Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
	        
            Graphics g = image.getGraphics();
            // render game
			State state = engine.getActiveState();
			g.setColor(Color.PINK);
			g.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
			state.render(g);
            g.dispose();
            // present frame
            g = bs.getDrawGraphics();
            g.drawImage(image, 0, 0, engine.getDisplayWidth(), engine.getDisplayHeight(), null);
            g.dispose();
            bs.show();
            
            // sleep to save CPU for next cycle
            try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates the engine instance and calls {@link #init()} on it.
	 */
	public static void main(String[] args) {
		engine = new Engine();
		engine.init();
	}

	/**
	 * Cleanly exits by disposing the JFrame
	 * and exiting the program.
	 */
	public void cleanExit() {
		//frame.dispose();
		System.exit(0);
	}

	/**
	 * Private inner class to handle window closing event.
	 * Calls {@link Engine#cleanExit()} on window close.
	 */
	private class EngineWindowListener implements WindowListener {
		@Override
		public void windowOpened(WindowEvent e) {
		}
		
		@Override
		public void windowClosing(WindowEvent e) {
			cleanExit();
		}
		
		@Override
		public void windowClosed(WindowEvent e) {
		}
		
		@Override
		public void windowIconified(WindowEvent e) {
		}
		
		@Override
		public void windowDeiconified(WindowEvent e) {
		}
		
		@Override
		public void windowActivated(WindowEvent e) {
		}
		
		@Override
		public void windowDeactivated(WindowEvent e) {
		}
	}
	
}
