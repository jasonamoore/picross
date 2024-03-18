package state;

import state.element.BackButton;
import state.element.Gallery;

public class GalleryState extends State {

	private Gallery gallery;
	
	public GalleryState() {
		gallery = new Gallery();
		add(gallery);
		add(new BackButton());
	}

	@Override
	public void focus(int status) {
		// fancy reveal last completed puzzle (?)
	}

}
