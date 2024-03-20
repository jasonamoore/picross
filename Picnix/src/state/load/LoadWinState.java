package state.load;

import java.awt.Graphics;
import java.io.IOException;

import engine.Engine;
import engine.Transition;
import picnix.Level;
import picnix.World;
import resource.bank.ImageBank;
import state.WinState;

public class LoadWinState extends LoadState {

	private World world;
	private Level level;
	
	private boolean unloading;
	
	public LoadWinState(World world, Level level) {
		this.world = world;
		this.level = level;
		setTransitionType(Transition.CURTAIN);
		setTransitionDuration(750);
		setMinWaitTime(0);
	}
	
	@Override
	public void load() {
		// load gallery images
		try {
			ImageBank.loadGalleryResources();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// open gallery state
		WinState ws = new WinState(world, level);
		setNextState(ws);
		done();
	}

	@Override
	public void unload() {
		unloading = true;
		// unload world resources (background image)
		ImageBank.unloadGalleryResources();
		// next state = null (denotes exit transition)
		setNextState(null);
		done();
	}
	
	@Override
	public void tick() {
		if (!unloading)
			super.tick();
		else if (done)
			Engine.getEngine().getStateManager().popUntilNextLoadState();
	}
	
	@Override
	public void render(Graphics g) {
		Transition.renderClosedCurtains(g);
	}

}
