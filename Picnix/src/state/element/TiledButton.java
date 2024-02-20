package state.element;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;

public class TiledButton extends Button {

	private int tileW, tileH;
	
	private BufferedImage[] tiles;
	private BufferedImage[] clickTiles;
	private BufferedImage[] disabledTiles;
	
	public TiledButton(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	public void setAllTileMaps(BufferedImage[] tiles, BufferedImage[] clickTiles, BufferedImage[] disabledTiles) {
		setTileMap(tiles);
		setClickTileMap(clickTiles);
		setDisabledTileMap(disabledTiles);
	}
	
	public void setTileMap(BufferedImage[] tiles) {
		this.tiles = tiles;
		tileW = tiles[0].getWidth();
		tileH = tiles[0].getHeight();
	}
	
	public void setClickTileMap(BufferedImage[] clickTiles) {
		this.clickTiles = clickTiles;
	}
	
	public void setDisabledTileMap(BufferedImage[] disabledTiles) {
		this.disabledTiles = disabledTiles;
	}
	
	@Override
	public void render(Graphics g) {
		// set rendering constraints
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		BufferedImage[] curTiles = null;
		// if disabled, and has a disabled background:
		if (!isEnabled() && disabledTiles != null)
			curTiles = disabledTiles;
		// if clicking, and has a click background:
		else if (beingClicked(Input.LEFT_CLICK) && clickTiles != null)
			curTiles = clickTiles;
		// (default) if has a regular background:
		else if (tiles != null)
			curTiles = tiles;
		// draw tiles
		if (curTiles != null) {
			for (int y = 0; y < height - tileH; y += tileH) {
				for (int x = 0; x < width - tileW; x += tileW) {
					int tileNum = x == 0 ? y == 0 ? 0 : 3 : y == 0 ? 1 : 4;
					g.drawImage(curTiles[tileNum], xp + x, yp + y, null);
				}
				g.drawImage(curTiles[y == 0 ? 2 : 5], xp + width - tileW, yp + y, null);
			}
			for (int x = 0; x < width - tileW; x += tileW) {
				g.drawImage(curTiles[x == 0 ? 6 : 7], xp + x, yp + height - tileH, null);
			}
			g.drawImage(curTiles[8], xp + width - tileW, yp + height - tileH, null);
		}
		// if hovering, finally draw outline
		if (!beingClicked(Input.LEFT_CLICK) && beingHovered()) {
			g.setColor(hoverColor);
			g.drawRect(xp - 1, yp - 1, width + 1, height + 1);
		}
		// reset rendering constriants
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
