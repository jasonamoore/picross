package state.load;

import picnix.Level;
import picnix.World;
import state.PuzzleState;

public class LoadPuzzleState extends LoadState {

	private int levelId;
	private int worldId;
	
	public LoadPuzzleState(int levelId, int worldId) {
		this.levelId = levelId;
		this.worldId = worldId;
	}
	
	@Override
	public void load() {
		World world = World.getWorld(worldId);
		Level level = world.loadLevel(levelId);
		// when finished loading, open next state (puzzle state)
		PuzzleState ps = new PuzzleState(world, level);
		setNextState(ps);
		done();
	}

	@Override
	public void unload() {
		// SaveData . write ...
		setNextState(null);
		done();
	}
	
	
}
