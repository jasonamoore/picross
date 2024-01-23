package engine;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import engine.thread.Executor;
import engine.thread.Renderer;
import engine.thread.Speaker;
import puzzle.Puzzle;
import resource.bank.AudioBank;
import resource.bank.FontBank;
import resource.bank.SpriteBank;
import state.PuzzleState;
import state.State;

/**
 * Singleton class that handles all system-level jobs. Creates the native UI
 * instance (JFrame) and creates and holds game thread wrappers. Also holds a
 * state manager, which determines what the engine does.
 */
public class Engine {

	private static Engine engine;

	public static final int SCREEN_WIDTH = 400;
	public static final int SCREEN_HEIGHT = 400;

	private int displayScale;
	private int displayWidth;
	private int displayHeight;
	private int displayOffsetX;
	private int displayOffsetY;
	
	private String title = "";
	private JFrame frame;

	private Executor executor;
	private Renderer renderer;
	private Speaker speaker;

	private StateManager stateManager;

	// private Stack<State> stateStack;

	/**
	 * Private constructor to prevent outside initialization.
	 */
	private Engine() {}
	
	/**
	 * @return Returns the singleton instance.
	 */
	public static Engine getEngine() {
		return engine;
	}
	
	/**
	 * Accessor for the engine's JFrame.
	 * @return The JFrame used by the engine.
	 */
	public JFrame getFrame() {
		return frame;
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
		return displayWidth;
	}
	
	/**
	 * Accessor for the display height.
	 * @return The height of this engine's display.
	 */
	public int getDisplayHeight() {
		return displayHeight;
	}
	
	/**
	 * Accessor for the horizontal display offset.
	 * @return The horizontal offset of this engine's display.
	 */
	public int getDisplayOffsetX() {
		return displayOffsetX;
	}
	
	/**
	 * Accessor for the vertical display offset.
	 * @return The vertical offset of this engine's display.
	 */
	public int getDisplayOffsetY() {
		return displayOffsetY;
	}

	/**
	 * Provides the currently active state.
	 * @return The current state of the engine's {@link StateManager}.
	 */
	public State getActiveState() {
		return stateManager.getTopState();
	}
	
	/**
	 * Called by the ResizeListener when the JFrame is resized.
	 * @param e The ComponentEvent received when the JFrame's inner content pane was resized.
	 */
	public void frameSizeChanged(ComponentEvent e) {
		// do stuff to update the screen
		updateDisplay();
	}
	
	public void maximizeDisplay(Insets insets) {
		// calculate frame size
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration config = gd.getDefaultConfiguration();
		Rectangle bounds = config.getBounds();
		// increase frame size as big as possible while fitting in display bounds
		displayScale = 1;
		displayWidth = SCREEN_WIDTH;
		displayHeight = SCREEN_HEIGHT;
		while (displayWidth * displayScale + insets.left + insets.right <= bounds.getWidth()
				&& displayHeight * displayScale + insets.top + insets.bottom <= bounds.getHeight()) {
			displayScale++;
		}
		// fit to the last valid scale
		displayScale--;
		displayWidth = SCREEN_WIDTH * displayScale;
		displayHeight = SCREEN_HEIGHT * displayScale;
		// set size plus insets, so window content size = width and height
		frame.setSize(insets.left + insets.right + displayWidth, insets.top + insets.bottom + displayHeight);
	}
	
	/**
	 * Called to update the screen size and rendering hints.
	 * Usually called when the JFrame is resized, so the
	 * optimal display size can be calculated.
	 */
	private void updateDisplay() {
		Insets insets = frame.getInsets();
		// first get inner size (frame size without insets)
		int innerWidth = frame.getWidth() - insets.left - insets.right;
		int innerHeight = frame.getHeight() - insets.top - insets.bottom;
		// now determine the largest screen scaling that can fit in this window]
		// increase frame size as big as possible while fitting in display bounds
		displayScale = 1;
		displayWidth = SCREEN_WIDTH;
		displayHeight = SCREEN_HEIGHT;
		while (displayWidth * displayScale <= innerWidth
				&& displayHeight * displayScale <= innerHeight) {
			displayScale++;
		}
		// fit to the last valid scale
		displayScale--;
		displayWidth = SCREEN_WIDTH * displayScale;
		displayHeight = SCREEN_HEIGHT * displayScale;
		// determine display hints
		displayOffsetX = Math.max(0, (innerWidth - displayWidth) / 2);
		displayOffsetY = Math.max(0, (innerHeight - displayHeight) / 2);
	}

	/**
	 * Creates and configures the native window (JFrame).
	 */
	private Insets frameInit() {
		frame = new JFrame(title);
		// make the window appear
		frame.setVisible(true);
		// set JFrame attributes
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		return frame.getInsets();
	}
	
	/**
	 * Draws a splash screen and makes the window visible.
	 * @throws IOException If the splash image cannot be loaded.
	 */
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
	
	/**
	 * Sets the size and position of the frame,
	 * and provides necessary input and window listeners.
	 */
	private void guiInit(Insets insets) {
		frame = new JFrame(title);
		// adjust size to fit user's display
		maximizeDisplay(insets);
		// set JFrame attributes
		frame.setLocationRelativeTo(null);
		// add input listeners
		Input input = Input.getInstance();
		frame.addKeyListener(input);
		frame.addMouseListener(input);
		frame.addMouseMotionListener(input);
		frame.addMouseWheelListener(input);
		// add window listeners here
		// frame.addWindowListener...
		frame.getContentPane().addComponentListener(new ResizeListener());
		frame.setAutoRequestFocus(true);
		// make the window appear
		frame.setVisible(true);
	}
	
	/**
	 * Initializes and configures the engine's default fields and necessary objects.
	 */
	private void init() {
		// create state manager
		//TODO THIS IS AN EXPLICIT TESTING PLUG-IN
		stateManager = new StateManager(new PuzzleState(new Puzzle(Puzzle.genPuzzle(5, 5))));
		// create the three thread managers
		executor = new Executor();
		renderer = new Renderer();
		speaker = new Speaker();
		// initialize input singleton
		Input.createInstance();
		// creates the JFrame
		Insets insets = frameInit();
		// draw splash screen (show window)
		try {
			splash();
		} catch (IOException ioe) {
			// could not load splash
		}
		// gets ready for displaying the game
		guiInit(insets);
		// load global resources
		SpriteBank.loadGlobalResources();
		FontBank.loadGlobalResources();
		AudioBank.loadGlobalResources();
		// start threads
		executor.start();
		renderer.start();
		speaker.start();
	}
	
	/**
	 * Creates the engine instance and calls {@link #init()} on it.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		engine = new Engine();
		engine.init();
	}

}
