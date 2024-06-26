package picnix;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import engine.Engine;
import resource.bank.ImageBank;
import resource.bank.Palette;

public class Island {

	private static final double HEIGHT_WIDTH_RATIO = 0.5;
	
	private static ArrayList<IslandObject> objs;
	
	static {
		// make objects
		objs = new  ArrayList<IslandObject>();
		// make 25 random trees
		for (int i = 0; i < 25; i++)
			objs.add(new IslandObject(ImageBank.treeobjsheet, Math.random() * 175, Math.random() * 2 * Math.PI, 14, 16));
		// park building
		objs.add(new IslandObject(ImageBank.parkobjsheet, 125, Math.PI / 2, 24, 17));
	}
	
	public static double currRenderRot;
	
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
		/*
		 * for (int i = 0; i < trees.length; i++) { double rad =
		 * trees[i][0] * islandScale; double rot = trees[i][1]; int offx =
		 * ImageBank.tree.getWidth() / 2; int offy = ImageBank.tree.getHeight(); int tx
		 * = (int) (-offx + islandOffsetX + Engine.SCREEN_WIDTH / 2 +
		 * Math.cos(-islandRotation + rot) * rad); int ty = (int) (-offy + islandOffsetY
		 * + Engine.SCREEN_HEIGHT / 2 + Math.sin(-islandRotation + rot) * rad *
		 * HEIGHT_WIDTH_RATIO); g.drawImage(ImageBank.tree, tx, ty, null); }
		 */
		// draw island objects
		currRenderRot = islandRotation;
		Collections.sort(objs);
		for (int i = 0; i < objs.size(); i++) {
			IslandObject obj = objs.get(i);
			int offx = obj.width / 2;
			int offy = obj.height;
			double srad = obj.rad * islandScale;
			int tx = (int) (-offx + islandOffsetX + Engine.SCREEN_WIDTH / 2 + Math.cos(-islandRotation + obj.rot) * srad);
			int ty = (int) (-offy + islandOffsetY + Engine.SCREEN_HEIGHT / 2 + Math.sin(-islandRotation + obj.rot) * srad * HEIGHT_WIDTH_RATIO);
			g.drawImage(obj.getSprite(islandRotation), tx, ty, null);
		}
	}
	
}

class IslandObject implements Comparable<IslandObject> {
	
	BufferedImage[] sheet;
	double rot, rad;
	double faceRot;
	int width, height;
	
	public IslandObject(BufferedImage[] sheet, double rad, double rot, int width, int height) {
		this.sheet = sheet;
		this.rad = rad;
		this.rot = rot;
		this.width = width;
		this.height = height;
	}

	public BufferedImage getSprite(double totalRot) {
		totalRot += faceRot;
		// bound totalRot between 0 and 2PI:
		int circles = (int) (totalRot / (2 * Math.PI));
		double adjRot = totalRot - (circles * 2 * Math.PI);
		if (adjRot < 0)
			adjRot += 2 * Math.PI;
		double rotProp = adjRot / (2 * Math.PI);
		// index will be between 0 and sheet.length:
		int index = (int) (rotProp * sheet.length);
		return sheet[index];
	}

	@Override
	public int compareTo(IslandObject other) {
		double thisY = Math.sin(rot - Island.currRenderRot) * rad;
		double otherY = Math.sin(other.rot - Island.currRenderRot) * other.rad;
		return (int) (thisY - otherY);
	}
	
}