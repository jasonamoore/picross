package resource.bank;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import resource.SoundEffect;

public class AudioBank {

	public static SoundEffect test;
	
	public static void loadGlobalResources() {
		test = loadSoundEffect("bloop.wav", 5);
	}
	
	public static SoundEffect loadSoundEffect(String src, int beltSize) {
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(
						AudioBank.class.getClassLoader().getResource(src));
			Clip[] belt = new Clip[beltSize];
			for (int i = 0; i < beltSize; i++) {
				belt[i] = AudioSystem.getClip();
				belt[i].open(stream);
			}
			return new SoundEffect(belt);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
