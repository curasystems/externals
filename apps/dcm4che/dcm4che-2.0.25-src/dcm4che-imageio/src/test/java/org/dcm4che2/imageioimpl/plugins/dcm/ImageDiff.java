package org.dcm4che2.imageioimpl.plugins.dcm;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.imageio.ImageIO;

/**
 * Performs a different on two images. Optionally writes the differenced
 * (second) image out, and writes out information about the differences.
 * 
 * @author bwallace
 * 
 */
public class ImageDiff {
	public static boolean writeImage = true;

	public static boolean writeDiff = true;

	public static boolean writeInfo = true;

	private long sumDifference = 0;

	private long sumSqrDifference = 0;

	private long pixelCount = 0;
	
	private long maxDiff = 0;
	
	private long allowedDiff;
	StringBuffer diffPos = new StringBuffer();

	public ImageDiff(BufferedImage i1, BufferedImage i2, String fileBase, long allowedDiff)
			throws IOException {
		this.allowedDiff = allowedDiff;
		assert i1 != null;
		assert i1.getWidth() == i2.getWidth();
		assert i1.getHeight() == i2.getHeight();
		computeDiffs(i1, i2, null);
		if (writeImage) {
			writeImage(i2, fileBase);
		}
		
		BufferedImage i3 = null;
		if (writeDiff) {
			if( i1.getType() != 0 ){
				i3 = new BufferedImage(i1.getWidth(), i1.getHeight(), i1.getType());
			}
			else
			{
				WritableRaster newRaster = i1.getRaster().createCompatibleWritableRaster();
				ColorModel cm =  i1.getColorModel();
				
				i3 = new BufferedImage(cm, newRaster, false, null);
			}
			
			writeImage(i3, fileBase + "-diff");
		}
		if (writeInfo) {
			writeInfo(fileBase, i3);
		}
	}

	/** Write info about the differences to the file. */
	private void writeInfo(String fileBase, BufferedImage i3) throws IOException {
		File f = new File(fileBase+".txt");
		if( maxDiff <= allowedDiff ) {
			if( f.exists() ) f.delete();
			return;
		}
		Writer w = new FileWriter(fileBase+".txt");
		w.write("# Image base "+fileBase+" information.\n");
		w.write("sumDifference="+sumDifference+"\n");
		double avg = sumDifference/(double) pixelCount;
		w.write("avgDifference="+avg+"\n");
		double avgSqr = sumSqrDifference/(double) pixelCount;
		w.write("stdDeviation="+Math.sqrt(avgSqr - avg*avg)+"\n");
		w.write("maxDiff="+maxDiff+"\n");
		w.write(diffPos.toString());
		w.close();
	}

	/**
	 * Write the iamge to the file base (as PNG, so as to preserve full
	 * fidelity)
	 */
	private void writeImage(BufferedImage i2, String fileBase) throws IOException {
		File f = new File(fileBase+".png");
		f.getParentFile().mkdirs();
		if( maxDiff <= allowedDiff ) {
			if( f.exists() ) f.delete();
			return;
		}
		ImageIO.write(i2,"png", new File(fileBase+".png"));
	}

	protected void computeDiffs(BufferedImage i1, BufferedImage i2,
			BufferedImage i3) {
		if( i1.getColorModel().getNumComponents()==1 ) {
			computeDiffsGray(i1,i2,i3);
		}
		else {
			computeDiffsColor(i1, i2, i3);
		}
	}

	/** Compute differences for grayscale */
	private void computeDiffsGray(BufferedImage i1, BufferedImage i2, BufferedImage i3) {
		int w = i1.getWidth();
		int[] d1 = new int[w];
		int[] d2 = new int[w];
		int[] d3 = new int[w];
		for(int y=0; y<i1.getHeight(); y++ ) {
			d1 = i1.getRaster().getPixels(0,y,w,1,d1);
			d2 = i2.getRaster().getPixels(0,y,w,1,d2);
			for(int x=0; x<w; x++ ) {
				d3[x] = Math.abs(d1[x]-d2[x]);
				pixelCount++;
				sumDifference += d3[x];
				sumSqrDifference += d3[x]*d3[x];
				if( d3[x]>maxDiff ) {
					maxDiff = d3[x];
					diffPos.append("Additional diff at ").append(x).append(",").append(y).append(" source ");
					diffPos.append(d1[x]).append(" final ").append(d2[x]).append("\n");
				}
			}
			if( i3!=null ) {
				i3.getRaster().setPixels(0,y,w,1,d3);
			}
		}
	}

	/** Compute differences for color */
	private void computeDiffsColor(BufferedImage i1, BufferedImage i2, BufferedImage i3) {
		int w = i1.getWidth();
		int[] d1 = new int[w*3];
		int[] d2 = new int[w*3];
		int[] d3 = new int[w*3];
		for(int y=0; y<i1.getHeight(); y++ ) {
			
			
			d1 = i1.getRaster().getPixels(0,y,w,1,d1);
			d2 = i2.getRaster().getPixels(0,y,w,1,d2);
			for(int x=0; x<w; x++ ) {
				d3[x] = Math.abs(d1[x]-d2[x]);
				pixelCount++;
				sumDifference += d3[x];
				sumSqrDifference += d3[x]*d3[x];
				if( d3[x]>maxDiff ) {
					maxDiff = d3[x];
					diffPos.append("Additional diff at ").append(x).append(",").append(y).append(" source ");
					diffPos.append(d1[x]).append(" final ").append(d2[x]).append("\n");
				}
			}
			if( i3!=null ) {
				i3.getRaster().setPixels(0,y,w,1,d3);
			}
		}
	}
	
	public long getMaxDiff() {
		return maxDiff;
	}

	public long getPixelCount() {
		return pixelCount;
	}

	public long getSumDifference() {
		return sumDifference;
	}

	public long getSumSqrDifference() {
		return sumSqrDifference;
	}

}
