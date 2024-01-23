package engine;

import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	// singleton implementation
	private static Input instance;
	
	/**
	 * Private constructor to prevent outside initialization.
	 */
	private Input() {}
	
	/**
	 * If not yet created, creates the singleton instance.
	 */
	public static void createInstance() {
		if (instance == null)
			instance = new Input();
	}
	
	/**
	 * Returns the singleton instance.
	 * @return The Input instance, or null if no instance has been created.
	 */
	public static Input getInstance() {
		return instance;
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
		if (code >= 0 && code <= KEY_MAX) {
			keysPressing[code] = true;
			keysReleased[code] = false;
			keyTimestamps[code] = System.currentTimeMillis();
			// records
			keyPressRecordHead = (keyPressRecordHead + 1) % RECORD_SIZE;
			keyPressButtonRecords[keyPressRecordHead] = code;
			keyPressTimeRecords[keyPressRecordHead] = System.currentTimeMillis();
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
			keysPressing[code] = false;
			keysReleased[code] = true;
			keyTimestamps[code] = System.currentTimeMillis();
			// records
			keyReleaseRecordHead = (keyReleaseRecordHead + 1) % RECORD_SIZE;
			keyReleaseButtonRecords[keyReleaseRecordHead] = code;
			keyReleaseTimeRecords[keyReleaseRecordHead] = System.currentTimeMillis();
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
	// the index of the most current record
	private int mousePressRecordHead = 0;
	
	// a set of circular arrays holding data about the previous 'RECORD_SIZE' mouse releases
	private int[] mouseReleaseButtonRecords = new int[RECORD_SIZE];
	private long[] mouseReleaseTimeRecords= new long[RECORD_SIZE];
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
			mouseButtonsPressing[code] = true;
			mouseButtonsReleased[code] = false;
			mouseTimestamps[code] = System.currentTimeMillis();
			// records
			mousePressRecordHead = (mousePressRecordHead + 1) % RECORD_SIZE;
			mousePressButtonRecords[mousePressRecordHead] = code;
			mousePressTimeRecords[mousePressRecordHead] = System.currentTimeMillis();
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
			mouseButtonsPressing[code] = false;
			mouseButtonsReleased[code] = true;
			mouseTimestamps[code] = System.currentTimeMillis();
			// records
			mouseReleaseRecordHead = (mouseReleaseRecordHead + 1) % RECORD_SIZE;
			mouseReleaseButtonRecords[mouseReleaseRecordHead] = code;
			mouseReleaseTimeRecords[mouseReleaseRecordHead] = System.currentTimeMillis();
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
		Engine engine = Engine.getEngine();
		// adjust for frame insets
		Insets insets = engine.getFrame().getInsets();
		motionRecordHead = (motionRecordHead + 1) % RECORD_SIZE;
		motionRecordsX[motionRecordHead] =
				(e.getX() - engine.getDisplayOffsetX() - insets.left) / engine.getDisplayScale();
		motionRecordsY[motionRecordHead] =
				(e.getY() - engine.getDisplayOffsetY() - insets.top) / engine.getDisplayScale();
		motionRecordsTime[motionRecordHead] = System.currentTimeMillis();
	}
	
	/*
	 * MOUSE WHEEL IMPLEMENTATION
	 */
	
	// a set of circular arrays holding data about the previous 'RECORD_SIZE' wheel movements
	private double[] wheelRecordsAmount = new double[RECORD_SIZE];
	private long[] wheelRecordsTime = new long[RECORD_SIZE];
	
	// the index of the most current record
	private int wheelRecordHead = 0;
	
	/**
	 * Records a new wheel movement in the wheel
	 * movement log, stamped at the current time.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		wheelRecordHead = (wheelRecordHead + 1) % RECORD_SIZE;
		wheelRecordsAmount[wheelRecordHead] = e.getPreciseWheelRotation();
		wheelRecordsTime[wheelRecordHead] = System.currentTimeMillis();
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
	 * @return The most recent record of the mouse x position.
	 * @see #getMouseXRecord
	 */
	public int getMouseX() {
		return getMouseXRecord(0);
	}
	
	/**
	 * Returns the current y position of the mouse.
	 * @return The most recent record of the mouse y position.
	 * @see #getMouseYRecord
	 */
	public int getMouseY() {
		return getMouseYRecord(0);
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
	public int getMouseXRecord(int lookback) {
		int lookHead = (motionRecordHead - lookback) % RECORD_SIZE;
		return motionRecordsTime[lookHead] != 0 ? motionRecordsX[lookHead] : -1;
	}
	
	/**
	 * Returns a historical y position of the mouse.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The y position {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMouseYRecord(int lookback) {
		int lookHead = (motionRecordHead - lookback) % RECORD_SIZE;
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
		int lookHead = (motionRecordHead - lookback) % RECORD_SIZE;
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
		int lookHead = (keyPressRecordHead - lookback) % RECORD_SIZE;
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
		int lookHead = (keyPressRecordHead - lookback) % RECORD_SIZE;
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
		int lookHead = (keyReleaseRecordHead - lookback) % RECORD_SIZE;
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
		int lookHead = (keyReleaseRecordHead - lookback) % RECORD_SIZE;
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
	 * The timestamp of the most recently pressed mouse button.
	 * @return A time in milliseconds, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonTimeRecord
	 */
	public long getLastMousePressTime() {
		return getMousePressTimeRecord(0);
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
	 * The timestamp of the most recently pressed mouse button.
	 * @return A time in milliseconds, or -1 if no mouse buttons have been pressed yet.
	 * @see #getMouseButtonTimeRecord
	 */
	public long getLastMouseReleaseTime() {
		return getMouseReleaseTimeRecord(0);
	}
	
	/**
	 * Returns the button code of a historical mouse press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The button code {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMousePressButtonRecord(int lookback) {
		int lookHead = (mousePressRecordHead - lookback) % RECORD_SIZE;
		return mousePressTimeRecords[lookHead] != 0 ? mousePressButtonRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the timestamp of a historical mouse press record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The button press time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getMousePressTimeRecord(int lookback) {
		int lookHead = (mousePressRecordHead - lookback) % RECORD_SIZE;
		return mousePressTimeRecords[lookHead] != 0 ? mousePressTimeRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the button code of a historical mouse release record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The button code {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public int getMouseReleaseButtonRecord(int lookback) {
		int lookHead = (mouseReleaseRecordHead - lookback) % RECORD_SIZE;
		return mouseReleaseTimeRecords[lookHead] != 0 ? mouseReleaseButtonRecords[lookHead] : -1;
	}
	
	/**
	 * Returns the timestamp of a historical mouse release record.
	 * @param lookback The distance of the desired record from the current record.
	 * 			(i.e., 0 is the current record, 1 is the previous record, etc.)
	 * @return The button release time {@code lookback} entries prior to the current.
	 * 			If this record does not exist, returns -1.
	 */
	public long getMouseReleaseTimeRecord(int lookback) {
		int lookHead = (mouseReleaseRecordHead - lookback) % RECORD_SIZE;
		return mouseReleaseTimeRecords[lookHead] != 0 ? mouseReleaseTimeRecords[lookHead] : -1;
	}
	
	/**
	 * The scroll amount of the most recent mouse wheel scroll.
	 * @return A button code, or -1 if no scroll has occurred yet.
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
		int lookHead = (wheelRecordHead - lookback) % RECORD_SIZE;
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
		int lookHead = (wheelRecordHead - lookback) % RECORD_SIZE;
		return wheelRecordsTime[lookHead] != 0 ? wheelRecordsTime[lookHead] : -1;
	}
	
	/**
	 * @return True if the mouse is within the window, false otherwise.
	 */
	public boolean isMouseFocused() {
		return mouseFocused;
	}
	
	// methods for consuming button releases have been processed
	
	/**
	 * If this key was in the released key log,
	 * it is consumed (unmarked in the released array).
	 * @param code
	 */
	public void consumeKeyRelease(int code) {
		keysReleased[code] = false;
	}
	
	/**
	 * If this key was in the released mouse button log,
	 * it is consumed (unmarked in the released array).
	 * @param code
	 */
	public void consumeMouseButtonRelease(int code) {
		mouseButtonsReleased[code] = false;
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
	
}
