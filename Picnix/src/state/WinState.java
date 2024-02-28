package state;

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
import resource.bank.Palette;
import util.Animation;
import util.Timer;

public class WinState extends State {

	private static final double HEIGHT_WIDTH_RATIO = 0.5;
	private static final int TOPDOWN_HEIGHT = Engine.SCREEN_HEIGHT - 50;
	private static final int CELL_SIZE = 35;
	
	private static final double CAM_SWITCH_TIME = 2000;
	private static final double CAM_MIN_SCALE = 1.5;
	private static final double CAM_MAX_SCALE = 5;
	
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
	private double plates[][];
	
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
		smoothRot = new Animation(0, 2 * Math.PI, 30000, Animation.EASE_OUT, Animation.NO_LOOP, true);
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
	
	private void randomizeCamera() {
		// small chance of going top down, if not already
		if (!topdown && Math.random() > 0.85) {
			camX = camY = 0;
			camScale = TOPDOWN_HEIGHT / height;
			topdown = true;
		}
		else {
			// focus on any point in the blanket
			camX = Math.random() * width - width / 2; 
			camY = Math.random() * height - height / 2;
			camScale = Math.random() * (CAM_MAX_SCALE - CAM_MIN_SCALE) + CAM_MIN_SCALE;
			topdown = false;
		}
	}
	
	@Override
	public void tick() {
		if (simulating) {
			double factor = !topdown ? 1 : 2;
			if (camTimer.elapsed() >= CAM_SWITCH_TIME * factor) {
				camTimer.reset(true);
				randomizeCamera();
			}
		}
		else
			super.tick();
	}

	@Override
	public void render(Graphics g) {
		// draw blanky
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		// if topdown, width and height scale equally
		double ratio = topdown ? 1 : HEIGHT_WIDTH_RATIO;
		double camRot = smoothRot.getValue();
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
	}

}
