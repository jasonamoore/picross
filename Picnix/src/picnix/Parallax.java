package picnix;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Engine;
import util.Timer;

public class Parallax {

	private Timer scrollTimer;
	private int scrollAmount;
	
	// the image to draw for this layer
	private ArrayList<Layer> layers;
	
	private boolean horizontal;
	private boolean vertical;
	
	public Parallax(boolean h, boolean v) {
		doHorizontalParallax(h);
		doVerticalParallax(v);
		scrollTimer = new Timer(false);
		// default allocate space for 4 layers
		layers = new ArrayList<Layer>(4);
	}
	
	public void addLayer(BufferedImage sheet, int offset, int speed, boolean auto) {
		layers.add(new Layer(sheet, offset, speed, auto));
	}
	
	public void resumeScroll() {
		scrollTimer.resume();
	}
	
	public void pauseScroll() {
		scrollTimer.pause();
	}
	
	public void setScroll(int amount) {
		scrollAmount = amount;
	}
	
	public void doHorizontalParallax(boolean horizontal) {
		this.horizontal = horizontal;
	}
	
	public void doVerticalParallax(boolean vertical) {
		this.vertical = vertical;
	}
	
	protected int getStartX(int layerIndex) {
		Layer lay = layers.get(layerIndex);
		if (!horizontal)
			return lay.offset;
		long elapsed = !lay.auto ? scrollAmount : scrollTimer.elapsed();
		int speed = lay.speed;
		elapsed %= speed;
		double progress = elapsed / (double) speed;
		return (int) -(progress * lay.sheet.getWidth());
	}
	
	protected int getStartY(int layerIndex) {
		Layer lay = layers.get(layerIndex);
		if (!vertical)
			return lay.offset;
		long elapsed = !lay.auto ? scrollAmount : scrollTimer.elapsed();
		int speed = lay.speed;
		elapsed %= speed;
		double progress = elapsed / (double) speed;
		return (int) -(progress * lay.sheet.getHeight());
	}
	
	public void render(Graphics g) {
		for (int i = 0; i < layers.size(); i++) {
			BufferedImage sheet = layers.get(i).sheet;
			int sx = getStartX(i);
			int sy = getStartY(i);
			int imgw = sheet.getWidth();
			int imgh = sheet.getHeight();
			// tile the texture to fill screen
			for (int y = sy; y < Engine.SCREEN_HEIGHT; y += imgh) {
				for (int x = sx; x < Engine.SCREEN_WIDTH; x += imgw) {
					g.drawImage(sheet, x, y, null);
					if (!horizontal)
						break;
				}
				if (!vertical)
					break;
			}
		}
	}
	
}

class Layer {
	
	BufferedImage sheet;
	int offset;
	int speed;
	boolean auto;
	
	Layer(BufferedImage sheet, int offset, int speed, boolean auto) {
		this.sheet = sheet;
		this.offset = offset;
		this.speed = speed;
		this.auto = auto;
	}
	
}