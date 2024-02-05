package state;

import picnic.Field;
import puzzle.Puzzle;
import resource.bank.ImageBank;
import state.element.Button;
import state.element.Container;
import state.element.ToolButton;

public class PuzzleState extends State {

	// an object to start field data, like the blanket and critters
	private Field field;
	
	// animation fields for UI stuff
	// tool collapse left anim (note: can reuse an array ;))
	// layer collapse right anim
	
	// tool and layer windows
	private Container toolbar;
	private Container layerbar;
	
	public PuzzleState(Puzzle puzzle) {
		field = new Field(puzzle);
		add(field);
		generateUI();
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
	}
	
	private void generateUI() {
		toolbar = new Container(0, 56, 80, 336);
		layerbar = new Container(400, 56, 80, 336);
		toolbar.setBackground(ImageBank.toolbar);
		layerbar.setBackground(ImageBank.layerbar);
		add(toolbar);
		add(layerbar);
		ToolButton plateTool = new ToolButton(this, ToolButton.PLATE, 16, 24, 45, 40);
		ToolButton forkTool = new ToolButton(this, ToolButton.PLATE, 16, 68, 45, 40);
		plateTool.setBackground(ImageBank.toolicons[0]);
		forkTool.setBackground(ImageBank.toolicons[1]);
		toolbar.add(plateTool);
		toolbar.add(forkTool);
	}
	
	public void toolClicked(int id) {
		// TODO
	}
	
	public void tick() {
		super.tick();
	}
	
}
