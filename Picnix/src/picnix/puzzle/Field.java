package picnix.puzzle;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Input;
import resource.bank.ImageBank;
import state.PuzzleState;
import state.element.Container;
import state.element.puzzle.ToolButton;
import util.Animation;

public class Field extends Container {

	public static final int FIELD_WIDTH = 1000;
	public static final int FIELD_HEIGHT = 675;
	
	private static final int EDGE_SCROLL_THRESHOLD = 20;
	private static final int EDGE_SCROLL_AMOUNT = 3;
	
	// parent puzzle state
	private PuzzleState puzState;
	// the picnic blanket, which has the puzzle loaded
	private Blanket blanket;

	// position of the camera over the field
	private int camX, camY;
	// camera size (screen size)
	private int camW = Engine.SCREEN_WIDTH;
	private int camH = Engine.SCREEN_HEIGHT;
	
	// anims for smoothing camera x/y movement
	private Animation camXAnim, camYAnim;
	
	// the background field image
	private BufferedImage[] fieldBackground;
	
	public Field(PuzzleState puzState, int worldId) {
		super(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
		this.puzState = puzState;
		setScrollersEnabled(false);
		setZ(-100);
		fieldBackground = ImageBank.tiledBackgrounds[worldId];
		blanket = new Blanket(this);
		int bw = puzState.getPuzzleDisplayWidth();
		int bh = puzState.getPuzzleDisplayHeight();
		int bx = (FIELD_WIDTH - bw) / 2;
		int by = (FIELD_HEIGHT - bh) / 2;
		blanket.setBounds(bx, by, bw, bh);
		add(blanket);
		// set to padded center camera
		setCamX(getCamCenterX(true));
		setCamY(getCamCenterY(true));
	}

	public Blanket getBlanket() {
		return blanket;
	}
	
	public PuzzleState getPuzzleState() {
		return puzState;
	}
	
	public int getCamCenterX(boolean padded) {
		int padding = padded ? puzState.getPuzzleLeftPadding() : 0;
		return (FIELD_WIDTH - camW - padding) / 2;
	}
	
	public int getCamCenterY(boolean padded) {
		int padding = padded ? puzState.getPuzzleTopPadding() : 0;
		return (FIELD_HEIGHT - camH - padding) / 2;
	}
	
	
	public void recenter(boolean padded) {
		camXAnim = new Animation(camX, getCamCenterX(padded), 777, Animation.EASE_OUT, Animation.NO_LOOP, true);
		camYAnim = new Animation(camY, getCamCenterY(padded), 777, Animation.EASE_OUT, Animation.NO_LOOP, true);
	}
	
	public void setCamX(int newX) {
		camX = Math.max(0, Math.min(FIELD_WIDTH - camW, newX));
	}
	
	public void setCamY(int newY) {
		camY = Math.max(0, Math.min(FIELD_HEIGHT - camH, newY));
	}
	
	@Override
	public int getScrollX() {
		return camX;
	}

	@Override
	public int getScrollY() {
		return camY;
	}
	
	private int camXAtClick;
	private int camYAtClick;
	private int clickXOffset;
	private int clickYOffset;
	private boolean dragging;
	
	@Override
	public void onClick(int mbutton) {
		super.onClick(mbutton);
		Input input = Input.getInstance();
		if (mbutton == Input.LEFT_CLICK && puzState.getCurrentTool() == ToolButton.PAN
				|| mbutton == Input.MIDDLE_CLICK) {
			dragging = true;
			camXAnim = null;
			camYAnim = null;
			camXAtClick = camX;
			camYAtClick = camY;
			clickXOffset = input.getMouseX();
			clickYOffset = input.getMouseY();
			puzState.setCenterEnabled(true);
			//puzState.fadeSidebars(true);
		}
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		Input input = Input.getInstance();
		if (!(input.isPressingMouseButton(Input.LEFT_CLICK)
				&& puzState.getCurrentTool() == ToolButton.PAN)
				&& !input.isPressingMouseButton(Input.MIDDLE_CLICK)) {
			dragging = false;
			//puzState.fadeSidebars(false);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		// update camera if dragging
		Input input = Input.getInstance();
		// if mouse is near edges in food puzzle mode, auto scroll
		if (puzState.getState() == PuzzleState.FOOD_SOLVING
				&& !dragging && blanket.isDraggingFood()) {
			int mx = input.getMouseX();
			int my = input.getMouseY();
			if (mx < EDGE_SCROLL_THRESHOLD)
				setCamX(camX - EDGE_SCROLL_AMOUNT);
			else if (mx > Engine.SCREEN_WIDTH - EDGE_SCROLL_THRESHOLD)
				setCamX(camX += EDGE_SCROLL_AMOUNT);
			if (my < EDGE_SCROLL_THRESHOLD)
				setCamY(camY -= EDGE_SCROLL_AMOUNT);
			else if (my > Engine.SCREEN_HEIGHT - EDGE_SCROLL_THRESHOLD)
				setCamY(camY += EDGE_SCROLL_AMOUNT);
		}
	}
	
	@Override
	public void render(Graphics g) {
		// updating drag cam position during render
		// which makes the camera look WAY smoother
		Input input = Input.getInstance();
		if (dragging) {
			int movedX = input.getMouseX() - clickXOffset;
			int movedY = input.getMouseY() - clickYOffset;
			setCamX(camXAtClick - movedX);
			setCamY(camYAtClick - movedY);
		}
		else {
			// if a cam anim is going, set camera to it
			if (camXAnim != null && camXAnim.isPlaying())
				setCamX(camXAnim.getIntValue());
			if (camYAnim != null && camYAnim.isPlaying())
				setCamY(camYAnim.getIntValue());
		}
		// background tiling / chunking ;)
		g.translate(-camX, -camY);
		for (int x = camX / 100; x * 100 < Engine.SCREEN_WIDTH + camX; x++) {
			for (int y = camY / 45; y * 45 < Engine.SCREEN_HEIGHT + camY; y++) {
				BufferedImage tile = fieldBackground[x + y * 10];
				g.drawImage(tile, x * 100, y * 45, null);
			}
		}
		g.translate(camX, camY);
	}
	
}
