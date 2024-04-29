package resource.bank;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import resource.FakeSong;
import resource.Song;
import resource.SoundEffect;

public class AudioBank {

	public static SoundEffect test;
	public static FakeSong pauseMusic;
	public static FakeSong parkMusic;
	
	public static void loadGlobalResources() {
		test = loadSoundEffect("bloop.wav", 5);
		pauseMusic = loadSong(true, "pause.wav");
		parkMusic = loadSong(true, "park0.wav", "fpark1.wav", "fpark2.wav", "fpark3.wav");
	}
	
	public static SoundEffect loadSoundEffect(String src, int beltSize) {
		try {
			URL srcURL = AudioBank.class.getClassLoader().getResource(src);
			Clip[] belt = new Clip[beltSize];
			for (int i = 0; i < beltSize; i++) {
				AudioInputStream stream = AudioSystem.getAudioInputStream(srcURL);
				belt[i] = AudioSystem.getClip();
				belt[i].open(stream);
			}
			return new SoundEffect(belt);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static FakeSong loadSong(boolean loop, String... trackSrcs) {
		try {
			Clip[] tracks = new Clip[trackSrcs.length];
			for (int i = 0; i < trackSrcs.length; i++) {
				URL srcURL = AudioBank.class.getClassLoader().getResource(trackSrcs[i]);
				AudioInputStream stream = AudioSystem.getAudioInputStream(srcURL);
				tracks[i] = AudioSystem.getClip();
				tracks[i].open(stream);
			}
			return new FakeSong(loop, tracks);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
