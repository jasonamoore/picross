package state.load;

import picnix.World;
import state.LevelSelectState;

public class LoadWorldState extends LoadState {

	private int worldId;
	
	public LoadWorldState(int worldId) {
		this.worldId = worldId;
	}
	
	@Override
	public void load() {
		// .. = SaveData.loadWorldSaveData(worldId);
		// when finished loading open next state (level select)
		LevelSelectState lss = new LevelSelectState(World.getWorld(worldId));
		setNextState(lss);
		done();
	}

	@Override
	public void unload() {
		World.unloadWorld(worldId);
		//ImageBank.unloadWorldImages(worldId);
	}
	
	
}
