package state.element;

import java.awt.Graphics;

import engine.Input;
import picnix.data.UserData;
import resource.bank.ImageBank;
import state.LevelSelectState;

public class LevelButton extends Button {
	
	private LevelSelectState levState;
	private int id;
	
	public LevelButton(LevelSelectState levState, int id, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.levState = levState;
		this.id = id;
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (mbutton == Input.LEFT_CLICK && beingHovered())
			levState.levelClicked(id);
	}
	
	@Override
	public void render(Graphics g) {
		super.render(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int wid = levState.getWorld().getId();
		if (UserData.isPuzzleCompleted(wid, id)) {
			g.drawImage(ImageBank.hiscorebar, xp + 3, yp + 37, null);
			int score = UserData.getPuzzleScore(wid, id);
			final int startX = 13, startY = 38;
			final int pWidth = 29, cWidth = 5;
			final int max = 999_999;
			String count = Integer.toString(Math.min(max, score));
			int xOff = startX + (pWidth - cWidth * count.length() + 1) / 2;
			for (int c = 0; c < count.length(); c++)
				g.drawImage(ImageBank.smallblacknums[count.charAt(c) - '0'],
						xp + c * cWidth + xOff, yp + startY, null);
		}
		else {
			g.drawImage(ImageBank.newlevelalert, xp + 4, yp + 4, null);
		}
	}
	
}
