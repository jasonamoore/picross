package state;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import engine.Engine;
import engine.Transition;
import picnix.Island;
import resource.bank.AudioBank;
import resource.bank.FontBank;
import resource.bank.ImageBank;
import state.element.Dialogue;
import state.element.Icon;
import state.element.TiledButton;
import state.load.LoadGalleryState;
import util.Animation;

public class TitleState extends State {

	public static final int ZOOM_TIME = 1000;
	private static final int SKY_HEIGHT = 200;
	private static final int OFF_Y = 150;
	private static final double SCALE = 0.75;

	private static final int SPINNING = 0;
	private static final int READY_TO_ZOOM = 2;
	private static final int ZOOMING = 3;
	private static final int UNZOOMING = 4;

	private Animation smoothRot;
	private Animation zoomRot;
	private Animation zoomToIsland;
	
	private int zoomState;
	
	public TitleState() {
		zoomState = SPINNING;
		smoothRot = new Animation(0, 2 * Math.PI, 10000, Animation.LINEAR, Animation.CONTINUE, true);
		zoomRot = new Animation(ZOOM_TIME, Animation.EASE_OUT, Animation.NO_LOOP);
		zoomToIsland = new Animation(0, 1, 1000, Animation.EASE_OUT, Animation.NO_LOOP, false);
		Icon titleIcon = new Icon(ImageBank.title, Engine.getScreenCenterX(350), 50);
		TiledButton play = new TiledButton(Engine.getScreenCenterX(200), 175, 200, 70) {
			@Override
			public void onButtonUp() {
				playButtonClicked();
			}
		};
		play.setAllTileMaps(ImageBank.bluebutton, ImageBank.bluebuttonclick, ImageBank.buttondisabled);
		play.setLabel(ImageBank.playlabel);
		TiledButton gallery = new TiledButton(Engine.getScreenCenterX(200), 255, 200, 50) {
			@Override
			public void onButtonUp() {
				galleryButtonClicked();
			}
		};
		gallery.setAllTileMaps(ImageBank.greenbutton, ImageBank.greenbuttonclick, ImageBank.buttondisabled);
		gallery.setLabel(ImageBank.gallerylabel);
		add(titleIcon);
		add(play);
		add(gallery);
		add(new Dialogue(FontBank.defout, "welcome to picnix",
				"press play to start throwing some antastic picnic parties",
				"once you solve some puzzles the gallery will start framing pictures"));
	}

	@Override
	public void focus(int status) {
		AudioBank.titleMusic.reset(true);
		if (zoomState == READY_TO_ZOOM)
			startZoom(true);
	}
	
	private boolean zooming() {
		return zoomState == ZOOMING || zoomState == UNZOOMING;
	}
	
	public void setZoomAnim(boolean forward, double startRot, double endRot) {
		zoomState = READY_TO_ZOOM;
		zoomRot.setFrom(startRot);
		zoomRot.setTo(endRot);
		zoomRot.setForward(forward);
		zoomToIsland.setForward(forward);
	}
	
	private void startZoom(boolean unzoom) {
		zoomState = unzoom ? UNZOOMING : ZOOMING;
		zoomRot.reset(true);
		zoomToIsland.reset(true);
		smoothRot.pause();
	}
	
	private void playButtonClicked() {
		AudioBank.titleMusic.pause();
		setZoomAnim(true, smoothRot.getValue(), 0);
		startZoom(false);
		// open world state, with a "part A" delay so the zoom anim plays first
		Engine.getEngine().getStateManager().transitionToState(new WorldSelectState(0), Transition.NONE, ZOOM_TIME, 0);
	}
	
	private void galleryButtonClicked() {
		AudioBank.titleMusic.pause();
		Engine.getEngine().getStateManager().transitionToState(new LoadGalleryState(), Transition.FADE, 250, 0);
	}
	
	@Override
	public void tick() {
		super.tick();
		if (zoomState == UNZOOMING && !zoomToIsland.isPlaying()) {
			zoomState = SPINNING;
			double curRot = zoomRot.getValue();
			smoothRot.setFrom(curRot);
			smoothRot.setTo(curRot + 2 * Math.PI);
			smoothRot.reset(true);
		}
	}
	
	@Override
	public void render(Graphics g) {
		double progress = zoomToIsland.getValue();
		int h = (int) Math.round(SKY_HEIGHT + (WorldSelectState.SKY_HEIGHT - SKY_HEIGHT) * progress);
		int offx = (int) Math.round(WorldSelectState.OFF_X * progress);
		int offy = (int) Math.round(OFF_Y + (WorldSelectState.OFF_Y - OFF_Y) * progress);
		double scl = SCALE + (WorldSelectState.SCALE - SCALE) * progress;
		double rot = zooming() ? zoomRot.getValue() : smoothRot.getValue();
		Island.renderIsland(g, h, offx, offy, scl, rot);
		// render normal stuff
		// if sliding out of screen, translate
		Graphics2D gg = (Graphics2D) g;
		AffineTransform oldTrans = gg.getTransform();
		g.translate((int) Math.round(Engine.SCREEN_WIDTH * progress), 0);
		super.render(g);
		gg.setTransform(oldTrans);
	}

}
