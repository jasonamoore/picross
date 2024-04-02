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
	public static final int SLIDE_TOP = 3;

	private State fromState, toState;
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
	Transition(boolean exiting, State toState, State oldState, int type, int durA, int durB) {
		this.exiting = exiting;
		this.toState = toState;
		this.fromState = oldState;
		this.type = type;
		this.durA = durA;
		this.durB = durB;
		timer = new Timer(true);
	}

	State getFromState() {
		return fromState;
	}
	
	boolean isExitTransition() {
		return exiting;
	}
	
	State getToState() {
		return toState;
	}
	
	private boolean inPartA() {
		return !checked;
	}
	
	private double getAProgress() {
		if (durA == 0)
			return 1;
		return Math.max(0, Math.min(1, timer.elapsed() / (double) durA));
	}
	
	private double getBProgress() {
		if (durB == 0)
			return inPartA() ? 0 : 1;
		return Math.max(0, Math.min(1, (timer.elapsed() - durA) / (double) durB));
	}
	
	private State getActiveState() {
		return inPartA() ? fromState : toState;
	}
	
	boolean needsStateSwitch() {
		if (timer.elapsed() > durA && !checked)
			return (checked = true);
		else
			return false;
	}
	
	boolean isFinished() {
		return checked && timer.elapsed() > durA + durB;
	}
	
	void render(Graphics g) {
		State active = getActiveState();
		if (type == Transition.NONE)
			active.render(g); // render just the active state
		else {
			boolean inA = inPartA();
			double aprog = getAProgress();
			double bprog = getBProgress();
			if (type == Transition.FADE) {
				// render active state underneath
				active.render(g);
				float opacity = (float) (inA ? aprog : 1.0 - bprog);
				Graphics2D gg = (Graphics2D) g;
				Composite oldComp = gg.getComposite();
				gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				gg.setColor(Palette.BLACK);
				gg.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
				gg.setComposite(oldComp);
			}
			else if (type == Transition.CURTAIN) {
				// render active state underneath
				active.render(g);
				int half = Engine.SCREEN_WIDTH / 2;
				double progress = inA ? aprog : 1 - bprog;
				// curtain should hold closed for a while - adjust progress
				progress = 1 - Math.min(1, progress / 0.5);
				double x1 = -half * progress;
				double x2 = half + half * progress;
				g.setColor(Palette.RED);
				g.fillRect((int) x1, 0, half, Engine.SCREEN_HEIGHT);
				g.fillRect((int) x2, 0, half, Engine.SCREEN_HEIGHT);
			}
			else if (type == Transition.SLIDE_TOP) {
				if (inA) {
					// always render fromState underneath
					fromState.render(g);
					// find what position toState should be rendered at
					int y = (int) ((1 - aprog) * -Engine.SCREEN_HEIGHT);
					// translate graphics and render
					g.translate(0, y);
					toState.render(g);
					g.translate(0, -y); // reset
				}
				else // just render the toState
					active.render(g);
			}
		}
	}

	public static void renderClosedCurtains(Graphics g) {
		int half = Engine.SCREEN_WIDTH / 2;
		double progress = 1;
		// curtain should hold closed for a while - adjust progress
		progress = 1 - Math.min(1, progress / 0.5);
		double x1 = -half * progress;
		double x2 = half + half * progress;
		g.setColor(Palette.RED);
		g.fillRect((int) x1, 0, half, Engine.SCREEN_HEIGHT);
		g.fillRect((int) x2, 0, half, Engine.SCREEN_HEIGHT);
	}
	
}
