package org.dcm4che2.iod.module.lut;

/** This class handles the sequences for a rescale slope and intercept LUT.
 * 
 *  See C.11.1 for information about the rescale intercept design.
 */
public class RescaleLut implements ILut {

	private float slope;
	private float intercept;
	private String type;

	/** Create a LUT on the given slope and intercept */
	public RescaleLut(float slope, float intercept, String type) {
		this.slope = slope;
		this.intercept = intercept;
		this.type = type;
	}


	/** Perform a rescale slope/intercept lookup */
	public float lookup(float value) {
		return value * slope + intercept;
	}

	/** Perform a rescale slope/intercept lookup */
	public int lookup(int value) {
		return (int) lookup((float) value);
	}

	public float getIntercept() {
		return intercept;
	}

	public void setIntercept(float intercept) {
		this.intercept = intercept;
	}

	public float getSlope() {
		return slope;
	}

	public void setSlope(float slope) {
		this.slope = slope;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
