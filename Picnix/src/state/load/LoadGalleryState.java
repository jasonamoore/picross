package state.load;

import java.io.IOException;

import picnix.data.UserData;
import resource.bank.ImageBank;
import state.GalleryState;

public class LoadGalleryState extends LoadState {

	private int worldId;
	
	public LoadGalleryState(int worldId) {
		this.worldId = worldId;
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
		GalleryState gs = new GalleryState();
		setNextState(gs);
		done();
	}

	@Override
	public void unload() {
		// unload world resources (background image)
		ImageBank.unloadGalleryResources();
		// save what gallery items were seen
		UserData.save();
		// next state = null (denotes exit transition)
		setNextState(null);
		done();
	}
	
	
}
