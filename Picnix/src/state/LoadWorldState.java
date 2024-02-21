package state;

import engine.Engine;
import picnix.World;
import resource.bank.ImageBank;

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
		Engine.getEngine().getStateManager().openState(lss, State.NEWLY_OPENED);
	}

	@Override
	public void unload() {
		World.unloadWorld(worldId);
		//ImageBank.unloadWorldImages(worldId);
	}
	
	
}
