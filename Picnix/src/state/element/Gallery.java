package state.element;

import java.awt.Color;
import java.awt.Graphics;

import engine.Engine;
import picnix.World;
import resource.bank.ImageBank;
import util.Animation;

public class Gallery extends Container {

	public static final int ITEM_SIZE = 45;
	private static final int TOP_MARGIN = 50;
	private static final int ITEM_MARGIN = 12;
	
	private GalleryItem[] items;
	
	private GalleryItem revealing;
	private Animation revealAnim;
	
	public Gallery() {
		this(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
	}
	
	public Gallery(int x, int y, int width, int height) {
		super(x, y, width, height, Engine.SCREEN_WIDTH, getGalleryHeight());
		items = new GalleryItem[totalItems()];
		int n = 0;
		for (int i = 0; i < World.NUM_WORLDS; i++) {
			int levs = World.getWorld(i).getLevelCount();
			for (int j = 0; j < levs; j++) {
				GalleryItem item = new GalleryItem(this, i, j);
				item.setBounds(getItemX(n), getItemY(n), ITEM_SIZE, ITEM_SIZE);
				add(item);
				items[n++] = item;
			}
		}
		revealAnim = new Animation(0, 1, 1000, Animation.EASE_IN, Animation.NO_LOOP, false);
	}

	public void doFancyReveal(int wid, int lid) {
		int n = lid;
		for (int i = 0; i < wid; i++)
			n += World.getWorld(i).getLevelCount();
		int x = getItemX(n);
		int y = getItemY(n);
		Scroller hor = getHorizontalScroller();
		Scroller ver = getVerticalScroller();
		hor.setNudgeSpeed(500);
		ver.setNudgeSpeed(500);
		hor.setViewportOffset(x + ITEM_SIZE / 2 - getWidth() / 2);
		ver.setViewportOffset(y + ITEM_SIZE / 2 - getHeight() / 2);
		revealing = items[n];
		revealAnim.setDelay(500);
		revealAnim.reset(true);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		disableScrollers();
	}
	
	public float getRevealOpacity() {
		return (float) revealAnim.getValue();
	}
	
	public GalleryItem getRevealingItem() {
		return revealing;
	}
	
	private static int getItemX(int n) {
		int col = n % itemsPerRow();
		return ITEM_MARGIN + col * (ITEM_MARGIN + ITEM_SIZE);
	}
	
	private static int getItemY(int n) {
		int row = n / itemsPerRow();
		return TOP_MARGIN + row * (ITEM_MARGIN + ITEM_SIZE);
	}
	
	private static int getGalleryHeight() {
		int h = TOP_MARGIN;
		h += (ITEM_SIZE + ITEM_MARGIN) * Math.ceil(totalItems() / (double) itemsPerRow());
		return h;
	}

	private static int itemsPerRow() {
		return (Engine.SCREEN_WIDTH - ITEM_MARGIN) / (ITEM_SIZE + ITEM_MARGIN);
	}
	
	private static int totalItems() {
		int totalItems = 0;
		for (int i = 0; i < World.NUM_WORLDS; i++)
			totalItems += World.getWorld(i).getLevelCount();
		return totalItems;
	}
	
	@Override
	public void render(Graphics g) {
		g.setColor(Color.PINK);
		g.fillRect(getDisplayX(), getDisplayY(), getWidth(), getHeight());
		super.render(g);
	}
	
}

class GalleryItem extends Element {
	
	private Gallery gallery;
	private int worldId, levelId;
	private boolean isNew;
	
	public GalleryItem(Gallery gallery, int wid, int lid) {
		this.gallery = gallery;
		worldId = wid;
		levelId = lid;
		setBackground(ImageBank.gallery[worldId][levelId]);
		//isNew = UserData.isGalleryItemNew(wid, lid);
	}
	/*
	@Override
	public int getDisplayX() {
		
	}
	
	@Override
	public int getDisplayY() {
		
	}
	
	@Override
	public int getWidth() {
		
	}
	
	@Override
	public int getHeight() {
		
	}*/
	
	@Override
	public float getOpacity() {
		if (gallery.getRevealingItem() == this) {
			return gallery.getRevealOpacity();
		}
		else
			return super.getOpacity();
	}
	
	@Override
	public void onHover() {
		super.onHover();
		isNew = false;
	}
	
	@Override
	public void render(Graphics g) {
		super.render(g);
		// draw "new"
		if (isNew);
	}
	
}
