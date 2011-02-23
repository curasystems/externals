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
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4che2.iod.composite;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.iod.module.composite.ClinicalTrialSeriesModule;
import org.dcm4che2.iod.module.composite.ClinicalTrialStudyModule;
import org.dcm4che2.iod.module.composite.ClinicalTrialSubjectModule;
import org.dcm4che2.iod.module.composite.GeneralEquipmentModule;
import org.dcm4che2.iod.module.composite.GeneralSeriesModule;
import org.dcm4che2.iod.module.composite.GeneralStudyModule;
import org.dcm4che2.iod.module.composite.PatientModule;
import org.dcm4che2.iod.module.composite.PatientStudyModule;
import org.dcm4che2.iod.module.general.SOPCommonModule;
import org.dcm4che2.iod.validation.ValidationContext;
import org.dcm4che2.iod.validation.ValidationResult;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 720 $ $Date: 2006-11-26 18:40:54 +0100 (Sun, 26 Nov 2006) $
 * @since Jun 9, 2006
 *
 */
public class Composite {
    
    protected final DicomObject dcmobj;
    protected final PatientModule patientModule;
    protected final ClinicalTrialSubjectModule clinicalTrialSubjectModule;
    protected final GeneralStudyModule generalStudyModule;
    protected final PatientStudyModule patientStudyModule;
    protected final ClinicalTrialStudyModule clinicalTrialStudyModule;
    protected final GeneralSeriesModule generalSeriesModule;
    protected final ClinicalTrialSeriesModule clinicalTrialSeriesModule;
    protected final GeneralEquipmentModule generalEquipmentModule;
    protected final SOPCommonModule sopCommonModule;

    public static Composite valueOf(DicomObject dcmobj) {
        String cuid = dcmobj.getString(Tag.SOPClassUID);
        if (cuid == null) {
            throw new IllegalArgumentException("Missing SOP Class UID");            
        }
        if (UID.ComputedRadiographyImageStorage.equals(cuid))
            return new CRImage(dcmobj);
        throw new UnsupportedOperationException("Unsupported SOP Class: "
                + UIDDictionary.getDictionary().prompt(cuid));
    }
    
    protected Composite(DicomObject dcmobj, GeneralSeriesModule seriesModule) {
        if (dcmobj == null) {
            throw new NullPointerException("dcmobj");
        }
        if (seriesModule == null) {
            throw new NullPointerException("seriesModule");
        }
        this.dcmobj = dcmobj;
        this.patientModule = new PatientModule(dcmobj);
        this.clinicalTrialSubjectModule = new ClinicalTrialSubjectModule(dcmobj);
        this.generalStudyModule = new GeneralStudyModule(dcmobj);
        this.patientStudyModule = new PatientStudyModule(dcmobj);
        this.clinicalTrialStudyModule = new ClinicalTrialStudyModule(dcmobj);
        this.generalSeriesModule = seriesModule;
        this.clinicalTrialSeriesModule = new ClinicalTrialSeriesModule(dcmobj);
        this.generalEquipmentModule = new GeneralEquipmentModule(dcmobj);
        this.sopCommonModule = new SOPCommonModule(dcmobj);
    }
    
    public void init() {
        patientModule.init();
        clinicalTrialSubjectModule.init();
        generalStudyModule.init();
        patientStudyModule.init();
        clinicalTrialStudyModule.init();
        generalSeriesModule.init();
        clinicalTrialSeriesModule.init();
        generalEquipmentModule.init();
        sopCommonModule.init();
    }

    public void validate(ValidationContext ctx, ValidationResult result) {
        patientModule.validate(ctx, result);
        clinicalTrialSubjectModule.validate(ctx, result);
        generalStudyModule.validate(ctx, result);
        patientStudyModule.validate(ctx, result);
        clinicalTrialStudyModule.validate(ctx, result);
        generalSeriesModule.validate(ctx, result);
        clinicalTrialSeriesModule.validate(ctx, result);
        generalEquipmentModule.validate(ctx, result);
        sopCommonModule.validate(ctx, result);
    }

    public DicomObject getDicomObject() {
        return dcmobj;
    }

    public final DicomObject getDcmobj() {
        return dcmobj;
    }

    public final PatientModule getPatientModule() {
        return patientModule;
    }

    public final ClinicalTrialSubjectModule getClinicalTrialSubjectModule() {
        return clinicalTrialSubjectModule;
    }
    
    public final GeneralStudyModule getGeneralStudyModule() {
        return generalStudyModule;
    }
    
    public final PatientStudyModule getPatientStudyModule() {
        return patientStudyModule;
    }

    public final ClinicalTrialStudyModule getClinicalTrialStudyModule() {
        return clinicalTrialStudyModule;
    }
    
    public final GeneralSeriesModule getGeneralSeriesModule() {
        return generalSeriesModule;
    }
    
    public final ClinicalTrialSeriesModule getClinicalTrialSeriesModule() {
        return clinicalTrialSeriesModule;
    }
    
    public final GeneralEquipmentModule getGeneralEquipmentModule() {
        return generalEquipmentModule;
    }
    
    public final SOPCommonModule getSopCommonModule() {
        return sopCommonModule;
    }

}
