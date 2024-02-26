package state.element.location;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import resource.bank.FontBank;
import resource.bank.ImageBank;
import state.element.Element;
import state.element.TextField;
import util.Animation;

public class WorldProgress extends Element {

	private static final int FILL_SIZE = 6;
	
	private TextField text;
	private double progress;
	private Animation progAnim;
	
	public WorldProgress(int x, int y, int w, int h) {
		super(x, y, w, h);
		int th = FontBank.test.getLineHeight();
		progAnim = new Animation(100, Animation.EASE_OUT, Animation.NO_LOOP);
		text = new TextField("test", FontBank.test, 0, (int) ((h - th) / 2), w, th);
		text.setAlignment(TextField.ALIGN_CENTER);
		add(text);
		setProgress(20, 102);
	}
	
	public void setProgress(int done, int total) {
		text.setText(String.format("%d / %d", done, total));
		progress = done / (double) total;
		progAnim.setFrom(progAnim.getValue());
		progAnim.setTo(progress);
		progAnim.reset(true);
	}
	
	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		// draw progress bar (background and progress fill, from anim)
		g.drawImage(background, xp, yp, null);
		g.clipRect(xp + 1, yp + 1, width - 2, height - 2);
		int fillX = 0;
		int fillWidth = (int) Math.ceil((width - 2) * progAnim.getValue());
		while (fillX < fillWidth) {
			g.drawImage(ImageBank.worldprogressfill, xp + 1 + fillX, yp + 1, null);
			fillX += FILL_SIZE;
		}
		//
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
