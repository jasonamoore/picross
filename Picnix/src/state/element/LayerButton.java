package state.element;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;
import puzzle.Puzzle;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.PuzzleState;

public class LayerButton extends Button {

	private static final int FRAME_NORMAL = 0;
	private static final int FRAME_CLICK = 1;

	public static final int PREVIEW_WIDTH = 60;
	public static final int PREVIEW_HEIGHT = 49;
	
	private PuzzleState puzState;
	
	private int layerId;
	
	private BufferedImage preview;
	
	private boolean active;
	
	public LayerButton(PuzzleState puzState, int id, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.puzState = puzState;
		layerId = id;
		makePreview();
	}
	
	public void setActive(boolean active) {
		this.active = active;
		setDisabled(active);
		if (!active)
			makePreview();
	}
	
	private final int CLICK_SHIFT = 2;
	
	private void makePreview() {
		final int verticalMargin = 4;
		Puzzle toPreview = puzState.getPuzzleByLayerId(layerId);
		preview = new BufferedImage(PREVIEW_WIDTH + CLICK_SHIFT, PREVIEW_HEIGHT + CLICK_SHIFT, BufferedImage.TYPE_INT_RGB);
		Graphics g = preview.getGraphics();
		int nr = toPreview.getRows();
		int nc = toPreview.getColumns();
		int cellSize = (PREVIEW_HEIGHT - verticalMargin) / nr;
		int offX = (PREVIEW_WIDTH - cellSize * nc) / 2 + CLICK_SHIFT;
		int offY = (PREVIEW_HEIGHT - cellSize * nr) / 2 + CLICK_SHIFT;
		g.setColor(Palette.PEAR);
		g.fillRect(0, 0, PREVIEW_WIDTH + CLICK_SHIFT, PREVIEW_HEIGHT + CLICK_SHIFT);
		Puzzle puzzle = puzState.getPuzzleByLayerId(layerId);
		Color plateCol = layerId == PuzzleState.MAGENTA ? Palette.MAGENTA :
			layerId == PuzzleState.YELLOW ? Palette.YELLOW : Palette.CYAN;
		for (int r = 0; r < nr; r++) {
			for (int c = 0; c < nc; c++) {
				int mark = puzzle.getMark(r, c);
				if (mark == Puzzle.CLEARED)
					g.setColor(plateCol);
				else if (mark == Puzzle.FLAGGED)
					g.setColor(Palette.STONE);
				else {
					int f = r % 2 + c % 2;
					Color col = f == 0 ? Palette.RED : f == 1 ? Palette.PINK : Palette.WHITE;
					g.setColor(col);
				}
				g.fillRect(offX + c * cellSize, offY + r * cellSize, cellSize, cellSize);
			}
		}
		g.dispose();
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (beingHovered() && mbutton == Input.LEFT_CLICK)
			puzState.layerClicked(layerId);
	}
	
	public int getLayerId() {
		return layerId;
	}

	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		boolean click = beingClicked(Input.LEFT_CLICK);
		if (!active)
			g.drawImage(preview, xp + 1 + (click ? 0 : -CLICK_SHIFT), yp + 1 + (click ? 0 : -CLICK_SHIFT), null);
		BufferedImage frame = !active ? ImageBank.layerframes[click ? FRAME_CLICK : FRAME_NORMAL] : ImageBank.layeractive;
		BufferedImage label = ImageBank.layernames[layerId];
		g.drawImage(frame, xp, yp, null);
		g.drawImage(label, xp, yp + 50, null);
		if (beingHovered()) { // draw focus rectangle
			g.setColor(hoverColor);
			g.drawRect(xp - 1, yp - 1, width + 1, height + 1);
		}
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
