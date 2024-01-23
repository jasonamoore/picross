package engine.thread;

import java.util.LinkedList;

import resource.Song;
import resource.SoundEffect;

public class Speaker extends ThreadManager {

	private LinkedList<SoundEffect> sfx;
	private LinkedList<Song> songs;
	private Song currentSong;
	
	public Speaker() {
		sfx = new LinkedList<SoundEffect>();
		songs = new LinkedList<Song>();
	}
	
	@Override
	protected void begin() {
		while (running) {
			if (paused)
				continue;
			while (!sfx.isEmpty()) {
				SoundEffect se = sfx.poll();
				se.play();
			}
		}
	}
	
	// add music stuff
	
	/**
	 * Adds a sound effect to the Speaker's queue.
	 * @param sound The sound effect to be queued.
	 */
	public void queueSound(SoundEffect sound) {
		sfx.add(sound);
	}
	
	/**
	 * Empties the Speaker sound effect queue.
	 */
	public void clearSounds() {
		sfx.clear();
	}
	
	/**
	 * Adds a song to the Speaker's music queue.
	 * @param sound The song to be queued.
	 */
	public void queueSong(Song song) {
		songs.add(song);
	}
	
	/**
	 * Empties the Speaker song queue.
	 */
	public void clearSongs() {
		songs.clear();
	}
	
}
