package state.element.puzzle;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Input;
import resource.bank.ImageBank;
import state.element.Button;
import state.element.Container;
import util.Animation;

public class Sidebar extends Container {

	private int type;
	
	private Button collapser;

	private Animation fadeAnim;
	private Animation collapseAnim;

	boolean fading;
	boolean collapsed;

	public static final int SIDEBAR_Y = 56;
	public static final int SIDEBAR_W = 80;
	public static final int SIDEBAR_H = 333;
	
	public static final int TOOLBAR_X = 0;
	public static final int LAYERBAR_X = Engine.SCREEN_WIDTH - SIDEBAR_W;
	
	public Sidebar(int x) {
		super(x, SIDEBAR_Y, SIDEBAR_W, SIDEBAR_H);
		type = x;
		collapser = new Button(x == TOOLBAR_X ? 7 : 59, 6, 14, 11) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (beingHovered() && mbutton == Input.LEFT_CLICK)
					setCollapsed(!collapsed);
			}
		};
		add(collapser);
		fadeAnim = new Animation(1, 0, 100, Animation.CUBIC, Animation.NO_LOOP, false);
		collapseAnim = new Animation(SIDEBAR_H, 19, 500, Animation.CUBIC, Animation.NO_LOOP, false);
		collapsed = false; // later this might be set by user's global prefs
		setCollapsed(collapsed);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		collapser.setVisible(visible);
	}
	
	public void setFade(boolean fade) {
		fading = fade;
		fadeAnim.reset(fade);
	}
	
	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean col) {
		collapsed = col;
		collapseAnim.setForward(collapsed);
		collapseAnim.resume();
		collapser.setBackgrounds(ImageBank.minimaxers[!collapsed ? 0 : 2],
				ImageBank.minimaxers[!collapsed ? 1 : 3], null);
	}
	
	@Override
	public float getOpacity() {
		return (float) fadeAnim.getValue();
	}
	
	@Override
	public int getHeight() {
		return collapseAnim.getIntValue();
	}

	public void render(Graphics g) {
		Graphics2D gg = (Graphics2D) g;
		Composite oldComp = gg.getComposite();
		gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getOpacity()));
		super.render(g);
		BufferedImage bottom = type == TOOLBAR_X ? ImageBank.toolbarbottom : ImageBank.layerbarbottom;
		g.drawImage(bottom, getDisplayX(), getDisplayY() + getHeight(), null);
		gg.setComposite(oldComp);
	}
	
}
