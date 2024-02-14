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

/**
 * A special type of Button used in the PuzzleState.
 * Controls navigation between puzzle layers, and
 * displays an iconified preview of the layer.
 */
public class LayerButton extends Button {

	// width/height of the mini preview
	public static final int PREVIEW_WIDTH = 60;
	public static final int PREVIEW_HEIGHT = 49;
	
	// amount the button visually insets on click
	private final int CLICK_SHIFT = 2;
	
	// parent PuzzleState
	private PuzzleState puzState;
	
	// the id of layer this button links to
	private int layerId;
	// whether this layer is active one
	private boolean active;
	// stores the rendered mini preview
	private BufferedImage preview;
	
	/**
	 * Creates a LayerButton tied to the given PuzzleState and the layer
	 * corresponding to the given id, with the given bounds.
	 * @param puzState
	 * @param id The id of the layer this LayerButton relates to.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public LayerButton(PuzzleState puzState, int id, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.puzState = puzState;
		layerId = id;
		makePreview();
	}

	/**
	 * @return The id of the layer this button corresponds to.
	 */
	public int getLayerId() {
		return layerId;
	}
	
	/**
	 * Marks whether the layer corresponding to this
	 * button is currently active.
	 * If active, this button should be disabled.
	 * If inactive, a mini preview needs to be rendered.
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
		setEnabled(!active);
		if (!active)
			makePreview();
	}

	/**
	 * Signals to the PuzzleState that this LayerButton was clicked.
	 */
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (beingHovered() && mbutton == Input.LEFT_CLICK)
			puzState.layerClicked(layerId);
	}
	
	/**
	 * Renders a mini preview of the puzzle layer this button corresponds to.
	 */
	private void makePreview() {
		// space to reserve on the edges of the preview
		final int verticalMargin = 4;
		// the puzzle we will render
		Puzzle toPreview = puzState.getPuzzleByLayerId(layerId);
		// create the preview BufferedImage if needed
		if (preview == null)
			preview = new BufferedImage(PREVIEW_WIDTH + CLICK_SHIFT, PREVIEW_HEIGHT + CLICK_SHIFT, BufferedImage.TYPE_INT_RGB);
		// get preview Graphics
		Graphics g = preview.getGraphics();
		int nr = toPreview.getRows();
		int nc = toPreview.getColumns();
		// how big to render each cell in the preview
		int cellSize = (PREVIEW_HEIGHT - verticalMargin) / nr;
		int offX = (PREVIEW_WIDTH - cellSize * nc) / 2 + CLICK_SHIFT;
		int offY = (PREVIEW_HEIGHT - cellSize * nr) / 2 + CLICK_SHIFT;
		// fill background with color
		g.setColor(Palette.PEAR);
		g.fillRect(0, 0, PREVIEW_WIDTH + CLICK_SHIFT, PREVIEW_HEIGHT + CLICK_SHIFT);
		// color of plate-cells (depends on layer id)
		Color plateCol = PuzzleState.getColorByLayerId(layerId);
		// loop through each row and column, render colored cell
		for (int r = 0; r < nr; r++) {
			for (int c = 0; c < nc; c++) {
				int mark = toPreview.getMark(r, c);
				if (mark == Puzzle.CLEARED)
					g.setColor(plateCol); // plate color
				else if (mark == Puzzle.FLAGGED)
					g.setColor(Palette.STONE); // fork color
				else { // if not a plate or fork, just use blanket color
					int f = r % 2 + c % 2;
					Color col = f == 0 ? Palette.RED : f == 1 ? Palette.PINK : Palette.WHITE;
					g.setColor(col);
				}
				// fill cell
				g.fillRect(offX + c * cellSize, offY + r * cellSize, cellSize, cellSize);
			}
		}
		g.dispose();
		// preview done!
	}

	/**
	 * Renders the layer frame and the mini preview, if applicable.
	 */
	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		boolean click = beingClicked(Input.LEFT_CLICK);
		if (!active)
			g.drawImage(preview, xp + 1 + (click ? 0 : -CLICK_SHIFT), yp + 1 + (click ? 0 : -CLICK_SHIFT), null);
		BufferedImage frame = !active ? ImageBank.layerframes[!click ? 0 : 1] : ImageBank.layeractive;
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
