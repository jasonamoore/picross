package picnix;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import engine.Engine;
import resource.bank.ImageBank;
import resource.bank.Palette;

public class Island {

	private static final double HEIGHT_WIDTH_RATIO = 0.5;
	
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
		// translate to island to origin plus given offset before rotating
		gg.translate(islandOffsetX + Engine.SCREEN_WIDTH / 2, islandOffsetY + Engine.SCREEN_HEIGHT / 2);
		// scale based on islandScale argument and island's width-to-height ratio
        gg.scale(islandScale, islandScale * HEIGHT_WIDTH_RATIO);
        // rotate based on argument
        gg.rotate(-islandRotation);
        // translate back, so drawing will be at the desired position
		gg.translate(-Engine.SCREEN_WIDTH / 2, -Engine.SCREEN_HEIGHT / 2);
		gg.drawImage(ImageBank.island, 0, 0, null);
		// revert graphics state
		gg.setClip(null);
		gg.setTransform(oldTrans);
	}
	
}
