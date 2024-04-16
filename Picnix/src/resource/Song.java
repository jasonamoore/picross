package resource;

import javax.sound.sampled.Clip;

public class Song {

	private boolean playing;
	
	private int trackHead;
	private Clip[] tracks;
	
	public Song(Clip[] tracks) {
		this.tracks = tracks;
		trackHead = 0;
	}
	
	public void pause() {
		playing = false;
		int syncVal = tracks[0].getFramePosition();
		for (int i = 0; i < tracks.length; i++) {
			tracks[i].stop();
			tracks[i].setFramePosition(syncVal);
		}
	}
	
	public void resume() {
		playing = true;
		for (int i = 0; i < tracks.length; i++)
			tracks[i].start();
	}
	
	public void addTrack() {
		if (trackHead == tracks.length)
			return; // reached last track
		if (trackHead > 0)
			tracks[trackHead].setFramePosition(tracks[trackHead - 1].getFramePosition());
		if (playing)
			tracks[trackHead].start();
		trackHead++;
	}
	
	public void removeTrack() {
		if (trackHead == 0)
			return; // reached first track
		if (playing)
			tracks[trackHead - 1].stop();
		trackHead--;
	}

}
