package state;

import engine.Engine;
import picnix.Level;
import picnix.World;

public class LoadLevelState extends LoadState {

	private int levelId;
	private int worldId;
	
	public LoadLevelState(int levelId, int worldId) {
		this.levelId = levelId;
		this.worldId = worldId;
	}
	
	@Override
	public void load() {
		World world = World.getWorld(worldId);
		Level level = world.loadLevel(levelId);
		//ImageBank.loadWorldImages(worldId);
		// when finished loading, open next state (puzzle state)
		PuzzleState ps = new PuzzleState(level);
		Engine.getEngine().getStateManager().openState(ps, State.NEWLY_OPENED);
	}

	@Override
	public void unload() {
		// SaveData . write ...
	}
	
	
}
