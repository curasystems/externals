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
 * See listed authors below.
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

package org.dcm4che2.iod.module.spatial;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;

/**
 * A class to represent the Spatial Fiducials Module.
 * <p>
 * 
 * @author Antonio Magni <dcm4ceph@antoniomagni.org>
 * 
 */
public class SpatialFiducialsModule extends Module {

    public SpatialFiducialsModule(DicomObject dcmobj) {
        super(dcmobj);
        // TODO Auto-generated constructor stub
    }

    /**
     * The date and time the content creation started.
     * <p>
     * Type 1
     * 
     * @return
     */
    public Date getContentDateTime() {
        return dcmobj.getDate(Tag.ContentDate, Tag.ContentTime);
    }

    /**
     * The date and time the content creation started.
     * <p>
     * Type 1
     * 
     * @param d
     */
    public void setContentDateTime(Date d) {
        dcmobj.putDate(Tag.ContentDate, VR.DA, d);
        dcmobj.putDate(Tag.ContentTime, VR.TM, d);
    }

    /**
     * A number that identifies this instance.
     * <p>
     * Type 1
     * 
     * @return
     */
    public String getInstanceNumber() {
        return dcmobj.getString(Tag.InstanceNumber);
    }

    /**
     * A number that identifies this instance.
     * <p>
     * Type 1
     * 
     * @param s
     */
    public void setInstanceNumber(String s) {
        dcmobj.putString(Tag.InstanceNumber, VR.IS, s);
    }

    /**
     * A label that is used to identify this registration.
     * <p>
     * Type 1
     */
    public String getContentLabel() {
        return dcmobj.getString(Tag.ContentLabel);
    }

    /**
     * A label that is used to identify this registration.
     * <p>
     * Type 1
     * 
     * @param cs
     */
    public void setContentLabel(String cs) {
        dcmobj.putString(Tag.ContentLabel, VR.CS, cs);
    }

    /**
     * A description of this registration.
     * <p>
     * Type 2
     * 
     * @return
     */
    public String getContentDescription() {
        return dcmobj.getString(Tag.ContentDescription);
    }

    /**
     * A description of this registration.
     * <p>
     * Type 2
     * 
     * @param lo
     */
    public void setContentDescription(String lo) {
        dcmobj.putString(Tag.ContentDescription, VR.LO, lo);
    }

    /**
     * Name of operator performing the registration (such as a technologist or
     * physician).
     * <p>
     * Type 2
     * 
     * @return
     */
    public String getContentCreatorsName() {
        return dcmobj.getString(Tag.ContentCreatorName);
    }

    /**
     * Name of operator performing the registration (such as a technologist or
     * physician).
     * <p>
     * Type 2
     * 
     * @param cs
     */
    public void setContentCreatorsName(String pn) {
        dcmobj.putString(Tag.ContentCreatorName, VR.PN, pn);
    }

    /**
     * A sequence of one or more items, each of which is a fiducial set.
     * <p>
     * Type 1
     * 
     * @param fidsets
     */
    public void setFiducialSets(FiducialSet[] fidsets) {
        updateSequence(Tag.FiducialSetSequence, fidsets);
    }
    
    /**
     * Set a single {@link FiducialSet}.
     * <p>
     * This is a shortcut method for {@link #setFiducialSets(FiducialSet[])},
     * useful when only one {@link FiducialSet} needs to be stored. Creates a
     * single element array and passes it to
     * {@link #setFiducialSets(FiducialSet[])}
     * 
     * @param fidset
     */
    public void setFiducialSet(FiducialSet fidset){
        FiducialSet[] fs = new FiducialSet[1];
        fs[0] = fidset;
        setFiducialSets(fs);
    }

    /**
     * A sequence of one or more items, each of which is a fiducial set.
     * <p>
     * Type 1
     * 
     * @return
     */
    public FiducialSet[] getFiducialSets() {
        return FiducialSet.toFiducialSets(dcmobj
                .get(Tag.FiducialSetSequence));
    }
}
