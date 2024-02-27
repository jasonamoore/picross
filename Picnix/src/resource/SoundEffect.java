package resource;

import javax.sound.sampled.Clip;

public class SoundEffect {

	private Clip[] belt;
	private int beltHead;
	
	public SoundEffect(Clip[] belt) {
		this.belt = belt;
		beltHead = 0;
	}
	
	public void play() {
		Clip curClip = belt[beltHead];
		if (!curClip.isRunning()) {
			curClip.setFramePosition(0);
			curClip.start();
			beltHead = (beltHead + 1) % belt.length;
		}
	}
	
}