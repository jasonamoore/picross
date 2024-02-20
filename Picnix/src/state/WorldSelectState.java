package state;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import engine.Engine;
import engine.Input;
import resource.bank.ImageBank;
import resource.bank.Palette;
import util.Animation;

public class WorldSelectState extends State {

	private double[] locations;
	private int curLoc;
	
	private Animation smoothRot;
	
	public WorldSelectState(double initRot) {
		smoothRot = new Animation(initRot, 0, 500, Animation.EASE_OUT, Animation.NO_LOOP, true);
		locations = new double[12];
		for (int i = 0; i < locations.length / 2 + 1; i++)
			locations[i] = i * 2 * Math.PI / 12;
		for (int i = 0; i < locations.length / 2; i++)
			locations[i + locations.length / 2] = -Math.PI + i * 2 * Math.PI / 12;
	}
	
	@Override
	public void focus(int status) {
	}
	
	@Override
	public void tick() {
		Input input = Input.getInstance();
		boolean leftKey = input.isPressingKey(KeyEvent.VK_LEFT);
		boolean rightKey = input.isPressingKey(KeyEvent.VK_RIGHT);
		if (leftKey && !rightKey) {
			int oldLoc = curLoc;
			curLoc = ++curLoc % locations.length;
			smoothRot(oldLoc);
			input.consumeKeyPress(KeyEvent.VK_LEFT);
		}
		else if (rightKey && !leftKey) {
			int oldLoc = curLoc;
			curLoc = (--curLoc + locations.length) % locations.length;
			smoothRot(oldLoc);
			input.consumeKeyPress(KeyEvent.VK_RIGHT);
		}
	}
	
	private void smoothRot(int oldLoc) {
		double from = smoothRot.getValue();
		if (curLoc == 6 && oldLoc == 5)
			from = from - 2 * Math.PI;
		else if (curLoc == 5 && oldLoc == 6)
			from = 2 * Math.PI + from;
		smoothRot.setFrom(from);
		smoothRot.setTo(locations[curLoc]);
		smoothRot.reset(true);
	}

	@Override
	public void render(Graphics g) {
		double rot = smoothRot.getValue();
		g.setColor(Palette.SKY);
		g.fillRect(0, 0, Engine.SCREEN_WIDTH, 150);
		g.setColor(Palette.PERIWINKLE);
		g.fillRect(0, 150, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - 150);
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		gg.setClip(0, 150, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - 150);
		gg.translate(-125 + Engine.SCREEN_WIDTH / 2, 75 + Engine.SCREEN_HEIGHT / 2);
        gg.scale(1, 0.5);
        gg.rotate(-rot);
		gg.translate(-Engine.SCREEN_WIDTH / 2, -Engine.SCREEN_HEIGHT / 2);
		gg.drawImage(ImageBank.island, 0, 0, null);
		gg.setClip(null);
		gg.setTransform(oldTrans);
	}
	
}
