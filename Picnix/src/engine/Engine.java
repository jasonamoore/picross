package engine;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
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
import resource.bank.ImageBank;
import state.PuzzleState;
import state.State;

/**
 * Singleton class that handles all system-level jobs. Creates the native UI
 * instance (JFrame) and creates and holds game thread wrappers. Also holds a
 * state manager, which determines what the engine does.
 */
public class Engine {

	private static Engine engine;

	public static final int SCREEN_WIDTH = 480;
	public static final int SCREEN_HEIGHT = 416;

	private int displayScale;
	
	private String title = "Picnix";
	private JFrame frame;
	public Canvas canvas;

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
	 * Accessor for the engine's Canvas.
	 * @return The Canvas component, inside the JFrame used by the engine.
	 */
	public Canvas getCanvas() {
		return canvas;
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
	 * Initializes and configures the engine's default fields and necessary objects.
	 */
	private void init() {
		// create state manager
		//TODO THIS IS AN EXPLICIT TESTING PLUG-IN
		stateManager = new StateManager(new PuzzleState(new Puzzle(Puzzle.genPuzzle(5, 5))));
		// creates the JFrame
		frameInit();
		// load global resources
		ImageBank.loadGlobalResources();
		FontBank.loadGlobalResources();
		AudioBank.loadGlobalResources();
		// create and start thread managers
		executor = new Executor();
		renderer = new Renderer();
		speaker = new Speaker();
		executor.start();
		speaker.start();
		renderer.begin();
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

	/**
	 * Closes all running threads and cleanly
	 * exists the Java application.
	 */
	public void cleanExit() {
		try {
			executor.stop();
			renderer.stop();
			speaker.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
