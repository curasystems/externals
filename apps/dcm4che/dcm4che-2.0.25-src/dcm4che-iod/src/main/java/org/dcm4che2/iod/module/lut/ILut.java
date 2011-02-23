package org.dcm4che2.iod.module.lut;

/**
 * Defines a general pixel value lookup table.
 * 
 * @author bwallace
 * @version Revision $Date:$
 * @since 07.15.2006
 */
public interface ILut {

	/** This method handles lookup of float values - this may interpolate between
	 * pixel values, or otherwise modify values.
	 * @param Grayscale value to lookup in the lookup table.
	 * @return looked up value.
	 */
	public float lookup(float value);
	
	/** This method just looks up the integer or short values directly 
	 * @param value to convert to float, use the float lookup 
	 * @return the int cast response from the float lookup.*/
	public int lookup(int value);
	
}
