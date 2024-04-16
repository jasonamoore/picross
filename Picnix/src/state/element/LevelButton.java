package state.element;

import java.awt.Graphics;

import engine.Engine;
import picnix.World;
import picnix.data.UserData;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.LevelSelectState;

public class LevelButton extends TiledButton {

	public static final int WIDTH = 200;
	public static final int HEIGHT = 200;
	
	// parent state
	private LevelSelectState levState;
	// turnstile id (0 = left, 1 = middle, 2 = right)
	private int levelId;
	
	public LevelButton(LevelSelectState levState, int levelId) {
		super(LevelSelectState.getButtonX(levelId), Engine.SCREEN_HEIGHT / 2, WIDTH, HEIGHT);
		this.levState = levState;
		this.levelId = levelId;
		setAllTileMaps(ImageBank.bluebutton, ImageBank.bluebuttonclick, ImageBank.buttondisabled);
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
	public void onButtonUp() {
		levState.levelClicked(levelId);
	}
	
	@Override
	public void render(Graphics g) {
		super.render(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int w = getWidth();
		int h = getHeight();
		// draw info
		World world = levState.getWorld();
		//boolean cleared = UserData.isPuzzleCleared(world.getId(), levelId);
		int hiscore = UserData.getPuzzleScore(world.getId(), levelId);
		g.setColor(Palette.WHITE);
		g.drawString("Picnic #" + levelId, xp + 10, yp + 10);
		if (hiscore > 0)
			g.drawString("Hiscore: " + hiscore, xp + 10, yp + 50);
	}
	
}
