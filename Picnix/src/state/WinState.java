package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import engine.Engine;
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
	
	private Animation smoothRot;

	private static final int ROT = 0;
	private static final int RAD = 1;
	private static final int X = 2;
	private static final int Y = 3;
	
	private static final int ANTS_PER_PLATE = 15;
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
		simTimer = new Timer(false);
		camTimer = new Timer(false);
		smoothRot = new Animation(0, 2 * Math.PI, SIMULATION_DURATION, Animation.EASE_OUT, Animation.NO_LOOP, true);
		plates = new double[puzzle.getFilledCellsInSolution()][2];
		int i = 0;
		for (int r = 0; r < puzzle.getRows(); r++) {
			for (int c = 0; c < puzzle.getColumns(); c++) {
				if (puzzle.isFilledInSolution(r, c)) {
					double px = c * CELL_SIZE - width / 2 + CELL_SIZE / 2;
					double py = r * CELL_SIZE - height / 2 + CELL_SIZE / 2;
					double angle = Math.atan(py / px);
					if (px < 0) // reflect angle
						angle = angle + Math.PI;
					double dist = Math.sqrt(px * px + py * py);
					plates[i][ROT] = angle;
					plates[i][RAD] = dist;
					i++;
				}
			}
		}
		// create ants around the edges
		int NUM_ANTS = puzzle.getFilledCellsInSolution() * ANTS_PER_PLATE;
		ants = new double[NUM_ANTS][4];
		for (int a = 0; a < NUM_ANTS; a++) {
			// find its target point
			double[] plate = plates[a / ANTS_PER_PLATE]; // the plate this ant targets
			ants[a][X] = Math.cos(plate[ROT]) * plate[RAD]; // the plate's x
			ants[a][Y] = Math.sin(plate[ROT]) * plate[RAD]; // the plate's y
			// tell this ant where to start from
			ants[a][ROT] = Math.random() * Math.PI; // any angle
			ants[a][RAD] = getMinimumRadiusToEdge(ants[a][X], ants[a][Y], ants[a][ROT]); // the nearest edge, plus some random factor
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
		topdown = false;
	}
	
	private void stopSimulation() {
		simulating = false;
		goTopDown();
	}
	
	private double getSimulationProgress() {
		return Math.max(0, Math.min(1, simTimer.elapsed() / (double) SIMULATION_DURATION));
	}
	
	private double getMinimumRadiusToEdge(double x, double y, double theta) {
		// slope of the angle
		double m = Math.tan(theta); // regular slope for left/right
		double m2 = 1.0 / m; // co tangent - for top/bottom
		// width to the right; height to the bottom
		double xo = Engine.SCREEN_WIDTH - x;
		double yo = Engine.SCREEN_HEIGHT - y;
		// find where this angle would intercept each side
		double left = y - x * m;
		double top = x - y * m2;
		double right = y + xo * m;
		double bottom = x + yo * m2;
		// find distance from here to each intercept point
		double leftDist = Math.sqrt(x * x + Math.pow(y - left, 2));
		double topDist = Math.sqrt(y * y + Math.pow(x - top, 2));
		double rightDist = Math.sqrt(xo * xo + Math.pow(y - right, 2));
		double bottomDist = Math.sqrt(yo * yo + Math.pow(x - bottom, 2));
		// find minimum of all distances
		return Math.min(leftDist, Math.min(topDist, Math.max(rightDist, bottomDist)));
	}
	
	private void goTopDown() {
		camX = camY = 0;
		camScale = TOPDOWN_FINAL_HEIGHT / height;
		topdown = true;
	}
	
	private void randomizeCamera() {
		boolean timeForMoreAfterTopdown = SIMULATION_DURATION - simTimer.elapsed() >=
			CAM_SWITCH_TIME + CAM_SWITCH_TIME * TOPDOWN_DILATION;
		// small chance of going top down, if not already
		if (!topdown && timeForMoreAfterTopdown && Math.random() > 0.85)
			goTopDown();
		else {
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
	}
	
	@Override
	public void tick() {
		if (simTimer.elapsed() > SIMULATION_DURATION) {
			stopSimulation();
		}
		if (simulating) {
			double factor = !topdown ? 1 : TOPDOWN_DILATION;
			boolean timeForMore = SIMULATION_DURATION - simTimer.elapsed() >= CAM_SWITCH_TIME;
			if (camTimer.elapsed() >= CAM_SWITCH_TIME * factor && timeForMore) {
				camTimer.reset(true);
				randomizeCamera();
			}
		}
		else
			super.tick();
	}

	@Override
	public void render(Graphics g) {
		goTopDown();
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
		
		BufferedImage plate = !topdown ? ImageBank.isoplate : ImageBank.plates35[0];
		for (int i = 0; i < plates.length; i++) {
			double rot = plates[i][ROT];
			double rad = plates[i][RAD] * camScale;
			int w = (int) (camScale * plate.getWidth());
			int h = (int) (camScale * plate.getHeight());
			int tx = (int) (-(w / 2) + camX + Engine.SCREEN_WIDTH / 2 + Math.cos(camRot + rot) * rad);
			int ty = (int) (-(h / 2) + camY + Engine.SCREEN_HEIGHT / 2 + Math.sin(camRot + rot) * rad * ratio);
			g.drawImage(plate, tx, ty, w, h, null);
		}
		for (int i = 0; i < ants.length; i++) {
			double rot = ants[i][ROT];
			double rad = ants[i][RAD] * (1 - getSimulationProgress());
			double px = ants[i][X];
			double py = ants[i][Y];
			int w = (int) (4 * camScale);
			int h = (int) (4 * camScale);
			double tx, ty;
			if (rad < 0) {
				tx = Math.random() * 35 + px + camX + Engine.SCREEN_WIDTH / 2;
				ty = Math.random() * 35 * ratio + py + camY + Engine.SCREEN_HEIGHT / 2;
			}
			else {
				tx = -(w / 2) + px + camX + Engine.SCREEN_WIDTH / 2 + Math.cos(camRot + rot) * rad;
				ty = -(h / 2) + py + camY + Engine.SCREEN_HEIGHT / 2 + Math.sin(camRot + rot) * rad * ratio;
			}
			g.setColor(Color.BLACK);
			g.fillRect((int) tx, (int) ty, w, h);
		}
	}

}
