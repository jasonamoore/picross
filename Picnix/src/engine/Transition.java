package engine;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import resource.bank.Palette;
import state.State;
import util.Timer;

public class Transition {

	public static final int NONE = 0;
	public static final int FADE = 1;
	public static final int CURTAIN = 2;

	private State toState;
	private State oldState;
	private int type;
	private int durA, durB;
	
	private boolean exiting;
	private boolean checked;
	private Timer timer;
	
	/**
	 * 
	 * @param state
	 * @param type
	 * @param durA
	 * @param durB
	 */
	Transition(State toState, State oldState, int type, int durA, int durB) {
		this.toState = toState;
		this.oldState = oldState;
		exiting = toState == null;
		this.type = type;
		this.durA = durA;
		this.durB = durB;
		timer = new Timer(true);
	}

	public State getOldState() {
		return oldState;
	}
	
	public boolean isExitTransition() {
		return exiting;
	}
	
	public State getToState() {
		return toState;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean inPartA() {
		return !checked;
	}
	
	public double getAProgress() {
		if (durA == 0)
			return 1;
		return Math.max(0, Math.min(1, timer.elapsed() / (double) durA));
	}
	
	public double getBProgress() {
		if (durB == 0)
			return inPartA() ? 0 : 1;
		return Math.max(0, Math.min(1, (timer.elapsed() - durA) / (double) durB));
	}
	
	public boolean needsStateSwitch() {
		if (timer.elapsed() > durA && !checked)
			return (checked = true);
		else
			return false;
	}
	
	public boolean isFinished() {
		return checked && timer.elapsed() > durA + durB;
	}
	
	public void render(Graphics g) {
		if (type == Transition.NONE)
			; // render nothing
		else if (type == Transition.FADE) {
			float opacity = (float) (inPartA() ? getAProgress() : 1.0 - getBProgress());
			Graphics2D gg = (Graphics2D) g;
			Composite oldComp = gg.getComposite();
			gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			gg.setColor(Palette.BLACK);
			gg.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
			gg.setComposite(oldComp);
		}
		else if (type == Transition.CURTAIN) {
			int half = Engine.SCREEN_WIDTH / 2;
			double progress = inPartA() ? getAProgress() : 1 - getBProgress();
			// curtain should hold closed for a while - adjust progress
			progress = 1 - Math.min(1, progress / 0.5);
			double x1 = -half * progress;
			double x2 = half + half * progress;
			g.setColor(Palette.RED);
			g.fillRect((int) x1, 0, half, Engine.SCREEN_HEIGHT);
			g.fillRect((int) x2, 0, half, Engine.SCREEN_HEIGHT);
		}
	}
	
}
