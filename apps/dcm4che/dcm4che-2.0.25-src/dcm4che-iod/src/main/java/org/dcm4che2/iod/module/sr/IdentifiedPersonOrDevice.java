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
package org.dcm4che2.iod.module.sr;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.macro.Code;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 720 $
 * @since 25.07.2006
 */
public class IdentifiedPersonOrDevice extends InsitutionNameAndCode {

    public IdentifiedPersonOrDevice(DicomObject dcmobj) {
	super(dcmobj);
    }

    public IdentifiedPersonOrDevice() {
	super(new BasicDicomObject());
    }

    public String getObserverType() {
        return dcmobj.getString(Tag.ObserverType);
    }

    public void setObserverType(String s) {
        dcmobj.putString(Tag.ObserverType, VR.CS, s);
    }

    public String getPersonName() {
        return dcmobj.getString(Tag.PersonName);
    }

    public void setPersonName(String s) {
        dcmobj.putString(Tag.PersonName, VR.PN, s);
    }
    
    public Code getPersonIdentificationCode() {
        DicomObject item = dcmobj.getNestedDicomObject(Tag.PersonIdentificationCodeSequence);
        return item != null ? new Code(item) : null;
    }

    public void setPersonIdentificationCode(Code code) {
        updateSequence(Tag.PersonIdentificationCodeSequence, code);
    }
    
    public String getStationName() {
        return dcmobj.getString(Tag.StationName);
    }
    
    public void setStationName(String s) {
        dcmobj.putString(Tag.StationName, VR.SH, s);
    }
    
    public String getDeviceUID() {
        return dcmobj.getString(Tag.DeviceUID);
    }

    public void setDeviceUID(String s) {
        dcmobj.putString(Tag.DeviceUID, VR.UI, s);
    }

    public String getManufacturer() {
        return dcmobj.getString(Tag.Manufacturer);
    }

    public void setManufacturer(String s) {
        dcmobj.putString(Tag.Manufacturer, VR.LO, s);
    }

    public String getManufacturerModelName() {
        return dcmobj.getString(Tag.ManufacturerModelName);
    }

    public void setManufacturerModelName(String s) {
        dcmobj.putString(Tag.ManufacturerModelName, VR.LO, s);
    }

}
