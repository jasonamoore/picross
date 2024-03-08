package state.element;

import java.awt.Color;
import java.awt.Graphics;

import engine.Engine;
import engine.Input;
import picnix.World;
import picnix.data.UserData;
import state.NewLevelSelectState;

public class NewLevelButton extends Button {

	public static final int WIDTH = 200;
	public static final int HEIGHT = 200;
	
	// parent state
	private NewLevelSelectState levState;
	// turnstile id (0 = left, 1 = middle, 2 = right)
	private int levelId;
	
	public NewLevelButton(NewLevelSelectState levState, int levelId) {
		super(NewLevelSelectState.getButtonX(levelId), Engine.SCREEN_HEIGHT / 2, WIDTH, HEIGHT);
		this.levState = levState;
		this.levelId = levelId;
	}
	
	@Override
	public int getDisplayX() {
		int midX = super.getDisplayX();
		return midX - getWidth() / 2;
	}
	
	@Override
	public int getDisplayY() {
		int midY = super.getDisplayY();
		return midY - getHeight() / 2;
	}
	
	@Override
	public int getWidth() {
		return (int) (super.getWidth() * getScale());
	}
	
	@Override
	public int getHeight() {
		return (int) (super.getHeight() * getScale());
	}
	
	private double getScale() {
		double focdist = Math.abs(levState.getFocalDistance(levelId));
		double scale;
		if (focdist < Engine.SCREEN_WIDTH / 4)
			scale = 1.0;
		else
			scale = Math.max(0.8, (Engine.SCREEN_WIDTH / 4) / focdist);
		return scale;
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (mbutton == Input.LEFT_CLICK && beingHovered())
			levState.levelClicked(levelId);
	}
	
	@Override
	public void render(Graphics g) {
		// scale to width/height
		//double scale = levState.getButtonScale(tid);
		// draw info
		World world = levState.getWorld();
		// if the level doesn't exist (off the turnstile edge)
		//if (levelId < 0 || levelId >= world.getLevelCount())
		//	return;
		//boolean layered = world.getLevels()[levelId];
		//boolean cleared = UserData.isPuzzleCleared(world.getId(), levelId);
		//int hiscore = UserData.getPuzzleScore(world.getId(), levelId);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int w = getWidth();
		int h = getHeight();
		g.setColor(Color.ORANGE);
		g.fillRect(xp, yp, w, h);
		g.setColor(Color.WHITE);
		g.drawString(Integer.toString(levelId), xp, yp);
	}
	
}
