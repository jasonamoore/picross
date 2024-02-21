package state;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import engine.Engine;
import engine.Transition;
import picnix.Island;
import picnix.World;
import resource.bank.ImageBank;
import state.element.Container;
import state.element.Icon;
import state.element.TiledButton;
import util.Animation;

public class TitleState extends State {

	private static final int SKY_HEIGHT = 200;
	private static final int OFF_Y = 150;
	private static final double SCALE = 0.75;
	
	private Animation smoothRot;
	private Animation zoomToIsland;
	
	private Container panel;
	
	public TitleState() {
		smoothRot = new Animation(0, 2 * Math.PI, 10000, Animation.LINEAR, Animation.CONTINUE, true);
		zoomToIsland = new Animation(0, 1, 1000, Animation.EASE_OUT, Animation.NO_LOOP, false);
		panel = new Container(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
		Icon titleIcon = new Icon(ImageBank.title);
		titleIcon.setBounds(Engine.getScreenCenterX(350), 50, 350, 125);
		TiledButton play = new TiledButton(Engine.getScreenCenterX(200), 250, 200, 100) {
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (beingHovered())
					playButtonClicked();
			}
		};
		play.setAllTileMaps(ImageBank.bluebutton, ImageBank.bluebuttonclick, ImageBank.buttondisabled);
		panel.add(titleIcon);
		panel.add(play);
		add(panel);
	}

	@Override
	public void focus(int status) {
		for (int i = 0; i < World.NUM_WORLDS; i++)
			World.loadWorld(i);
	}
	
	private void playButtonClicked() {
		smoothRot = new Animation(smoothRot.getValue(), 0, 1000, Animation.EASE_OUT, Animation.NO_LOOP, true);
		zoomToIsland.resume(); // play the zoom to island anim
		// open world state, with a "part A" delay so the zoom anim plays first
		Engine.getEngine().getStateManager().transitionToState(
				new WorldSelectState(0), Transition.NONE, 1000, 0, NEWLY_OPENED);
	}
			
	@Override
	public void render(Graphics g) {
		double progress = zoomToIsland.getValue();;
		int h = (int) (SKY_HEIGHT + (WorldSelectState.SKY_HEIGHT - SKY_HEIGHT) * progress);
		int offx = (int) (WorldSelectState.OFF_X * progress);
		int offy = (int) (OFF_Y + (WorldSelectState.OFF_Y - OFF_Y) * progress);
		double scl = SCALE + (WorldSelectState.SCALE - SCALE) * progress;
		double rot = smoothRot.getValue();
		Island.renderIsland(g, h, offx, offy, scl, rot);
		// render normal stuff
		// if sliding out of screen, translate
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		g.translate((int) (Engine.SCREEN_WIDTH * progress), 0);
		super.render(g);
		gg.setTransform(oldTrans);
	}

}
