package state.element;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;
import resource.bank.AudioBank;

public class TiledButton extends Button {

	private static final int CLICK_OFFSET = 1;
	
	private int tileW, tileH;
	
	private BufferedImage[] tiles;
	private BufferedImage[] clickTiles;
	private BufferedImage[] disabledTiles;
	private BufferedImage label;
	
	private Color fill;
	private Color clickFill;
	private Color disabledFill;
	
	public TiledButton(int x, int y, int w, int h) {
		super(x, y, w, h);
		setSounds(AudioBank.buttonDown, AudioBank.buttonUp);
	}

	public void setAllTileMaps(BufferedImage[] tiles, BufferedImage[] clickTiles, BufferedImage[] disabledTiles) {
		setTileMap(tiles);
		setClickTileMap(clickTiles);
		setDisabledTileMap(disabledTiles);
	}
	
	public void setLabel(BufferedImage label) {
		this.label = label;
	}
	
	public void setTileMap(BufferedImage[] tiles) {
		this.tiles = tiles;
		tileW = tiles[0].getWidth();
		tileH = tiles[0].getHeight();
		// try to make a fill color
		BufferedImage midTile = tiles[4];
		int color = midTile.getRGB(0, 0);
		boolean solid = true;
		for (int y = 0; y < midTile.getHeight() && solid; y++)
			for (int x = 0; x < midTile.getWidth() && solid; x++)
				 solid = midTile.getRGB(x, y) == color;
		if (solid)
			fill = new Color(tiles[4].getRGB(0, 0));
	}
	
	public void setClickTileMap(BufferedImage[] clickTiles) {
		this.clickTiles = clickTiles;
		BufferedImage midTile = clickTiles[4];
		int color = midTile.getRGB(0, 0);
		boolean solid = true;
		for (int y = 0; y < midTile.getHeight() && solid; y++)
			for (int x = 0; x < midTile.getWidth() && solid; x++)
				 solid = midTile.getRGB(x, y) == color;
		if (solid)
			clickFill = new Color(clickTiles[4].getRGB(0, 0));
	}
	
	public void setDisabledTileMap(BufferedImage[] disabledTiles) {
		this.disabledTiles = disabledTiles;
		BufferedImage midTile = disabledTiles[4];
		int color = midTile.getRGB(0, 0);
		boolean solid = true;
		for (int y = 0; y < midTile.getHeight() && solid; y++)
			for (int x = 0; x < midTile.getWidth() && solid; x++)
				 solid = midTile.getRGB(x, y) == color;
		if (solid)
			disabledFill = new Color(disabledTiles[4].getRGB(0, 0));
	}
	
	@Override
	public void render(Graphics g) {
		// set rendering constraints
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int dw = getWidth();
		int dh = getHeight();
		//
		boolean clicking = beingClicked(Input.LEFT_CLICK);
		boolean enabled = isEnabled();
		BufferedImage[] curTiles = null;
		// if disabled, and has a disabled background:
		if (!enabled && disabledTiles != null)
			curTiles = disabledTiles;
		// if clicking, and has a click background:
		else if (clicking && clickTiles != null)
			curTiles = clickTiles;
		// (default) if has a regular background:
		else if (tiles != null)
			curTiles = tiles;
		// draw tiles
		if (curTiles != null) {
			for (int y = 0; y < dh - tileH; y += tileH) {
				for (int x = 0; x < dw - tileW; x += tileW) {
					int tileNum = x == 0 ? y == 0 ? 0 : 3 : y == 0 ? 1 : 4;
					if (tileNum == 4 && fill != null)
						continue; // if we can just fill this tile with fill color, skip
					g.drawImage(curTiles[tileNum], xp + x, yp + y, null);
				}
				g.drawImage(curTiles[y == 0 ? 2 : 5], xp + dw - tileW, yp + y, null);
			}
			for (int x = 0; x < dw - tileW; x += tileW) {
				g.drawImage(curTiles[x == 0 ? 6 : 7], xp + x, yp + dh - tileH, null);
			}
			g.drawImage(curTiles[8], xp + dw - tileW, yp + dh - tileH, null);
		}
		// fill the middle of the button with color
		if (fill != null) {
			g.setColor(enabled ? clicking ? clickFill : fill : disabledFill);
			g.fillRect(xp + tileW, yp + tileH, dw - tileW * 2, dh - tileH * 2);
		}
		// now draw label over the button
		if (label != null) {
			// find center position
			int bias = clicking ? CLICK_OFFSET : 0;
			int lx = xp + (dw - label.getWidth()) / 2 + bias;
			int ly = yp + (dh - label.getHeight()) / 2 + bias;
			g.drawImage(label, lx, ly, null);
		}
		// if hovering, finally draw outline
		if (!clicking && beingHovered()) {
			g.setColor(hoverColor);
			g.drawRect(xp - 1, yp - 1, dw + 1, dh + 1);
		}
		// reset rendering constriants
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
