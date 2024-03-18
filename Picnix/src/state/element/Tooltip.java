package state.element;

import java.awt.Graphics;

import engine.Engine;
import engine.Input;
import resource.Font;
import resource.bank.Palette;
import util.Animation;

public class Tooltip extends Element {

	private static final int PADDING = 5;
	private static final int DEFAULT_DELAY = 1000;
	
	private Element owner;
	private TextField tip;
	
	private boolean horizontal;
	
	private Animation fade;
	
	public Tooltip(Element owner, String text, Font font, int width, int distance, boolean horizontal) {
		this.owner = owner;
		this.horizontal = horizontal;
		int textWidth = width - PADDING * 2;
		int height = TextField.getTextHeight(text, font, textWidth) + PADDING * 2;
		// don't need to set x/y if getDisplayX/Y() is overridden (so just set to 0)
		int x = horizontal ? determineX(owner, width, distance) : 0;
		int y = !horizontal ? determineY(owner, height, distance) : 0;
		setBounds(x, y, width, height);
		setZ(100);
		tip = new TextField(text, font, 5, 5, width);
		add(tip);
		fade = new Animation(0, 1, 150, Animation.EASE_IN, Animation.NO_LOOP, false);
		fade.setDelay(DEFAULT_DELAY);
		hide();
	}
	
	public void setDelay(int delay) {
		fade.setDelay(delay);
	}

	public void show() {
		setVisible(true);
		setChildrenVisible(true);
		fade.reset(true);
	}
	
	public void hide() {
		setVisible(false);
		setChildrenVisible(false);
	}
	
	@Override
	public int getDisplayX() {
		if (horizontal)
			return super.getDisplayX();
		else { // center tooltip to mouse position
			Input input = Input.getInstance();
			// keep within horizontal bounds of element
			int ownerX = owner.getDisplayX();
			int myW = getWidth();
			int boundedMouseX = Math.max(ownerX,
					Math.min(ownerX + owner.getWidth(), input.getMouseX()));
			// center box to mouse position
			int centeredX = boundedMouseX - myW / 2;
			return Math.max(0, Math.min(Engine.SCREEN_WIDTH - myW, centeredX));
		}
	}
	
	@Override
	public int getDisplayY() {
		if (!horizontal)
			return super.getDisplayY();
		else { // center tooltip to mouse position
			Input input = Input.getInstance();
			// keep within vertical bounds of element
			int ownerY = owner.getDisplayY();
			int myH = getHeight();
			int boundedMouseY = Math.max(ownerY,
					Math.min(ownerY + owner.getHeight(), input.getMouseY()));
			// center box to mouse position
			int centeredY = boundedMouseY - myH / 2;
			return Math.max(0, Math.min(Engine.SCREEN_HEIGHT - myH, centeredY));
		}
	}
	
	@Override
	public float getOpacity() {
		return (float) fade.getValue();
	}
	
	private static int determineX(Element owner, int tipw, int dist) {
		int ox = owner.getDisplayX();
		int ow = owner.getWidth();
		int tx1 = ox + ow + dist;
		int tx2 = ox - tipw - dist;
		int cenDist1 = Math.abs(tx1 + tipw / 2 - Engine.SCREEN_WIDTH / 2);
		int cenDist2 = Math.abs(tx2 + tipw / 2 - Engine.SCREEN_WIDTH / 2);
		// pick the position that is closer to center of screen
		return cenDist1 < cenDist2 ? tx1 : tx2;
	}
	
	private static int determineY(Element owner, int tiph, int dist) {
		int oy = owner.getDisplayY();
		int oh = owner.getHeight();
		int ty1 = oy + oh + dist;
		int ty2 = oy - tiph - dist;
		int cenDist1 = Math.abs(ty1 + tiph / 2 - Engine.SCREEN_HEIGHT / 2);
		int cenDist2 = Math.abs(ty2 + tiph / 2 - Engine.SCREEN_HEIGHT/ 2);
		// pick the position that is closer to center of screen
		return cenDist1 < cenDist2 ? ty1 : ty2;
	}
	
	@Override
	public void render(Graphics g) {
		setRenderComposite(g);
		g.setColor(Palette.CLAY);
		g.fillRect(getDisplayX(), getDisplayY(), getWidth(), getHeight());
		//super.render(g);
	}
	
}
