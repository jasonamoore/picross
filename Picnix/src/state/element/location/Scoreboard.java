package state.element.location;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import resource.bank.ImageBank;
import state.element.Element;

public class Scoreboard extends Element {
	
	private int score = 981276450;
	
	public Scoreboard(int x, int y, int w, int h) {
		super(x, y, w, h);
		setBackground(ImageBank.scoreboard);
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		// positioning stuff for digits
		final int digitWidth = 14;
		final int xOff = -2, yOff = 2;
		// draw the score digits
		g.drawImage(background, xp, yp, null);
		String scoreText = Integer.toString(score);
		for (int i = scoreText.length() - 1; i >= 0; i--)
			g.drawImage(ImageBank.scoreboarddigits[scoreText.charAt(i) - '0'],
					xp + width - (scoreText.length() - i) * digitWidth + xOff, yp + yOff, null);
		//
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}

}
