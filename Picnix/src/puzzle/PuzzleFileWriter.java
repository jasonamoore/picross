package puzzle;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PuzzleFileWriter {

	private static final String SOURCE = "L:\\Users\\Jason\\Documents\\Programming\\picross\\levels\\source";
	private static final String BIN = "L:\\Users\\Jason\\Documents\\Programming\\picross\\levels\\bin";
	private static final String LAY_SOURCE = "L:\\Users\\Jason\\Documents\\Programming\\picross\\levels\\lay_source";
	private static final String LAY_BIN = "L:\\Users\\Jason\\Documents\\Programming\\picross\\levels\\lay_bin";
	private static final String WORLD_BIN = "L:\\Users\\Jason\\Documents\\Programming\\picross\\levels\\world";
	
	public static void main(String args[]) throws IOException {
		encodeAllNormal(SOURCE, BIN);
		encodeAllLayered(LAY_SOURCE, LAY_BIN);
		combineWorld("philosophy", BIN, WORLD_BIN);
		combineWorld("frutas", LAY_BIN, WORLD_BIN);
	}
	
	private static void combineWorld(String name, String source, String bin) throws IOException {
		File indir = new File(source);
		int count = indir.listFiles().length;
		File outfile = new File(bin + "\\" + name + ".pwr");
		outfile.createNewFile();
		FileOutputStream fos = new FileOutputStream(outfile);
		DataOutputStream dos = new DataOutputStream(fos);
		dos.write(count);
		for (File f : indir.listFiles()) {
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			int nextByte = 0;
			while ((nextByte = dis.read()) != -1)
				dos.write(nextByte);
			dis.close();
		}
		dos.close();
	}

	private static void encodeAllLayered(String source, String bin) throws IOException {
		File indir = new File(source);
		for (File f : indir.listFiles())
			encodeLayered(f, bin);
	}

	private static void encodeLayered(File image, String bin) throws IOException {
		File outfile = new File(bin + "\\" + image.getName().split("\\.")[0] + ".puz");
		outfile.createNewFile();
		BufferedImage img = ImageIO.read(image);
		FileOutputStream fos = new FileOutputStream(outfile);
		DataOutputStream dos = new DataOutputStream(fos);
		dos.write(8); // time limit
		dos.write(3); // mistakes
		dos.write(img.getHeight());
		dos.write(img.getWidth());
		for (int i = 0; i < 3; i++) {
			int b = 0;
			byte curByte = 0;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int col = img.getRGB(x, y) & 0xffffff;
					curByte |= (assessCol(i, col) ? 1 : 0) << b;
					b++;
					if (b == 8) {
						dos.write(curByte);
						b = 0;
						curByte = 0;
					}
				}
			}
			// final byte
			if (b != 0)
				dos.write(curByte);
		}
		dos.close();
	}
	
	private static boolean assessCol(int layerid, int color) {
		if (color == 0xffffff)
			return false;
		else if (color == 0)
			return true;
		int r = (color >> 16) & 0xff;
		int g = (color >> 8) & 0xff;
		int b = color & 0xff;
		boolean blend = r + g + b <= 0xff;
		if (layerid == 0 && (r > 0 || b > 0) && (color == 0xff00ff || blend))
			return true;
		if (layerid == 1 && (r > 0 || g > 0) && (color == 0xffff00 || blend))
			return true;
		if (layerid == 2 && (g > 0 || b > 0) && (color == 0x00ffff || blend))
			return true;
		return false;
	}
	
	private static void encodeAllNormal(String source, String bin) throws IOException {
		File indir = new File(source);
		for (File f : indir.listFiles())
			encodeNormal(f, bin);
	}

	private static void encodeNormal(File image, String bin) throws IOException {
		File outfile = new File(bin + "\\" + image.getName().split("\\.")[0] + ".puz");
		outfile.createNewFile();
		BufferedImage img = ImageIO.read(image);
		FileOutputStream fos = new FileOutputStream(outfile);
		DataOutputStream dos = new DataOutputStream(fos);
		dos.write(8); // time limit
		dos.write(3); // mistakes
		dos.write(img.getHeight());
		dos.write(img.getWidth());
		int b = 0;
		byte curByte = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int col = img.getRGB(x, y) & 0xffffff;
				curByte |= (col == 0 ? 1 : 0) << b;
				b++;
				if (b == 8) {
					dos.write(curByte);
					b = 0;
					curByte = 0;
				}
			}
		}
		// final byte
		if (b != 0)
			dos.write(curByte);
		dos.close();
	}
	
}
