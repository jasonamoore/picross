package resource;

import java.util.ArrayList;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class SoundEffect {

	private Clip[] belt;
	
	private ArrayList<Clip> ready;
	
	public SoundEffect(Clip[] belt) {
		this.belt = belt;
		ready = new ArrayList<Clip>(belt.length);
		for (int i = 0; i < belt.length; i++) {
			belt[i].addLineListener(new SoundEffectListener());
			ready.add(belt[i]);
		}
	}
	
	public void play() {
		if (ready.size() == 0)
			return;
		ready.remove(0).start();
	}

	protected void readyClip(Clip clip) {
		clip.setFramePosition(0);
		ready.add(0, clip);
	}

	private class SoundEffectListener implements LineListener {

		@Override
		public void update(LineEvent event) {
			LineEvent.Type type = event.getType();
			Clip stopped = (Clip) event.getSource();
			if (type == LineEvent.Type.STOP && stopped.getFramePosition() == stopped.getFrameLength())
				readyClip((Clip) event.getSource());
		}
		
	}
	
}