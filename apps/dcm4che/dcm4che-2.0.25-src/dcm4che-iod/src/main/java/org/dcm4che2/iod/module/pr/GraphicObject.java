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

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.iod.module.Module;

/** Represents a part of the Graphic Annotation Module, the graphic object from
 * C.10.5
 * @author bwallace
 *
 */
public class GraphicObject extends Module {
   public static final String POINT="POINT";
   public static final String POLYLINE="POLYLINE";
   public static final String INTERPOLATED="INTERPOLATED";
   public static final String CIRCLE="CIRCLE";
   public static final String ELLIPSE="ELLIPSE";
   
   public GraphicObject(DicomObject dcmobj) {
	   super(dcmobj);
   }
   
   /** Create graphic objects from the sub-sequence */
   public static GraphicObject[] toGraphicObjects(DicomElement sq) {
       if (sq == null || !sq.hasItems()) {
           return null;
       }
       GraphicObject[] a = new GraphicObject[sq.countItems()];
       for (int i = 0; i < a.length; i++) {
           a[i] = new GraphicObject(sq.getDicomObject(i));
       }
       return a;
   }
   
   /**
    * Gets the graphic type associated with this object.  
    * 
    * @return One of POINT, POLYLINE, INTERPOLATED, CIRCLE and ELLIPSE
    */
   public String getGraphicType() {
	   return dcmobj.getString(Tag.GraphicType);
   }
   
   /** Indicate if the graphic is to be filled in */
   public boolean getGraphicFilled() {
	  String grFill = dcmobj.getString(Tag.GraphicFilled);
	  if( grFill==null ) return false;
	  return grFill.equalsIgnoreCase("Y");
   }
   
   public String getGraphicAnnotationUnits() {
	   return dcmobj.getString(Tag.GraphicAnnotationUnits);
   }
   
   public int getGraphicDimensions() {
	   return dcmobj.getInt(Tag.GraphicDimensions);
   }
   
   public int getNumberOfGraphicPoints() {
	   return dcmobj.getInt(Tag.NumberOfGraphicPoints);
   }
   
   public float[] getGraphicData() {
	   return dcmobj.getFloats(Tag.GraphicData);
   }
}
