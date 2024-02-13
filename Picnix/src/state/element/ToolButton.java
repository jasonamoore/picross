package state.element;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;
import resource.bank.ImageBank;
import state.PuzzleState;

public class ToolButton extends Button {

	// tool ids
	public static final int PLATE = 0;
	public static final int FORKS = 1;
	public static final int GUESS = 2;
	public static final int CLEAR = 3;
	public static final int PAN = 4;
	public static final int CENTER = 5;
	public static final int MAYBE_PLATE = 6;
	public static final int MAYBE_FORKS = 7;
	public static final int UNDO = 8;
	public static final int REDO = 9;
	
	private PuzzleState puzState;
	
	private int toolId;
	
	private BufferedImage toolImage;
	
	public ToolButton(PuzzleState pz, int tid, int x, int y, int w, int h) {
		super(x, y, w, h);
		puzState = pz;
		toolId = tid;
		if (tid < UNDO) {
			toolImage = ImageBank.toolicons[toolId];
			setBackgrounds(ImageBank.toolbacks[toolId % 2],
					ImageBank.toolbacks[toolId % 2 + 2],
					ImageBank.toolbacks[toolId % 2 + 4]);
		} else {
			int isRedo = toolId - UNDO;
			toolImage = ImageBank.tooldos[isRedo];
			BufferedImage back = ImageBank.toolbacks[UNDO % 2];
			BufferedImage clickBack = ImageBank.toolbacks[UNDO % 2 + 2];
			BufferedImage disBack = ImageBank.toolbacks[UNDO % 2 + 4];
			int sx = back.getWidth()/2 * isRedo;
			int sw = back.getWidth()/2;
			int sh = back.getHeight();
			setBackgrounds(back.getSubimage(sx, 0, sw, sh),
					clickBack.getSubimage(sx, 0, sw, sh),
					disBack.getSubimage(sx, 0, sw, sh));
		}
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (beingHovered() && mbutton == Input.LEFT_CLICK)
			puzState.toolClicked(toolId);
	}

	public int getToolId() {
		return toolId;
	}
	
	@Override
	public void render(Graphics g) {
		super.render(g);
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		g.drawImage(toolImage, xp, yp, null);
		g.setClip(null);
		parent.setRenderClips(g);
		if (puzState.getCurrentTool() == toolId)
			g.drawImage(ImageBank.toolarrows[0], xp - 12, yp + 10, null);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
