package state.load;

import java.io.IOException;

import picnix.World;
import resource.bank.ImageBank;
import state.LevelSelectState;

public class LoadWorldState extends LoadState {

	private int worldId;
	
	public LoadWorldState(int worldId) {
		this.worldId = worldId;
	}
	
	@Override
	public void load() {
		// load world resources (background image)
		try {
			ImageBank.loadWorldResources(worldId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// when finished loading open next state (level select)
		LevelSelectState lss = new LevelSelectState(World.getWorld(worldId));
		setNextState(lss);
		done();
	}

	@Override
	public void unload() {
		// unload world resources (background image)
		ImageBank.unloadWorldResources(worldId);
		// next state = null (denotes exit transition)
		setNextState(null);
		done();
	}
	
	
}
