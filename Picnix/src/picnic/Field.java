package picnic;

import java.awt.Graphics;

import engine.Engine;
import engine.Input;
import puzzle.Puzzle;
import resource.bank.ImageBank;
import state.PuzzleState;
import state.element.Container;
import state.element.ToolButton;
import util.Animation;

public class Field extends Container {

	private PuzzleState puzState;
	// the picnic blanket, which has the puzzle loaded
	private Blanket blanket;
	
	private static final int FIELD_WIDTH = 1000;
	private static final int FIELD_HEIGHT = 600;

	// position of the camera over the field
	private int camX, camY;
	// camera size (screen size)
	private int camW = Engine.SCREEN_WIDTH;
	private int camH = Engine.SCREEN_HEIGHT;
	
	// anims for smoothing camera x/y movement
	private Animation camXAnim, camYAnim;
	// keeps track of camera movement velocity
	//private double camXVel, camYVel;
	
	// position & size of blanket within the field
	private int bx, by, bw, bh;
	
	public Field(PuzzleState puzState, Puzzle puzzle) {
		super(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
		disableScrollers();
		setZ(-100);
		this.puzState = puzState;
		blanket = new Blanket(this, puzzle);
		bw = blanket.getPixelWidth();
		bh = blanket.getPixelHeight();
		bx = (FIELD_WIDTH - bw) / 2;
		by = (FIELD_HEIGHT - bh) / 2;
		blanket.setBounds(bx, by, bw, bh);
		add(blanket);
		setCamX(getCamCenterX());
		setCamY(getCamCenterY());
	}

	
	public PuzzleState getPuzzleState() {
		return puzState;
	}
	
	public int getCamCenterX() {
		return (FIELD_WIDTH - camW) / 2;
	}
	
	public int getCamCenterY() {
		return (FIELD_HEIGHT - camH) / 2;	
	}
	
	
	public void recenter() {
		camXAnim = new Animation(camX, getCamCenterX(), 777, Animation.EASE_OUT, Animation.LOOP_NONE, true);
		camYAnim = new Animation(camY, getCamCenterY(), 777, Animation.EASE_OUT, Animation.LOOP_NONE, true);
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
			// set smooth cam move // TODO area of definite memory improvement - resuse/modify same array instead of making new objects
			//camXAnim = new Animation(camX, camX + camXVel, 200, Animation.EASE_OUT, Animation.LOOP_NONE, true);
			//camYAnim = new Animation(camY, camY + camYVel, 200, Animation.EASE_OUT, Animation.LOOP_NONE, true);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		Input input = Input.getInstance();
		if (dragging) {
			//int lastCamX = camX;
			//int lastCamY = camY;
			int movedX = input.getMouseX() - clickXOffset;
			int movedY = input.getMouseY() - clickYOffset;
			setCamX(camXAtClick - movedX);
			setCamY(camYAtClick - movedY);
			//camXVel = (camXVel + camX - lastCamX) * .995;
			//camYVel = (camYVel + camY - lastCamY) * .995;
			//System.out.println(camXVel + ", " + camYVel);
		}
		else {
			// if a cam anim is going, set camera to it
			if (camXAnim != null && camXAnim.isPlaying())
				setCamX(camXAnim.getIntValue());
			if (camYAnim != null && camYAnim.isPlaying())
				setCamY(camYAnim.getIntValue());
		}
	}
	
	@Override
	public void render(Graphics g) {
		g.translate(-camX, -camY);
		g.drawImage(ImageBank.grassback, 0, 0, FIELD_WIDTH, FIELD_HEIGHT, null);
		g.translate(camX, camY);
	}
	
}
