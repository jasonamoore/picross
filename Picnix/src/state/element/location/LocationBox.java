package state.element.location;

import engine.Engine;
import picnix.World;
import picnix.data.UserData;
import resource.bank.ImageBank;
import state.WorldSelectState;
import state.element.Container;
import state.element.Icon;

public class LocationBox extends Container {

	private static final int X_POS = Engine.SCREEN_WIDTH - 180;
	private static final int WIDTH = 168;
	private static final int HEIGHT = 394;
	
	private WorldSelectState worState;

	private Icon banner;
	private LevelSetBox easy;
	private LevelSetBox hard;
	private LockedSetBox lock;
	
	public LocationBox(WorldSelectState worState) {
		super(X_POS, 0, WIDTH, HEIGHT);
		this.worState = worState;
		setBackground(ImageBank.locationboxframe);
		// location label at top
		banner = new Icon(null, 11, 31, 146, 44);
		easy = new LevelSetBox(worState, true);
		easy.setBounds(11, 84, 146, 146);
		hard = new LevelSetBox(worState, false);
		hard.setBounds(11, 239, 146, 146);
		hard.setExisting(false);
		hard.setChildrenExisting(false);
		lock = new LockedSetBox();
		lock.setBounds(11, 239, 146, 146);
		add(banner);
		add(easy);
		add(hard);
		add(lock);
	};

	public void update(int curLoc) {
		int easyId = World.getEasyWorldId(curLoc);
		int hardId = World.getHardWorldId(curLoc);
		World easyWorld = World.getWorld(easyId);
		World hardWorld = World.getWorld(hardId);
		int easyScore = UserData.getWorldScore(easyId);
		int hardScore = UserData.getWorldScore(hardId);
		int unlockScore = hardWorld.getUnlockScore();
		boolean unlocked = easyScore >= unlockScore;
		banner.setBackground(ImageBank.locationbanners[curLoc]);
		hard.setExisting(unlocked);
		hard.setChildrenExisting(unlocked);
		lock.setExisting(!unlocked);
		lock.setChildrenExisting(!unlocked);
		easy.getProgressBar().setProgress(UserData.getPuzzlesCompleted(easyId),
				easyWorld.getLevelCount());
		easy.getScoreboard().setScore(easyScore);
		hard.getProgressBar().setProgress(UserData.getPuzzlesCompleted(hardId),
				hardWorld.getLevelCount());
		hard.getScoreboard().setScore(hardScore);
		lock.setProgress(easyScore, unlockScore);
	}
	
	@Override
	public int getDisplayY() {
		return worState.getBoxY() + (parent != null ? parent.getDisplayY() - parent.getScrollY() : 0);
	}
	
	@Override
	public float getOpacity() {
		return worState.getBoxOpacity();
	}

}
