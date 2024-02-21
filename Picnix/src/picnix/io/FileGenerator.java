package picnix.io;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FileGenerator {

	private static final String SOURCE = "C:\\Users\\Jason\\Desktop\\picross\\picross\\levels\\source";
	private static final String BIN = "C:\\Users\\Jason\\Desktop\\picross\\picross\\levels\\bin";
	private static final String WORLD_BIN = "C:\\Users\\Jason\\Desktop\\picross\\picross\\levels\\allworlds";
	
	public static void main(String args[]) throws IOException {
		encodeAll(SOURCE, BIN);
		combineIntoWorld("test2", BIN, WORLD_BIN);
	}
	
	private static void combineIntoWorld(String name, String source, String bin) throws IOException {
		File indir = new File(source);
		int count = indir.listFiles().length;
		File outfile = new File(bin + "\\" + name + ".pwr");
		outfile.createNewFile();
		FileOutputStream fos = new FileOutputStream(outfile);
		DataOutputStream dos = new DataOutputStream(fos);
		dos.write(count); // write the count metadata
		int b = 0;
		byte curByte = 0;
		// writes metadata bits for if puzzles are normal or layered
		for (File f : indir.listFiles()) {
			// write a 1 if filetype is lpz, else a 0 bit
			boolean layered = f.getName().split("\\.")[1].equals("lpz");
			curByte |= (layered ? 1 : 0) << b;
			b++;
			if (b == 8) {
				dos.write(curByte);
				b = 0;
				curByte = 0;
			}
		}
		// write final byte (padded)
		if (b != 0)
			dos.write(curByte);
		// actually copy in all the puzzle data one by one
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
	
	private static void encodeAll(String source, String bin) throws IOException {
		File indir = new File(source);
		for (File f : indir.listFiles())
			encode(f, bin);
	}

	private static void encode(File image, String bin) throws IOException {
		BufferedImage img = ImageIO.read(image);
		boolean layered = containsColor(img);
		File outfile = new File(bin + "\\" + image.getName().split("\\.")[0] + (layered ? ".lpz" : ".puz"));
		outfile.createNewFile();
		FileOutputStream fos = new FileOutputStream(outfile);
		DataOutputStream dos = new DataOutputStream(fos);
		dos.write(img.getHeight()); // rows
		dos.write(img.getWidth()); // cols
		dos.write(8); // time limit
		dos.write(3); // mistakes
		for (int i = 0; i < (layered ? 3 : 1); i++) {
			int b = 0;
			byte curByte = 0;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int col = img.getRGB(x, y) & 0xffffff;
					if (layered)
						curByte |= (assessCol(i, col) ? 1 : 0) << b;
					else
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
		}
		dos.close();
	}

	private static boolean containsColor(BufferedImage img) {
		for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++) {
				int col = img.getRGB(x, y) & 0xffffff;
				// abs value of white = 1 or black = 0
				if (col != 0 && col != 0xffffff)
					return true;
			}
		return false;
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
	
}
