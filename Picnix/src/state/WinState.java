package state;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Engine;
import engine.Input;
import engine.Transition;
import picnix.Level;
import picnix.Parallax;
import picnix.World;
import picnix.puzzle.Puzzle;
import resource.bank.AudioBank;
import resource.bank.FontBank;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.element.Gallery;
import state.element.TextField;
import util.Animation;
import util.Timer;

public class WinState extends State {

	private static final int FREEFALL = 0;
	private static final int SIMULATING = 1;
	private static final int SHOWING = 2;
	private static final int DONE = 3;
	private static final int CLOSING = 4;

	private static final double FREEFALL_HEIGHT = Engine.SCREEN_HEIGHT * 1.5;
	private static final double FREEFALL_START = (Engine.SCREEN_HEIGHT - FREEFALL_HEIGHT) / 2;
	
	private static final double HEIGHT_WIDTH_RATIO = 0.5;
	private static final int TOPDOWN_FINAL_HEIGHT = Engine.SCREEN_HEIGHT - 50;
	private static final int CELL_SIZE = 35;

	private static final int FREEFALL_DURATION = 5000;
	private static final int SIMULATION_DURATION = 30000;
	private static final int SHOW_DURATION = 3500;
	private static final int HALFTIME = SIMULATION_DURATION / 2;
	
	private static final double CAM_SWITCH_TIME = 2000;
	private static final int TOPDOWN_DILATION = 2;
	private static final double CAM_MIN_SCALE = 2;
	private static final double CAM_MAX_SCALE = 5;
	
	private static final int ANT_SPREAD = 150;
	private static final int FOOD_DROP_DELAY = 1000;
	private static final int MAX_DELAY = 10000;
	private static final int MAX_FOOD_DROP_OFFSET = 50;
	
	private static final int[] JIGGLE = {71, 13, 73, 17};
	private static final Color[] COLORS = {Palette.BLACK, Palette.MAGENTA, Palette.YELLOW, Palette.RED, Palette.CYAN, Palette.PERIWINKLE, Palette.PEAR};
	
	private static final int ROT = 0;
	private static final int RAD = 1;
	private static final int IMG = 2;
	private static final int DELAY = 2;
	private static final int STOP = 3;
	
	private static final int GALLERY_PADDING = 100;	
	private static final int GALLERY_PAUSE_TIME = 2000;
	private static final String GALLERY_STRING = "click anywhere to continue";

	private double[][] plates;
	private double[][] ants;
	private int antsPerPlate;
	private ArrayList<Food> food;
	
	private Timer sceneTimer;
	private Timer camTimer;
	
	private int state;

	private int rows, cols;
	private int width, height;

	private double camX, camY;
	private double camScale = 1;
	private Animation smoothRot;
	private boolean topdown;
	private double topdownScale;
	
	private Animation galleryAnim;
	private Timer galleryTimer;
	private TextField contText;
	
	private World world;
	private Level level;
	private Puzzle[] puzzles;
	
	private Parallax background;
	
	public WinState(World world, Level level) {
		this.world = world;
		this.level = level;
		puzzles = level.getPuzzles();
		rows = puzzles[0].getRows();
		cols = puzzles[0].getColumns();
		width = cols * CELL_SIZE;
		height = rows * CELL_SIZE;
		topdownScale = TOPDOWN_FINAL_HEIGHT / height;
		sceneTimer = new Timer(false);
		camTimer = new Timer(false);
		smoothRot = new Animation(0, 2 * Math.PI, SIMULATION_DURATION, Animation.EASE_OUT, Animation.NO_LOOP, true);
		galleryAnim =  new Animation(0, 1, 250, Animation.EASE_IN, Animation.NO_LOOP, false);
		galleryTimer = new Timer(false);
		contText = new TextField(GALLERY_STRING, FontBank.defout, 0, 345, Engine.SCREEN_WIDTH);
		contText.setVisible(false);
		contText.setAlignment(TextField.ALIGN_CENTER);
		add(contText);
		// SIM STUFF:
		// plates for the filled cells
		int max = 0;
		for (int i = 0; i < puzzles.length; i++)
			max = Math.max(max, puzzles[i].getFilledCellsInSolution());
		plates = new double[max][3];
		// create ants around the edges
		antsPerPlate = 150 / rows;
		int numAnts = plates.length * antsPerPlate;
		ants = new double[numAnts][4];
		boolean[][] visited = new boolean[rows][cols];
		int i = 0;
		for (int r = 0; r < puzzles[0].getRows(); r++) {
			for (int c = 0; c < puzzles[0].getColumns(); c++) {
				int plateColId = getPlateColorIndex(r, c);
				// if there is any plate here
				if (plateColId != -1) {
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
					plates[i][IMG] = getPlateColorIndex(r, c);
					// make the ants for this plate
					for (int a = 0; a < antsPerPlate; a++) {
						// find its target point
						int aid = i * antsPerPlate + a;
						double abspx = px * topdownScale + Engine.SCREEN_WIDTH / 2;
						double abspy = py * topdownScale + Engine.SCREEN_HEIGHT / 2;
						// tell this ant where to start from
						ants[aid][ROT] = Math.random() * 2 * Math.PI; // any angle
						// the nearest edge, plus some random factor
						ants[aid][RAD] = getMinimumRadiusToEdge(abspx, abspy, ants[aid][ROT]) / topdownScale
								+ Math.random() * ANT_SPREAD;
						ants[aid][DELAY] = FOOD_DROP_DELAY + Math.random() * MAX_DELAY;
						ants[aid][STOP] = Math.random() * (CELL_SIZE - 5) * 0.5;
					}
					// next plate
					i++;
				}
				else // make this spot ineligible for food placement
					visited[r][c] = true;
			}
		}
		food = new ArrayList<Food>();
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
					tryCreateFood(visited, r, c);
		// parallax
		background = new Parallax(true, true);
		background.addLayer(ImageBank.winparatest1, 0, 6000, true);
		background.addLayer(ImageBank.winparatest2, 0, 4000, true);
		//background.addLayer(ImageBank.winparatest3, 0, -4500, true);
		background.resumeScroll();
	}

	private void tryCreateFood(boolean[][] visited, int r, int c) {
		if (visited[r][c])
			return;
		double px = c * CELL_SIZE - width / 2 + CELL_SIZE / 2;
		double py = r * CELL_SIZE - height / 2 + CELL_SIZE / 2;
		double angle;
		if (px == 0)
			angle = py > 0 ? Math.PI / 2 : 3 * Math.PI / 2;
		else								 // reflect angle
			angle = Math.atan(py / px) + (px < 0 ? Math.PI : 0);
		double dist = Math.sqrt(px * px + py * py);
		Food f = new Food();
		f.context = this;
		f.rot = angle;
		f.rad = dist;
		f.type = 0;
		f.width = 1;
		f.height = 1;
		f.dropOffset = (int) (Math.random() * MAX_FOOD_DROP_OFFSET);
		food.add(f);
	}

	@Override
	public void focus(int status) {
		//startFreefall();
		startSimulation();
	}
	
	/*private void startFreefall() {
		state = FREEFALL;
		sceneTimer.resume();
	}*/
	
	private void startSimulation() {
		state = SIMULATING;
		sceneTimer.reset(true);
		camTimer.resume();
		goRandomIsometric();
	}
	
	private void stopSimulation() {
		state = SHOWING;
		sceneTimer.reset(true);
		goTopDown();
	}
	
	private void showGallery() {
		state = DONE;
		Gallery gallery = new Gallery(GALLERY_PADDING, GALLERY_PADDING,
				Engine.SCREEN_WIDTH - GALLERY_PADDING * 2, Engine.SCREEN_HEIGHT - GALLERY_PADDING * 2);
		gallery.setEnabled(false);
		add(gallery);
		gallery.doFancyReveal(world.getId(), level.getId());
		galleryAnim.resume();
		galleryTimer.resume();
	}
	
	private int getPlateColorIndex(int r, int c) {
		if (!level.isLayered())
			return puzzles[0].isFilledInSolution(r, c) ? 0 : -1;
		boolean magenta = puzzles[PuzzleState.MAGENTA].isFilledInSolution(r, c);
		boolean yellow = puzzles[PuzzleState.YELLOW].isFilledInSolution(r, c);
		boolean cyan = puzzles[PuzzleState.CYAN].isFilledInSolution(r, c);
		int result = (magenta ? 1 : 0) + (yellow ? 1 : 0) << 1 + (cyan ? 1 : 0) << 2;
		return result > 0 ? result : -1;
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
		// find minimum of side distances (+ some grace room)
		return Math.min(distA, distB) + CELL_SIZE;
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
		boolean timeForMoreAfterTopdown = SIMULATION_DURATION - sceneTimer.elapsed() >=
			CAM_SWITCH_TIME + CAM_SWITCH_TIME * TOPDOWN_DILATION;
		// small chance of going top down, if not already
		if (!topdown && timeForMoreAfterTopdown && Math.random() > 0.85)
			goTopDown();
		else
			goRandomIsometric();
	}
	
	public double getCamRot() {
		return !topdown ? smoothRot.getValue() : 0;
	}
	
	@Override
	public void navigateBack() {
		AudioBank.winMusic.pause();
		state = CLOSING;
		Engine.getEngine().getStateManager().transitionExitState(Transition.FADE, 250, 0);
	}
	
	@Override
	public void tick() {
		if (state == FREEFALL) {
			if (sceneTimer.elapsed() > FREEFALL_DURATION)
				startSimulation();
		}
		else if (state == SIMULATING) {
			if (sceneTimer.elapsed() > SIMULATION_DURATION)
				stopSimulation();
			else {
				double factor = !topdown ? 1 : TOPDOWN_DILATION;
				boolean timeForMore = SIMULATION_DURATION - sceneTimer.elapsed() >= CAM_SWITCH_TIME;
				if (camTimer.elapsed() >= CAM_SWITCH_TIME * factor && timeForMore) {
					camTimer.reset(true);
					tryNewScene();
				}
			}
		}
		else if (state == SHOWING) {
			if (sceneTimer.elapsed() > SHOW_DURATION)
				showGallery();
		}
		else {
			super.tick();
			// check a left click to move on
			if (state == DONE && galleryTimer.elapsed() > GALLERY_PAUSE_TIME &&
				Input.getInstance().isPressingMouseButton(Input.LEFT_CLICK))
					navigateBack();
		}
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Palette.MAGENTA);
		g.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
		background.render(g);
		if (state == FREEFALL) {
			double progress = sceneTimer.elapsed() / (double) FREEFALL_DURATION;
			boolean plates = progress < 0.5;
			double locprog = plates ? progress / 0.5 : (progress - 0.5) / 0.5;
			for (int i = 0; i < 3; i++) {
				BufferedImage sprite = plates ? ImageBank.isoplate : ImageBank.hiddenpreview;
				double y = FREEFALL_START + locprog * FREEFALL_HEIGHT + (i - 1) * 10;
				g.drawImage(sprite, 40 + i * 50, (int) y, null);
			}
		}
		else {
			long elapsed = sceneTimer.elapsed();
			if (state != SIMULATING)
				elapsed += SIMULATION_DURATION;
			// draw blanky
			Graphics2D gg = (Graphics2D) g;
			AffineTransform oldTrans = gg.getTransform();
			// if topdown, width and height scale equally
			double ratio = topdown ? 1 : HEIGHT_WIDTH_RATIO;
			double camRot = getCamRot();
			float opacity = Math.max(0, Math.min(1, // halfway through sim, start fading in
					(elapsed - HALFTIME) / (float) HALFTIME));
			Composite oldComp = gg.getComposite();
			Composite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
			Composite invAlphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - opacity);
			// translate field to origin plus given offset before rotating
			gg.translate(Engine.SCREEN_WIDTH / 2 + camX, Engine.SCREEN_HEIGHT / 2 + camY);
			gg.scale(camScale, camScale * ratio);
	        // rotate based on argument
	        gg.rotate(camRot);
	        // translate back, so drawing will be at the desired position
			gg.translate(-width / 2, -height / 2);
			// draw blanket
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					gg.setComposite(invAlphaComp);
					g.drawImage(ImageBank.cells35[r % 2 + c % 2],
							c * CELL_SIZE, r * CELL_SIZE, null);
					gg.setComposite(oldComp);
					int plateColId = getPlateColorIndex(r, c);
					if (plateColId != -1) {
						// slowly fill cells
						g.setColor(COLORS[plateColId]);
						gg.setComposite(alphaComp);
						g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
						gg.setComposite(oldComp);
					}
				}
			}
			// revert graphics state
			gg.setClip(null);
			gg.setTransform(oldTrans);
			// ~~~ draw field entities
			// plates & food slowly fade out
			gg.setComposite(invAlphaComp);
			for (int i = 0; i < plates.length; i++) {
				BufferedImage plateimg = !topdown ? ImageBank.isoplate : ImageBank.plates35[0];
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
			// sort so they render in the right order
			/*Collections.sort(food);
			for (int i = 0; i < food.size(); i++) {
				// the food to render:
				Food f = food.get(i);
				BufferedImage foodImage = ImageBank.hiddenpreview;
				// find plate's x and y pos, with cam rot, relative to center of blanket
				double fx = Math.cos(camRot + f.rot) * f.rad;
				double fy = Math.sin(camRot + f.rot) * f.rad;
				// width and height of image to draw
				double fw = camScale * foodImage.getWidth();
				double fh = camScale * foodImage.getHeight();
				// location to draw at - translated by camera position and to center of screen
				double tx = fx * camScale - fw / 2 + camX + Engine.SCREEN_WIDTH / 2;
				double ty = fy * camScale * ratio - fh + camY + Engine.SCREEN_HEIGHT / 2;
				// draw the food on top
				double foodProgress = Math.min(1, elapsed / (double) FOOD_DROP_DELAY);
				double dropy = ty - (Engine.SCREEN_HEIGHT + f.dropOffset) * (1 - foodProgress);
				g.drawImage(foodImage, (int) tx, (int) dropy, (int) fw, (int) fh, null);
			}*/
			// revert to regular composite
			gg.setComposite(oldComp);
			for (int i = 0; i < ants.length; i++) {
				// get the plate this ant targets
				double[] plate = plates[i / antsPerPlate];
				// calculate the plate's x and y pos, as above
				double px = Math.cos(camRot + plate[ROT]) * plate[RAD];
				double py = Math.sin(camRot + plate[ROT]) * plate[RAD];
				// calc ant's progress towards its goal plate
				double progress = Math.max(0, (elapsed - ants[i][DELAY]) /
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
				// width and height of image to draw
				double aw = 4 * camScale;
				double ah = 4 * camScale;
				// location to draw at - translated by camera position and to center of screen
				double tx = ax * camScale - aw / 2 + camX + Engine.SCREEN_WIDTH / 2;
				double ty = ay * camScale * ratio - ah / 2 + camY + Engine.SCREEN_HEIGHT / 2;
				// if reached stop radius, jitter around
				if (stopped) {
					int token = (int) (elapsed % 500 / 125);
					int xhash = (int) (rad * JIGGLE[token]);
					int yhash = (int) (rot * JIGGLE[token]);
					tx += xhash % 3 - 1;
					ty += yhash % 3 - 1;
				}
				g.setColor(Palette.BLACK);
				g.fillRect((int) tx, (int) ty, (int) aw, (int) ah);
			}
			if (state == DONE || state == CLOSING) {
				float backopacity = (float) galleryAnim.getValue();
				gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backopacity / 2));
				g.setColor(Palette.BLACK);
				g.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
				gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backopacity));
				boolean showText = galleryTimer.elapsed() > GALLERY_PAUSE_TIME && (int) (galleryTimer.elapsedSec() * 2) % 2 == 0;
				contText.setVisible(showText);
				super.render(g);
			}
		}
	}

}

class Food implements Comparable<Food> {
	
	WinState context;
	
	int type;
	int width, height;
	int dropOffset;
	
	double rot, rad;

	@Override
	public int compareTo(Food other) {
		double camRot = context.getCamRot();
		double thisY = Math.sin(rot + camRot) * rad;
		double otherY = Math.sin(other.rot + camRot) * other.rad;
		return (int) (thisY - otherY);
	}
	
}