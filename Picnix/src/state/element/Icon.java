package state.element;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A concrete version of Element, with no extra functionality,
 * aside from slightly tweaked rendering.
 */
public class Icon extends Element {
	
	/**
	 * Creates an Icon with undefined bounds and a given background.
	 * @param image The background image of the Icon.
	 */
	public Icon(BufferedImage image) {
		setBackground(image);
	}
	
	/**
	 * Creates an Icon with the given bounds and given background.
	 * @param x The x position of the Icon (relative to its parent).
	 * @param y The y position of the Icon (relative to its parent).
	 * @param w The width of the Icon.
	 * @param h The height of the Icon.
	 * @param image The background image of the Icon.
	 */
	public Icon(BufferedImage image, int x, int y, int w, int h) {
		super(x, y, w, h);
		setBackground(image);
	}

	/**
	 * Creates an Icon with the given position and given background.
	 * The width and height of the element will be equal to that of
	 * the image supplied as its background.
	 * @param x The x position of the Icon (relative to its parent).
	 * @param y The y position of the Icon (relative to its parent).
	 * @param image The background image of the Icon.
	 */
	public Icon(BufferedImage image, int x, int y) {
		this(image, x, y, image.getWidth(), image.getHeight());
	}

	/**
	 * Differs from the default rendering behavior in that it
	 * forces the background image to scale to the Icon's size bounds.
	 */
	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		if (background != null)
			g.drawImage(background, getDisplayX(), getDisplayY(), getWidth(), getHeight(), null);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}	
	
}
