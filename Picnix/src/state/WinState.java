package state;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import engine.Engine;
import picnix.Level;
import picnix.World;
import resource.bank.ImageBank;
import resource.bank.Palette;
import util.Animation;
import util.Timer;

public class WinState extends State {

	private static final int SKY_HEIGHT = 100;
	private static final double HEIGHT_WIDTH_RATIO = 0.5;
	
	private World world;
	private Level level;
	
	private int rows, cols;
	
	private BufferedImage background;
	
	private Timer simTimer;
	private boolean simulating;
	private boolean topdown;
	
	private Animation smoothRot;
	
	public WinState(World world, Level level) {
		this.world = world;
		this.level = level;
		rows = level.getPuzzles()[0].getRows();
		cols = level.getPuzzles()[0].getColumns();
		background = ImageBank.backgrounds[world.getId()];
		simTimer = new Timer(false);
		smoothRot = new Animation(0, 2 * Math.PI, 10000, Animation.LINEAR, Animation.CONTINUE, true);
	}

	@Override
	public void focus(int status) {
		simulating = true;
		simTimer.resume();
	}
	
	@Override
	public void tick() {
		if (simulating) {
			
		}
	}
	
	@Override
	public void render(Graphics g) {
		renderField(g, 1, smoothRot.getValue());
	}
	
	public void renderField(Graphics g, double fieldScale, double fieldRotation) {
		// draw sky
		g.setColor(Palette.SKY);
		g.fillRect(0, 0, Engine.SCREEN_WIDTH, SKY_HEIGHT);
		g.setColor(Palette.PERIWINKLE);
		g.fillRect(0, SKY_HEIGHT, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - SKY_HEIGHT);

		final int size = 15;
		// draw field
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		// clip field below horizon
		gg.setClip(0, SKY_HEIGHT, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - SKY_HEIGHT);
		// translate field to origin plus given offset before rotating
		gg.translate(Engine.SCREEN_WIDTH / 2, Engine.SCREEN_HEIGHT / 2);
		// scale based on islandScale argument and island's width-to-height ratio
        gg.scale(fieldScale, fieldScale * HEIGHT_WIDTH_RATIO);
        // rotate based on argument
        gg.rotate(fieldRotation);
        // translate back, so drawing will be at the desired position
		gg.translate(-(size * rows) / 2, -(size * cols) / 2);
		// draw blanket
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				g.drawImage(ImageBank.cells15[r % 2 + c % 2], c * size, r * size, null);
			}
		}
		// revert graphics state
		gg.setClip(null);
		gg.setTransform(oldTrans);
		// draw field entities
		/*
		for (int i = 0; i < trees.length; i++) {
			double rad = trees[i][0] * islandScale;
			double rot = trees[i][1];
			int offx = ImageBank.tree.getWidth() / 2;
			int offy = ImageBank.tree.getHeight();
			int tx = (int) (-offx + islandOffsetX + Engine.SCREEN_WIDTH / 2 + Math.cos(-islandRotation + rot) * rad);
			int ty = (int) (-offy + islandOffsetY + Engine.SCREEN_HEIGHT / 2 + Math.sin(-islandRotation + rot) * rad * HEIGHT_WIDTH_RATIO);
			g.drawImage(ImageBank.tree, tx, ty, null);
		} */
	}
	
}
