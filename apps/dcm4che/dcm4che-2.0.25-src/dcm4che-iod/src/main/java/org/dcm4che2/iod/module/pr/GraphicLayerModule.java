/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace, <wayfarer3130@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che2.iod.module.pr;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.iod.module.Module;

/**
 * C.10.7 Provide the colour, z-index and other related graphical layer
 * information.
 * 
 * @author Bill Wallace <wayfarer3130@gmail.com>
 * @version $Revision$ $Date$
 * @since July 19, 2007
 */
public class GraphicLayerModule extends Module {

	/** Create a display shutter IOD instance on the given dicom object data. */
	public GraphicLayerModule(DicomObject dcmobj) {
		super(dcmobj);
	}

	/**
	 * Gets a map of String to GraphicLayerModule for all the available graphic
	 * layers.
	 */
	public static Map toGraphicLayerMap(DicomObject dcmobj) {
		DicomElement sq = dcmobj.get(Tag.GraphicLayerSequence);
		if (sq == null || !sq.hasItems()) {
			return null;
		}
		int sqCount = sq.countItems();
		Map<String, GraphicLayerModule> layers = new HashMap<String, GraphicLayerModule>(
				sqCount);
		for (int i = 0; i < sqCount; i++) {
			GraphicLayerModule glm=new GraphicLayerModule(sq.getDicomObject(i));
			layers.put(glm.getGraphicLayer(),glm);
		}
		return layers;
	}

	/** Gets a string that identifies this layer within this GSPS object */
	public String getGraphicLayer() {
		return dcmobj.getString(Tag.GraphicLayer);
	}

	/**
	 * Get the p-value on the range 0..65535 to display, defaulting to a
	 * mid-gray if absent.
	 */
	public int getGraphicLayerRecommendedDisplayGrayscaleValue() {
		return dcmobj.getInt(Tag.GraphicLayerRecommendedDisplayGrayscaleValue,
				32768);
	}

	public int[] getGraphicLayerRecommendedDisplayCIELabValue() {
		return dcmobj.getInts(Tag.GraphicLayerRecommendedDisplayCIELabValue);
	}

	public float[] getFloatLab() {
		return DisplayShutterModule
				.convertToFloatLab(getGraphicLayerRecommendedDisplayCIELabValue());
	}

	public int getGraphicLayerOrder() {
		return dcmobj.getInt(Tag.GraphicLayerOrder);
	}

	public String getGraphicLayerDescription() {
		return dcmobj.getString(Tag.GraphicLayerDescription);
	}

	/**
	 * Get the recommended RGB value to display this graphic layer in.
	 * 
	 * @deprecated No recommended to be used any longer.
	 * @return the RGB value to display as an integer triplet.
	 */
	@Deprecated
	public int[] getGraphicLayerRecommendedDisplayRGBValue() {
		return dcmobj.getInts(Tag.GraphicLayerRecommendedDisplayRGBValue);
	}
}
