package state.element.location;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import engine.Input;
import resource.bank.FontBank;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.WorldSelectState;
import state.element.Element;
import state.element.TextField;
import state.element.TiledButton;

public class LevelSetBox extends Element {

	public static final int ANIM_DELAY = 125;
	public static final int ANIM_DURATION = 500;
	
	private static final String LABEL_1 = "puzzles completed";
	private static final String LABEL_2 = "total score";
	
	private TiledButton goButton;
	private WorldProgress progBar;
	private Scoreboard score;
	
	private boolean easy;
	
	public LevelSetBox(WorldSelectState worState, boolean easy) {
		this.easy = easy;
		goButton = new TiledButton(7, 10, 132, 42) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (mbutton == Input.LEFT_CLICK && beingHovered());
					worState.open(easy);
			}
		};
		goButton.setLabel(ImageBank.levelsetbuttonlabels[easy ? 0 : 1]);
		if (easy)
			goButton.setAllTileMaps(ImageBank.greenbutton, ImageBank.greenbuttonclick, ImageBank.buttondisabled);
		else
			goButton.setAllTileMaps(ImageBank.redbutton, ImageBank.redbuttonclick, ImageBank.buttondisabled);
		progBar = new WorldProgress(7, 71, 132, 23);
		progBar.setBackground(ImageBank.worldprogressbars[easy ? 0 : 1]);
		score = new Scoreboard(7, 113, 132, 23);
		add(goButton);
		add(progBar);
		add(score);
		// text labels
		TextField puzComp = new TextField(LABEL_1, FontBank.test, 9, 60, 200);
		TextField totScore = new TextField(LABEL_2, FontBank.test, 9, 102, 200);
		add(puzComp);
		add(totScore);
	}

	public WorldProgress getProgressBar() {
		return progBar;
	}
	
	public Scoreboard getScoreboard() {
		return score;
	}
	
	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int dw = getWidth();
		int dh = getHeight();
		//
		Color main = easy ? Palette.PALE_GREEN : Palette.PALE_RED;
		Color shadow = easy ? Palette.SEAFOAM: Palette.PINK;
		g.setColor(main);
		g.fillRect(xp + 2, yp + 2, dw - 2, dh - 2);
		g.setColor(shadow);
		g.fillRect(xp, yp, 2, dh);
		g.fillRect(xp, yp, dw, 2);
		//
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
