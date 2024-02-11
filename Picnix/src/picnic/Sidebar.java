package picnic;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import engine.Engine;
import engine.Input;
import resource.bank.ImageBank;
import state.element.Button;
import state.element.Container;
import util.Animation;

public class Sidebar extends Container {

	Button collapser;

	Animation fadeAnim;
	Animation collapseAnim;

	boolean fading;
	boolean collapsed;

	private static final int SIDEBAR_Y = 56;
	private static final int SIDEBAR_W = 80;
	private static final int SIDEBAR_H = 336;
	
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
		collapseAnim = new Animation(SIDEBAR_H, 20, 500, Animation.CUBIC, Animation.LOOP_NONE, false);
		collapsed = false; // later this might be set by user's global prefs
		setCollapsed(collapsed);
	}

	public void setFade(boolean fade) {
		fading = fade;
		fadeAnim.reset(fade);
	}

	public void setCollapsed(boolean col) {
		collapsed = col;
		collapseAnim.setForward(collapsed, true);
		setChildrenDisabled(collapsed);
		collapser.setDisabled(false); // always enabled
		//
		if (collapsed) {}
			//collapser.setBackgrounds(ImageBank.maximize[0], ImageBank.maximize[1]);
		else
			collapser.setBackgrounds(ImageBank.minimize[0], ImageBank.minimize[1]);
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
		gg.setComposite(oldComp);
	}
	
}
