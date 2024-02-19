package state.element;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import puzzle.Puzzle;
import resource.bank.ImageBank;

/**
 * A simple Element for displaying the completion progress
 * of a certain puzzle layer, in bar form.
 */
public class LayerProgress extends Element {
	
	// (max) fill size of the bar
	private static int BAR_WIDTH = 60;
	private static int BAR_HEIGHT = 11;
	
	// the puzzle this progress bar observes
	private Puzzle observed;
	// the color to draw the progress bar 
	private Color color;
	
	/**
	 * Creates a LayerProgress bar with the given Puzzle, Color,
	 * and the given bounds.
	 * @param p The Puzzle to observe progress of.
	 * @param c The fill Color of the progress bar.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public LayerProgress(Puzzle p, Color c, int x, int y, int w, int h) {
		super(x, y, w, h);
		observed = p;
		color = c;
	}
	
	/**
	 * Calculates the completion progress of the observed Puzzle.
	 * @return Progress, as a percentage (0 to 1).
	 */
	public double getProgress() {
		return observed.getCorrectCells() / (double) observed.getFilledCellsInSolution();
	}
	
	/**
	 * Draws the progress bar using the {@link #getProgress()} calculation.
	 */
	@Override
	public void render(Graphics g) {
		int xp = getDisplayX();
		int yp = getDisplayY();
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		g.drawImage(ImageBank.layerprogress, xp, yp, null);
		g.setColor(color);
		g.fillRect(xp + 1, yp + 1, (int) (getProgress() * BAR_WIDTH), BAR_HEIGHT);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
