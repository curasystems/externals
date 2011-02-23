package org.dcm4che2.tool.dcmmover;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.composite.PatientModule;
import org.dcm4che2.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for the data used to anonymize the DICOM objects being moved.
 * Restrictive about what attributes can be modified but wide open about
 * attributes that can be removed.
 * 
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
public class ObjectTransformData {

    static Logger log = LoggerFactory.getLogger(ObjectTransformData.class);

    // Collection of DICOM object data to modify during the move
    private PatientModule data;

    // Collection of tags for DICOM object attributes that should be removed
    // during the move
    private ArrayList<Integer> removeAttrs;

    public ObjectTransformData() {
        data = new PatientModule(new BasicDicomObject());
        removeAttrs = new ArrayList<Integer>();
    }

    public DicomObject getDicomObject() {
        return data.getDicomObject();
    }

    public void addAttrToRemove(int tag) {
        removeAttrs.add(new Integer(tag));
    }

    public List<Integer> getAttrsToRemoveList() {
        return removeAttrs;
    }

    //
    // Patient attributes
    //

    public String getPatientName() {
        return data.getPatientName();
    }

    public void setPatientName(String s) {
        data.setPatientName(s);
    }

    public String getPatientId() {
        return data.getPatientID();
    }

    public void setPatientId(String s) {
        data.setPatientID(s);
    }

    public Date getPatientBirthDate() {
        return data.getPatientBirthDate();
    }

    public void setPatientBirthDate(Date d) {
        data.setPatientBirthDate(d);
    }

    /**
     * Sets the patient birth date from a string
     * 
     * @param d_str
     *            Date in format "YYYYMMDD"
     */
    public void setPatientBirthDate(String d_str) {
        final String fn = "setPatientBirthDate: ";

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            setPatientBirthDate(df.parse(d_str));
        } catch (ParseException e) {
            log.error(fn + "Bad date format - '" + d_str + "'");
            e.printStackTrace();
        }
    }

    public Date getPatientBirthTime() {
        return data.getPatientBirthTime();
    }

    public void setPatientBirthTime(Date d) {
        data.setPatientBirthTime(d);
    }

    /**
     * Sets the patient birth time from a string
     * 
     * @param d_str
     *            Time in format "HHMMSS"
     */
    public void setPatientBirthTime(String d_str) {
        final String fn = "setPatientBirthTime: ";

        DateFormat df = new SimpleDateFormat("HHmmss");
        try {
            setPatientBirthTime(df.parse(d_str));
        } catch (ParseException e) {
            log.error(fn + "Bad time format - '" + d_str + "'");
            e.printStackTrace();
        }
    }

    public String getPatientSex() {
        return data.getPatientSex();
    }

    public void setPatientSex(String s) {
        data.setPatientSex(s);
    }

    public String getEthnicGroup() {
        return data.getEthnicGroup();
    }

    public void setEthnicGroup(String s) {
        data.setEthnicGroup(s);
    }

    //
    // Study properties
    //

    public void setAccessionNumber(String s) {
        data.getDicomObject().putString(Tag.AccessionNumber, VR.SH, s);
    }

    public void setStudyId(String s) {
        data.getDicomObject().putString(Tag.StudyID, VR.LO, s);
    }

    public void setPerformingPhysicianName(String s) {
        data.getDicomObject().putString(Tag.PerformingPhysicianName, VR.PN, s);
    }

    public void setReferringPhysicianName(String s) {
        data.getDicomObject().putString(Tag.ReferringPhysicianName, VR.PN, s);
    }

    public void setStudyDate(Date d) {
        data.getDicomObject().putDate(Tag.StudyDate, VR.DA, d);
    }

    /**
     * Sets the study date from a string
     * 
     * @param d_str
     *            Date in format "YYYYMMDD"
     */
    public void setStudyDate(String d_str) {
        final String fn = "setStudyDate: ";

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        try {
            setStudyDate(df.parse(d_str));
        } catch (ParseException e) {
            log.error(fn + "Bad date format - '" + d_str + "'");
            e.printStackTrace();
        }
    }

    public void setStudyTime(Date d) {
        data.getDicomObject().putDate(Tag.StudyTime, VR.TM, d);
    }

    /**
     * Sets the study time from a string
     * 
     * @param d_str
     *            Time in format "HHMMSS"
     */
    public void setStudyTime(String d_str) {
        final String fn = "setStudyTime: ";

        DateFormat df = new SimpleDateFormat("HHmmss");
        try {
            setStudyTime(df.parse(d_str));
        } catch (ParseException e) {
            log.error(fn + "Bad time format - '" + d_str + "'");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("Data to add/modify:\n");
        strBuf.append(data.getDicomObject().toString());
        strBuf.append("Data to remove:");
        for (Iterator<Integer> li = removeAttrs.iterator(); li.hasNext();) {
            strBuf.append("\n" + TagUtils.toString(li.next().intValue()));
        }
        strBuf.append("\n");
        return strBuf.toString();
    }
}
