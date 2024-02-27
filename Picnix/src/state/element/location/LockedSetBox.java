package state.element.location;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import resource.bank.ImageBank;
import resource.bank.Palette;
import state.element.Element;
import state.element.Icon;
import util.Animation;

public class LockedSetBox extends Element {

	private static final int KEY_START_X = 10;
	private static final int KEY_MOVE_DISTANCE = 75;
	private static final int KEY_Y = 66;

	private static int LOCK_LEFT_X = 94;
	private static int LOCK_RIGHT_X = 94 + 17;
	private static int LOCK_WIDTH = 36;
	private static int LOCK_Y = 50;
	
	private Animation keyAnim;
	private WorldProgress progBar;
	
	public LockedSetBox() {
		Icon lockedLabel = new Icon(ImageBank.lockedlabel, 13, 12);
		progBar = new WorldProgress(27, 111, 92, 23);
		progBar.setBackground(ImageBank.lockedprogress);
		// don't show any text in the progress bar
		progBar.setChildrenExisting(false);
		keyAnim = new Animation(LevelSetBox.ANIM_DURATION, Animation.EASE_OUT, Animation.NO_LOOP);
		keyAnim.setDelay(LevelSetBox.ANIM_DELAY);
		add(lockedLabel);
		add(progBar);
	}
	
	public void setProgress(int done, int total) {
		progBar.setProgress(done, total);
		keyAnim.setFrom(KEY_START_X);
		keyAnim.setTo(KEY_START_X + done / (double) total * KEY_MOVE_DISTANCE);
		keyAnim.reset(true);
	}
	
	@Override
	public void render(Graphics g) {
		super.render(g);
		setRenderClips(g);
		Composite oldComp  = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int dw = getWidth();
		int dh = getHeight();
		// fill background color
		g.setColor(Palette.GREY);
		g.fillRect(xp + 2, yp + 2, dw - 2, dh - 2);
		g.setColor(Palette.RAIN);
		g.fillRect(xp, yp, 2, dh);
		g.fillRect(xp, yp, dw, 2);
		// draw key and lock
		g.clipRect(xp, yp, LOCK_LEFT_X + LOCK_WIDTH, dh);
		g.drawImage(ImageBank.lock[0], xp + LOCK_LEFT_X, yp + LOCK_Y, null);
		g.drawImage(ImageBank.key, xp + keyAnim.getIntValue(), yp + KEY_Y, null);
		g.drawImage(ImageBank.lock[1], xp + LOCK_RIGHT_X, yp + LOCK_Y, null);
		// draw lines from easy score to locked bar (violates clip)
		g.setClip(null);
		g.setColor(Palette.TIDAL);
		g.translate(xp, yp);
		g.drawLine(109, 134, 109, 140);
		g.drawLine(109, 140, 140, 140);
		g.drawLine(114, 134, 114, 137);
		g.drawLine(114, 137, 140, 137);
		g.drawLine(140, 140, 140, 0);
		g.drawLine(140, -10, 140, -15);
		g.drawLine(140, -15, 122, -15);
		g.drawLine(122, -15, 122, -19);
		g.drawLine(134, -15, 134, -19);
		g.translate(-xp, -yp);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
