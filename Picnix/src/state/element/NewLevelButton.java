package state.element;

import java.awt.Color;
import java.awt.Graphics;

import picnix.World;
import picnix.data.UserData;
import state.NewLevelSelectState;

public class NewLevelButton extends Button {

	public static final int WIDTH = 200;
	public static final int HEIGHT = 200;
	
	// parent state
	private NewLevelSelectState levState;
	// turnstile id (0 = left, 1 = middle, 2 = right)
	private int tid;
	
	public NewLevelButton(NewLevelSelectState levState, int tid) {
		this.levState = levState;
		this.tid = tid;
	}
	
	@Override
	public int getDisplayX() {
		return levState.getButtonX(tid);
	}
	
	@Override
	public int getDisplayY() {
		return levState.getButtonY(tid);
	}
	
	@Override
	public void render(Graphics g) {
		// scale to width/height
		double scale = levState.getButtonScale(tid);
		// draw info
		World world = levState.getWorld();
		int levelId = levState.getLevelId(tid);
		// if the level doesn't exist (off the turnstile edge)
		//if (levelId < 0 || levelId >= world.getLevelCount())
		//	return;
		//boolean layered = world.getLevels()[levelId];
		//boolean cleared = UserData.isPuzzleCleared(world.getId(), levelId);
		//int hiscore = UserData.getPuzzleScore(world.getId(), levelId);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int w = (int) (WIDTH * scale);
		int h = (int) (HEIGHT * scale);
		g.setColor(Color.ORANGE);
		g.fillRect(xp, yp, w, h);
		g.setColor(Color.WHITE);
		g.drawString(Integer.toString(tid), xp, yp);
	}
	
}
