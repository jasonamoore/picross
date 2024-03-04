package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Input;
import picnix.Level;
import picnix.World;
import picnix.puzzle.Puzzle;
import resource.bank.ImageBank;
import util.Animation;
import util.Timer;

public class WinState extends State {

	private static final double HEIGHT_WIDTH_RATIO = 0.5;
	private static final int TOPDOWN_FINAL_HEIGHT = Engine.SCREEN_HEIGHT - 50;
	private static final int CELL_SIZE = 35;
	
	private static final double CAM_SWITCH_TIME = 2000;
	private static final int TOPDOWN_DILATION = 2;
	private static final double CAM_MIN_SCALE = 2;
	private static final double CAM_MAX_SCALE = 5;
	
	private static final int SIMULATION_DURATION = 30000;
	
	private World world;
	private Level level;
	private Puzzle puzzle;

	private int rows, cols;
	private int width, height;
	
	private double camX, camY;
	private double camScale = 1;
	
	private Timer simTimer;
	private Timer camTimer;
	private boolean simulating;
	
	private boolean topdown;
	private double topdownScale;
	
	private Animation smoothRot;

	private static final int ROT = 0;
	private static final int RAD = 1;
	private static final int DELAY = 2;
	private static final int STOP = 3;

	private static final int ANTS_PER_PLATE = 30;
	private static final int MAX_DELAY = 10000;
	private double[][] plates;
	private double[][] ants;
	
	public WinState(World world, Level level) {
		this.world = world;
		this.level = level;
		puzzle = level.getPuzzles()[0];
		rows = level.getPuzzles()[0].getRows();
		cols = level.getPuzzles()[0].getColumns();
		width = cols * CELL_SIZE;
		height = rows * CELL_SIZE;
		topdownScale = TOPDOWN_FINAL_HEIGHT / height;
		simTimer = new Timer(false);
		camTimer = new Timer(false);
		smoothRot = new Animation(0, 2 * Math.PI, SIMULATION_DURATION, Animation.EASE_OUT, Animation.NO_LOOP, true);
		// plates for the filled cells
		plates = new double[puzzle.getFilledCellsInSolution()][2];
		// create ants around the edges
		int numAnts = plates.length * ANTS_PER_PLATE;
		ants = new double[numAnts][4];
		int i = 0;
		for (int r = 0; r < puzzle.getRows(); r++) {
			for (int c = 0; c < puzzle.getColumns(); c++) {
				if (puzzle.isFilledInSolution(r, c)) {
					double px = c * CELL_SIZE - width / 2 + CELL_SIZE / 2;
					double py = r * CELL_SIZE - height / 2 + CELL_SIZE / 2;
					double angle;
					if (px == 0)
						angle = py > 0 ? Math.PI / 2 : 3 * Math.PI / 2;
					else								 // reflect angle
						angle = Math.atan(py / px) + (px < 0 ? Math.PI : 0);
					double dist = Math.sqrt(px * px + py * py);
					plates[i][ROT] = angle;
					plates[i][RAD] = dist;
					// make the ants for this plate
					for (int a = 0; a < ANTS_PER_PLATE; a++) {
						// find its target point
						int aid = i * ANTS_PER_PLATE + a;
						double abspx = px * topdownScale + Engine.SCREEN_WIDTH / 2;
						double abspy = py * topdownScale + Engine.SCREEN_HEIGHT / 2;
						// tell this ant where to start from
						ants[aid][ROT] = Math.random() * 2 * Math.PI; // any angle
						// the nearest edge, plus some random factor
						ants[aid][RAD] = getMinimumRadiusToEdge(abspx, abspy, ants[aid][ROT]) / topdownScale;
						ants[aid][DELAY] = Math.random() * MAX_DELAY;
						ants[aid][STOP] = Math.random() * CELL_SIZE * 0.5;
					}
					// next plate
					i++;
				}
			}
		}
	}

	@Override
	public void focus(int status) {
		startSimulation();
	}
	
	private void startSimulation() {
		simulating = true;
		simTimer.resume();
		camTimer.resume();
		goTopDown();
	}
	
	private void stopSimulation() {
		simulating = false;
		goTopDown();
	}
	
	private double getMinimumRadiusToEdge(double x, double y, double theta) {
		// slope of the angle
		double m = Math.tan(theta); // regular slope for left/right
		double m2 = 1.0 / m; // cotangent - for top/bottom
		// width to the right; height to the bottom
		double xo = Engine.SCREEN_WIDTH - x;
		double yo = Engine.SCREEN_HEIGHT - y;
		//System.out.println("x: " + x + " y: " + y + " xo: " + xo + " yo: " + yo);
		// find where this angle would intercept each side
		double left = y - x * m;
		double top = x - y * m2;
		double right = y + xo * m;
		double bottom = x + yo * m2;
		//System.out.println("l: " + left + " t: " + top + " r: " + right + " b: " + bottom);
		// find distance from here to each intercept point
		double leftDist = Math.sqrt(x * x + Math.pow(y - left, 2));
		double topDist = Math.sqrt(y * y + Math.pow(x - top, 2));
		double rightDist = Math.sqrt(xo * xo + Math.pow(y - right, 2));
		double bottomDist = Math.sqrt(yo * yo + Math.pow(x - bottom, 2));
		//System.out.println("ld: " + leftDist + " td: " + topDist + " rd: " + rightDist + " bd: " + bottomDist);
		// find the two sides relevant to this angle
		double distA = theta >= Math.PI / 2 && theta < 3 * Math.PI / 2 ? leftDist : rightDist;
		double distB = theta >= 0 && theta < Math.PI ? bottomDist : topDist;
		//System.out.println("a: " + distA + "b: " + distB);
		// find minimum of side distances (20 grace)
		return Math.min(distA, distB) + 20;
	}
	
	private void goTopDown() {
		camX = camY = 0;
		camScale = topdownScale;
		topdown = true;
	}
	
	private void goRandomIsometric() {
		final int posRandThresh = height / 5;
		final int sclRandThresh = 1;
		double oldCamX = camX;
		double oldCamY = camY;
		double oldCamScale = camScale;
		do {
			// focus on any point in the blanket - but make sure random point changes enough
			camX = Math.random() * width - width / 2;
			camY = Math.random() * height - height / 2;
			camScale = (int) (Math.random() * (CAM_MAX_SCALE - CAM_MIN_SCALE) + CAM_MIN_SCALE);
		} while (Math.abs(camX - oldCamX) < posRandThresh ||
				Math.abs(camY - oldCamY) < posRandThresh ||
				Math.abs(oldCamScale - camScale) < sclRandThresh);
		topdown = false;
	}
	
	private void tryNewScene() {
		boolean timeForMoreAfterTopdown = SIMULATION_DURATION - simTimer.elapsed() >=
			CAM_SWITCH_TIME + CAM_SWITCH_TIME * TOPDOWN_DILATION;
		// small chance of going top down, if not already
		if (!topdown && timeForMoreAfterTopdown && Math.random() > 0.85)
			goTopDown();
		else
			goRandomIsometric();
	}
	
	@Override
	public void tick() {
		if (Input.getInstance().isPressingKey(KeyEvent.VK_A)) {
			goTopDown();
		}
		if (Input.getInstance().isPressingKey(KeyEvent.VK_D)) {
			goRandomIsometric();
			Input.getInstance().consumeKeyPress(KeyEvent.VK_D);
		}
		if (Input.getInstance().isPressingKey(KeyEvent.VK_0)) {
			Engine.getEngine().getStateManager().exitTopState(false);
			Engine.getEngine().getStateManager().exitTopState(false);
		}
		if (simTimer.elapsed() > SIMULATION_DURATION) {
			stopSimulation();
		}
		if (simulating) {
			double factor = !topdown ? 1 : TOPDOWN_DILATION;
			boolean timeForMore = SIMULATION_DURATION - simTimer.elapsed() >= CAM_SWITCH_TIME;
			if (camTimer.elapsed() >= CAM_SWITCH_TIME * factor && timeForMore) {
				camTimer.reset(true);
				tryNewScene();
			}
		}
		else
			super.tick();
	}

	@Override
	public void render(Graphics g) {
		//goTopDown();
		// draw blanky
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		// if topdown, width and height scale equally
		double ratio = topdown ? 1 : HEIGHT_WIDTH_RATIO;
		double camRot = !topdown ? smoothRot.getValue() : 0;
		// translate field to origin plus given offset before rotating
		gg.translate(Engine.SCREEN_WIDTH / 2 + camX, Engine.SCREEN_HEIGHT / 2 + camY);
		gg.scale(camScale, camScale * ratio);
        // rotate based on argument
        gg.rotate(camRot);
        // translate back, so drawing will be at the desired position
		gg.translate(-width / 2, -height / 2);
		// draw blanket
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				g.drawImage(ImageBank.cells35[r % 2 + c % 2],
						c * CELL_SIZE, r * CELL_SIZE, null);
		// revert graphics state
		gg.setClip(null);
		gg.setTransform(oldTrans);
		// draw field entities
		
		BufferedImage plateimg = !topdown ? ImageBank.isoplate : ImageBank.plates35[0];
		for (int i = 0; i < plates.length; i++) {
			// get rot and rad values for plate
			double rot = plates[i][ROT];
			double rad = plates[i][RAD];
			// find plate's x and y pos, with cam rot, relative to center of blanket
			double px = Math.cos(camRot + rot) * rad;
			double py = Math.sin(camRot + rot) * rad;
			// width and height of image to draw
			double pw = camScale * plateimg.getWidth();
			double ph = camScale * plateimg.getHeight();
			// location to draw at - translated by camera position and to center of screen
			double tx = px * camScale - pw / 2 + camX + Engine.SCREEN_WIDTH / 2;
			double ty = py * camScale * ratio - ph / 2 + camY + Engine.SCREEN_HEIGHT / 2;
			g.drawImage(plateimg, (int) tx, (int) ty, (int) pw, (int) ph, null);
		}
		for (int i = 0; i < ants.length; i++) {
			// get the plate this ant targets
			double[] plate = plates[i / ANTS_PER_PLATE];
			// calculate the plate's x and y pos, as above
			double px = Math.cos(camRot + plate[ROT]) * plate[RAD];
			double py = Math.sin(camRot + plate[ROT]) * plate[RAD];
			// calc ant's progress towards its goal plate
			double progress = Math.max(0, (simTimer.elapsed() - ants[i][DELAY]) /
									(double) (SIMULATION_DURATION - MAX_DELAY));
			// get ant's rot and rad values
			double rot = ants[i][ROT];
			double rad = ants[i][RAD] * (1 - progress);
			// check if ant is stopped, then keep rad at stop value
			boolean stopped = rad < ants[i][STOP];
			if (stopped) // put ant at stop radius
				rad = ants[i][STOP];
			// ant pos, based on angle and progress from plate (rel to center of blanket)
			double ax = px + Math.cos(camRot + rot) * rad;
			double ay = py + Math.sin(camRot + rot) * rad;
			// if reached stop radius, jitter around
			if (stopped) {
				int token = (int) (simTimer.elapsed() % 500);
				int xoff, yoff;
				if (token < 125) {
					xoff = -1;
					yoff = 0;
				}
				else if (token < 250) {
					xoff = 0;
					yoff = 0;
				}
				else if (token < 375) {
					xoff = 1;
					yoff = -1;
				}
				else {
					xoff = 0;
					yoff = 1;
				}
				ax += xoff;
				ay += yoff;
			}
			// width and height of image to draw
			double aw = 4 * camScale;
			double ah = 4 * camScale;
			// location to draw at - translated by camera position and to center of screen
			double tx = ax * camScale - aw / 2 + camX + Engine.SCREEN_WIDTH / 2;
			double ty = ay * camScale * ratio - ah / 2 + camY + Engine.SCREEN_HEIGHT / 2;
			g.setColor(Color.BLACK);
			g.fillRect((int) tx, (int) ty, (int) aw, (int) ah);
		}
	}

}
