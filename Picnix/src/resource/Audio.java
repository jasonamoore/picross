package resource;

import java.io.File;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// rework all of this dramatically

public abstract class Audio {

	private int beltCap = 1;
	private Clip[] belt;
	private boolean playing = false;

	public Audio(String path, int bc) {
		beltCap = bc;
		belt = new Clip[beltCap];
		for (int i = 0; i < beltCap; i++) {
			belt[i] = load(path);
		}
	}

	public void play() {
		Clip avail = null;
		for (int i = 0; i < beltCap; i++) {
			Clip c = belt[i];
			if (c.getLongFramePosition() == c.getFrameLength()) {
				c.setFramePosition(0);
			}
			if (c.getLongFramePosition() == 0) {
				avail = c;
				break;
			}
		}
		if (avail != null)
			avail.start();
	}

	public static Clip load(String src) {
			Clip audio = null;
			try {
				audio = AudioSystem.getClip();
				URL url = new File(src).toURI().toURL();
				AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
				audio.open(inputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return audio;
		}
	
}
