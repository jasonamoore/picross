package state.element;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;
import resource.SoundEffect;
import resource.bank.Palette;

/**
 * Extends the Element class by adding multiple backgrounds
 * that render depending on element state (clicking, disabled).
 * Also renders an outline on mouse hover, with customizable color.
 */
public class Button extends Element {

	// color for the hover outline
	protected Color hoverColor = Palette.WHITE;
	// backgrounds for button states
	protected BufferedImage clickBackground;
	protected BufferedImage disabledBackground;
	
	// sfx for clicking
	private SoundEffect onDownSound;
	private SoundEffect onUpSound;
	
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
	
	public void setSounds(SoundEffect down, SoundEffect up) {
		onDownSound = down;
		onUpSound = up;
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
	 * Sets the clips to include just outside of this element's width and height,
	 * so that button's hover outlines can render properly.
	 */
	@Override
	public void setRenderClips(Graphics g) {
		// clip to parent bounds
		if (parent != null)
			g.setClip(parent.getDisplayX(), parent.getDisplayY(), parent.getWidth(), parent.getHeight());
		// add clip to only draw bg image within this elem's bounds
		g.clipRect(getDisplayX() - 1, getDisplayY() - 1, getWidth() + 2, getHeight() + 2);
	}
	
	@Override
	public void onClick(int mbutton) {
		super.onClick(mbutton);
		if (mbutton == Input.LEFT_CLICK)
			if (onDownSound != null) onDownSound.play();
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (mbutton == Input.LEFT_CLICK
				&& beingHovered()) {
			if (onUpSound != null) onUpSound.play();
			onButtonUp();
		}
	}
	
	protected void onButtonUp() {}
	
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
			if (beingHovered() && hoverColor != null) {
				g.setColor(hoverColor);
				g.drawRect(xp - 1, yp - 1, getWidth() + 1, getHeight() + 1);
			}
		}
		// reset rendering constriants
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
