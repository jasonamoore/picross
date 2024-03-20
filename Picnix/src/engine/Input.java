package engine;

import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

/**
 * Implements various AWT input listeners for managing game input.
 * Provides methods to get info about current and past user inputs.
 */
public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	// singleton instance
	private static Input instance;
	
	// the engine the input is tied to
	private Engine engine;
	
	/**
	 * Private constructor to prevent outside initialization.
	 */
	private Input() {
		engine = Engine.getEngine();
	}
	
	/**
	 * Returns the singleton instance. If no instance exists,
	 * one will be created.
	 * @return The Input instance.
	 */
	public static Input getInstance() {
		return instance == null ? 
				(instance = new Input()) : instance;
	}
	
	/**
	 * Used to find the index in the circular array
	 * given a relative index. Basically, does non-negative modulus.
	 */
	private static int getWrappedHead(int rin) {
		return (rin + RECORD_SIZE) % RECORD_SIZE;
	}
	
	/* ~~~
	 * 
	 * INPUT IMPLEMENTATION
	 * 
	 * ~~~
	 */
	
	// the number of records to store in circular record arrays
	private static final int RECORD_SIZE = 100;
	
	/*
	 * KEY PRESS IMPLEMENTATION
	 */
	
	// can hold almost any key on modern keyboards
	private static final int KEY_MAX = KeyEvent.VK_CONTEXT_MENU;
	
	// hold the data about which keys are pressed
	// and when they were pressed (i.e. for how long)
	private boolean[] keysPressing = new boolean[KEY_MAX + 1];
	private boolean[] keysReleased = new boolean[KEY_MAX + 1];
	private boolean[] keysIgnoring = new boolean[KEY_MAX + 1];
	private boolean autoIgnore = false;
	private long[] keyTimestamps = new long[KEY_MAX + 1];

	// a set of circular arrays holding data about the previous 'RECORD_SIZE' key presses
	private int[] keyPressButtonRecords = new int[RECORD_SIZE];
	private long[] keyPressTimeRecords = new long[RECORD_SIZE];
	// the index of the most current record
	private int keyPressRecordHead = 0;
	
	// a set of circular arrays holding data about the previous 'RECORD_SIZE' key releases
	private int[] keyReleaseButtonRecords = new int[RECORD_SIZE];
	private long[] keyReleaseTimeRecords = new long[RECORD_SIZE];
	// the index of the most current record
	private int keyReleaseRecordHead = 0;
	
	/**
	 * Records that this key was pressed by setting keysPressing at the index of
	 * its key code to true. Records the timestamp of the key press in keyTimestamps.
	 * Also adds a timestamped entry to the set of key press records.
	 * If the code is invalid, does nothing.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		// if code is a valid key to store
		if (code >= 0 && code <= KEY_MAX && !keysIgnoring[code]) {
			synchronized (this) {
				keysPressing[code] = true;
				keysReleased[code] = false;
				if (autoIgnore)
					keysIgnoring[code] = true;
				keyTimestamps[code] = e.getWhen();
				// records
				keyPressRecordHead = (keyPressRecordHead + 1) % RECORD_SIZE;
				keyPressButtonRecords[keyPressRecordHead] = code;
				keyPressTimeRecords[keyPressRecordHead] = e.getWhen();
			}
		}
	}
	
	/**
	 * Records that this key was released by setting keysPressing at the index of
	 * its key code to false. Also flags the key release by setitng keysReleased at the
	 * index of its key code to true, and records the timestamp of release in keyTimestamps.
	 * If the code is invalid, does nothing.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		// if code is a valid key to store
		if (code >= 0 && code <= KEY_MAX) {
			synchronized (this) {
				keysPressing[code] = false;
				keysReleased[code] = true;
				keysIgnoring[code] = false;
				keyTimestamps[code] = e.getWhen();
				// records
				keyReleaseRecordHead = (keyReleaseRecordHead + 1) % RECORD_SIZE;
				keyReleaseButtonRecords[keyReleaseRecordHead] = code;
				keyReleaseTimeRecords[keyReleaseRecordHead] = e.getWhen();
			}
		}
	}
	
	/**
	 * Ignored.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		// do nothing
	}
	
	/*
	 * MOUSE PRESS IMPLEMENTATION
	 */
	
	// mouse button constants for clarity
	public static int LEFT_CLICK = 1;
	public static int MIDDLE_CLICK = 2;
	public static int RIGHT_CLICK = 3;	
	// used for filtering mouse press/release records
	public static int ANY_CLICK = 0;
	
	// whether the mouse is within the window
	private boolean mouseFocused;

	// the number of mouse buttons currently in use
	private static final int MOUSE_MAX = Math.max(RIGHT_CLICK, MouseInfo.getNumberOfButtons());
	
	// hold the data about which keys are pressed
	// and when they were pressed (i.e. for how long)
	private boolean[] mouseButtonsPressing = new boolean[MOUSE_MAX + 1];
	private boolean[] mouseButtonsReleased = new boolean[MOUSE_MAX + 1];
	private long[] mouseTimestamps = new long[MOUSE_MAX + 1];
	
	// a set of circular arrays holding data about the previous 'RECORD_SIZE' mouse presses
	private int[] mousePressButtonRecords = new int[RECORD_SIZE];
	private long[] mousePressTimeRecords= new long[RECORD_SIZE];
	private int[] mousePressXPosRecords = new int[RECORD_SIZE];
	private int[] mousePressYPosRecords = new int[RECORD_SIZE];
	// the index of the most current record
	private int mousePressRecordHead = 0;
	
	// a set of circular arrays holding data about the previous 'RECORD_SIZE' mouse releases
	private int[] mouseReleaseButtonRecords = new int[RECORD_SIZE];
	private long[] mouseReleaseTimeRecords= new long[RECORD_SIZE];
	private int[] mouseReleaseXPosRecords = new int[RECORD_SIZE];
	private int[] mouseReleaseYPosRecords = new int[RECORD_SIZE];
	// the index of the most current record
	private int mouseReleaseRecordHead = 0;
	
	private int mouseButtonDecode(MouseEvent e) {
		// will check if it is left, middle, or right, and use
		// the predefined constant if so. otherwise, use button code
		if (SwingUtilities.isLeftMouseButton(e))
			return LEFT_CLICK;
		else if (SwingUtilities.isMiddleMouseButton(e))
			return MIDDLE_CLICK;
		else if (SwingUtilities.isRightMouseButton(e))
			return RIGHT_CLICK;
		else
			return e.getButton();
	}
	
	/**
	 * Records that this mouse button was pressed by setting the mouseButtonsPressing
	 * at the index of its button code to true. Records the timestamp of the mouse
	 * press in mouseTimestamps.
	 * Also adds a timestamped entry to the set of mouse press records.
	 * If the code is invalid, does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		int code = mouseButtonDecode(e);
		if (code >= 0 && code <= MOUSE_MAX) {
			synchronized (this) {
				mouseButtonsPressing[code] = true;
				mouseButtonsReleased[code] = false;
				mouseTimestamps[code] = e.getWhen();
				// records
				mousePressRecordHead = (mousePressRecordHead + 1) % RECORD_SIZE;
				mousePressButtonRecords[mousePressRecordHead] = code;
				mousePressTimeRecords[mousePressRecordHead] = e.getWhen();
				mousePressXPosRecords[mousePressRecordHead] = e.getX() / engine.getDisplayScale();
				mousePressYPosRecords[mousePressRecordHead] = e.getY() / engine.getDisplayScale();
			}
		}
	}

	/**
	 * Records that this mouse button was released by setting the mouseButtonsReleased
	 * at the index of its button code to true. Records the timestamp of the mouse
	 * press in mouseTimestamps.
	 * Also adds a timestamped entry to the set of mouse release records.
	 * If the code is invalid, does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		int code = mouseButtonDecode(e);
		if (code >= 0 && code <= MOUSE_MAX) {
			synchronized (this) {
				mouseButtonsPressing[code] = false;
				mouseButtonsReleased[code] = true;
				mouseTimestamps[code] = e.getWhen();
				// records
				mouseReleaseRecordHead = (mouseReleaseRecordHead + 1) % RECORD_SIZE;
				mouseReleaseButtonRecords[mouseReleaseRecordHead] = code;
				mouseReleaseXPosRecords[mouseReleaseRecordHead] = e.getX() / engine.getDisplayScale();
				mouseReleaseYPosRecords[mouseReleaseRecordHead] = e.getY() / engine.getDisplayScale();
				mouseReleaseTimeRecords[mouseReleaseRecordHead] = e.getWhen();
			}
		}
	}
	
	/**
	 * Ignored.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// do nothing
	}

	/**
	 * Sets mouseFocused to true.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		mouseFocused = true;
	}
	
	/**
	 * Sets mouseFocused to false.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		mouseFocused = false;
	}
	
	// a set of circular arrays holding data about the previous 'RECORD_SIZE' mouse movements
	private int[] motionRecordsX = new int[RECORD_SIZE];
	private int[] motionRecordsY = new int[RECORD_SIZE];
	private long[] motionRecordsTime = new long[RECORD_SIZE];
	// the last recorded mouse position
	private int mouseX, mouseY;
	
	// the index of the most current record
	private int motionRecordHead = 0;
	
	/**
	 * Passes the event to the {@link #mouseMoved} method.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	/**
	 * Records a new mouse movement in the mouse
	 * movement log, stamped at the current time.
	 * The mouse position is adjusted by the engine's display
	 * offsets and scale, meaning position (0, 0) is the top
	 * left corner of the rendered game display, and 
	 * ({@link Engine.SCREEN_WIDTH}, {@link Engine.SCREEN_HEIGHT})
	 * is the bottom right corner of the display.
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		synchronized (this) {
			mouseX = e.getX() / engine.getDisplayScale();
			mouseY = e.getY() / engine.getDisplayScale();
			motionRecordHead = (motionRecordHead + 1) % RECORD_SIZE;
			motionRecordsX[motionRecordHead] = mouseX;
			motionRecordsY[motionRecordHead] = mouseY;
			motionRecordsTime[motionRecordHead] = e.getWhen();
		}
	}
	
	/*
	 * MOUSE WHEEL IMPLEMENTATION
	 */
	
	// a set of circular arrays holding data about the previous 'RECORD_SIZE' wheel movements
	private double[] wheelRecordsAmount = new double[RECORD_SIZE];
	private long[] wheelRecordsTime = new long[RECORD_SIZE];
	private double unconsumedScrollAmount = 0;
	
	// the index of the most current record
	private int wheelRecordHead = 0;
	
	/**
	 * Records a new wheel movement in the wheel
	 * movement log, stamped at the current time.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		synchronized (this) {
			unconsumedScrollAmount += e.getPreciseWheelRotation();
			wheelRecordHead = (wheelRecordHead + 1) % RECORD_SIZE;
			wheelRecordsAmount[wheelRecordHead] = e.getPreciseWheelRotation();
			wheelRecordsTime[wheelRecordHead] = e.getWhen();
		}
	}
	
	/* ~~~
	 * 
	 * ACCESSORS AND UTILITIES
	 * 
	 * ~~~
	 */

	/**
	 * Given a key code, checks whether this key is currently being pressed.
	 * @param code The key code to search for.
	 * @return True if the key is being pressed, otherwise false.
	 * @see {@link KeyEvent} VK codes.
	 */
	public boolean isPressingKey(int code) {
		if (code >= 0 && code <= KEY_MAX)
			return keysPressing[code];
		else
			return false;
	}
	
	/**
	 * Given a key code, checks whether this key was recently released.
	 * Note that this will return true after a key was released until
	 * its entry in the key release array has been manually consumed.
	 * @param code The key code to search for.
	 * @return True if the key is being pressed, otherwise false.
	 * @see {@link KeyEvent} VK codes.
	 */
	public boolean hasReleasedKey(int code) {
		if (code >= 0 && code <= KEY_MAX)
			return keysReleased[code];
		else
			return false;
	}
	
	/**
	 * Given a key code, finds the time this button last changed state.
	 * @param code The key code to search for.
	 * @return The time, in milliseconds, that the key was pressed or released.
	 */
	public long getKeyChangeTime(int code) {
		if (code >= 0 && code <= KEY_MAX)
			return keyTimestamps[code];
		else
			return -1;
	}
	
	/**
	 * Given a key code, finds how long the key has been in its current state.
	 * @param code The key code to search for.
	 * @return The duration, in milliseconds, since the key changed state.
	 */
	public long getKeyDuration(int code) {
		if (code >= 0 && code <= KEY_MAX)
			return System.currentTimeMillis() - keyTimestamps[code];
		else
			return -1;
	}
	
	/**
	 * Given a mouse button, checks whether this button is currently being pressed.
	 * @param code The mouse button code to search for.
	 * @return True if the button is being pressed, otherwise false.
	 */
	public boolean isPressingMouseButton(int code) {
		if (code >= 0 && code <= MOUSE_MAX)
			return mouseButtonsPressing[code];
		else
			return false;
	}
	
	/**
	 * Given a mouse button, checks whether this button was recently released.
	 * Note that this will return true after a mouse button was released until
	 * its entry in the mouse button release array has been manually consumed.
	 * @param code The mouse button code to search for.
	 * @return True if the button is being pressed, otherwise false.
	 */
	public boolean hasReleasedMouseButton(int code) {
		if (code >= 0 && code <= MOUSE_MAX)
			return mouseButtonsReleased[code];
		else
			return false;
	}
	
	/**
	 * Given a mouse button, finds the time this button last changed state.
	 * (was pressed or released).
	 * @param code The mouse button code to search for.
	 * @return The time, in milliseconds, that the key was pressed or released.
	 */
	public long getMouseButtonChangeTime(int code) {
		if (code >= 0 && code <= MOUSE_MAX)
			return mouseTimestamps[code];
		else
			return -1;
	}
	
	/**
	 * Given a mouse button, finds how long the button has been in its current state.
	 * @param code The mouse button code to search for.
	 * @return The duration, in milliseconds, since the button changed state.
	 */
	public long getMouseButtonDuration(int code) {
		if (code >= 0 && code <= MOUSE_MAX)
			return System.currentTimeMillis() - mouseTimestamps[code];
		else
			return -1;
	}
	
	/**
	 * Returns the current x position of the mouse.
	 * @return The x position of the most recent mouse movement.
	 */
	public int getMouseX() {
		return mouseX;
	}
	
	/**
	 * Returns the current y position of the mouse.
	 * @return The y position of the most recent mouse movement.
	 */
	public int getMouseY() {
		return mouseY;
	}
	
	/**
	 * Returns the time that the mouse was last moved.
	 * @return The most recent record of movement time, in milliseconds.
	 * @see #getMouseMoveTimeRecord
	 */
	public long getMouseMoveTime() {
		return getMouseMoveTimeRecord(0);
	}
	
	/**
	 * Returns a historical x position of the mouse.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The x position {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMouseMoveXRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(motionRecordHead - lookback);
		return motionRecordsTime[lookHead] != 0 ? motionRecordsX[lookHead] : -1;
	}
	
	/**
	 * Returns a historical y position of the mouse.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The y position {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMouseMoveYRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(motionRecordHead - lookback);
		return motionRecordsTime[lookHead] != 0 ? motionRecordsY[lookHead] : -1;
	}
	
	/**
	 * Returns a historical movement time of the mouse.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The move time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getMouseMoveTimeRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(motionRecordHead - lookback);
		return motionRecordsTime[lookHead] != 0 ? motionRecordsTime[lookHead] : -1;
	}

	/**
	 * The key code of the most recently pressed key.
	 * @return A key code, or -1 if no keys have been pressed yet.
	 * @see #getKeyButtonRecord
	 */
	public int getLastKeyPressButton() {
		return getKeyPressButtonRecord(0);
	}
	
	/**
	 * The timestamp of the most recently pressed key.
	 * @return A time in milliseconds, or -1 if no keys have been pressed yet.
	 * @see #getKeyTimeRecord
	 */
	public long getLastKeyPressTime() {
		return getKeyPressTimeRecord(0);
	}
	
	/**
	 * The key code of the most recently released key.
	 * @return A key code, or -1 if no keys have been released yet.
	 * @see #getKeyButtonRecord
	 */
	public int getLastKeyReleaseButton() {
		return getKeyReleaseButtonRecord(0);
	}
	
	/**
	 * The timestamp of the most recently released key.
	 * @return A time in milliseconds, or -1 if no keys have been released yet.
	 * @see #getKeyTimeRecord
	 */
	public long getLastKeyReleaseTime() {
		return getKeyReleaseTimeRecord(0);
	}
	
	/**
	 * Returns the key code of a historical key press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The key press {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getKeyPressButtonRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(keyPressRecordHead - lookback);
		return keyPressTimeRecords[lookHead] != 0 ? keyPressButtonRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the timestamp of a historical key press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The key press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getKeyPressTimeRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(keyPressRecordHead - lookback);
		return keyPressTimeRecords[lookHead] != 0 ? keyPressTimeRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the key code of a historical key press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The key press {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getKeyReleaseButtonRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(keyReleaseRecordHead - lookback);
		return keyReleaseTimeRecords[lookHead] != 0 ? keyReleaseButtonRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the timestamp of a historical key press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The key press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getKeyReleaseTimeRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(keyReleaseRecordHead - lookback);
		return keyReleaseTimeRecords[lookHead] != 0 ? keyReleaseTimeRecords[lookHead] : -1;
	}
	
	/**
	 * The button code of the most recently pressed mouse button.
	 * @return A button code, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonRecord
	 */
	public int getLastMousePressButton() {
		return getMousePressButtonRecord(0);
	}
	
	/**
	 * The timestamp of the most recently pressed mouse button (matching the given code).
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return A time in milliseconds, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonTimeRecord
	 */
	public long getLastMousePressTime(int code) {
		return getMousePressTimeRecord(code, 0);
	}
	
	/**
	 * The x position of the most recent mouse press (matching the given code).
	 * @param code The code of the mouse button to search for press records of, or ANY_CLICK for all records.
	 * @return A button code, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonRecord
	 */
	public int getLastMousePressXPosition(int code) {
		return getMousePressXPositionRecord(code, 0);
	}
	
	/**
	 * The y position of the most recent mouse press (matching the given code).
	 * @param code The code of the mouse button to search for press records of, or ANY_CLICK for all records.
	 * @return A button code, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonRecord
	 */
	public int getLastMousePressYPosition(int code) {
		return getMousePressYPositionRecord(code, 0);
	}
	
	/**
	 * The button code of the most recently pressed mouse button.
	 * @return A button code, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonRecord
	 */
	public int getLastMouseReleaseButton() {
		return getMouseReleaseButtonRecord(0);
	}
	
	/**
	 * The timestamp of the most recently pressed mouse button (matching the given code).
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return A time in milliseconds, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonTimeRecord
	 */
	public long getLastMouseReleaseTime(int code) {
		return getMouseReleaseTimeRecord(code, 0);
	}
	
	/**
	 * The x position of the most recent mouse release (matching the given code).
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return A button code, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonRecord
	 */
	public int getLastMouseReleaseXPosition(int code) {
		return getMouseReleaseXPositionRecord(code, 0);
	}
	
	/**
	 * The y position of the most recent mouse release (matching the given code).
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return A button code, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonRecord
	 */
	public int getLastMouseReleaseYPosition(int code) {
		return getMouseReleaseYPositionRecord(code, 0);
	}
	
	/**
	 * Returns the button code of a historical mouse press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The button code {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMousePressButtonRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(mousePressRecordHead - lookback);
		return mousePressTimeRecords[lookHead] != 0 ? mousePressButtonRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the timestamp of a historical mouse press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @param code The code of the mouse button to search for press records of, or ANY_CLICK for all records.
	 * @return The button press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getMousePressTimeRecord(int code, int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		// lookback through all records
		if (code == ANY_CLICK) {
			int lookHead = getWrappedHead(mousePressRecordHead - lookback);
			return mousePressTimeRecords[lookHead] != 0 ? mousePressTimeRecords[lookHead] : -1;
		}
		// lookback through records that match the code
		else {
			int matched = 0;
			for (int i = 0; i < RECORD_SIZE; i++) {
				int lookHead = getWrappedHead(mousePressRecordHead - i);
				int button = mousePressButtonRecords[lookHead];
				if (button == code)
					matched++;
				if (matched > lookback)
					return mousePressTimeRecords[lookHead];
			}
			return -1;
		}
	}
	
	/**
	 * Returns the x position of a historical mouse press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @param code The code of the mouse button to search for press records of, or ANY_CLICK for all records.
	 * @return The button press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMousePressXPositionRecord(int code, int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		// lookback through all records
		if (code == ANY_CLICK) {
			int lookHead = getWrappedHead(mousePressRecordHead - lookback);
			return mousePressXPosRecords[lookHead] != 0 ? mousePressXPosRecords[lookHead] : -1;
		}
		// lookback through records that match the code
		else {
			int matched = 0;
			for (int i = 0; i < RECORD_SIZE; i++) {
				int lookHead = getWrappedHead(mousePressRecordHead - i);
				int button = mousePressButtonRecords[lookHead];
				if (button == code)
					matched++;
				if (matched > lookback)
					return mousePressXPosRecords[lookHead];
			}
			return -1;
		}
	}
	
	/**
	 * Returns the y position of a historical mouse press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @param code The code of the mouse button to search for press records of, or ANY_CLICK for all records.
	 * @return The button press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMousePressYPositionRecord(int code, int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		synchronized (this) {
			// lookback through all records
			if (code == ANY_CLICK) {
				int lookHead = getWrappedHead(mousePressRecordHead - lookback);
				return mousePressYPosRecords[lookHead] != 0 ? mousePressYPosRecords[lookHead] : -1;
			}
			// lookback through records that match the code
			else {
				int matched = 0;
				for (int i = 0; i < RECORD_SIZE; i++) {
					int lookHead = getWrappedHead(mousePressRecordHead - i);
					int button = mousePressButtonRecords[lookHead];
					if (button == code)
						matched++;
					if (matched > lookback)
						return mousePressYPosRecords[lookHead];
				}
				return -1;
			}
		}
	}
	
	/**
	 * Returns the button code of a historical mouse release record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return The button code {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMouseReleaseButtonRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(mouseReleaseRecordHead - lookback);
		return mouseReleaseTimeRecords[lookHead] != 0 ? mouseReleaseButtonRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the timestamp of a historical mouse release record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return The button release time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getMouseReleaseTimeRecord(int code, int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		// lookback through all records
		if (code == ANY_CLICK) {
			int lookHead = getWrappedHead(mouseReleaseRecordHead - lookback);
			return mouseReleaseTimeRecords[lookHead] != 0 ? mouseReleaseTimeRecords[lookHead] : -1;
		}
		// lookback through records that match the code
		else {
			int matched = 0;
			for (int i = 0; i < RECORD_SIZE; i++) {
				int lookHead = getWrappedHead(mouseReleaseRecordHead - i);
				int button = mouseReleaseButtonRecords[lookHead];
				if (button == code)
					matched++;
				if (matched > lookback)
					return mouseReleaseTimeRecords[lookHead];
			}
			return -1;
		}
	}
	
	/**
	 * Returns the x position of a historical mouse release record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return The button press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMouseReleaseXPositionRecord(int code, int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		// lookback through all records
		if (code == ANY_CLICK) {
			int lookHead = getWrappedHead(mouseReleaseRecordHead - lookback);
			return mouseReleaseXPosRecords[lookHead] != 0 ? mouseReleaseXPosRecords[lookHead] : -1;
		}
		// lookback through records that match the code
		else {
			int matched = 0;
			for (int i = 0; i < RECORD_SIZE; i++) {
				int lookHead = getWrappedHead(mouseReleaseRecordHead - i);
				int button = mouseReleaseButtonRecords[lookHead];
				if (button == code)
					matched++;
				if (matched > lookback)
					return mouseReleaseXPosRecords[lookHead];
			}
			return -1;
		}
	}
	
	/**
	 * Returns the y position of a historical mouse release record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @param code The code of the mouse button to search for release records of, or ANY_CLICK for all records.
	 * @return The button press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMouseReleaseYPositionRecord(int code, int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		// lookback through all records
		if (code == ANY_CLICK) {
			int lookHead = getWrappedHead(mouseReleaseRecordHead - lookback);
			return mouseReleaseYPosRecords[lookHead] != 0 ? mouseReleaseYPosRecords[lookHead] : -1;
		}
		// lookback through records that match the code
		else {
			int matched = 0;
			for (int i = 0; i < RECORD_SIZE; i++) {
				int lookHead = getWrappedHead(mouseReleaseRecordHead - i);
				int button = mouseReleaseButtonRecords[lookHead];
				if (button == code)
					matched++;
				if (matched > lookback)
					return mouseReleaseYPosRecords[lookHead];
			}
			return -1;
		}
	}
	
	/**
	 * Returns the amount that the mouse wheel has been scrolled
	 * (the scroll delta), since the the last time it was consumed.
	 * @return
	 */
	public double getUnconsumedScrollAmount() {
		return unconsumedScrollAmount;
	}
	
	/**
	 * The scroll amount of the most recent mouse wheel scroll.
	 * @return The most recent scroll amount, or -1 if there was no scroll.
	 * @see #getScrollAmountRecord
	 */
	public double getLastScrollAmount() {
		return getScrollAmountRecord(0);
	}
	
	/**
	 * The timestamp of the most recent mouse wheel scroll.
	 * @return A time in milliseconds, or -1 if no scroll has occurred yet.
	 * @see #getScrollTimeRecord
	 */
	public long getLastScrollTime() {
		return getScrollTimeRecord(0);
	}
	
	/**
	 * Returns the scroll amount of a historical mouse wheel scroll record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The scroll amount {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public double getScrollAmountRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(wheelRecordHead - lookback);
		return wheelRecordsTime[lookHead] != 0 ? wheelRecordsAmount[lookHead] : -1;
	}
	
	/**
	 * Returns the timestamp of a historical mouse wheel scroll record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The scroll time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getScrollTimeRecord(int lookback) {
		// can't lookback more than records store
		if (lookback > RECORD_SIZE)
			return -1;
		int lookHead = getWrappedHead(wheelRecordHead - lookback);
		return wheelRecordsTime[lookHead] != 0 ? wheelRecordsTime[lookHead] : -1;
	}
	
	/**
	 * @return True if the mouse is within the window, false otherwise.
	 */
	public boolean isMouseFocused() {
		return mouseFocused;
	}
	
	/**
	 * Sets the behavior for ignoring repeated key presses
	 * occuring before a key is released.
	 * Repeated key presses may occur when a button is held
	 * and the operating system "types" the button repeatedly.
	 * @param ignore If true, automatically ignores repeated presses.
	 */
	public void setAutomaticKeyTypeIgnore(boolean ignore) {
		autoIgnore = ignore;
	}
	
	/**
	 * Ignores any future presses of this key until the
	 * key is released. Repeated key presses may occur
	 * when a button is held and the operating system
	 * "types" the button repeatedly.
	 * @param code
	 * @see 
	 */
	public void ignoreKey(int code) {
		if (code >= 0 && code < keysIgnoring.length)
			keysIgnoring[code] = true;
	}
	
	// methods for consuming button press/releases that have been processed
	
	/**
	 * If this key is currently marked as being pressed,
	 * it is consumed (unmarked in the pressed array).
	 * @param code
	 */
	public void consumeKeyPress(int code) {
		if (code >= 0 && code < keysPressing.length)
			keysPressing[code] = false;
	}
	
	/**
	 * If this mouse button is currently marked as being
	 * pressed, it is consumed (unmarked in the pressed array).
	 * @param code
	 */
	public void consumeMouseButtonPress(int code) {
		if (code >= 0 && code < mouseButtonsPressing.length)
			mouseButtonsPressing[code] = false;
	}
	
	/**
	 * If this key was in the released key log,
	 * it is consumed (unmarked in the released array).
	 * @param code
	 */
	public void consumeKeyRelease(int code) {
		if (code >= 0 && code < keysReleased.length)
			keysReleased[code] = false;
	}
	
	/**
	 * If this key was in the released mouse button log,
	 * it is consumed (unmarked in the released array).
	 * @param code
	 */
	public void consumeMouseButtonRelease(int code) {
		if (code >= 0 && code < mouseButtonsReleased.length)
		mouseButtonsReleased[code] = false;
	}
	
	/**
	 * Resets the unconsumed scroll amount to 0.
	 */
	public void consumeMouseWheelScroll() {
		unconsumedScrollAmount = 0;
	}
	
	/**
	 * Consumes all key and mouse button releases by clearing
	 * the keysReleased and mouseButtonsReleased arrays.
	 */
	public void consumeAllReleases() {
		// clear key releases
		for (int i = 0; i < keysReleased.length; i++)
			keysReleased[i] = false;
		// clear mouse release
		for (int i = 0; i < mouseButtonsReleased.length; i++)
			mouseButtonsReleased[i] = false;
	}
	
	/**
	 * Consumes all key and mouse button releases,
	 * and any processed mouse wheel scroll.
	 * Useful for switching between game states
	 * when focus changes and input should be reset.
	 */
	public void consumeAll() {
		consumeAllReleases();
		consumeMouseWheelScroll();
	}
	
}
