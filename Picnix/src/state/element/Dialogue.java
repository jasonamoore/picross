package state.element;

import java.awt.Graphics;

import engine.Engine;
import resource.Font;
import resource.bank.Palette;
import util.Timer;

public class Dialogue extends Button {

	private static final int BOX_MARGIN = 15;
	private static final int BOX_HEIGHT = 100;
	private static final int TEXT_PADDING = 15;
	
	private static final double CHARACTER_PER_MS = 0.125;
	
	private Timer timer;
	private int index;
	private String[] lines;

	private TextField textField;
	
	public Dialogue(Font font, String... lines) {
		setBounds(BOX_MARGIN, calcBoxY(), calcBoxWidth(), BOX_HEIGHT);
		this.lines = lines;
		index = 0;
		timer = new Timer(false);
		textField = new TextField(font, TEXT_PADDING, TEXT_PADDING, calcBoxWidth() - TEXT_PADDING * 2);
		textField.setEnabled(false); // make sure text is not clickable (it would eat box clicks)
		add(textField);
		next();
	}
	
	public int calcBoxY() {
		return Engine.SCREEN_HEIGHT - BOX_MARGIN - BOX_HEIGHT;
	}
	
	public int calcBoxWidth() {
		return Engine.SCREEN_WIDTH - BOX_MARGIN * 2;
	}
	
	private void next() {
		if (index >= lines.length)
			onFinish();
		else {
			textField.setText(lines[index]);
			textField.setCharacterLimit(0);
			index++;
			timer.reset(true);
		}
	}
	
	private void onFinish() {
		//setExisting(false);
		state.remove(this);
	}
	
	@Override
	public void onButtonUp() {
		next();
	}
	
	@Override
	public void tick() {
		super.tick();
		// update character display limit
		int amount = (int) (CHARACTER_PER_MS * timer.elapsed());
		textField.setCharacterLimit(amount);
	}
	
	@Override
	public void render(Graphics g) {
		// draw box
		g.setColor(Palette.BLACK);
		g.fillRect(BOX_MARGIN, calcBoxY(), calcBoxWidth(), BOX_HEIGHT);
	}
	
}
