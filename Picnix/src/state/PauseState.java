package state;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Transition;
import picnix.Parallax;
import resource.bank.AudioBank;
import resource.bank.ImageBank;
import state.element.Icon;
import state.element.TiledButton;

public class PauseState extends State {

	public static final int PAUSE_YOFFSET = 50;
	public static final int OPT_WIDTH = 100;
	public static final int OPT_HEIGHT = 50;
	
	private Parallax background;
	
	public PauseState() {
		AudioBank.pauseMusic.resume();
		BufferedImage paused = ImageBank.paused;
		Icon pause = new Icon(paused, Engine.getScreenCenterX(paused.getWidth()),
				Engine.getScreenCenterY(paused.getHeight()) - PAUSE_YOFFSET);
		TiledButton resume = new TiledButton(Engine.getScreenCenterX(OPT_WIDTH), 260,
				OPT_WIDTH, OPT_HEIGHT) {
			@Override
			public void onButtonUp() {
				resume();
			}
		};
		TiledButton quit = new TiledButton(Engine.getScreenCenterX(OPT_WIDTH), 320,
				OPT_WIDTH, OPT_HEIGHT) {
			@Override
			public void onButtonUp() {
				quit();
			}
		};
		resume.setAllTileMaps(ImageBank.greenbutton, ImageBank.greenbuttonclick, ImageBank.buttondisabled);
		quit.setAllTileMaps(ImageBank.redbutton, ImageBank.redbuttonclick, ImageBank.buttondisabled);
		add(pause);
		add(resume);
		add(quit);
		// parallax
		background = new Parallax(true, true);
		background.addLayer(ImageBank.pausepara, 0, 8000, true);
		background.resumeScroll();
	}

	@Override
	public void focus(int status) {
		// do little cute thing
	}
	
	public void resume() {
		AudioBank.pauseMusic.pause();
		Engine.getEngine().getStateManager().transitionExitState(Transition.SLIDE_BOTTOM, 250, 0);
	}
	
	public void quit() {
		AudioBank.pauseMusic.pause();
		AudioBank.pauseMusic.reset(false);
		Engine.getEngine().getStateManager().popUntilNextLoadState();
	}
	
	@Override
	public void render(Graphics g) {
		background.render(g);
		super.render(g);
	}
	
}
