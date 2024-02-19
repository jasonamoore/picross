package state.element;

import java.awt.Graphics;

import resource.bank.Palette;
import state.LevelSelectState;

public class LevelButton extends Button {

	private LevelSelectState levSelState;
	private int id;
	
	public LevelButton(LevelSelectState levSelState, int id, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.levSelState = levSelState;
		this.id = id;
	}
	
	@Override
	public void onClick(int mbutton) {
		super.onClick(mbutton);
		levSelState.levelClicked(id);
	}
	
	@Override
	public void render(Graphics g) {
		int xp = getDisplayX();
		int yp = getDisplayY();
		g.setColor(Palette.PURPLE);
		g.fillRect(xp, yp, width, height);
		g.setColor(Palette.WHITE);
		g.drawString(Integer.toString(id), xp, yp);
	}
	
}
