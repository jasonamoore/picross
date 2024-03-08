package state;

import engine.Engine;
import engine.Transition;
import state.element.BackButton;
import state.element.Button;

public class GalleryState extends State {

	private Gallery gallery;
	
	public GalleryState() {
		gallery = new Gallery();
		add(gallery);
		add(new BackButton());
	}

	@Override
	public void focus(int status) {
		
	}

}
