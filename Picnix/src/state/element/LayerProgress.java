package state.element;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import puzzle.Puzzle;
import resource.bank.ImageBank;

public class LayerProgress extends Element {

	private Puzzle observed;
	private Color color;
	
	private static int BAR_WIDTH = 60;
	private static int BAR_HEIGHT = 11;
	
	public LayerProgress(Puzzle p, Color c, int x, int y, int w, int h) {
		super(x, y, w, h);
		observed = p;
		color = c;
	}
	
	public double getProgress() {
		return observed.getCorrectCells() / (double) observed.getTotalCellsInSolution();
	}
	
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
