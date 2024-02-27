package state.element.location;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import resource.bank.ImageBank;
import state.element.Element;
import util.Animation;

public class Scoreboard extends Element {
	
	private int score;
	private Animation scoreAnim;
	
	public Scoreboard(int x, int y, int w, int h) {
		super(x, y, w, h);
		scoreAnim = new Animation(0, 1, LevelSetBox.ANIM_DURATION, Animation.EASE_OUT, Animation.NO_LOOP, false);
		scoreAnim.setDelay(LevelSetBox.ANIM_DELAY);
		setBackground(ImageBank.scoreboard);
	}
	
	public void setScore(int score) {
		this.score = score;
		scoreAnim.reset(true);
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
		String scoreText = Integer.toString((int) Math.ceil(score * scoreAnim.getValue()));
		for (int i = scoreText.length() - 1; i >= 0; i--)
			g.drawImage(ImageBank.scoreboarddigits[scoreText.charAt(i) - '0'],
					xp + getWidth() - (scoreText.length() - i) * digitWidth + xOff, yp + yOff, null);
		//
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}

}
