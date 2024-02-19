package util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import engine.Engine;

public class Debug {

	// ~~DEBUG STUFF~~
	private static long[] usedMemory = new long[Engine.SCREEN_WIDTH];
	private static long maxUsedMem = 0;
	private static long runtimeMem = Runtime.getRuntime().maxMemory();
	private static double lastGCTime = System.currentTimeMillis();
	private static double GCperMin;
	//
	private static double[] tickTimes = new double[Engine.SCREEN_WIDTH];
	private static double lastTPS;
	private static double lowestTPS = Double.POSITIVE_INFINITY;
	private static int lastTickDur;
	//
	private static double[] renderTimes = new double[Engine.SCREEN_WIDTH];
	private static double lastFPS;
	private static double lowestFPS = Double.POSITIVE_INFINITY;
	private static int lastRendDur;
	//
	static long appStart = System.currentTimeMillis();
	
	public static void doTickDebug(long nowTime, long lastTime) {
		for (int i = 1; i < Engine.SCREEN_WIDTH; i++) {
			tickTimes[i - 1] = tickTimes[i];
			usedMemory[i - 1] = usedMemory[i];
		}
		tickTimes[tickTimes.length - 1] = (nowTime - lastTime) / 1_000_000;
		usedMemory[usedMemory.length - 1] = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		maxUsedMem = Math.max(maxUsedMem, usedMemory[usedMemory.length - 1]);
		if (usedMemory[usedMemory.length - 1] < usedMemory[usedMemory.length - 2]) {
			GCperMin = 60000.0 / (System.currentTimeMillis() - lastGCTime);
			lastGCTime = System.currentTimeMillis();
		}
		lastTPS = 1_000_000_000.0 / (nowTime - lastTime);
		lowestTPS = Math.min(lowestTPS, lastTPS);
		lastTickDur = (int) (System.nanoTime() - nowTime) / 1_000_000;
	}
	
	public static void doRenderDebug(long nowTime, long lastTime) {
		for (int i = 1; i < renderTimes.length; i++) {
			renderTimes[i - 1] = renderTimes[i];
		}
		renderTimes[renderTimes.length - 1] = (nowTime - lastTime) / 1_000_000;
		lastFPS = 1_000_000_000.0 / (nowTime - lastTime);
		lowestFPS = Math.min(lowestFPS, lastFPS);
		lastRendDur = (int) (System.nanoTime() - nowTime) / 1_000_000;
	}
	
	public static void drawDebug(Graphics2D g) {
		int graphH = Engine.SCREEN_HEIGHT / 2;
		String xmx = String.format("%05.2f GB", runtimeMem / 1_000_000_000.0); // gigabyte conversion
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
		
		g.setColor(Color.MAGENTA);
		g.drawLine(0, Engine.SCREEN_HEIGHT / 2, Engine.SCREEN_WIDTH, graphH);
		g.drawString("y = 1000 ms / " + xmx, 0, graphH - 1);
		
		for (int i = 0; i < Engine.SCREEN_WIDTH; i++) {
			g.setColor(Color.GREEN);
			g.drawLine(i, Engine.SCREEN_HEIGHT, i + 1, (int) (Engine.SCREEN_HEIGHT - graphH * (renderTimes[i] / 1000.0)));
			g.setColor(Color.RED);
			g.drawLine(i, Engine.SCREEN_HEIGHT, i + 1, (int) (Engine.SCREEN_HEIGHT - graphH * (tickTimes[i] / 1000.0)));
			g.setColor(Color.BLUE);
			g.drawLine(i, Engine.SCREEN_HEIGHT, i + 1, (int) (Engine.SCREEN_HEIGHT - graphH * ((double) usedMemory[i] / runtimeMem)));
		}
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 500, 40);
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g.setColor(Color.WHITE);
		g.drawString("TPS: " + String.format("%05.2f/s", lastTPS), 0, 30);
		g.drawString("LOWTPS: " + String.format("%02d/s", (int) lowestTPS), 100, 30);
		g.drawString("FPS: " + String.format("%05.2f/s", lastFPS), 0, 20);
		g.drawString("LOWFPS: " + String.format("%02d/s", (int) lowestTPS), 100, 20);
		g.drawString("GC/MIN: " + String.format("%02.3f/m", GCperMin), 200, 30);
		//g.drawString("RDUR: " + String.format("%04dms", lastRendDur), 200, 20);
		
		double curpercent = (double) usedMemory[usedMemory.length - 1] / runtimeMem * 100;
		double maxpercent = (double) maxUsedMem / runtimeMem * 100;
		g.drawString("MAX%: " + String.format("%05.2f%%", maxpercent), 300, 30);
		g.drawString("MEM%: " + String.format("%05.2f%%", curpercent), 300, 20);
		g.drawString("TM: " + xmx, 400, 30);
		g.drawString("GC: " + String.format("%05dms", (int) (System.currentTimeMillis() - lastGCTime)), 400, 20);
	}

	public static void printDebug() {
		String xmx = String.format("%05.2f GB", runtimeMem / 1_000_000_000.0); // gigabyte conversion
		System.out.println("TPS: " + String.format("%05.2f/s", lastTPS));
		System.out.println("LOWTPS: " + String.format("%02d/s", (int) lowestTPS));
		System.out.println("FPS: " + String.format("%05.2f/s", lastFPS));
		System.out.println("LOWFPS: " + String.format("%02d/s", (int) lowestTPS));
		System.out.println("TDUR: " + String.format("%04dms", lastTickDur));
		System.out.println("RDUR: " + String.format("%04dms", lastRendDur));
		
		double curpercent = (double) usedMemory[usedMemory.length - 1] / runtimeMem * 100;
		double maxpercent = (double) maxUsedMem / runtimeMem * 100;
		System.out.println("MAX%: " + String.format("%05.2f%%", maxpercent));
		System.out.println("MEM%: " + String.format("%05.2f%%", curpercent));
		System.out.println("TM: " + xmx);
		System.out.println("GC: " + String.format("%05dms", (int) (System.currentTimeMillis() - lastGCTime)));
	}
	
}
