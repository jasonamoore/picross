package picnix;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import engine.Engine;
import resource.bank.ImageBank;
import resource.bank.Palette;

public class Island {

	private static final double HEIGHT_WIDTH_RATIO = 0.5;
	
	private static final int NUM_OBJS = 1;
	
	private static double[][] trees;
	private static IslandObject[] objs;
	
	static {
		// make 25 random trees
		trees = new double[25][2];
		for (int i = 0; i < trees.length; i++) {
			trees[i][0] = Math.random() * 175;
			trees[i][1] = Math.random() * 2 * Math.PI;
		}
		// make objects
		objs = new IslandObject[NUM_OBJS];
		objs[0] = new IslandObject(ImageBank.parkobjsheet, 25, Math.PI / 5, 24, 17);
	}
	
	public static void renderIsland(Graphics g, int skyHeight, int islandOffsetX, int islandOffsetY, double islandScale, double islandRotation) {
		// draw sky
		g.setColor(Palette.SKY);
		g.fillRect(0, 0, Engine.SCREEN_WIDTH, skyHeight);
		g.setColor(Palette.PERIWINKLE);
		g.fillRect(0, skyHeight, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - skyHeight);
		
		// draw island
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		// clip island below horizon
		gg.setClip(0, skyHeight, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT - skyHeight);
		// translate island to origin plus given offset before rotating
		gg.translate(islandOffsetX + Engine.SCREEN_WIDTH / 2, islandOffsetY + Engine.SCREEN_HEIGHT / 2);
		// scale based on islandScale argument and island's width-to-height ratio
        gg.scale(islandScale, islandScale * HEIGHT_WIDTH_RATIO);
        // rotate based on argument
        gg.rotate(-islandRotation);
        // translate back, so drawing will be at the desired position
		gg.translate(-ImageBank.island.getWidth() / 2, -ImageBank.island.getHeight() / 2);
		// draw island
		gg.drawImage(ImageBank.island, 0, 0, null);
		// revert graphics state
		gg.setClip(null);
		gg.setTransform(oldTrans);
		// draw island entities
		for (int i = 0; i < trees.length; i++) {
			double rad = trees[i][0] * islandScale;
			double rot = trees[i][1];
			int offx = ImageBank.tree.getWidth() / 2;
			int offy = ImageBank.tree.getHeight();
			int tx = (int) (-offx + islandOffsetX + Engine.SCREEN_WIDTH / 2 + Math.cos(-islandRotation + rot) * rad);
			int ty = (int) (-offy + islandOffsetY + Engine.SCREEN_HEIGHT / 2 + Math.sin(-islandRotation + rot) * rad * HEIGHT_WIDTH_RATIO);
			g.drawImage(ImageBank.tree, tx, ty, null);
		}
		// draw island objects
		for (int i = 0; i < objs.length; i++) {
			IslandObject obj = objs[i];
			int offx = obj.width / 2;
			int offy = obj.height;
			int tx = (int) (-offx + islandOffsetX + Engine.SCREEN_WIDTH / 2 + Math.cos(-islandRotation + obj.rot) * obj.rad);
			int ty = (int) (-offy + islandOffsetY + Engine.SCREEN_HEIGHT / 2 + Math.sin(-islandRotation + obj.rot) * obj.rad * HEIGHT_WIDTH_RATIO);
			g.drawImage(obj.getSprite(islandRotation + obj.rot), tx, ty, null);
		}
	}
	
}

class IslandObject {
	
	BufferedImage[] sheet;
	double rot, rad;
	int width, height;
	
	public IslandObject(BufferedImage[] sheet, double rad, double rot, int width, int height) {
		this.sheet = sheet;
		this.rad = rad;
		this.rot = rot;
		this.width = width;
		this.height = height;
	}

	public BufferedImage getSprite(double totalRot) {
		// bound totalRot between 0 and 2PI:
		int circles = (int) (totalRot / (2 * Math.PI));
		double adjRot = totalRot - (circles * 2 * Math.PI);
		if (adjRot < 0)
			adjRot += 2 * Math.PI;
		double chunkSize = (2 * Math.PI) / sheet.length;
		// index will be between 0 and sheet.length:
		int index = (int) Math.floor(adjRot / chunkSize);
		return sheet[index];
	}
	
}