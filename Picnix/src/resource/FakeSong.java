package resource;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class FakeSong {

	private boolean playing;
	
	private int trackHead;
	private Clip[] tracks;
	
	private boolean loop;
	
	public FakeSong(boolean loop, Clip[] tracks) {
		this.loop = loop;
		this.tracks = tracks;
		trackHead = 1;
		for (int i = 0; i < tracks.length; i++) {
			tracks[i].addLineListener(new SongListener());
			if (loop) // 0 to -1 means loop from start to end
				tracks[i].setLoopPoints(0, -1);
		}
	}
	
	public void pause() {
		playing = false;
		for (int i = 0; i < tracks.length; i++)
			tracks[i].stop();
	}
	
	public void resume() {
		playing = true;
		//for (int i = 0; i < tracks.length; i++)
		//	tracks[i].stop();
		//int sync = tracks[0].getFramePosition();
		//for (int i = 0; i < tracks.length; i++)
		//	tracks[i].setFramePosition(sync);
		tracks[trackHead - 1].stop();
		int sync = tracks[trackHead - 1].getFramePosition();
		tracks[trackHead].setFramePosition(sync);
		if (loop)
			tracks[trackHead].loop(Clip.LOOP_CONTINUOUSLY);
		else
			tracks[trackHead].start();
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
