package state.element;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;

/**
 * Extends the Element class by adding multiple backgrounds
 * that render depending on element state (clicking, disabled).
 * Also renders an outline on mouse hover, with customizable color.
 */
public class Button extends Element {

	// color for the hover outline
	protected Color hoverColor = Color.RED;
	// backgrounds for button states
	protected BufferedImage clickBackground;
	protected BufferedImage disabledBackground;
	
	/**
	 * Creates a Button with undefined bounds.
	 */
	public Button() {
		super();
	}
	
	/**
	 * Creates a Button with the given bounds.
	 * @param x The x position of the Button (relative to its parent).
	 * @param y The y position of the Button (relative to its parent).
	 * @param w The width of the Button.
	 * @param h The height of the Button.
	 */
	public Button(int x, int y, int w, int h) {
		super(x, y, w, h);
	}
	
	/**
	 * Sets all three button state backgrounds at once.
	 * @param reg The regular (default) background to render.
	 * @param cbg The background to render when button is being clicked.
	 * @param dis The background to render when the button is disabled.
	 */
	public void setBackgrounds(BufferedImage reg, BufferedImage cbg, BufferedImage dis) {
		setBackground(reg);
		setClickBackground(cbg);
		setDisabledBackground(dis);
	}
	
	/**
	 * Sets the button's background to render while being clicked.
	 * @param cbg The click background.
	 */
	public void setClickBackground(BufferedImage cbg) {
		clickBackground = cbg;
	}

	/**
	 * Sets the button's background to render when disabled.
	 * @param cbg The disabled background.
	 */
	public void setDisabledBackground(BufferedImage dis) {
		disabledBackground = dis;
	}
	
	/**
	 * Sets the color of the outline rendered when this button is being hovered.
	 * @param hc The hover color.
	 */
	public void setHoverOutlineColor(Color hc) {
		hoverColor = hc;
	}
	
	/**
	 * Renders the button, using whichever of its backgrounds
	 * is applicable to its current state and exists.
	 * Also draws a hover outline if the button is being
	 * hovered (and not clicked or disabled).
	 */
	@Override
	public void render(Graphics g) {
		// set rendering constraints
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		// if disabled, and has a disabled background:
		if (!isEnabled() && disabledBackground != null)
			g.drawImage(disabledBackground, xp, yp, null);
		// if clicking, and has a click background:
		else if (beingClicked(Input.LEFT_CLICK) && clickBackground != null)
			g.drawImage(clickBackground, xp, yp, null);
		// (default) if has a regular background:
		else if (background != null) {
			g.drawImage(background, xp, yp, null);
			// if hovering, draw outline
			if (beingHovered()) {
				g.setColor(hoverColor);
				g.drawRect(xp - 1, yp - 1, width + 1, height + 1);
			}
		}
		// reset rendering constriants
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
