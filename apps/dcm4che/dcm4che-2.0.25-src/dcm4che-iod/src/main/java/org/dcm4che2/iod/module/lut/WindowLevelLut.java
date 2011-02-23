package org.dcm4che2.iod.module.lut;

/** Implements a window level LUT.
 *  See C.11.2.1.2 for information on the bias values and lookup conditions.
 *  In general, there is a bias of -0.5 so that an integer value can be used to
 *  specify the "center" of a range of odd length (that is, of an even number of
 *  consecutive integers, such as 256 - the width is 255, while the center of 0..255 
 *  127.5 - this bias makes this "center" 128 to allow it to be specified as an integer.
 *  */
public class WindowLevelLut implements ILut {

	/** See C.11.2.1.2 for the range bias information - the causes a pixel at a 
	 * half way point to be set to the next value up, so as to keep integer values
	 * for a 1-1 translation.
	 */
	private static final float RANGE_BIAS = 0.5f;
	/** See C.11.2.1.2 for the center bias - this is represented as c-0.5 in the DICOM standard. */
	private static final float CENTER_BIAS = -0.5f;
	/** See C.11.2.1.2 for the width bias - this is represented as w-1 in the DICOM standard. */
	private static final int WIDTH_BIAS = -1;
	private float center;
	private float width;
	private String description;
	private int ymin;
	private int ymax;

	/** Create a window level mapping, on the standard 0->255 output bytes */
	public WindowLevelLut(float center, float width, String description) {
		this(center,width,description,0,255);
	}
	
	/** Create a window level mapping */
	public WindowLevelLut(float center, float width, String description, int ymin, int ymax) {
		this.center = center+CENTER_BIAS;
		this.width = width+WIDTH_BIAS;
		this.description = description;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	/** Perform a window level lookup */
	public float lookup(float value) {
		if( value < center-width/2 ) return ymin;
		if( value > center+width/2 ) return ymax;
		return ((value-center)/width + RANGE_BIAS) * (ymax-ymin)+ymin;
	}

	public float getCenter() {
		return center-CENTER_BIAS;
	}

	public void setCenter(float center) {
		this.center = center+CENTER_BIAS;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getWidth() {
		return width-WIDTH_BIAS;
	}

	public void setWidth(float width) {
		this.width = width+WIDTH_BIAS;
	}

	public int getYmax() {
		return ymax;
	}

	public void setYmax(int ymax) {
		this.ymax = ymax;
	}

	public int getYmin() {
		return ymin;
	}

	public void setYmin(int ymin) {
		this.ymin = ymin;
	}

	/** Perform a window level lookup */
	public int lookup(int value) {
		return (int) lookup((float) value);
	}

}
