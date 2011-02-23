package org.dcm4che2.iod.module.composite;

import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.value.PixelRepresentation;

public class GeneralEquipmentModule extends Module {

    public GeneralEquipmentModule(DicomObject dcmobj) {
        super(dcmobj);
    }

    public String getManufacturer() {
        return dcmobj.getString(Tag.Manufacturer);
    }

    public void setManufacturer(String s) {
        dcmobj.putString(Tag.Manufacturer, VR.LO, s);
    }

    public String getInstitutionName() {
        return dcmobj.getString(Tag.InstitutionName);
    }

    public void setInstitutionName(String s) {
        dcmobj.putString(Tag.InstitutionName, VR.LO, s);
    }

    public String getInstitutionAddress() {
        return dcmobj.getString(Tag.InstitutionAddress);
    }

    public void setInstitutionAddress(String s) {
        dcmobj.putString(Tag.InstitutionAddress, VR.ST, s);
    }

    public String getStationName() {
        return dcmobj.getString(Tag.StationName);
    }
    
    public void setStationName(String s) {
        dcmobj.putString(Tag.StationName, VR.SH, s);
    }
    
    public String getInstitutionalDepartmentName() {
        return dcmobj.getString(Tag.InstitutionalDepartmentName);
    }

    public void setInstitutionalDepartmentName(String s) {
        dcmobj.putString(Tag.InstitutionalDepartmentName, VR.LO, s);
    }

    public String getManufacturerModelName() {
        return dcmobj.getString(Tag.ManufacturerModelName);
    }

    public void setManufacturerModelName(String s) {
        dcmobj.putString(Tag.ManufacturerModelName, VR.LO, s);
    }

    public String getDeviceSerialNumber() {
        return dcmobj.getString(Tag.DeviceSerialNumber);
    }

    public void setDeviceSerialNumber(String s) {
        dcmobj.putString(Tag.DeviceSerialNumber, VR.LO, s);
    }

    public String[] getSoftwareVersions() {
        return dcmobj.getStrings(Tag.SoftwareVersions);
    }

    public void setSoftwareVersions(String[] ss) {
        dcmobj.putStrings(Tag.SoftwareVersions, VR.LO, ss);
    }

    public float[] getSpatialResolution() {
        return dcmobj.getFloats(Tag.SpatialResolution);
    }

    public void setSoftwareVersions(float[] floats) {
        dcmobj.putFloats(Tag.SpatialResolution, VR.DS, floats);
    }

    public Date getDateTimeOfLastCalibration() {
        return dcmobj.getDate(Tag.DateOfLastCalibration,
                Tag.TimeOfLastCalibration);
    }

    public void setDateTimeOfLastCalibration(Date d) {
        dcmobj.putDate(Tag.DateOfLastCalibration, VR.DA, d);
        dcmobj.putDate(Tag.TimeOfLastCalibration, VR.TM, d);
    }

    public int getLargestPixelValueInSeries() {
        return dcmobj.getInt(Tag.PixelPaddingValue);
    }

    public void setLargestPixelValueInSeries(int s) {
        dcmobj.putInt(Tag.PixelPaddingValue,
                PixelRepresentation.isSigned(dcmobj) ? VR.SS : VR.US, s);
    }

}
