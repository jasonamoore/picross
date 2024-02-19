package state.element;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Input;
import resource.bank.ImageBank;
import util.Animation;

public class Sidebar extends Container {

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
		collapser = new Button(x == TOOLBAR_X ? 7 : 59, 6, 14, 11) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (beingHovered() && mbutton == Input.LEFT_CLICK)
					setCollapsed(!collapsed);
			}
		};
		add(collapser);
		fadeAnim = new Animation(1, 0, 100, Animation.CUBIC, Animation.LOOP_NONE, false);
		collapseAnim = new Animation(SIDEBAR_H, 19, 500, Animation.CUBIC, Animation.LOOP_NONE, false);
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
		collapseAnim.setForward(collapsed, true);
		collapser.setBackgrounds(ImageBank.minimaxers[!collapsed ? 0 : 2],
				ImageBank.minimaxers[!collapsed ? 1 : 3], null);
	}
	
	@Override
	public float getOpacity() {
		return (float) fadeAnim.getValue();
	}
	
	public void tick() {
		super.tick();
		if (collapseAnim != null)
			setBounds(x, y, width, collapseAnim.getIntValue());
	}

	public void render(Graphics g) {
		Graphics2D gg = (Graphics2D) g;
		Composite oldComp = gg.getComposite();
		gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getOpacity()));
		super.render(g);
		BufferedImage bottom = x == TOOLBAR_X ? ImageBank.toolbarbottom : ImageBank.layerbarbottom;
		g.drawImage(bottom, getDisplayX(), getDisplayY() + height, null);
		gg.setComposite(oldComp);
	}
	
}
