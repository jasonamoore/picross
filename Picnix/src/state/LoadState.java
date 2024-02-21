package state;

import java.awt.Graphics;

public abstract class LoadState extends State {
	
	@Override
	public void focus(int status) {
		if (status == State.NEWLY_OPENED)
			load();
		else
			unload();
	}
	
	public abstract void load();
	
	public abstract void unload();
	
	@Override
	public void render(Graphics g) {
		// render load animation
		
		// render tips or whatever
	}
	
}
