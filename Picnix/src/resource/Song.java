package resource;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class Song {

	private boolean playing;
	
	private int trackHead;
	private Clip[] tracks;
	
	private boolean loop;
	
	public Song(boolean loop, Clip[] tracks) {
		this.loop = loop;
		this.tracks = tracks;
		trackHead = 1;
		for (int i = 0; loop && i < tracks.length; i++)
			tracks[i].setLoopPoints(0, -1);
	}
	
	public void pause() {
		playing = false;
		for (int i = 0; i < tracks.length; i++)
			tracks[i].stop();
	}
	
	public void resume() {
		playing = true;
		for (int i = 0; i < tracks.length; i++)
			tracks[i].stop();
		int sync = tracks[0].getFramePosition();
		for (int i = 0; i < tracks.length; i++)
			tracks[i].setFramePosition(sync);
		for (int i = 0; i < tracks.length; i++) {
			if (i < trackHead && !tracks[i].isRunning()) {
				if (i > 0) tracks[i].setFramePosition(sync);//tracks[i-1].getFramePosition());
				if (loop)
					tracks[i].loop(Clip.LOOP_CONTINUOUSLY);
				else
					tracks[i].start();
			}
			else if (i >= trackHead)
				tracks[i].stop();
		}
		tracks[0].start();
	}
	
	public void reset(boolean play) {
		for (int i = 0; i < tracks.length; i++)
			tracks[i].setFramePosition(0);
		if (play)
			resume();
	}
	
	public void stripTracks() {
		// set trackHead back to start
		setEnabledTracks(1);
	}
	
	public void addTrack() {
		setEnabledTracks(trackHead + 1);
	}
	
	public void removeTrack() {
		setEnabledTracks(trackHead - 1);
	}
	
	public void setEnabledTracks(int count) {
		if (count < 1 || count > tracks.length)
			return;
		trackHead = count;
		if (playing)
			resume();
	}

	private class SongListener implements LineListener {
		@Override
		public void update(LineEvent event) {
			// only loop if stopped because ended
			//if (playing && event.getType() == Type.STOP) {
			//	playing = false;
			//	reset(loop);
			//}
			//if (playing && event.getType() == Type.START) {
				// sync all tracks
			//	int syncVal = (int) event.getFramePosition();
			//	for (int i = 0; i < trackHead; i++)
			//		tracks[i].setFramePosition(syncVal);
			//}
		}
	}
	
}
