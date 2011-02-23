package org.dcm4che2.iod.module.lut;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.iod.module.composite.GeneralImageModule;

/** Provide access to the various types of LUTs available in images,
 * both as raw values, and as ILut implementations that perform the lookups.
 * @author bwallace
 * @version $Revision$ $Date$
 * @since July 15, 2007

 */
public class LutModule extends GeneralImageModule {

	/** Create a LUT module object */
	public LutModule(DicomObject dcmobj) {
		super(dcmobj);
	}

	/** Get the modality LUT module */
	public ModalityLutModule getModalityLutModule() {
		return new ModalityLutModule(dcmobj);
	}
	
	/** Get the VOI LUT Module */
	public VoiLutModule getVoiLutModule() {
		return new VoiLutModule(dcmobj);
	}
}
